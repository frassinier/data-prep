package org.talend.dataprep.transformation.api.action.metadata.column;

import static org.apache.commons.lang.StringUtils.EMPTY;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.AbstractActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.IColumnAction;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;

/**
 * Rename a column.
 *
 * If the column to rename does not exist or the new name is already used, nothing happen.
 */
@Component(Rename.ACTION_BEAN_PREFIX + Rename.RENAME_ACTION_NAME)
public class Rename extends AbstractActionMetadata implements IColumnAction {

    /** Action name. */
    public static final String RENAME_ACTION_NAME = "rename_column"; //$NON-NLS-1$

    /** Name of the new column parameter. */
    public static final String NEW_COLUMN_NAME_PARAMETER_NAME = "new_column_name"; //$NON-NLS-1$

    /** Parameters (column name, new column name...) */
    private final List<Parameter> parameters;

    public Rename() {
        this(EMPTY);
    }

    public Rename(final String defaultName) {
        this.parameters = super.getParameters();
        this.parameters.add(new Parameter(NEW_COLUMN_NAME_PARAMETER_NAME, Type.STRING.getName(), defaultName));
    }

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return RENAME_ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.COLUMNS.getDisplayName();
    }

    /**
     * @see ActionMetadata#getParameters()
     */
    @Override
    @Nonnull
    public List<Parameter> getParameters() {
        return parameters;
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return true;
    }

    /**
     * @param column A {@link ColumnMetadata column} information.
     * @return A rename action with <code>column</code> name as default name.
     */
    @Override
    public ActionMetadata adapt(ColumnMetadata column) {
        if (column == null) {
            return this;
        }
        return new Rename(column.getName());
    }

    @Override
    protected void beforeApply(Map<String, String> parameters) {
    }

    @Override
    public void applyOnColumn(DataSetRow row, TransformationContext context, Map<String, String> parameters, String columnId) {
        final String newColumnName = parameters.get(NEW_COLUMN_NAME_PARAMETER_NAME);
        final RowMetadata rowMetadata = row.getRowMetadata();
        final ColumnMetadata column = rowMetadata.getById(columnId);
        if (column != null) {
            column.setName(newColumnName);
        }
    }
}