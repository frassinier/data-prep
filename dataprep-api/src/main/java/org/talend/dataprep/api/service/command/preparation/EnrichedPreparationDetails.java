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

package org.talend.dataprep.api.service.command.preparation;

import static org.talend.dataprep.exception.error.CommonErrorCodes.UNEXPECTED_EXCEPTION;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.service.command.common.ChainedCommand;
import org.talend.dataprep.command.GenericCommand;
import org.talend.dataprep.command.preparation.PreparationDetailsGet;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.security.SecurityProxy;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netflix.hystrix.HystrixCommand;

/**
 * Command used to add information from the dataset into the preparation details.
 */
@Component
@Scope("prototype")
public class EnrichedPreparationDetails extends ChainedCommand<InputStream, InputStream> {

    /** Used to retrieve the dataset metadata. */
    @Autowired
    private SecurityProxy securityProxy;

    /** The preparation detail json root node. */
    private ObjectNode preparationJsonRootNode;

    /**
     * Default constructor.
     *
     * @param preparationDetailsInput the preparation details input.
     */
    // private constructor to ensure the use of IoC
    private EnrichedPreparationDetails(PreparationDetailsGet preparationDetailsInput) { // NOSONAR used by IoC
        super(PREPARATION_GROUP, preparationDetailsInput);
        execute(this::onExecute);
        on(HttpStatus.OK).then(this::processPreparation);
    }

    /**
     * Get the preparation details and read the content as a json tree.
     * 
     * @throws IOException if an error occurs.
     */
    @PostConstruct
    private void getPreparationDetails() throws IOException { // NOSONAR see the @PostConstruct
        // this need to be performed outside the run method as it is overriden in this class and
        // executed as the technical user.
        preparationJsonRootNode = (ObjectNode) objectMapper.readTree(getInput());
    }

    /**
     * @return the http request to use to retrieve the dataset metadata out of the preparation details.
     */
    private HttpRequestBase onExecute() { // NOSONAR method reference from
        // because a preparation repository is needed to parse a PreparationDetails, let's use the json tree model
        final String dataSetId = preparationJsonRootNode.get("dataSetId").asText();
        return new HttpGet(datasetServiceUrl + "/datasets/" + dataSetId + "/metadata");
    }

    /**
     * Wraps the call to get the dataset as the technical user as the dataset may not be shared for instance.
     * 
     * @see GenericCommand#run()
     */
    @Override
    protected InputStream run() throws Exception {
        try {
            securityProxy.asTechnicalUser();
            return super.run();
        } finally {
            securityProxy.releaseIdentity();
        }
    }

    /**
     * Update the preparation details with additional information from the dataset metadata.
     * 
     * @param request the request used to retrieve the dataset metadata.
     * @param response the response that holds the dataset metadata.
     * @return the input stream to sent to the client.
     */
    private InputStream processPreparation(HttpRequestBase request, HttpResponse response) { // NOSONAR method reference
        try {
            final DataSet dataSet = objectMapper.readerFor(DataSet.class).readValue(response.getEntity().getContent());
            final DataSetMetadata metadata = dataSet.getMetadata();

            // update the full run flag
            boolean allowFullRun = metadata.getContent().getLimit().isPresent();
            preparationJsonRootNode.put("allowFullRun", allowFullRun);

            return IOUtils.toInputStream(preparationJsonRootNode.toString());

        } catch (IOException e) {
            throw new TDPException(UNEXPECTED_EXCEPTION, e);
        } finally {
            request.releaseConnection();
        }
    }

    /**
     * @see HystrixCommand#getFallback()
     */
    @Override
    protected InputStream getFallback() {
        return IOUtils.toInputStream(preparationJsonRootNode.asText());
    }

}
