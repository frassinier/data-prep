// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.service;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.api.org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.metrics.VolumeMetered;
import org.talend.dataprep.transformation.api.transformer.TransformerFactory;
import org.talend.dataprep.transformation.format.FormatRegistrationService;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Base class used to share code across all TransformationService implementation.
 */
public abstract class BaseTransformationService {

    /** This class' logger. */
    private static final Logger LOG = LoggerFactory.getLogger(BaseTransformationService.class);

    /** The transformer factory. */
    @Autowired
    protected TransformerFactory factory;

    /** The format registration service. */
    @Autowired
    protected FormatRegistrationService formatRegistrationService;

    /** Preparation service url. */
    @Value("${preparation.service.url}")
    protected String preparationServiceUrl;

    /** DataSet service url. */
    @Value("${dataset.service.url}")
    protected String datasetServiceUrl;

    /** Http client used to retrieve datasets or preparations. */
    @Autowired
    protected HttpClient httpClient;

    /** The dataprep ready to use jackson object mapper. */
    @Autowired
    protected ObjectMapper mapper;

    /** Spring application context. */
    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    private List<ExportStrategy> exportStrategies;

    /**
     * Return the format that matches the given name or throw an error if the format is unknown.
     *
     * @param formatName the format name.
     * @return the format that matches the given name.
     */
    protected ExportFormat getFormat(String formatName) {
        final ExportFormat format = formatRegistrationService.getByName(formatName.toUpperCase());
        if (format == null) {
            LOG.error("Export format {} not supported", formatName);
            throw new TDPException(TransformationErrorCodes.OUTPUT_TYPE_NOT_SUPPORTED);
        }
        return format;
    }

    @RequestMapping(value = "/apply", method = POST, consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Run the transformation given the provided export parameters", notes = "This operation transforms the dataset or preparation using parameters in export parameters.")
    @VolumeMetered
    public StreamingResponseBody execute(
            @ApiParam(value = "Preparation id to apply.") @RequestBody @Valid final ExportParameters parameters) {
        LOG.debug("Export for preparation #{}.", parameters.getPreparationId());
        // Full run execution (depends on the export parameters).
        try {
            final List<ExportStrategy> orderedStrategies = exportStrategies.stream() //
                    .sorted((s1, s2) -> s1.order() - s2.order()) //
                    .collect(Collectors.toList());
            final Optional<ExportStrategy> electedStrategy = orderedStrategies.stream()
                    .filter(exportStrategy -> exportStrategy.accept(parameters)) //
                    .findFirst();
            if (electedStrategy.isPresent()) {
                LOG.debug("Strategy for execution: {}", electedStrategy.get().getClass());
                return electedStrategy.get().execute(parameters);
            } else {
                throw new IllegalArgumentException("Not valid export parameters (no preparation id nor data set id.");
            }
        } catch (TDPException e) {
            throw e;
        } catch (Exception e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_TRANSFORM_DATASET, e);
        }
    }
}
