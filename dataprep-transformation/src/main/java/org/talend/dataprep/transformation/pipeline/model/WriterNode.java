package org.talend.dataprep.transformation.pipeline.model;

import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.transformation.api.transformer.TransformerWriter;
import org.talend.dataprep.transformation.cache.TransformationMetadataCacheKey;
import org.talend.dataprep.transformation.pipeline.Monitored;
import org.talend.dataprep.transformation.pipeline.Signal;
import org.talend.dataprep.transformation.pipeline.Visitor;
import org.talend.dataprep.transformation.pipeline.node.TerminalNode;

import com.fasterxml.jackson.databind.ObjectWriter;

public class WriterNode extends TerminalNode implements Monitored {

    private static final Logger LOGGER = LoggerFactory.getLogger(WriterNode.class);

    private final TransformerWriter writer;

    private final ContentCache contentCache;

    private final String stepId;

    private RowMetadata lastRowMetadata;

    private boolean startRecords = false;

    private long totalTime;

    private int count;

    public WriterNode(TransformerWriter writer, ContentCache contentCache, String stepId) {
        this.writer = writer;
        this.contentCache = contentCache;
        this.stepId = stepId;
    }

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        final long start = System.currentTimeMillis();
        try {
            if (!startRecords) {
                writer.startObject();
                writer.fieldName("records");
                writer.startArray();
                startRecords = true;
            }
            lastRowMetadata = metadata;
            if (row.shouldWrite()) {
                row.setRowMetadata(metadata);
                writer.write(row);
            }
        } catch (IOException e) {
            LOGGER.error("Unable to write record.", e);
        } finally {
            totalTime += System.currentTimeMillis() - start;
            count++;
        }
    }

    @Override
    public void signal(Signal signal) {
        if (signal == Signal.END_OF_STREAM || signal == Signal.CANCEL) {
            final long start = System.currentTimeMillis();
            try {
                writer.endArray(); // <- end records
                writer.fieldName("metadata"); // <- start metadata
                writer.startObject();

                writer.fieldName("columns");
                writer.write(lastRowMetadata);

                writer.endObject();
                writer.endObject(); // <- end data set
                writer.flush();
            } catch (IOException e) {
                LOGGER.error("Unable to end writer.", e);
            } finally {
                totalTime += System.currentTimeMillis() - start;

            }
        } else {
            LOGGER.debug("Unhandled signal {}.", signal);
        }

        // Cache computed metadata for later reuse
        try {
            ObjectMapper mapper = new ObjectMapper();
            final ObjectWriter objectWriter = mapper.writerFor(RowMetadata.class);
            final OutputStream stream = contentCache.put(new TransformationMetadataCacheKey(stepId), ContentCache.TimeToLive.DEFAULT);
            objectWriter.writeValue(stream, lastRowMetadata);
            writer.flush();
            stream.close();
        } catch (IOException e) {
            LOGGER.debug("Unable to cache metadata for step #{}", stepId, e);
        }
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitNode(this);
    }

    @Override
    public long getTotalTime() {
        return totalTime;
    }

    @Override
    public long getCount() {
        return count;
    }

    public TransformerWriter getWriter() {
        return writer;
    }
}
