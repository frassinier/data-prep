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

package org.talend.dataprep.transformation.api.action.metadata.fill;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.date.BaseDateTests;

import javax.annotation.PostConstruct;

/**
 * Unit test for the FillWithIntegerIfEmpty action.
 *
 * @see FillIfEmpty
 */
public class FillWithIntegerIfEmptyTest extends BaseDateTests {

    /** The action to test. */
    @Autowired
    private FillIfEmpty action;

    @PostConstruct
    public void init() {
        action = (FillIfEmpty) action.adapt(ColumnMetadata.Builder.column().type(Type.INTEGER).build());
    }

    @Test
    public void test_adapt() throws Exception {
        assertThat(action.adapt((ColumnMetadata) null), is(action));
        ColumnMetadata column = column().name("myColumn").id(0).type(Type.INTEGER).build();
        assertThat(action.adapt(column), not(is(action)));
    }

    @Test
    public void should_fill_empty_integer() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "David Bowie");
        values.put("0002", "");
        values.put("0003", "Something");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(Collections.singletonList(ColumnMetadata.Builder.column() //
                .type(Type.INTEGER) //
                .computedId("0002") //
                .build()));

        final DataSetRow row = new DataSetRow(rowMetadata, values);

        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                this.getClass().getResourceAsStream("fillEmptyIntegerAction.json"));

        // when
        ActionTestWorkbench.test(row, action.create(parameters));

        // then
        Assert.assertEquals("25", row.get("0002"));
        Assert.assertEquals("David Bowie", row.get("0001"));
    }

    @Test
    public void should_not_fill_empty_integer() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "David Bowie");
        values.put("0002", "not empty");
        values.put("0003", "Something");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.setColumns(Collections.singletonList(ColumnMetadata.Builder.column() //
                .type(Type.INTEGER) //
                .computedId("0002") //
                .build()));

        final DataSetRow row = new DataSetRow(rowMetadata, values);

        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                this.getClass().getResourceAsStream("fillEmptyIntegerAction.json"));

        // when
        ActionTestWorkbench.test(row, action.create(parameters));

        // then
        Assert.assertEquals("not empty", row.get("0002"));
        Assert.assertEquals("David Bowie", row.get("0001"));
    }

    @Test
    public void should_fill_empty_integer_other_column() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0001", "David Bowie");
        values.put("0002", "");
        values.put("0003", "10");

        final RowMetadata rowMetadata = new RowMetadata();
        rowMetadata.addColumn(ColumnMetadata.Builder.column().type(Type.INTEGER).computedId("0002").build());
        rowMetadata.addColumn(ColumnMetadata.Builder.column().type(Type.INTEGER).computedId("0003").build());

        final DataSetRow row = new DataSetRow(rowMetadata, values);

        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                this.getClass().getResourceAsStream("fillEmptyIntegerAction.json"));

        // when
        parameters.put(FillIfEmpty.MODE_PARAMETER, FillIfEmpty.OTHER_COLUMN_MODE);
        parameters.put(FillIfEmpty.SELECTED_COLUMN_PARAMETER, "0003");
        ActionTestWorkbench.test(row, action.create(parameters));

        // then
        Assert.assertEquals("10", row.get("0002"));
        Assert.assertEquals("David Bowie", row.get("0001"));
    }

    @Test
    public void should_accept_column() {
        assertTrue(action.acceptColumn(getColumn(Type.INTEGER)));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(action.acceptColumn(getColumn(Type.NUMERIC)));
        assertFalse(action.acceptColumn(getColumn(Type.ANY)));
    }

}