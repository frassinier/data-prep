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

import static org.talend.dataprep.transformation.api.action.metadata.math.SquareRoot.SQRT_NAME;

import org.apache.commons.math3.util.FastMath;
import org.springframework.stereotype.Component;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;

/**
 * Create a new column with square root value
 */
@Component(ActionMetadata.ACTION_BEAN_PREFIX + SQRT_NAME)
public class SquareRoot extends AbstractMathNoParameterAction {

    protected static final String SQRT_NAME = "square_root_numbers";

    @Override
    protected String calculateResult(String columnValue) {
        double value = BigDecimalParser.toBigDecimal(columnValue).doubleValue();

        return value < 0 ? ERROR_RESULT : Double.toString(FastMath.sqrt(value));
    }

    @Override
    protected String getColumnNameSuffix() {
        return "square_root";
    }

    @Override
    public String getName() {
        return SQRT_NAME;
    }

}
