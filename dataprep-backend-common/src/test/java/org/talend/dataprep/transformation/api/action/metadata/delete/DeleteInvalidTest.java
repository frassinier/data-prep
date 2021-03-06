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

package org.talend.dataprep.transformation.api.action.metadata.delete;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.*;
import static org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils.getColumn;

import java.io.IOException;
import java.util.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.quality.AnalyzerService;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadataTestUtils;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;

/**
 * Test class for DeleteInvalid action. Creates one consumer, and test it.
 *
 * @see DeleteInvalid
 */
public class DeleteInvalidTest extends AbstractMetadataBaseTest {

    /** The action to test. */
    @Autowired
    private DeleteInvalid deleteInvalid;

    @Autowired
    private AnalyzerService analyzerService;

    private Map<String, String> parameters;

    /**
     * Default constructor.
     */
    public DeleteInvalidTest() throws IOException {
        parameters = ActionMetadataTestUtils
                .parseParameters(DeleteInvalidTest.class.getResourceAsStream("deleteInvalidAction.json"));
    }

    @Test
    public void testActionScope() throws Exception {
        assertThat(deleteInvalid.getActionScope(), hasItem("invalid"));
    }

    @Test
    public void should_delete_because_non_valid() {
        // given
        final Map<String, String> values = new HashMap<>();
        values.put("0000", "David Bowie");
        values.put("0001", "N");
        values.put("0002", "Something");

        final DataSetRow row = new DataSetRow(values);
        final RowMetadata rowMetadata = row.getRowMetadata();
        rowMetadata.getById("0001").setType(Type.STRING.getName());
        rowMetadata.getById("0001").getQuality().setInvalidValues(Collections.singleton("N"));

        //when
        final Action action = factory.create(deleteInvalid, parameters);
        final ActionContext context = new ActionContext(new TransformationContext(), rowMetadata);
        context.setParameters(parameters);
        action.getRowAction().apply(row, context);

        // then
        assertTrue(row.isDeleted());
        assertEquals("David Bowie", row.get("0000"));
    }


    @Test
    public void should_accept_column() {
        for (Type type : Type.values()) {
            assertTrue(deleteInvalid.acceptColumn(getColumn(type)));
        }
    }

}
