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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getRow;
import static org.talend.dataprep.transformation.api.action.metadata.text.ExtractStringTokens.MODE_PARAMETER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.parameters.Parameter;

/**
 * Test class for ExtractStringTokens action. Creates one consumer, and test it.
 *
 * @see ExtractStringTokens
 */
public class ExtractStringTokensTest extends AbstractMetadataBaseTest {

    /**
     * The action to test.
     */
    @Autowired
    private ExtractStringTokens action;

    /** The action parameters. */
    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils.parseParameters(ExtractStringTokensTest.class.getResourceAsStream("extractStringTokensAction.json"));
    }

    @Test
    public void testName() throws Exception {
        assertEquals("extract_string_tokens", action.getName());
    }

    @Test
    public void testParameters() throws Exception {
        final List<Parameter> parameters = action.getParameters();
        assertThat(parameters.size(), is(6));
        assertThat(parameters.stream().filter(p -> StringUtils.equals(p.getName(), MODE_PARAMETER)).count(), is(1L));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.SPLIT.getDisplayName()));
    }

    @Test
    public void should_extract_tokens_1() {
        // given
        final DataSetRow row = getRow("lorem bacon", "Great #bigdata presentations at #TalendConnect", "01/01/2015");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Great #bigdata presentations at #TalendConnect");
        expectedValues.put("0003", "bigdata");
        expectedValues.put("0004", "TalendConnect");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_extract_tokens_2() {
        // given
        final DataSetRow row = getRow("lorem bacon", "dn=abcd/cn=xxxx/cn=yyyy/on=zzzz", "01/01/2015");
        parameters.put(ExtractStringTokens.PARAMETER_REGEX, "[co]n=(\\w+)");
        parameters.put(ExtractStringTokens.LIMIT, "3");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "dn=abcd/cn=xxxx/cn=yyyy/on=zzzz");
        expectedValues.put("0003", "xxxx");
        expectedValues.put("0004", "yyyy");
        expectedValues.put("0005", "zzzz");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_TDP_1195() {
        // given
        final DataSetRow row = getRow("lorem bacon", "10413 UNIVERSITY BLVD", "01/01/2015");
        parameters.put(ExtractStringTokens.PARAMETER_REGEX, "(\\d+) ([NSEW])?");
        parameters.put(ExtractStringTokens.LIMIT, "3");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "10413 UNIVERSITY BLVD");
        expectedValues.put("0003", "10413");
        expectedValues.put("0004", "");
        expectedValues.put("0005", "");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_extract_tokens_single_column() {
        // given
        final DataSetRow row = getRow("lorem bacon", "Great #bigdata presentations at #TalendConnect", "01/01/2015");
        parameters.put(MODE_PARAMETER, ExtractStringTokens.SINGLE_COLUMN_MODE);
        parameters.put(ExtractStringTokens.PARAMETER_SEPARATOR, ",");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Great #bigdata presentations at #TalendConnect");
        expectedValues.put("0003", "bigdata,TalendConnect");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_extract_tokens_single_column_alt_separator() {
        // given
        final DataSetRow row = getRow("lorem bacon", "Great #bigdata presentations at #TalendConnect", "01/01/2015");
        parameters.put(MODE_PARAMETER, ExtractStringTokens.SINGLE_COLUMN_MODE);
        parameters.put(ExtractStringTokens.PARAMETER_SEPARATOR, "LOL");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Great #bigdata presentations at #TalendConnect");
        expectedValues.put("0003", "bigdataLOLTalendConnect");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_extract_tokens_multiple_groups() {
        // given
        final DataSetRow row = getRow("lorem bacon", "smallet@talend.com and stef@yopmail.com", "01/01/2015");
        parameters.put(ExtractStringTokens.PARAMETER_REGEX, "(\\w+)@(\\w+[.]\\w+)");
        parameters.put(ExtractStringTokens.LIMIT, "4");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "smallet@talend.com and stef@yopmail.com");
        expectedValues.put("0003", "smallet");
        expectedValues.put("0004", "talend.com");
        expectedValues.put("0005", "stef");
        expectedValues.put("0006", "yopmail.com");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }


    @Test
    public void should_extract_tokens_multiple_groups_single_column() {
        // given
        final DataSetRow row = getRow("lorem bacon", "smallet@talend.com and stef@yopmail.com", "01/01/2015");
        parameters.put(ExtractStringTokens.PARAMETER_REGEX, "(\\w+)@(\\w+[.]\\w+)");
        parameters.put(MODE_PARAMETER, ExtractStringTokens.SINGLE_COLUMN_MODE);
        parameters.put(ExtractStringTokens.PARAMETER_SEPARATOR, ",");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "smallet@talend.com and stef@yopmail.com");
        expectedValues.put("0003", "smallet,talend.com,stef,yopmail.com");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_extract_tokens_more_match_than_limit() {
        // given
        final DataSetRow row = getRow("lorem bacon", "Great #bigdata and #dataquality presentations at #TalendConnect", "01/01/2015");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Great #bigdata and #dataquality presentations at #TalendConnect");
        expectedValues.put("0003", "bigdata");
        expectedValues.put("0004", "dataquality");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void test_TDP_786_empty_pattern() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Je vais bien (tout va bien)");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        parameters.put(ExtractStringTokens.PARAMETER_REGEX, "");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(values, row.values());
    }


    @Test
    public void test_TDP_786_null_pattern() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Je vais bien (tout va bien)");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        parameters.put(ExtractStringTokens.PARAMETER_REGEX, null);

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(values, row.values());
    }

    @Test
    public void test_TDP_831_invalid_pattern() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Je vais bien (tout va bien)");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        parameters.put(ExtractStringTokens.PARAMETER_REGEX, "(");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(values, row.values());
    }

    @Test
    public void should_extract_tokens_twice() {
        final DataSetRow row = getRow("lorem bacon", "Great #bigdata presentations at #TalendConnect", "01/01/2015");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Great #bigdata presentations at #TalendConnect");
        expectedValues.put("0005", "bigdata");
        expectedValues.put("0006", "TalendConnect");
        expectedValues.put("0003", "bigdata");
        expectedValues.put("0004", "TalendConnect");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters), factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    /**
     * @see Action#getRowAction()
     */
    @Test
    public void should_extract_token_no_match() {
        // given
        final DataSetRow row = getRow("lorem bacon", "Great bigdata presentations at TalendConnect", "01/01/2015");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Great bigdata presentations at TalendConnect");
        expectedValues.put("0003", "");
        expectedValues.put("0004", "");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void should_extract_token_empty_input() {
        // given
        final DataSetRow row = getRow("lorem bacon", "", "01/01/2015");

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "");
        expectedValues.put("0003", "");
        expectedValues.put("0004", "");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, factory.create(action, parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    /**
     * @see Action#getRowAction()
     */
    @Test
    public void should_update_metadata() {
        // given
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("0000", "recipe"));
        input.add(createMetadata("0001", "steps"));
        input.add(createMetadata("0002", "last update"));
        final RowMetadata rowMetadata = new RowMetadata(input);

        final List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("0000", "recipe"));
        expected.add(createMetadata("0001", "steps"));
        expected.add(createMetadata("0003", "steps_part_1"));
        expected.add(createMetadata("0004", "steps_part_2"));
        expected.add(createMetadata("0002", "last update"));

        // when
        ActionTestWorkbench.test(rowMetadata, factory.create(action, parameters));

        // then
        assertEquals(expected, rowMetadata.getColumns());
    }

    /**
     * @see Action#getRowAction()
     */
    @Test
    public void should_update_metadata_twice() {
        // given
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("0000", "recipe"));
        input.add(createMetadata("0001", "steps"));
        input.add(createMetadata("0002", "last update"));
        final RowMetadata rowMetadata = new RowMetadata(input);

        final List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("0000", "recipe"));
        expected.add(createMetadata("0001", "steps"));
        expected.add(createMetadata("0005", "steps_part_1"));
        expected.add(createMetadata("0006", "steps_part_2"));
        expected.add(createMetadata("0003", "steps_part_1"));
        expected.add(createMetadata("0004", "steps_part_2"));
        expected.add(createMetadata("0002", "last update"));

        // when
        ActionTestWorkbench.test(rowMetadata, factory.create(action, parameters), factory.create(action, parameters));

        assertEquals(expected, rowMetadata.getColumns());
    }

    public void should_not_update_metadata_because_null_separator() throws IOException {
        // given
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("0000", "recipe"));
        input.add(createMetadata("0001", "steps"));
        input.add(createMetadata("0002", "last update"));
        final RowMetadata rowMetadata = new RowMetadata(input);

        parameters.put(ExtractStringTokens.PARAMETER_REGEX, "");

        // when
        ActionTestWorkbench.test(rowMetadata, factory.create(action, parameters));

        // then
        assertEquals(input, rowMetadata.getColumns());
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.STRING)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptColumn(getColumn(Type.NUMERIC)));
        assertFalse(action.acceptColumn(getColumn(Type.FLOAT)));
        assertFalse(action.acceptColumn(getColumn(Type.DATE)));
        assertFalse(action.acceptColumn(getColumn(Type.BOOLEAN)));
    }

    /**
     * @param name name of the column metadata to create.
     * @return a new column metadata
     */
    protected ColumnMetadata createMetadata(String id, String name) {
        return ColumnMetadata.Builder.column().computedId(id).name(name).type(Type.STRING).headerSize(12).empty(0).invalid(2)
                .valid(5).build();
    }

}
