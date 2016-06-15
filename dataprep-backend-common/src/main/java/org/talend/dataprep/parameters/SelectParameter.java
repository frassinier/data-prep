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

package org.talend.dataprep.parameters;

import static java.util.Collections.emptyList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.impl.client.HttpClientBuilder;
import org.talend.dataprep.i18n.MessagesBundle;
import org.talend.dataprep.util.MessagesBundleContext;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Parameter that should be displayed as a select box in the UI.
 */
public class SelectParameter extends Parameter implements Serializable {

    /** Serialization UID. */
    private static final long serialVersionUID = 1L;

    private final boolean isRadio;

    /** The select items. */
    @JsonIgnore // will be part of the Parameter#configuration
    private List<Item> items;

    /** True if multiple items can be selected. */
    @JsonIgnore // will be part of the Parameter#configuration
    private boolean multiple;

    /**
     * Private constructor to ensure the use of builder.
     *  @param name The parameter name.
     * @param defaultValue The parameter default value.
     * @param implicit True if the parameter is implicit.
     * @param canBeBlank True if the parameter can be blank.
     * @param items List of items for this select parameter.
     * @param multiple True if multiple selection is allowed.
     * @param isRadio <code>true</code> if the rendering code should prefer radio buttons instead of drop down list.
     */
    private SelectParameter(String name, String defaultValue, boolean implicit, boolean canBeBlank, List<Item> items,
                            boolean multiple, boolean isRadio) {
        super(name, ParameterType.SELECT, defaultValue, implicit, canBeBlank);
        this.isRadio = isRadio;
        setItems(items);
        setMultiple(multiple);
    }

    /**
     * @return <code>true</code> if the rendering code should prefer radio buttons instead of drop down list.
     */
    public boolean isRadio() {
        return isRadio;
    }

    /**
     * @return the Items
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * @param items the items to set.
     */
    public void setItems(List<Item> items) {
        addConfiguration("values", items);
        this.items = items;
    }

    /**
     * @return the Multiple
     */
    public boolean isMultiple() {
        return multiple;
    }

    /**
     * @param multiple the multiple to set.
     */
    public void setMultiple(boolean multiple) {
        addConfiguration("multiple", multiple);
        this.multiple = multiple;
    }

    /**
     * Models a select item.
     */
    public static class Item {

        /** the item value. */
        private String value;

        /** the item label. */
        private String label;

        /** The optional inline parameter. */
        @JsonProperty("parameters")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<Parameter> inlineParameters;

        /**
         * Create a select Item. The item's label will be by default looked up with key ("choice." + value). You may
         * override this behavior using {@link #setLabel(String)}.
         *
         * @param value the item value.
         * @param parameters the item optional parameters.
         * @see MessagesBundle#getString(String)
         * @see #setLabel(String)
         */
        private Item(String value, List<Parameter> parameters) {
            this.value = value;
            this.label = MessagesBundleContext.get() //
                    .getString("choice." + value, value);
            this.inlineParameters = parameters;
        }

        /**
         * Create a select Item.
         *
         * @param value the item value.
         */
        private Item(String value) {
            this(value, emptyList());
        }

        /**
         * @return the Value
         */
        public String getValue() {
            return value;
        }

        /**
         * @return the InlineParameters
         */
        public List<Parameter> getInlineParameters() {
            return inlineParameters;
        }

        public String getLabel() {
            final String lookupKey = "parameter." + label + ".label";
            final String translatedLabel = MessagesBundleContext.get().getString(lookupKey);
            return lookupKey.equals(translatedLabel) ? label : translatedLabel;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public static class Builder {

            private String label;
            
            private String value;

            private List<Parameter> inlineParameters;

            public static Builder builder() {
                return new Builder();
            }

            public Builder value(String value) {
                this.value = value;
                return this;
            }

            public Builder label(String label) {
                this.label = label;
                return this;
            }

            public Builder inlineParameters(List<Parameter> inlineParameters) {
                this.inlineParameters = inlineParameters;
                return this;
            }

            public Item build() {
                Item toReturn = new Item(value, inlineParameters == null ? emptyList() : inlineParameters);
                if (label != null) {
                    toReturn.setLabel(label);
                }
                return toReturn;
            }
        }
    }

    /**
     * Builder used to simplify the syntax of creation.
     */
    public static class Builder {

        /** The parameter name. */
        private String name = "";

        /** List of items. */
        private List<Item> items = new ArrayList<>();

        /** The parameter default value. */
        private String defaultValue = "";

        /** True if the parameter is not displayed to the user. */
        private boolean implicit = false;

        /** True if the parameter can be blank. */
        private boolean canBeBlank = false;

        /** True if the selection is multiple. */
        private boolean multiple = false;

        /** True if rendering should prefer radio buttons to render parameters choices */
        private boolean isRadio = false;

        /**
         * @return A SelectParameter builder.
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * Set the name of the select parameter.
         *
         * @param name the name of the select parameter.
         * @return the builder to carry on building the column.
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Set the defaultValue of the select parameter.
         *
         * @param defaultValue the default value of the select parameter.
         * @return the builder to carry on building the column.
         */
        public Builder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        /**
         * Set the implicit of the select parameter.
         *
         * @param implicit true if the parameter is implicit.
         * @return the builder to carry on building the column.
         */
        public Builder implicit(boolean implicit) {
            this.implicit = implicit;
            return this;
        }

        /**
         * Set the canBeBlank of the select parameter.
         *
         * @param canBeBlank true if the parameter is implicit.
         * @return the builder to carry on building the column.
         */
        public Builder canBeBlank(boolean canBeBlank) {
            this.canBeBlank = canBeBlank;
            return this;
        }

        /**
         * Add an item to the select parameter builder.
         *
         * @param value the item value.
         * @param parameter the item optional parameter.
         * @return the builder to carry on building the column.
         */
        public Builder item(String value, Parameter... parameter) {
            this.items.add(new Item(value, Arrays.asList(parameter)));
            return this;
        }

        /**
         * Add an item to the select parameter builder.
         *
         * @param value the item value.
         * @return the builder to carry on building the column.
         * @see Item#setLabel(String) to override default translated label.
         */
        public Builder item(String value) {
            this.items.add(new Item(value, emptyList()));
            return this;
        }

        /**
         * Add an item to the select parameter builder.
         *
         * @param value the item value.
         * @param label the item label
         * @return the builder to carry on building the column.
         */
        public Builder item(String value, String label) {
            final Item item = new Item(value, emptyList());
            item.setLabel(label);
            this.items.add(item);
            return this;
        }

        /**
         * Add all items to the select parameter builder.
         *
         * @param items the item name.
         * @return the builder to carry on building the column.
         */
        public Builder items(List<Item> items) {
            this.items.addAll(items);
            return this;
        }

        public Builder radio(boolean isRadio) {
            this.isRadio = isRadio;
            return this;
        }

        /**
         * Build the column with the previously entered values.
         *
         * @return the built column metadata.
         */
        public SelectParameter build() {
            return new SelectParameter(name, defaultValue, implicit, canBeBlank, items, multiple, isRadio);
        }
    }

}
