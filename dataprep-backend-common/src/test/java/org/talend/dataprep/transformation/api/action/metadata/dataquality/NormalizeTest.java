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
package org.talend.dataprep.transformation.api.action.metadata.dataquality;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.category.ActionCategory;
import org.talend.dataprep.transformation.api.action.metadata.text.LowerCase;

/**
 * Test class for LowerCase action. Creates one consumer, and test it.
 *
 * @see LowerCase
 */
public class NormalizeTest {

    /** The action to test. */
    private Normalize action;

    private Map<String, String> parameters;

    @Before
    public void init() throws IOException {
        action = new Normalize();
        parameters = ActionMetadataTestUtils.parseParameters(NormalizeTest.class.getResourceAsStream("normalize.json"));
    }

    @Test
    public void testAdapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.STRING).build();
        assertThat(action.adapt(column), is(action));
    }

    @Test
    public void testCategory() throws Exception {
        assertThat(action.getCategory(), is(ActionCategory.STRINGS_ADVANCED.getDisplayName()));
    }

    /**
     * @see LowerCase#create(Map)
     */
    @Test
    public void should_normalize() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "Vincent");
        values.put("entity", "François et Stéphane sont là");
        values.put("joined", "May 20th 2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, Object> expectedValues = new HashMap<>();
        expectedValues.put("name", "Vincent");
        expectedValues.put("entity", "francois et stephane sont la");
        expectedValues.put("joined", "May 20th 2015");

        //when
        ActionTestWorkbench.test(row, action.create(parameters));

        //then
        assertEquals(expectedValues, row.values());
    }

    /**
     * @see LowerCase#create(Map)
     */
    @Test
    public void should_do_nothing_since_column_does_not_exist() {
        //given
        final Map<String, String> values = new HashMap<>();
        values.put("name", "Vincent");
        values.put("joined", "May 20th 2015");
        final DataSetRow row = new DataSetRow(values);

        final Map<String, Object> expectedValues = new HashMap<>();
        expectedValues.put("name", "Vincent");
        expectedValues.put("joined", "May 20th 2015");

        //when
        ActionTestWorkbench.test(row, action.create(parameters));

        //then
        assertEquals(expectedValues, row.values());
    }

    @Test
    public void testNormalize(){
        assertEquals("francois",action.normalize("François"));
        assertEquals("r&d",action.normalize("R&d"));
        assertEquals("eecauou",action.normalize("éèçàüöù"));
        assertEquals("cœur",action.normalize("cœur"));
        assertEquals("c'est ouf",action.normalize(" c'est ouf "));
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
}
