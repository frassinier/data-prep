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
package org.talend.dataprep.transformation.api.action.metadata.column;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.dataset.location.SemanticDomain;
import org.talend.dataprep.api.dataset.statistics.Statistics;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.date.BaseDateTests;

/**
 * Test class for Split action. Creates one consumer, and test it.
 *
 * @see CopyColumnMetadata
 */
public class CopyColumnTest extends BaseDateTests {

    /** The action to test. */
    @Autowired
    private CopyColumnMetadata action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        parameters = ActionMetadataTestUtils.parseParameters(CopyColumnTest.class.getResourceAsStream("copyColumnAction.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.COLUMN_METADATA.getDisplayName()));
    }

    /**
     * @see CopyColumnMetadata#create(Map)
     */
    @Test
    public void should_copy_row() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "lorem bacon");
        values.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        values.put("0002", "01/01/2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, String> expectedValues = new HashMap<>();
        expectedValues.put("0000", "lorem bacon");
        expectedValues.put("0001", "Bacon ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0003", "Bacon ipsum dolor amet swine leberkas pork belly");
        expectedValues.put("0002", "01/01/2015");

        // when
        ActionTestWorkbench.test(row, action.create(parameters));

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
        expected.add(createMetadata("0003", "steps_copy"));
        expected.add(createMetadata("0002", "last update"));

        // when
        ActionTestWorkbench.test(rowMetadata, action.create(parameters));

        // then
        assertEquals(expected, rowMetadata.getColumns());
    }

    /**
     * Test with non-consecutive columns ids. This can occurs after some column deletion.
     */
    @Test
    public void test_TDP_1184() {
        // given
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(createMetadata("0000", "recipe"));
        input.add(createMetadata("0001", "steps"));
        input.add(createMetadata("0003", "last update"));
        final RowMetadata rowMetadata = new RowMetadata(input);

        final List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("0000", "recipe"));
        expected.add(createMetadata("0001", "steps"));
        expected.add(createMetadata("0004", "steps_copy"));
        expected.add(createMetadata("0003", "last update"));

        // when
        ActionTestWorkbench.test(rowMetadata, action.create(parameters));

        // then
        final List<ColumnMetadata> columns = rowMetadata.getColumns();
        assertEquals(expected, columns);
    }

    @Test
    public void should_copy_statistics() throws Exception {
        // given
        final ColumnMetadata original = createMetadata("0001", "column");
        original.setStatistics(new Statistics());
        final List<ColumnMetadata> input = new ArrayList<>();
        input.add(original);
        final RowMetadata rowMetadata = new RowMetadata(input);

        final ColumnMetadata transformed = createMetadata("0002", "column");
        original.setStatistics(new Statistics());
        final List<ColumnMetadata> expected = new ArrayList<>();
        expected.add(createMetadata("0001", "column"));
        expected.add(transformed);

        // when
        ActionTestWorkbench.test(rowMetadata, action.create(parameters));

        // then
        assertEquals(expected.get(1).getStatistics(), original.getStatistics());
    }

    @Test
    public void should_copy_semantic() throws Exception {
        List<ColumnMetadata> input = new ArrayList<>();
        final ColumnMetadata original = createMetadata("0001", "column");
        original.setStatistics(new Statistics());

        SemanticDomain semanticDomain = new SemanticDomain("mountain_goat", "Mountain goat pale pale", 1);

        original.setDomain("beer");
        original.setDomainFrequency(1);
        original.setDomainLabel("the best beer");
        original.setSemanticDomains(Collections.singletonList(semanticDomain));

        input.add(original);
        RowMetadata rowMetadata = new RowMetadata(input);

        Assertions.assertThat(rowMetadata.getColumns()).isNotNull().isNotEmpty().hasSize(1);

        ActionTestWorkbench.test(rowMetadata, action.create(parameters));

        List<ColumnMetadata> expected = rowMetadata.getColumns();

        Assertions.assertThat(expected).isNotNull().isNotEmpty().hasSize(2);

        assertEquals(expected.get(1).getStatistics(), original.getStatistics());

        Assertions.assertThat(expected.get(1)) //
                .isEqualToComparingOnlyGivenFields(original, "domain", "domainLabel", "domainFrequency");

        Assertions.assertThat(expected.get(1).getSemanticDomains()).isNotNull() //
                .isNotEmpty().contains(semanticDomain);
    }

    @Test
    public void test_TDP_567_with_force_true() throws Exception {
        List<ColumnMetadata> input = new ArrayList<>();
        final ColumnMetadata original = createMetadata("0001", "column");
        original.setStatistics(new Statistics());

        SemanticDomain semanticDomain = new SemanticDomain("mountain_goat", "Mountain goat pale pale", 1);

        original.setDomain("beer");
        original.setDomainFrequency(1);
        original.setDomainLabel("the best beer");
        original.setDomainForced(true);
        original.setTypeForced(true);
        original.setSemanticDomains(Collections.singletonList(semanticDomain));

        input.add(original);
        RowMetadata rowMetadata = new RowMetadata(input);

        Assertions.assertThat(rowMetadata.getColumns()).isNotNull().isNotEmpty().hasSize(1);

        ActionTestWorkbench.test(rowMetadata, action.create(parameters));

        List<ColumnMetadata> expected = rowMetadata.getColumns();

        Assertions.assertThat(expected).isNotNull().isNotEmpty().hasSize(2);

        assertEquals(expected.get(1).getStatistics(), original.getStatistics());

        Assertions.assertThat(expected.get(1)) //
                .isEqualToComparingOnlyGivenFields(original, "domain", "domainLabel", "domainFrequency", "domainForced",
                        "typeForced");

        Assertions.assertThat(expected.get(1).getSemanticDomains()).isNotNull() //
                .isNotEmpty().contains(semanticDomain);
    }

    @Test
    public void test_TDP_567_with_force_false() throws Exception {
        List<ColumnMetadata> input = new ArrayList<>();
        final ColumnMetadata original = createMetadata("0001", "column");
        original.setStatistics(new Statistics());

        SemanticDomain semanticDomain = new SemanticDomain("mountain_goat", "Mountain goat pale pale", 1);

        original.setDomain("beer");
        original.setDomainFrequency(1);
        original.setDomainLabel("the best beer");
        original.setDomainForced(false);
        original.setTypeForced(false);
        original.setSemanticDomains(Collections.singletonList(semanticDomain));

        input.add(original);
        RowMetadata rowMetadata = new RowMetadata(input);

        Assertions.assertThat(rowMetadata.getColumns()).isNotNull().isNotEmpty().hasSize(1);

        ActionTestWorkbench.test(rowMetadata, action.create(parameters));

        List<ColumnMetadata> expected = rowMetadata.getColumns();

        Assertions.assertThat(expected).isNotNull().isNotEmpty().hasSize(2);

        assertEquals(expected.get(1).getStatistics(), original.getStatistics());

        Assertions.assertThat(expected.get(1)) //
                .isEqualToComparingOnlyGivenFields(original, "domain", "domainLabel", "domainFrequency", "domainForced",
                        "typeForced");

        Assertions.assertThat(expected.get(1).getSemanticDomains()).isNotNull() //
                .isNotEmpty().contains(semanticDomain);
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.ANY)));
    }

    @Override
    protected ColumnMetadata.Builder columnBaseBuilder() {
        return super.columnBaseBuilder().headerSize(12).valid(5).invalid(2);
    }
}
