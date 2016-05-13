//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.transformation.api.transformer.configuration;

import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.format.export.ExportFormat;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.format.JsonFormat;

/**
 * Full configuration for a transformation.
 */
public class Configuration {



    public enum Volume {
        LARGE,
        SMALL;
    }
    private final String stepId;

    /** The format format {@link ExportFormat} */
    private final String format;

    /** The actions in JSON string format */
    private final String actions;

    /** The arguments for format */
    private final Map<String, String> arguments;

    /** Where to write the transformed content. */
    private final OutputStream output;

    private final boolean allowMetadataChange;

    private final Volume dataVolume;

    /** List of transformation context, one per action. */
    private TransformationContext transformationContext;

    /**
     * Constructor for the transformer configuration.
     */
    protected Configuration(final OutputStream output, //
                            final String format, //
                            final String actions, //
                            final Map<String, String> arguments, //
                            final String stepId, //
                            boolean allowMetadataChange, //
                            final Volume dataVolume) {
        this.output = output;
        this.format = format;
        this.actions = actions;
        this.arguments = arguments;
        this.stepId = stepId;
        this.allowMetadataChange = allowMetadataChange;
        this.dataVolume = dataVolume;
        this.transformationContext = new TransformationContext();
    }

    /**
     * @return a TransformerConfiguration mapper.
     */
    public static Builder builder() {
        return new Builder();
    }

    public String stepId() {
        return stepId;
    }

    /**
     * @return The expected output {@link ExportFormat format} of the transformation.
     */
    public String formatId() {
        return format;
    }

    /**
     * @return The actions (as JSON string) to apply in transformation.
     */
    public String getActions() {
        return actions;
    }

    /**
     * @return Arguments for the {@link ExportFormat format type} (parameters depend on the format type).
     */
    public Map<String, String> getArguments() {
        return arguments;
    }

    /**
     * @return the writer where to write the transform dataset.
     */
    public OutputStream output() {
        return output;
    }

    /**
     * @return the transformation context that match the given index.
     */
    public TransformationContext getTransformationContext() {
        return transformationContext;
    }

    public Volume volume() {
        return dataVolume;
    }

    public boolean isAllowMetadataChange() {
        return allowMetadataChange;
    }

    /**
     * Builder pattern used to simplify code writing.
     */
    public static class Builder {

        /**
         * The format format {@link ExportFormat}
         */
        private String format = JsonFormat.JSON;

        /**
         * The actions in JSON string format
         */
        private String actions = StringUtils.EMPTY;

        /**
         * The actions in Map
         */
        private Map<String, String> arguments = Collections.emptyMap();

        /** Where to write the transformed content. */
        private OutputStream output;

        /** Gives hint on the amount of data the transformer may expect */
        private Volume dataVolume = Volume.SMALL;

        private String stepId;

        private boolean allowMetadataChange = true;

        /**
         * @param output where to write the transformed dataset.
         * @return the mapper to chain calls.
         */
        public Builder output(final OutputStream output) {
            this.output = output;
            return this;
        }

        /**
         * @return a new {@link Configuration} from the mapper setup.
         */
        public Configuration build() {
            return new Configuration(output, format, actions, arguments, stepId, allowMetadataChange, dataVolume);
        }

        /**
         * Builder DSL for format setter
         *
         * @param format The format type id.
         * @return The mapper
         */
        public Builder format(final String format) {
            this.format = format;
            return this;
        }

        /**
         * Builder DSL for actions setter
         *
         * @param actions The actions in JSON string format.
         * @return The mapper
         */
        public Builder actions(final String actions) {
            this.actions = actions;
            return this;
        }

        /**
         * Builder DSL for arguments setter
         *
         * @param arguments The arguments in Map
         * @return The mapper
         */
        public Builder args(final Map<String, String> arguments) {
            this.arguments = arguments;
            return this;
        }

        public Builder stepId(final String stepId) {
            this.stepId = stepId;
            return this;
        }

        public Builder volume(Volume dataVolume) {
            this.dataVolume = dataVolume;
            return this;
        }

        public Builder allowMetadataChange(boolean allowMetadataChange) {
            this.allowMetadataChange = allowMetadataChange;
            return this;
        }
    }
}
