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

import static org.talend.dataprep.transformation.api.action.metadata.math.Pow.POW_NAME;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.util.FastMath;
import org.springframework.stereotype.Component;
import org.talend.daikon.number.BigDecimalParser;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;

/**
 * Calculate Pow with a constant or an other column 
 */
@Component(ActionMetadata.ACTION_BEAN_PREFIX + POW_NAME)
public class Pow extends AbstractMathOneParameterAction {

    protected static final String POW_NAME = "pow_numbers";

    @Override
    public String getName() {
        return POW_NAME;
    }

    @Override
    protected String getColumnNameSuffix() {
        return "pow";
    }

    @Override
    protected String calculateResult(String columnValue, String parameter) {
        
        String pow = Double.toString(BigDecimalParser.toBigDecimal(columnValue).doubleValue());

        if (StringUtils.isNotBlank(parameter)) {
            pow = Double.toString(FastMath.pow(BigDecimalParser.toBigDecimal(columnValue).doubleValue(), //
                    BigDecimalParser.toBigDecimal(parameter).doubleValue()));
        }
        return pow;
    }
}
