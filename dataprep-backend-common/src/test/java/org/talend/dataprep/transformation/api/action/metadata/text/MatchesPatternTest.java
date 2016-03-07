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

import java.io.IOException;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;

/**
 * Test class for Match Pattern action. Creates one consumer, and test it.
 *
 * @see Split
 */
public class MatchesPatternTest extends AbstractMetadataBaseTest {

    /**
     * The action to test.
     */
    @Autowired
    private MatchesPattern action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils.parseParameters(MatchesPatternTest.class.getResourceAsStream("matchesPattern.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.STRINGS.getDisplayName()));
    }

    @Test
    public void shouldMatchPattern() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon");
        expectedValues.put("0003", "true");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, action.create(parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void shouldMatchPattern_starts_with() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon");
        expectedValues.put("0003", "true");
        expectedValues.put("0002", "01/01/2015");

        parameters.put(MatchesPattern.PATTERN_PARAMETER, "custom");
        parameters.put(MatchesPattern.MANUAL_PATTERN_PARAMETER, generateJson("Bac", "starts_with"));

        // when
        ActionTestWorkbench.test(row, action.create(parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    /**
     * Test with an invalid regex pattern as token and mode is not REGEX.
     */
    @Test
    public void shouldMatchPattern_contains_invalid_regex() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Ba(con");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Ba(con");
        expectedValues.put("0003", "true");
        expectedValues.put("0002", "01/01/2015");

        parameters.put(MatchesPattern.PATTERN_PARAMETER, "custom");
        parameters.put(MatchesPattern.MANUAL_PATTERN_PARAMETER, generateJson("(", "contains"));

        // when
        ActionTestWorkbench.test(row, action.create(parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void shouldNotMatchPattern_starts_with() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon");
        expectedValues.put("0003", "false");
        expectedValues.put("0002", "01/01/2015");

        parameters.put(MatchesPattern.PATTERN_PARAMETER, "custom");
        parameters.put(MatchesPattern.MANUAL_PATTERN_PARAMETER, generateJson("Bak", "starts_with"));

        // when
        ActionTestWorkbench.test(row, action.create(parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void shouldOrNotMatchPattern() {
        assertFalse(action.computeNewValue(" ", buildPatternActionContext("[a-zA-Z]+")));
        assertTrue(action.computeNewValue("aA", buildPatternActionContext("[a-zA-Z]+")));

        assertFalse(action.computeNewValue("Ouch !", buildPatternActionContext("[a-zA-Z0-9]*")));
        assertTrue(action.computeNewValue("Houba 2 fois", buildPatternActionContext("[a-zA-Z0-9 ]*")));
    }

    @Test
    public void shouldNotMatchPattern() {
        assertFalse(action.computeNewValue(" ", buildPatternActionContext("[a-zA-Z]+")));
        assertFalse(action.computeNewValue("aaaa8", buildPatternActionContext("[a-zA-Z]*")));
        assertFalse(action.computeNewValue(" a8 ", buildPatternActionContext("[a-zA-Z]*")));
        assertFalse(action.computeNewValue("aa:", buildPatternActionContext("[a-zA-Z]*")));
    }

    @Test
    public void shouldMatchOrNotoEmptyString() {
        assertTrue(action.computeNewValue("", buildPatternActionContext(".*")));
        assertTrue(action.computeNewValue("", buildPatternActionContext("[a-zA-Z]*")));
        assertFalse(action.computeNewValue(" ", buildPatternActionContext("[a-zA-Z]+")));
        assertTrue(action.computeNewValue(" ", buildPatternActionContext("[a-zA-Z ]+")));
    }

    private ActionContext buildPatternActionContext(String regex) {
        ActionContext context = new ActionContext(new TransformationContext());
        context.setRowMetadata(new RowMetadata());
        context.setParameters(Collections.singletonMap(MatchesPattern.PATTERN_PARAMETER, regex));
        action.compile(context);
        return context;
    }

    @Test
    public void shouldMatchEmptyStringEmptyPattern() {
        assertFalse(action.computeNewValue("", buildPatternActionContext("")));
        assertFalse(action.computeNewValue("  ", buildPatternActionContext("")));
        assertFalse(action.computeNewValue("un petit texte", buildPatternActionContext("")));
    }

    @Test
    public void shouldNotMatchBadPattern() {
        assertFalse(action.computeNewValue("", buildPatternActionContext("*")));
        assertFalse(action.computeNewValue("  ", buildPatternActionContext("*")));
        assertFalse(action.computeNewValue("un petit texte", buildPatternActionContext("*")));
    }

    @Test
    public void shouldMatchPatternTwice() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon");
        expectedValues.put("0004", "true");
        expectedValues.put("0003", "true");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, action.create(parameters), action.create(parameters));

        // then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void shouldUpdateMetadata() {
        // given
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("0000", "recipe"));
        input.add(createMetadata("0001", "steps"));
        input.add(createMetadata("0002", "last update"));
        final RowMetadata rowMetadata = new RowMetadata(input);

        final List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("0000", "recipe"));
        expected.add(createMetadata("0001", "steps"));
        expected.add(createMetadata("0003", "steps_matching", Type.BOOLEAN));
        expected.add(createMetadata("0002", "last update"));

        // when
        ActionTestWorkbench.test(rowMetadata, action.create(parameters));

        // then
        assertEquals(expected, rowMetadata.getColumns());
    }

    @Test
    public void shouldUpdateMetadataTwice() {
        // given
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("0000", "recipe"));
        input.add(createMetadata("0001", "steps"));
        input.add(createMetadata("0002", "last update"));
        final RowMetadata rowMetadata = new RowMetadata(input);

        final List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("0000", "recipe"));
        expected.add(createMetadata("0001", "steps"));
        expected.add(createMetadata("0004", "steps_matching", Type.BOOLEAN));
        expected.add(createMetadata("0003", "steps_matching", Type.BOOLEAN));
        expected.add(createMetadata("0002", "last update"));

        // when
        ActionTestWorkbench.test(rowMetadata, action.create(parameters), action.create(parameters));

        // then
        assertEquals(expected, rowMetadata.getColumns());
    }

    @Test
    public void shouldAcceptColumn() {
        assertTrue(action.acceptColumn(getColumn(Type.STRING)));
    }

    @Test
    public void shouldNotAcceptColumn() {
        assertFalse(action.acceptColumn(getColumn(Type.NUMERIC)));
        assertFalse(action.acceptColumn(getColumn(Type.FLOAT)));
        assertFalse(action.acceptColumn(getColumn(Type.DATE)));
        assertFalse(action.acceptColumn(getColumn(Type.BOOLEAN)));
    }

    private ColumnMetadata createMetadata(String id, String name) {
        return createMetadata(id, name, Type.BOOLEAN);
    }

    private ColumnMetadata createMetadata(String id, String name, Type type) {
        return ColumnMetadata.Builder.column().computedId(id).name(name).type(type).headerSize(12).empty(0).invalid(2).valid(5)
                .build();
    }



}
