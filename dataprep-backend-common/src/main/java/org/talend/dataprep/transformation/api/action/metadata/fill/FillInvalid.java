// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.transformation.api.action.metadata.fill;

import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.DATA_CLEANSING;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;

import java.util.Set;

@Component(ActionMetadata.ACTION_BEAN_PREFIX + FillInvalid.FILL_INVALID_ACTION_NAME)
@Scope(value = "prototype")
public class FillInvalid extends AbstractFillWith implements ColumnAction {

    private static final String FILL_INVALID_BOOLEAN = "fillinvalidwithdefaultboolean"; //$NON-NLS-1$

    private static final String FILL_INVALID_DATE = "fillinvalidwithdefaultdate"; //$NON-NLS-1$

    private static final String FILL_INVALID_NUMERIC = "fillinvalidwithdefaultnumeric"; //$NON-NLS-1$

    public static final String FILL_INVALID_ACTION_NAME = "fillinvalidwithdefault"; //$NON-NLS-1$

    private static final String ACTION_PREFIX = "action.";

    private static final String ACTION_DESCRIPTION = ".desc";

    private static final String ACTION_LABEL = ".label";

    @Autowired
    private ApplicationContext applicationContext;

    public FillInvalid() {
        this(Type.STRING);
    }

    public FillInvalid(Type type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return FILL_INVALID_ACTION_NAME;
    }

    @Override
    public String getDescription() {
        if (Type.BOOLEAN.isAssignableFrom(type)) {
            return getMessagesBundle().getString(ACTION_PREFIX + FILL_INVALID_BOOLEAN + ACTION_DESCRIPTION);
        } else if (Type.DATE.isAssignableFrom(type)) {
            return getMessagesBundle().getString(ACTION_PREFIX + FILL_INVALID_DATE + ACTION_DESCRIPTION);
        } else if (Type.NUMERIC.isAssignableFrom(type)) {
            return getMessagesBundle().getString(ACTION_PREFIX + FILL_INVALID_NUMERIC + ACTION_DESCRIPTION);
        } else {
            return getMessagesBundle().getString(ACTION_PREFIX + FILL_INVALID_ACTION_NAME + ACTION_DESCRIPTION);
        }
    }

    @Override
    public String getLabel() {
        if (Type.BOOLEAN.isAssignableFrom(type)) {
            return getMessagesBundle().getString(ACTION_PREFIX + FILL_INVALID_BOOLEAN + ACTION_LABEL);
        } else if (Type.DATE.isAssignableFrom(type)) {
            return getMessagesBundle().getString(ACTION_PREFIX + FILL_INVALID_DATE + ACTION_LABEL);
        } else if (Type.NUMERIC.isAssignableFrom(type)) {
            return getMessagesBundle().getString(ACTION_PREFIX + FILL_INVALID_NUMERIC + ACTION_LABEL);
        } else {
            return getMessagesBundle().getString(ACTION_PREFIX + FILL_INVALID_ACTION_NAME + ACTION_LABEL);
        }
    }

    @Override
    public String getCategory() {
        return DATA_CLEANSING.getDisplayName();
    }

    @Override
    public boolean shouldBeProcessed(String value, ColumnMetadata colMetadata) {
        return colMetadata.getQuality().getInvalidValues().contains(value);
    }

    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return Type.BOOLEAN.isAssignableFrom(Type.get(column.getType())) //
                || Type.DATE.isAssignableFrom(Type.get(column.getType())) //
                || Type.NUMERIC.isAssignableFrom(Type.get(column.getType())) //
                || Type.STRING.isAssignableFrom(Type.get(column.getType()));
    }

    @Override
    public ActionMetadata adapt(ColumnMetadata column) {
        if (column == null || !acceptColumn(column)) {
            return this;
        }
        return applicationContext.getBean(getClass(), Type.valueOf(column.getType().toUpperCase()));
    }

    @Override
    public Set<Behavior> getBehavior() {
        Set<Behavior> behaviors = super.getBehavior();
        behaviors.add(Behavior.NEED_STATISTICS);
        return behaviors;
    }
}
