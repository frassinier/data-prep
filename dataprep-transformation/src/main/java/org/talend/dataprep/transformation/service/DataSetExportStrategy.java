package org.talend.dataprep.transformation.service;

import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.command.dataset.DataSetSampleGet;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.transformation.api.transformer.configuration.Configuration;

import com.fasterxml.jackson.core.JsonParser;

@Component
public class DataSetExportStrategy extends ExportStrategy {

    @Override
    public int order() {
        return 1;
    }

    @Override
    public boolean accept(ExportParameters parameters) {
        return parameters.getContent() == null //
                && !StringUtils.isEmpty(parameters.getDatasetId()) //
                && StringUtils.isEmpty(parameters.getPreparationId());
    }

    @Override
    public StreamingResponseBody execute(ExportParameters parameters) {
        final String formatName = parameters.getExportType();
        final ExportFormat format = getFormat(formatName);
        ExportUtils.setExportHeaders(parameters.getExportName(), format);
        return outputStream -> {
            // get the dataset content (in an auto-closable block to make sure it is properly closed)
            final DataSetSampleGet dataSetGet = applicationContext.getBean(DataSetSampleGet.class, parameters.getDatasetId());
            try (InputStream datasetContent = dataSetGet.execute()) {
                try (JsonParser parser = mapper.getFactory().createParser(datasetContent)) {
                    // Create dataset
                    final DataSet dataSet = mapper.readerFor(DataSet.class).readValue(parser);
                    // get the actions to apply (no preparation ==> dataset export ==> no actions)
                    Configuration configuration = Configuration.builder() //
                            .args(parameters.getArguments()) //
                            .outFilter(rm -> filterService.build(parameters.getFilter(), rm)) //
                            .format(format.getName()) //
                            .volume(Configuration.Volume.SMALL) //
                            .output(outputStream) //
                            .build();
                    factory.get(configuration).transform(dataSet, configuration);
                }
            } catch (TDPException e) {
                throw e;
            } catch (Exception e) {
                throw new TDPException(TransformationErrorCodes.UNABLE_TO_TRANSFORM_DATASET, e);
            }
        };
    }
}
