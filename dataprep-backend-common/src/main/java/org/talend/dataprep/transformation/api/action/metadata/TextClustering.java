package org.talend.dataprep.transformation.api.action.metadata;

import static org.talend.dataprep.api.preparation.Action.Builder.builder;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

@Component(TextClustering.ACTION_BEAN_PREFIX + TextClustering.TEXT_CLUSTERING)
public class TextClustering extends AbstractDynamicAction {

    /** The action name. */
    public static final String TEXT_CLUSTERING = "textclustering";

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return TEXT_CLUSTERING;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.QUICKFIX.getDisplayName();
    }

    /**
     * @see ActionMetadata#create(Map)
     */
    @Override
    public Action create(Map<String, String> parameters) {
        return builder().withRow((row, context) -> {
            final String columnName = parameters.get(COLUMN_ID);
            final String value = row.get(columnName);

            // replace only the value if present in parameters
            final String replaceValue = parameters.get(value);
            if (replaceValue != null) {
                row.set(columnName, replaceValue);
            }
        }).build();
    }

    /**
     * @see ActionMetadata#accept(ColumnMetadata)
     */
    @Override
    public boolean accept(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }
}
