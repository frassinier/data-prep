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
package org.talend.dataprep.transformation.api.action.metadata.math;

import java.math.BigDecimal;
import java.text.CharacterIterator;
import java.text.DecimalFormat;
import java.text.StringCharacterIterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ColumnAction;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;
import org.talend.dataprep.transformation.api.action.parameters.SelectParameter;

/**
 * This will extract the numeric part
 *
 * We use metric prefix from <a href="https://en.wikipedia.org/wiki/Metric_prefix">Wikipedia</a>
 *
 * <ul>
 * <li>tera, T, 1000000000000</li>
 * <li>giga, G, 1000000000</li>
 * <li>mega, M, 1000000</li>
 * <li>kilo, k, 1000</li>
 * <li>hecto, h, 100</li>
 * <li>deca, da, 10</li>
 * <li>(none), (none), 1</li>
 * <li>deci, d, 0.1</li>
 * <li>centi, c, 0.01</li>
 * <li>milli, m, 0.001</li>
 * <li>micro, μ, 0.000001</li>
 * <li>nano, n, 0.000000001</li>
 * <li>pico p 0.000000000001</li>
 * </ul>
 */
@Component(ActionMetadata.ACTION_BEAN_PREFIX + ExtractNumber.EXTRACT_NUMBER_ACTION_NAME)
public class ExtractNumber extends ActionMetadata implements ColumnAction {

    public static final String EXTRACT_NUMBER_ACTION_NAME = "extract_number"; //$NON-NLS-1$

    public static final String DEFAULT_RESULT = "0";

    /**
     * the maximum fraction digits displayed in the output
     */
    public static final int MAX_FRACTION_DIGITS_DISPLAY = 30;

    public static String DECIMAL_SEPARATOR = "decimal_separator"; //$NON-NLS-1$

    public static String DOT = ".";

    public static String COMMA = ",";

    /**
     * K: the prefix, V: the value
     */
    private Map<String, MetricPrefix> metricPrefixes = new HashMap<>(13);

    /**
     * Initialize the metrics
     * <ul>
     * <li>tera, T, 1000000000000</li>
     * <li>giga, G, 1000000000</li>
     * <li>mega, M, 1000000</li>
     * <li>kilo, k, 1000</li>
     * <li>hecto, h, 100</li>
     * <li>deca, da, 10</li>
     * <li>(none), (none), 1</li>
     * <li>deci, d, 0.1</li>
     * <li>centi, c, 0.01</li>
     * <li>milli, m, 0.001</li>
     * <li>micro, μ, 0.000001</li>
     * <li>nano, n, 0.000000001</li>
     * <li>pico p 0.000000000001</li>
     * </ul>
     */
    @PostConstruct
    public void initialize() {
        // initialize all standard Metric prefix
        metricPrefixes.put("T", new MetricPrefix(new BigDecimal("1000000000000"), "tera", "T"));
        metricPrefixes.put("G", new MetricPrefix(new BigDecimal("1000000000"), "giga", "G"));
        metricPrefixes.put("M", new MetricPrefix(new BigDecimal("1000000"), "mega", "M"));
        metricPrefixes.put("k", new MetricPrefix(new BigDecimal("1000"), "kilo", "k"));
        metricPrefixes.put("h", new MetricPrefix(new BigDecimal("100"), "hecto", "h"));
        metricPrefixes.put("da", new MetricPrefix(new BigDecimal("10"), "deca", "da"));
        metricPrefixes.put("d", new MetricPrefix(new BigDecimal("0.1"), "deci", "d"));
        metricPrefixes.put("c", new MetricPrefix(new BigDecimal("0.01"), "centi", "c"));
        metricPrefixes.put("m", new MetricPrefix(new BigDecimal("0.001"), "milli", "m"));
        metricPrefixes.put("μ", new MetricPrefix(new BigDecimal("0.000001"), "micro", "μ"));
        metricPrefixes.put("n", new MetricPrefix(new BigDecimal("0.000000001"), "nano", "n"));
        metricPrefixes.put("p", new MetricPrefix(new BigDecimal("0.000000000001"), "pico", "p"));
    }

    @Override
    public String getName() {
        return EXTRACT_NUMBER_ACTION_NAME;
    }

    @Override
    public String getCategory() {
        return ActionCategory.SPLIT.getDisplayName();
    }

    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return true;
    }

    @Override
    @Nonnull
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();

        //@formatter:off
        parameters.add(
        SelectParameter.Builder.builder() //
            .name(DECIMAL_SEPARATOR) //
            .item( COMMA ) //
            .item( DOT ) //
            .defaultValue(DOT) //
            .build()
        );
        //@formatter:on

        return parameters;
    }

    @Override
    public void compile(ActionContext actionContext) {
        super.compile(actionContext);
        if (actionContext.getActionStatus() == ActionContext.ActionStatus.OK) {
            final String columnId = actionContext.getColumnId();
            final RowMetadata rowMetadata = actionContext.getRowMetadata();
            final ColumnMetadata column = rowMetadata.getById(columnId);
            actionContext.column("result", r -> {
                final ColumnMetadata c = ColumnMetadata.Builder //
                        .column() //
                        .name(column.getName() + "_number") //
                        .type(Type.NUMERIC) //
                        .build();
                rowMetadata.insertAfter(columnId, c);
                return c;
            });
        }
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final Map<String, String> parameters = context.getParameters();
        String decimalSeparator = parameters.get(DECIMAL_SEPARATOR);
        if (StringUtils.isEmpty(decimalSeparator)) {
            decimalSeparator = DOT;
        }

        // create new column and append it after current column
        final String newColumnId = context.column("result");
        row.set(newColumnId, extractNumber(row.get(columnId), decimalSeparator));
    }

    protected String extractNumber(String value, String decimalSeparator) {

        if (StringUtils.isEmpty(value)) {
            return DEFAULT_RESULT;
        }

        StringCharacterIterator iter = new StringCharacterIterator(value);

        StringBuilder firstPart = new StringBuilder();
        StringBuilder secondPart = new StringBuilder();
        boolean decimalSeparatorFound = false;
        MetricPrefix metricPrefix = null;

        for (char c = iter.first(); c != CharacterIterator.DONE; c = iter.next()) {
            if (StringUtils.equals(decimalSeparator, String.valueOf(c))) {
                decimalSeparatorFound = true;
                continue;
            }
            if (NumberUtils.isNumber(String.valueOf(c))) {
                if (decimalSeparatorFound) {
                    secondPart.append(c);
                } else {
                    firstPart.append(c);
                }
                continue;
            }
            // we take the last metric prefix after digit found
            MetricPrefix found = metricPrefixes.get(String.valueOf(c));
            if (found != null) {
                metricPrefix = found;
            }
        }

        if (firstPart.length() < 1 && secondPart.length() < 1) {
            return DEFAULT_RESULT;
        }
        if (firstPart.length() < 1) {
            firstPart.append('0');
        }

        BigDecimal bigDecimal = new BigDecimal(firstPart.append(DOT).append(secondPart).toString());

        if (metricPrefix != null) {
            bigDecimal = bigDecimal.multiply(metricPrefix.getMultiply());
        }

        DecimalFormat decimalFormat = new DecimalFormat("0.#");
        decimalFormat.setMaximumFractionDigits(MAX_FRACTION_DIGITS_DISPLAY);
        return decimalFormat.format(bigDecimal.stripTrailingZeros());

    }

    public static class MetricPrefix {

        private final String name, sign;

        private final BigDecimal multiply;

        public MetricPrefix(BigDecimal multiply, String name, String sign) {
            this.multiply = multiply;
            this.name = name;
            this.sign = sign;
        }

        public BigDecimal getMultiply() {
            return multiply;
        }

        public String getName() {
            return name;
        }

        public String getSign() {
            return sign;
        }
    }

}
