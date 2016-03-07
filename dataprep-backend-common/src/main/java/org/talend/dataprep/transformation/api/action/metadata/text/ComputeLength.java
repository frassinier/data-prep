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

package org.talend.dataprep.transformation.api.action.metadata.text;

import java.util.EnumSet;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

@Component(ComputeLength.ACTION_BEAN_PREFIX + ComputeLength.LENGTH_ACTION_NAME)
public class ComputeLength extends ActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String LENGTH_ACTION_NAME = "compute_length"; //$NON-NLS-1$

    /**
     * The column appendix.
     */
    public static final String APPENDIX = "_length"; //$NON-NLS-1$

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return LENGTH_ACTION_NAME;
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.STRINGS.getDisplayName();
    }

    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (context.getActionStatus() == ActionContext.ActionStatus.OK) {
            final RowMetadata rowMetadata = context.getRowMetadata();
            final String columnId = context.getColumnId();
            final ColumnMetadata column = rowMetadata.getById(columnId);
            context.column(column.getName() + APPENDIX, (r) -> {
                final ColumnMetadata c = ColumnMetadata.Builder //
                        .column() //
                        .name(column.getName() + APPENDIX) //
                        .type(Type.INTEGER) //
                        .empty(column.getQuality().getEmpty()) //
                        .invalid(column.getQuality().getInvalid()) //
                        .valid(column.getQuality().getValid()) //
                        .headerSize(column.getHeaderSize()) //
                        .build();
                rowMetadata.insertAfter(columnId, c);
                return c;
            });

        }
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        // create new column and append it after current column
        final RowMetadata rowMetadata = context.getRowMetadata();
        final String columnId = context.getColumnId();
        final ColumnMetadata column = rowMetadata.getById(columnId);
        final String lengthColumn = context.column(column.getName() + APPENDIX);

        // Set length value
        final String value = row.get(columnId);
        row.set(lengthColumn, value == null ? "0" : String.valueOf(value.length()));
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }
}
