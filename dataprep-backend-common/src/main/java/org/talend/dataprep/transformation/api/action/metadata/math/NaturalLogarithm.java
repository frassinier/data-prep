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
package org.talend.dataprep.transformation.api.action.metadata.math;

import static org.talend.dataprep.transformation.api.action.metadata.math.NaturalLogarithm.NATURAL_LOGARITHM_NAME;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.math3.util.FastMath;
import org.springframework.stereotype.Component;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;

/**
 * Create a new column with Natural Logarithm
 */
@Component(ActionMetadata.ACTION_BEAN_PREFIX + NATURAL_LOGARITHM_NAME)
public class NaturalLogarithm extends AbstractMathNoParameterAction {

    protected static final String NATURAL_LOGARITHM_NAME = "natural_logarithm_numbers";

    @Override
    protected String calculateResult(String columnValue) {
        double value = BigDecimalParser.toBigDecimal(columnValue).doubleValue();

        double result = FastMath.log(value);

        return Double.isNaN(result) ? ERROR_RESULT : Double.toString(result);
    }

    @Override
    protected String getColumnNameSuffix() {
        return "natural_logarithm";
    }

    @Override
    public String getName() {
        return NATURAL_LOGARITHM_NAME;
    }

}
