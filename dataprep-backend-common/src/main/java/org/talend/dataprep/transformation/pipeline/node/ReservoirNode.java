package org.talend.dataprep.transformation.pipeline.node;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.statistics.StatisticsAdapter;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.transformation.pipeline.Monitored;
import org.talend.dataprep.transformation.pipeline.Signal;
import org.talend.dataprep.transformation.pipeline.Visitor;
import org.talend.dataprep.util.FilesHelper;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.talend.datascience.common.inference.Analyzer;
import org.talend.datascience.common.inference.Analyzers;

public class ReservoirNode extends AnalysisNode implements Monitored {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservoirNode.class);

    private final JsonGenerator generator;

    private final File reservoir;

    private RowMetadata rowMetadata;

    private long totalTime;

    private long count;

    private Analyzer<Analyzers.Result> resultAnalyzer;

    public ReservoirNode(Function<List<ColumnMetadata>, Analyzer<Analyzers.Result>> analyzer, //
                         Predicate<ColumnMetadata> filter, //
                         StatisticsAdapter adapter) {
        super(analyzer, filter, adapter);
        try {
            reservoir = File.createTempFile("ReservoirNode", ".zip");
            JsonFactory factory = new JsonFactory();
            generator = factory.createGenerator(new GZIPOutputStream(new FileOutputStream(reservoir), true));
            generator.writeStartObject();
            generator.writeFieldName("records");
            generator.writeStartArray();
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        final long start = System.currentTimeMillis();
        try {
            if (!row.isDeleted()) {
                final List<ColumnMetadata> filteredColumns = metadata.getColumns().stream().filter(filter).collect(Collectors.toList());
                final Set<String> filteredColumnNames = filteredColumns.stream().map(ColumnMetadata::getId).collect(Collectors.toSet());
                // Lazy initialization of the result analyzer
                if (resultAnalyzer == null) {
                    resultAnalyzer = analyzer.apply(filteredColumns);
                }
                // Proceed to inline analysis and store results.
                final List<ColumnMetadata> columns = metadata.getColumns();
                try {
                    generator.writeStartObject();
                    rowMetadata = metadata;
                    final String[] values = row.order(columns).toArray(DataSetRow.SKIP_TDP_ID.and(e -> filteredColumnNames.contains(e.getKey())));
                    try {
                        resultAnalyzer.analyze(values);
                    } catch (Exception e) {
                        LOGGER.debug("Unable to analyze row '{}'.", Arrays.toString(values), e);
                    }
                    columns.stream().forEach(column -> {
                        try {
                            generator.writeStringField(column.getId(), row.get(column.getId()));
                        } catch (IOException e) {
                            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
                        }
                    });
                    generator.writeEndObject();
                } catch (IOException e) {
                    throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
                }
            }
        } finally {
            totalTime += System.currentTimeMillis() - start;
            count++;
        }
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitNode(this);
    }

    @Override
    public void signal(Signal signal) {
        final long start = System.currentTimeMillis();
        try {
            if (signal == Signal.END_OF_STREAM) {
                // End temporary output
                generator.writeEndArray();
                generator.writeEndObject();
                generator.flush();
                // Send stored records to next steps
                if (rowMetadata != null) {
                    // Adapt row metadata
                    adapter.adapt(rowMetadata.getColumns(), resultAnalyzer.getResult(), filter);
                    resultAnalyzer.close();

                    final Analyzer<Analyzers.Result> configuredAnalyzer = analyzer.apply(rowMetadata.getColumns());
                    final ObjectMapper mapper = new ObjectMapper();
                    try (JsonParser parser = mapper.getFactory()
                            .createParser(new GZIPInputStream(new FileInputStream(reservoir)))) {
                        final DataSet dataSet = mapper.readerFor(DataSet.class).readValue(parser);
                        dataSet.getRecords().forEach(r -> r.order(rowMetadata.getColumns()).toArray(DataSetRow.SKIP_TDP_ID));
                        adapter.adapt(rowMetadata.getColumns(), resultAnalyzer.getResult(), filter);
                    } finally {
                        try {
                            configuredAnalyzer.close();
                        } catch (Exception e) {
                            LOGGER.debug("Unable to close analyzer.", e);
                        }
                    }
                    // Continue process
                    try (JsonParser parser = mapper.getFactory()
                            .createParser(new GZIPInputStream(new FileInputStream(reservoir)))) {
                        final DataSet dataSet = mapper.readerFor(DataSet.class).readValue(parser);
                        dataSet.getRecords().forEach(r -> link.exec().emit(r, rowMetadata));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to perform delayed analysis.", e);
        } finally {
            FilesHelper.deleteQuietly(reservoir);
            totalTime += System.currentTimeMillis() - start;
        }
        super.signal(signal);
    }

    @Override
    public long getTotalTime() {
        return totalTime;
    }

    @Override
    public long getCount() {
        return count;
    }
}
