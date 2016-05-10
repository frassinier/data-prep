package org.talend.dataprep.command;

import com.netflix.hystrix.HystrixCommand;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.talend.dataprep.http.HttpResponseContext;

import java.io.InputStream;

public class CommandHelper {

    private CommandHelper() {
    }

    public static StreamingResponseBody toStreaming(final HystrixCommand<InputStream> command) {
        return outputStream -> {
            IOUtils.copyLarge(command.execute(), outputStream);
            outputStream.flush();
        };
    }

    public static StreamingResponseBody toStreaming(final GenericCommand<InputStream> command) {
        return outputStream -> {
            // copy all headers from the command response so that the mime-type is correctly forwarded for instance
            for (Header header : command.getCommandResponseHeaders()) {
                HttpResponseContext.header(header.getName(), header.getValue());
            }
            IOUtils.copyLarge(command.execute(), outputStream);
            outputStream.flush();
        };
    }
}
