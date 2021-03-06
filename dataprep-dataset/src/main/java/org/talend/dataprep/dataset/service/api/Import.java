package org.talend.dataprep.dataset.service.api;

import java.util.List;

import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.util.MessagesBundleContext;

public class Import {

    private final String locationType;

    private final String contentType;

    private final List<Parameter> parameters;

    private final boolean dynamic;

    private final boolean defaultImport;

    public Import(String locationType, String contentType, List<Parameter> parameters, boolean dynamic, boolean defaultImport) {
        this.locationType = locationType;
        this.contentType = contentType;
        this.parameters = parameters;
        this.dynamic = dynamic;
        this.defaultImport = defaultImport;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public boolean isDefaultImport() {
        return defaultImport;
    }

    public String getContentType() {
        return contentType;
    }

    public String getLocationType() {
        return locationType;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public String getLabel() {
        return MessagesBundleContext.get().getString("import." + locationType + ".label");
    }

    public String getTitle() {
        return MessagesBundleContext.get().getString("import." + locationType + ".title");
    }
}
