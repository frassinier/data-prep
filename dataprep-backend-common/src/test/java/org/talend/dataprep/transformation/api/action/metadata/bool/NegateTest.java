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

package org.talend.dataprep.transformation.api.action.metadata.bool;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.date.BaseDateTests;

/**
 * Test class for Negate action.
 *
 * @see Negate
 */
public class NegateTest extends BaseDateTests {

    /** The action to test. */
    @Autowired
    private Negate action;

    private Map<String, String> parameters;

    /**
     * Default empty constructor
     */
    public NegateTest() throws IOException {
        parameters = ActionMetadataTestUtils.parseParameters(NegateTest.class.getResourceAsStream("negateAction.json"));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.BOOLEAN.getDisplayName()));
    }

    @Test
    public void testActionScope() throws Exception {
        assertThat(action.getActionScope(), is(new ArrayList<>()));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void should_negate_true() {
        // given
        Map<String, String> values = new HashMap<>();
        values.put("name", "Vincent");
        values.put("entity", "R&D");
        values.put("active", "true");
        DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, action.create(parameters));

        // then
        assertThat(row.get("active"), is("False"));
    }

    @Test
    public void should_negate_false() {
        // given
        Map<String, String> values = new HashMap<>();
        values.put("name", "Vincent");
        values.put("entity", "R&D");
        values.put("active", "false");
        DataSetRow row = new DataSetRow(values);

        //when
        ActionTestWorkbench.test(row, action.create(parameters));

        // then
        assertThat(row.get("active"), is("True"));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.BOOLEAN)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptColumn(getColumn(Type.NUMERIC)));
        assertFalse(action.acceptColumn(getColumn(Type.FLOAT)));
        assertFalse(action.acceptColumn(getColumn(Type.DATE)));
        assertFalse(action.acceptColumn(getColumn(Type.STRING)));
    }
}
