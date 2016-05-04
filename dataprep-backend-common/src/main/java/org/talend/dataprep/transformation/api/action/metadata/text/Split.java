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

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory.SPLIT;
import static org.talend.dataprep.parameters.ParameterType.INTEGER;
import static org.talend.dataprep.parameters.ParameterType.STRING;

import java.util.*;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.parameters.Parameter;
import org.talend.dataprep.parameters.SelectParameter;

/**
 * Split a cell value on a separator.
 */
@Component(Split.ACTION_BEAN_PREFIX + Split.SPLIT_ACTION_NAME)
public class Split extends ActionMetadata implements ColumnAction {

    /** The action name. */
    public static final String SPLIT_ACTION_NAME = "split"; //$NON-NLS-1$

    /** The split column appendix. */
    public static final String SPLIT_APPENDIX = "_split_"; //$NON-NLS-1$

    /** The selected separator within the provided list. */
    protected static final String SEPARATOR_PARAMETER = "separator"; //$NON-NLS-1$

    /** The string separator specified by the user. Should be used only if SEPARATOR_PARAMETER value is 'other'. */
    protected static final String MANUAL_SEPARATOR_PARAMETER_STRING = "manual_separator_string"; //$NON-NLS-1$

    /** The regex separator specified by the user. Should be used only if SEPARATOR_PARAMETER value is 'other'. */
    protected static final String MANUAL_SEPARATOR_PARAMETER_REGEX = "manual_separator_regex"; //$NON-NLS-1$

    /** Number of items produces by the split. */
    protected static final String LIMIT = "limit"; //$NON-NLS-1$

    /** This class' logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(Split.class);

    public static final String NEW_COLUMNS_CONTEXT = "newColumns";

    /**
     * @see org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return SPLIT_ACTION_NAME;
    }

    /**
     * @see org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata#getCategory()
     */
    @Override
    public String getCategory() {
        return SPLIT.getDisplayName();
    }

    @Override
    @Nonnull
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();
        parameters.add(new Parameter(LIMIT, INTEGER, "2"));
        //@formatter:off
        parameters.add(SelectParameter.Builder.builder()
                        .name(SEPARATOR_PARAMETER)
                        .canBeBlank(true)
                        .item(":")
                        .item(";")
                        .item(",")
                        .item("@")
                        .item("-")
                        .item("_")
                        .item(" ", "<space>")
                        .item("\t", "<tab>")
                        .item("other (string)", new Parameter(MANUAL_SEPARATOR_PARAMETER_STRING, STRING, EMPTY))
                        .item("other (regex)", new Parameter(MANUAL_SEPARATOR_PARAMETER_REGEX, STRING, EMPTY))
                        .defaultValue(":")
                        .build()
        );
        //@formatter:on
        return parameters;
    }

    /**
     * @see org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return Type.STRING.equals(Type.get(column.getType()));
    }

    /**
     * @see ActionMetadata#compile(ActionContext)
     */
    @Override
    public void compile(ActionContext context) {
        super.compile(context);
        if (context.getActionStatus() == ActionContext.ActionStatus.OK) {
            if (StringUtils.isEmpty(getSeparator(context))) {
                LOGGER.warn("Cannot split on an empty separator");
                context.setActionStatus(ActionContext.ActionStatus.CANCELED);
            }
            // Create split columns
            final RowMetadata rowMetadata = context.getRowMetadata();
            final String columnId = context.getColumnId();
            final ColumnMetadata column = rowMetadata.getById(columnId);
            final Deque<String> lastColumnId = new ArrayDeque<>();
            final Map<String, String> parameters = context.getParameters();
            int limit = Integer.parseInt(parameters.get(LIMIT));
            final List<String> newColumns = new ArrayList<>();
            lastColumnId.push(columnId);
            for (int i = 0; i < limit; i++) {
                final int newColumnIndex = i + 1;
                newColumns.add(context.column(column.getName() + SPLIT_APPENDIX + i,
                        r -> {
                            final ColumnMetadata c = ColumnMetadata.Builder //
                                    .column() //
                                    .type(Type.STRING) //
                                    .computedId(StringUtils.EMPTY) //
                                    .name(column.getName() + SPLIT_APPENDIX + newColumnIndex) //
                                    .build();
                            lastColumnId.push(rowMetadata.insertAfter(lastColumnId.pop(), c));
                            return c;
                        }
                ));
            }
            context.get(NEW_COLUMNS_CONTEXT, p -> newColumns); // Save new column names for apply
        }
    }

    /**
     * @see ColumnAction#applyOnColumn(DataSetRow, ActionContext)
     */
    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final Map<String, String> parameters = context.getParameters();
        final String columnId = context.getColumnId();
        // Set the split values in newly created columns
        final String originalValue = row.get(columnId);
        if (originalValue == null) {
            return;
        }
        // Perform the split
        String realSeparator = getSeparator(context);
        if (!isRegexMode(context)) {
            realSeparator = '[' + realSeparator + ']';
        }
        final int limit = Integer.parseInt(parameters.get(LIMIT));
        final String[] split = originalValue.split(realSeparator, limit);
        final List<String> newColumns = context.get(NEW_COLUMNS_CONTEXT); // Get new columns computed in compile
        if (split.length != 0) {
            final Iterator<String> iterator = newColumns.iterator();
            for (int i = 0; i < limit && iterator.hasNext(); i++) {
                final String newValue = i < split.length ? split[i] : EMPTY;
                row.set(iterator.next(), newValue);
            }
        }
    }

    /**
     * @param context The action context.
     * @return True if the separator is a regex.
     */
    private boolean isRegexMode(ActionContext context) {
        final Map<String, String> parameters = context.getParameters();
        return StringUtils.equals("other (regex)", parameters.get(SEPARATOR_PARAMETER));
    }

    /**
     * @param context The action context.
     * @return The separator from the parameters.
     */
    private String getSeparator(ActionContext context) {
        final Map<String, String> parameters = context.getParameters();
        if (StringUtils.equals("other (string)", parameters.get(SEPARATOR_PARAMETER))) {
            return parameters.get(MANUAL_SEPARATOR_PARAMETER_STRING);
        } else if (StringUtils.equals("other (regex)", parameters.get(SEPARATOR_PARAMETER))) {
            return parameters.get(MANUAL_SEPARATOR_PARAMETER_REGEX);
        } else {
            return parameters.get(SEPARATOR_PARAMETER);
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return EnumSet.of(Behavior.METADATA_CREATE_COLUMNS);
    }
}
