package org.talend.dataprep.transformation.service;

import static org.talend.daikon.exception.ExceptionContext.build;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.UNABLE_TO_READ_PREPARATION;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.filter.FilterService;
import org.talend.dataprep.api.org.talend.dataprep.api.export.ExportParameters;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.preparation.Step;
import org.talend.dataprep.cache.ContentCache;
import org.talend.dataprep.command.preparation.PreparationDetailsGet;
import org.talend.dataprep.command.preparation.PreparationGetActions;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.TransformationErrorCodes;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.http.HttpResponseContext;
import org.talend.dataprep.transformation.api.transformer.TransformerFactory;
import org.talend.dataprep.transformation.format.FormatRegistrationService;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class ExportStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportStrategy.class);

    @Autowired
    protected ApplicationContext applicationContext;

    /** The root step. */
    @Resource(name = "rootStep")
    Step rootStep;

    @Autowired
    protected ObjectMapper mapper;

    @Autowired
    protected ContentCache contentCache;

    @Autowired
    protected FilterService filterService;

    /** The format registration service. */
    @Autowired
    protected FormatRegistrationService formatRegistrationService;

    /** The transformer factory. */
    @Autowired
    protected TransformerFactory factory;

    public abstract int order();

    public abstract boolean accept(ExportParameters parameters);

    public abstract StreamingResponseBody execute(ExportParameters parameters);

    /**
     * Return the format that matches the given name or throw an error if the format is unknown.
     *
     * @param formatName the format name.
     * @return the format that matches the given name.
     */
    protected ExportFormat getFormat(String formatName) {
        final ExportFormat format = formatRegistrationService.getByName(formatName.toUpperCase());
        if (format == null) {
            LOGGER.error("Export format {} not supported", formatName);
            throw new TDPException(TransformationErrorCodes.OUTPUT_TYPE_NOT_SUPPORTED);
        }
        return format;
    }

    protected String getActions(String preparationId, String stepId) {
        String actions;
        if (StringUtils.isBlank(preparationId)) {
            actions = "{\"actions\": []}";
        } else {
            final PreparationGetActions getActionsCommand = applicationContext.getBean(PreparationGetActions.class, preparationId,
                    stepId);
            try {
                actions = "{\"actions\": " + IOUtils.toString(getActionsCommand.execute()) + '}';
            } catch (IOException e) {
                final ExceptionContext context = ExceptionContext.build().put("id", preparationId).put("version", stepId);
                throw new TDPException(UNABLE_TO_READ_PREPARATION, e, context);
            }
        }
        return actions;
    }

    protected String getActions(String preparationId, String startStepId, String endStepId) {
        if (rootStep.id().equals(startStepId)) {
            return getActions(preparationId, endStepId);
        }
        String actions;
        if (StringUtils.isBlank(preparationId)) {
            actions = "{\"actions\": []}";
        } else {
            try {
                final PreparationGetActions startStepActions = applicationContext.getBean(PreparationGetActions.class,
                        preparationId, startStepId);
                final PreparationGetActions endStepActions = applicationContext.getBean(PreparationGetActions.class,
                        preparationId, endStepId);
                final StringWriter actionsAsString = new StringWriter();
                final Action[] startActions = mapper.readValue(startStepActions.execute(), Action[].class);
                final Action[] endActions = mapper.readValue(endStepActions.execute(), Action[].class);
                if (endActions.length > startActions.length) {
                    final Action[] filteredActions = (Action[]) ArrayUtils.subarray(endActions, startActions.length,
                            endActions.length);
                    LOGGER.debug("Reduced actions list from {} to {} action(s)", endActions.length, filteredActions.length);
                    mapper.writeValue(actionsAsString, filteredActions);
                } else {
                    LOGGER.debug("Unable to reduce list of actions (has {})", endActions.length);
                    mapper.writeValue(actionsAsString, endActions);
                }

                return "{\"actions\": " + actionsAsString + '}';
            } catch (IOException e) {
                final ExceptionContext context = ExceptionContext.build().put("id", preparationId).put("version", endStepId);
                throw new TDPException(UNABLE_TO_READ_PREPARATION, e, context);
            }
        }
        return actions;
    }

    /**
     * @param preparationId the wanted preparation id.
     * @return the preparation out of its id.
     */
    protected Preparation getPreparation(String preparationId) {
        final PreparationDetailsGet preparationDetailsGet = applicationContext.getBean(PreparationDetailsGet.class,
                preparationId);
        try (InputStream details = preparationDetailsGet.execute()) {
            return mapper.readerFor(Preparation.class).readValue(details);
        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_READ_PREPARATION, e, build().put("id", preparationId));
        }

    }

}
