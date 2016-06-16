package org.talend.dataprep.transformation.service;

import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.http.HttpResponseContext;

public class ExportUtils {
    private ExportUtils() {
    }

    public static void setExportHeaders(String exportName, ExportFormat format) {
        HttpResponseContext.contentType(format.getMimeType());
        HttpResponseContext.header("Content-Disposition", "attachment; filename=\"" + exportName + format.getExtension() + "\"");
    }
}
