package org.talend.dataprep.api.service;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.talend.dataprep.api.APIErrorCodes;
import org.talend.dataprep.api.service.api.DynamicParamsInput;
import org.talend.dataprep.api.service.command.dataset.DataSetGet;
import org.talend.dataprep.api.service.command.preparation.PreparationGetContent;
import org.talend.dataprep.api.service.command.transformation.SuggestActionParams;
import org.talend.dataprep.api.service.command.transformation.SuggestColumnActions;
import org.talend.dataprep.api.service.command.transformation.Transform;
import org.talend.dataprep.exception.CommonErrorCodes;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.TDPExceptionContext;
import org.talend.dataprep.metrics.Timed;

import com.netflix.hystrix.HystrixCommand;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@RestController
@Api(value = "api", basePath = "/api", description = "Transformation API")
public class TransformAPI extends APIService {

    @RequestMapping(value = "/api/transform/{id}", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Transforms a data set given data set id. This operation retrieves data set content and pass it to the transformation service.", notes = "Returns the data set modified with the provided actions in request body.")
    public void transform(@PathVariable(value = "id") @ApiParam(value = "Data set id.") String dataSetId,
            @ApiParam(value = "Actions to perform on data set (as JSON format).") InputStream body, HttpServletResponse response) {
        if (dataSetId == null) {
            throw new IllegalArgumentException("Data set id cannot be null.");
        }
        LOG.debug("Transforming dataset id #{} (pool: {})...", dataSetId, getConnectionManager().getTotalStats());
        try {
            // Configure transformation flow
            String encodedActions = Base64.getEncoder().encodeToString(IOUtils.toByteArray(body));
            response.setHeader("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
            HttpClient client = getClient();
            HystrixCommand<InputStream> contentRetrieval = getCommand(DataSetGet.class, client, dataSetId, false, false);
            HystrixCommand<InputStream> transformation = getCommand(Transform.class, client, contentRetrieval, encodedActions);

            // Perform transformation
            ServletOutputStream outputStream = response.getOutputStream();
            InputStream input = transformation.execute();

            IOUtils.copyLarge(input, outputStream);
            outputStream.flush();
        } catch (IOException e) {
            LOG.error("error while applying transform " + e.getMessage(), e);
            throw new TDPException(APIErrorCodes.UNABLE_TO_TRANSFORM_DATASET, e, TDPExceptionContext.build().put("dataSetId",
                    dataSetId));
        }

        LOG.debug("Transformation of dataset id #{} done.", dataSetId);
    }

    /**
     * Suggest the possible actions for a given column.
     *
     * Although not rest compliant, this is done via a post in order to pass all the column metadata in the request body
     * without risking breaking the url size limit if GET would be used.
     *
     * @param body the column description (json encoded) in the request body.
     * @param response the http response.
     */
    @RequestMapping(value = "/api/transform/suggest/column", method = RequestMethod.POST, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get suggested actions for a data set column.", notes = "Returns the suggested actions for the given column in decreasing order of likeness.")
    @Timed
    public void suggestColumnActions(@ApiParam(value = "Column Metadata content as JSON") InputStream body,
            HttpServletResponse response) {

        HttpClient client = getClient();

        // Asks transformation service for suggested actions for column type and domain
        HystrixCommand<InputStream> getSuggestedActions = getCommand(SuggestColumnActions.class, client, body);
        // Returns actions
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            IOUtils.copyLarge(getSuggestedActions.execute(), outputStream);
            outputStream.flush();
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    /**
     * Get the suggested action dynamic params. Dynamic params depends on the context (dataset / preparation / actual
     * transformations)
     *
     * @param response the http response.
     */
    @RequestMapping(value = "/api/transform/suggest/{action}/params", method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get the transformation dynamic parameters", notes = "Returns the transformation parameters.")
    @Timed
    public void suggestActionParams(
            @ApiParam(value = "Transformation name.")
            @PathVariable("action")
            final String action,
            @ApiParam(value = "Suggested dynamic transformation input (preparation id or dataset id")
            @Valid
            final DynamicParamsInput dynamicParamsInput,
            final HttpServletResponse response) {

        try {
            // get preparation/dataset content
            HystrixCommand<InputStream> inputData;
            if (isNotBlank(dynamicParamsInput.getPreparationId())) {
                inputData = getCommand(PreparationGetContent.class, getClient(), dynamicParamsInput.getPreparationId(), "head");
            } else {
                inputData = getCommand(DataSetGet.class, getClient(), dynamicParamsInput.getDatasetId(), false, true);
            }

            // get params, passing content in the body
            final HystrixCommand<InputStream> getActionDynamicParams = getCommand(SuggestActionParams.class, getClient(),
                    inputData, action, dynamicParamsInput.getColumnId());

            // trigger calls and return last call content
            final ServletOutputStream outputStream = response.getOutputStream();
            IOUtils.copyLarge(getActionDynamicParams.execute(), outputStream);
            outputStream.flush();
        } catch (IOException e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_GET_DYNAMIC_ACTION_PARAMS, e);
        }
    }
}
