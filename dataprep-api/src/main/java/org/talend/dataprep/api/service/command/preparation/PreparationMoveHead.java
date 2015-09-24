package org.talend.dataprep.api.service.command.preparation;

import static org.talend.dataprep.api.service.command.common.Defaults.asNull;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.service.APIService;
import org.talend.dataprep.api.service.command.common.GenericCommand;

@Component
@Scope("request")
public class PreparationMoveHead extends GenericCommand<Void> {

    private PreparationMoveHead(final HttpClient client, final String preparationId, final String headId) {
        super(APIService.PREPARATION_GROUP, client);
        execute(() -> new HttpPut(preparationServiceUrl + "/preparations/" + preparationId + "/head/" + headId));
        on(HttpStatus.OK).then(asNull());
    }
}