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

package org.talend.dataprep.transformation.api.action.metadata.delete;

import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.FILTERED;

import java.util.EnumSet;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

@Component(KeepOnly.ACTION_BEAN_PREFIX + KeepOnly.KEEP_ONLY_ACTION_NAME)
public class KeepOnly extends ActionMetadata implements ColumnAction {

    public static final String KEEP_ONLY_ACTION_NAME = "keep_only";

    @Override
    public String getName() {
        return KEEP_ONLY_ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return FILTERED.getDisplayName();
    }

    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return true;
    }

    @Override
    protected boolean implicitFilter() {
        return false;
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        if (!getFilter(context.getParameters()).test(row)) {
            row.setDeleted(true);
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.VALUES_ALL);
    }
}
