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

package org.talend.dataprep.transformation.api.action.metadata.column;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

/**
 * Change the type of a column <b>This action is not displayed in the UI it's here to ease recording it as a Step It's
 * available from column headers</b>
 */
@Component(TypeChange.ACTION_BEAN_PREFIX + TypeChange.TYPE_CHANGE_ACTION_NAME)
public class TypeChange extends ActionMetadata implements ColumnAction {

    /**
     * The action name.
     */
    public static final String TYPE_CHANGE_ACTION_NAME = "type_change"; //$NON-NLS-1$

    public static final String NEW_TYPE_PARAMETER_KEY = "new_type";

    private static final Logger LOGGER = LoggerFactory.getLogger(TypeChange.class);

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return TYPE_CHANGE_ACTION_NAME;
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return true;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return ActionCategory.COLUMN_METADATA.getDisplayName();
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final Map<String, String> parameters = context.getParameters();
        LOGGER.debug("TypeChange for columnId {} with parameters {} ", columnId, parameters);
        final ColumnMetadata columnMetadata = context.getRowMetadata().getById(columnId);
        final String newType = parameters.get(NEW_TYPE_PARAMETER_KEY);
        if (StringUtils.isNotEmpty(newType)) {
            columnMetadata.setType(newType);
            columnMetadata.setTypeForced(true);
            // erase domain
            columnMetadata.setDomain("");
            columnMetadata.setDomainLabel("");
            columnMetadata.setDomainFrequency(0);
            // We must set this to fix TDP-838: we force the domain to empty
            columnMetadata.setDomainForced(true);
        }
        context.setActionStatus(ActionContext.ActionStatus.DONE);
    }

    @Override
    public ActionMetadata adapt(ColumnMetadata column) {
        return this;
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CHANGE_TYPE);
    }
}
