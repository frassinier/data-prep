package org.talend.dataprep.api.service.command.folder;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.dataprep.api.service.APIService;
import org.talend.dataprep.api.service.command.common.GenericCommand;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;

import java.net.URISyntaxException;

import static org.talend.dataprep.api.service.command.common.Defaults.asNull;
import static org.talend.dataprep.exception.error.APIErrorCodes.UNABLE_TO_DELETE_FOLDER;

@Component
@Scope("request")
public class RemoveFolder extends GenericCommand<Void> {

    private RemoveFolder(HttpClient client, String path) {
        super(APIService.DATASET_GROUP, client);
        execute(() -> onExecute(path));
        onError(e -> new TDPException(UNABLE_TO_DELETE_FOLDER, e, ExceptionContext.build()));
        on(HttpStatus.OK).then(asNull());
    }

    private HttpRequestBase onExecute( String path) {
        try {

            URIBuilder uriBuilder = new URIBuilder(datasetServiceUrl + "/folders");
            uriBuilder.addParameter("path", path);
            HttpDelete delete = new HttpDelete(uriBuilder.build());
            return delete;
        } catch (URISyntaxException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

}