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

package org.talend.dataprep.transformation.api.action.metadata.fillinvalid;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataprep.transformation.api.action.metadata.common.ImplicitParameters;
import org.talend.dataprep.transformation.api.action.metadata.fill.FillIfEmpty;
import org.talend.dataprep.transformation.api.action.metadata.fill.FillInvalid;

/**
 * Unit test for the FillWithStringIfInvalid action.
 * 
 * @see FillInvalid
 */
public class FillWithStringIfInvalidTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    @Autowired
    private FillInvalid fillInvalid;

    @PostConstruct
    public void init() {
        fillInvalid = (FillInvalid) fillInvalid.adapt(ColumnMetadata.Builder.column().type(Type.STRING).build());
    }

    @Test
    public void should_fill_non_valid_string() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "N");
        values.put("0002", "100");

        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMetadata = row.getRowMetadata();
        rowMetadata.getById("0002").setType(Type.STRING.getName());
        rowMetadata.getById("0002").getQuality().setInvalidValues(newHashSet("100"));

        Map<String, String> parameters = ActionMetadataTestUtils
                .parseParameters(this.getClass().getResourceAsStream("fillInvalidStringAction.json"));
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0002");

        // when
        final Action action = factory.create(fillInvalid, parameters);
        final ActionContext context = new ActionContext(new TransformationContext(), rowMetadata);
        context.setParameters(parameters);
        action.getRowAction().apply(row, context);

        // then
        assertEquals("beer", row.get("0002"));
        assertEquals("David Bowie", row.get("0000"));
    }

    @Test
    public void should_not_fill_non_valid_string() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "N");
        values.put("0002", "wine");

        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMetadata = row.getRowMetadata();
        rowMetadata.getById("0002").setType(Type.STRING.getName());
        rowMetadata.getById("0002").getQuality().setInvalidValues(newHashSet("100"));

        Map<String, String> parameters = ActionMetadataTestUtils
                .parseParameters(this.getClass().getResourceAsStream("fillInvalidStringAction.json"));
        parameters.put(ImplicitParameters.COLUMN_ID.getKey().toLowerCase(), "0002");

        // when
        final Action action = factory.create(fillInvalid, parameters);
        final ActionContext context = new ActionContext(new TransformationContext(), rowMetadata);
        context.setParameters(parameters);
        action.getRowAction().apply(row, context);

        // then
        assertEquals("wine", row.get("0002"));
        assertEquals("David Bowie", row.get("0000"));
    }

    @Test
    public void should_fill_empty_string_other_column() throws Exception {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "DC");
        values.put("0002", "Something");

        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMetadata = row.getRowMetadata();
        rowMetadata.getById("0001").setType(Type.STRING.getName());
        rowMetadata.getById("0001").getQuality().setInvalidValues(newHashSet("DC"));
        rowMetadata.getById("0002").setType(Type.STRING.getName());

        Map<String, String> parameters = ActionMetadataTestUtils.parseParameters( //
                this.getClass().getResourceAsStream("fillInvalidStringAction.json"));

        // when
        parameters.put(FillIfEmpty.MODE_PARAMETER, FillIfEmpty.OTHER_COLUMN_MODE);
        parameters.put(FillIfEmpty.SELECTED_COLUMN_PARAMETER, "0002");
        final Action action = factory.create(fillInvalid, parameters);
        final ActionContext context = new ActionContext(new TransformationContext(), rowMetadata);
        context.setParameters(parameters);
        action.getRowAction().apply(row, context);

        // then
        Assert.assertEquals("Something", row.get("0001"));
        Assert.assertEquals("David Bowie", row.get("0000"));
    }

    @Test
    public void should_accept_column() {
        assertTrue(fillInvalid.acceptColumn(getColumn(Type.STRING)));
    }

    @Test
    public void should_adapt_null() throws Exception {
        assertThat(fillInvalid.adapt((ColumnMetadata) null), is(fillInvalid));
    }

    @Test
    public void should_not_accept_column() {
        assertFalse(fillInvalid.acceptColumn(getColumn(Type.ANY)));
    }
}