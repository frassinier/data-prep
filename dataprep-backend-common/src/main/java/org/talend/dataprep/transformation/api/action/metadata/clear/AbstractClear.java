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

package org.talend.dataprep.transformation.api.action.metadata.clear;

import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

/**
 * Abstract class used as base class for clear cells actions.
 */
public abstract class AbstractClear extends ActionMetadata implements ColumnAction {

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String value = row.get(columnId);
        final ColumnMetadata colMetadata = context.getRowMetadata().getById(columnId);
        if (toClear(colMetadata, value, context)) {
            row.set(columnId, StringUtils.EMPTY);
        }
    }

    /**
     *
     * @param colMetadata
     * @param value
     * @param context
     * @return return <code>true</code> if the column must be cleared
     */
    protected abstract boolean toClear(ColumnMetadata colMetadata, String value, ActionContext context);

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_COLUMN);
    }

}
