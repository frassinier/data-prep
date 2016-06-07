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

package org.talend.dataprep.transformation.api.action.metadata.datamasking;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.setStatistics;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataquality.datamasking.semantic.MaskableCategoryEnum;

/**
 * Test class for MaskDataByDomain action.
 *
 * @see MaskDataByDomain
 */
public class MaskDataByDomainTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    private MaskDataByDomain action;

    private Map<String, String> parameters;

    private static final Logger LOGGER = LoggerFactory.getLogger(MaskDataByDomainTest.class);

    @Before
    public void init() throws IOException {
        action = new MaskDataByDomain();
        parameters = ActionMetadataTestUtils
                .parseParameters(MaskDataByDomainTest.class.getResourceAsStream("maskDataByDomainAction.json"));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.DATA_MASKING.getDisplayName()));
    }

    @Test
    public void testShouldMaskDatetime() throws IOException {

        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "2015-09-15");
        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMeta = row.getRowMetadata();
        ColumnMetadata colMeta = rowMeta.getById("0000");
        colMeta.setType(Type.DATE.getName());
        setStatistics(row, "0000", MaskDataByDomainTest.class.getResourceAsStream("statistics_datetime.json"));

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        final String resultValue = row.values().get("0000").toString();
        assertTrue("The result [" + resultValue + "] should be a masked date but actually not.",
                resultValue.matches("^2015\\-[0-1][0-9]\\-[0-3][0-9]$"));
    }

    @Test
    public void testShouldMaskEmail() {

        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "azerty@talend.com");
        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMeta = row.getRowMetadata();
        ColumnMetadata colMeta = rowMeta.getById("0000");
        colMeta.setDomain(MaskableCategoryEnum.EMAIL.name());

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "XXXXXX@talend.com");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void testShouldMaskInteger() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "12");
        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMeta = row.getRowMetadata();
        ColumnMetadata colMeta = rowMeta.getById("0000");
        colMeta.setType(Type.INTEGER.getName());

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        int realValueAsInteger = Integer.parseInt((String) row.values().get("0000"));
        LOGGER.info("Row value: {}", realValueAsInteger);
        assertTrue(realValueAsInteger >= 10 && realValueAsInteger <= 14);
    }

    @Test
    public void testShouldMaskDecimal_well_typed() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "12.5");
        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMeta = row.getRowMetadata();
        ColumnMetadata colMeta = rowMeta.getById("0000");
        colMeta.setType(Type.FLOAT.getName());

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        float realValueAsFloat = Float.parseFloat((String) row.values().get("0000"));
        LOGGER.info("Row value: {}", realValueAsFloat);
        assertTrue(realValueAsFloat >= 10 && realValueAsFloat <= 14);
    }

    @Test
    public void testShouldMaskDecimal_wrongly_typed() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "12.5");
        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMeta = row.getRowMetadata();
        ColumnMetadata colMeta = rowMeta.getById("0000");
        colMeta.setType(Type.INTEGER.getName());

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        float realValueAsFloat = Float.parseFloat((String) row.values().get("0000"));
        LOGGER.info("Row value: {}", realValueAsFloat);
        assertTrue(realValueAsFloat >= 10 && realValueAsFloat <= 14);
    }

    @Test
    public void testShouldMaskInteger_wrongly_typed() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "12");
        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMeta = row.getRowMetadata();
        ColumnMetadata colMeta = rowMeta.getById("0000");
        colMeta.setType(Type.FLOAT.getName());

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        int realValueAsInteger = Integer.parseInt((String) row.values().get("0000"));
        LOGGER.info("Row value: {}", realValueAsInteger);
        assertTrue(realValueAsInteger >= 10 && realValueAsInteger <= 14);
    }

    @Test
    public void testShouldIgnoreEmpty() {

        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", " ");
        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMeta = row.getRowMetadata();
        ColumnMetadata colMeta = rowMeta.getById("0000");
        colMeta.setDomain(MaskableCategoryEnum.EMAIL.name());

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", " ");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }


    @Test
    public void testShouldUseDefaultMaskingForInvalid() {

        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "bla bla");
        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMeta = row.getRowMetadata();
        ColumnMetadata colMeta = rowMeta.getById("0000");
        colMeta.setDomain(MaskableCategoryEnum.EMAIL.name());

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "XXXXXXX");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void testShouldNotMaskUnsupportedDataType() {

        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "azerty@talend.com");
        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMeta = row.getRowMetadata();
        ColumnMetadata colMeta = rowMeta.getById("0000");
        colMeta.setType(Type.ANY.getName());

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "azerty@talend.com");

        // when
        ActionTestWorkbench.test(row, actionRegistry, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_skip_unvalid_row_and_keep_on() {
        // given
        long rowId = 120;

        // row 1
        Map<String, String> rowContent = new HashMap<>();
        rowContent.put("0000", "null");
        rowContent.put("0001", "David");
        final DataSetRow row1 = new DataSetRow(rowContent);
        row1.setTdpId(rowId++);

        // row 2
        rowContent = new HashMap<>();
        rowContent.put("0000", "12");
        rowContent.put("0001", "John");
        final DataSetRow row2 = new DataSetRow(rowContent);
        row2.setTdpId(rowId++);

        final RowMetadata rowMeta = row1.getRowMetadata();
        ColumnMetadata colMeta = rowMeta.getById("0000");
        colMeta.setType(Type.INTEGER.getName());

        //when
        ActionTestWorkbench.test(Arrays.asList(row1, row2), actionRegistry, factory.create(action, parameters));

        // then
        // assert that line #1 is left unchanged
        assertEquals("null", row1.values().get("0000"));
        // assert that line #2 is masked
        int realValueAsInteger = Integer.parseInt((String) row2.values().get("0000"));
        LOGGER.info("Row value: {}", realValueAsInteger);
      //  assertNotEquals(12, realValueAsInteger);
        assertTrue(realValueAsInteger >= 10 && realValueAsInteger <= 14);
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.STRING)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptColumn(getColumn(Type.DATE)));
        assertFalse(action.acceptColumn(getColumn(Type.NUMERIC)));
        assertFalse(action.acceptColumn(getColumn(Type.INTEGER)));
        assertFalse(action.acceptColumn(getColumn(Type.FLOAT)));
        assertFalse(action.acceptColumn(getColumn(Type.BOOLEAN)));
        assertFalse(action.acceptColumn(getColumn(Type.ANY)));
    }
}
