package org.talend.dataprep.transformation.api.action.metadata;

import java.util.Collections;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.talend.dataprep.api.type.Type;

@Component(value = FillWithDefaultIfEmptyInteger.ACTION_BEAN_PREFIX + FillWithDefaultIfEmptyInteger.FILL_EMPTY_ACTION_NAME)
public class FillWithDefaultIfEmptyInteger extends AbstractDefaultIfEmpty {

    public static final String FILL_EMPTY_ACTION_NAME = "fillemptywithdefaultinteger"; //$NON-NLS-1$

    @Override
    public String getName() {
        return FILL_EMPTY_ACTION_NAME;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] { COLUMN_NAME_PARAMETER, new Parameter(DEFAULT_VALUE_PARAMETER, Type.INTEGER.getName(), "0") };
    }

    @Override
    public Item[] getItems() {
        return new Item[0];
    }

    @Override
    public Set<Type> getCompatibleColumnTypes() {
        return Collections.singleton(Type.INTEGER);
    }

}
