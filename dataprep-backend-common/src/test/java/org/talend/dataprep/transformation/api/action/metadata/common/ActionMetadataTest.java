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

package org.talend.dataprep.transformation.api.action.metadata.common;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.talend.dataprep.exception.error.CommonErrorCodes.MISSING_ACTION_SCOPE;
import static org.talend.dataprep.transformation.api.action.metadata.category.ScopeCategory.*;

import java.util.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.transformation.api.action.ActionTestWorkbench;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.metadata.AbstractMetadataBaseTest;
import org.talend.dataprep.parameters.Parameter;

@ActiveProfiles(profiles = "test")
public class ActionMetadataTest extends AbstractMetadataBaseTest {

    @Autowired(required = false)
    private CellTransformation cellTransformation;

    @Autowired(required = false)
    private LineTransformation lineTransformation;

    @Autowired(required = false)
    private ColumnTransformation columnTransformation;

    @Autowired(required = false)
    private TableTransformation tableTransformation;

    @Test
    public void acceptScope_should_pass_with_cell_transformation() throws Exception {
        // when
        final boolean result = cellTransformation.acceptScope(CELL);

        // then
        assertThat(result, is(true));
    }

    @Test
    public void acceptScope_should_pass_with_line_transformation() throws Exception {
        // when
        final boolean result = lineTransformation.acceptScope(LINE);

        // then
        assertThat(result, is(true));
    }

    @Test
    public void acceptScope_should_pass_with_column_transformation() throws Exception {
        // when
        final boolean result = columnTransformation.acceptScope(COLUMN);

        // then
        assertThat(result, is(true));
    }

    @Test
    public void acceptScope_should_pass_with_table_transformation() throws Exception {
        //when
        final boolean result = tableTransformation.acceptScope(DATASET);

        // then
        assertThat(result, is(true));
    }

    @Test
    public void acceptScope_should_fail_with_non_cell_transformation() throws Exception {
        // when
        final boolean result = columnTransformation.acceptScope(CELL);

        // then
        assertThat(result, is(false));
    }

    @Test
    public void acceptScope_should_fail_with_non_line_transformation() throws Exception {
        // when
        final boolean result = cellTransformation.acceptScope(LINE);

        // then
        assertThat(result, is(false));
    }

    @Test
    public void acceptScope_should_fail_with_non_column_transformation() throws Exception {
        // when
        final boolean result = tableTransformation.acceptScope(COLUMN);

        // then
        assertThat(result, is(false));
    }

    @Test
    public void acceptScope_should_fail_with_non_table_transformation() throws Exception {
        //when
        final boolean result = columnTransformation.acceptScope(DATASET);

        // then
        assertThat(result, is(false));
    }

    @Test
    public void default_parameters_should_contains_implicit_parameters() throws Exception {
        // when
        final List<Parameter> defaultParams = columnTransformation.getParameters();

        // then
        assertThat(defaultParams, containsInAnyOrder(ImplicitParameters.getParameters().toArray(new Parameter[3])));
    }

    @Test
    public void create_should_throw_exception_when_scope_parameters_are_not_consistent() throws Exception {
        // given
        final Map<String, String> parameters = new HashMap<>();

        // when
        try {
            factory.create(columnTransformation, parameters);
            fail("should have thrown TDPException because scope parameters are inconsistent (scope is missing)");
        }

        // then
        catch (final TDPException e) {
            assertThat(e.getCode(), is(MISSING_ACTION_SCOPE));
        }
    }

    @Test
    public void create_result_should_call_execute_on_cell() throws Exception {
        // given
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("scope", "cell");
        parameters.put("column_id", "0000");
        parameters.put("row_id", "58");

        final Map<String, String> rowValues = new HashMap<>();
        rowValues.put("0000", "toto");
        final DataSetRow row = new DataSetRow(rowValues);
        row.setTdpId(58L);

        final Action action = factory.create(cellTransformation, parameters);

        // when
        ActionTestWorkbench.test(row, actionRegistry, action);

        // then
        assertThat(row.get("0000"), is("TOTO"));
    }

    @Test
    public void create_result_should_not_call_execute_on_cell_with_wrong_row_id() throws Exception {
        // given
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("scope", "cell");
        parameters.put("column_id", "0001");
        parameters.put("row_id", "58");

        final Map<String, String> rowValues = new HashMap<>();
        rowValues.put("0001", "toto");
        final DataSetRow row = new DataSetRow(rowValues);
        row.setTdpId(60L);

        final Action action = factory.create(cellTransformation, parameters);

        // when
        ActionTestWorkbench.test(row, actionRegistry, action);

        // then
        assertThat(row.get("0001"), is("toto"));
    }

    @Test
    public void create_result_should_call_execute_on_line() throws Exception {
        // given
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("scope", "line");
        parameters.put("row_id", "58");

        final Map<String, String> rowValues = new HashMap<>();
        rowValues.put("0001", "toto");
        rowValues.put("0002", "tata");
        final DataSetRow row = new DataSetRow(rowValues);
        row.setTdpId(58L);

        final Action action = factory.create(lineTransformation, parameters);

        // when
        ActionTestWorkbench.test(row, actionRegistry, action);

        // then
        assertThat(row.get("0001"), is("TOTO"));
        assertThat(row.get("0002"), is("TATA"));
    }

    @Test
    public void create_result_should_not_call_execute_on_line_with_wrong_row_id() throws Exception {
        // given
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("scope", "line");
        parameters.put("row_id", "58");

        final Map<String, String> rowValues = new HashMap<>();
        rowValues.put("0001", "toto");
        rowValues.put("0002", "tata");
        final DataSetRow row = new DataSetRow(rowValues);
        row.setTdpId(60L);

        final Action action = factory.create(lineTransformation, parameters);

        // when
        ActionTestWorkbench.test(row, actionRegistry, action);

        // then
        assertThat(row.get("0001"), is("toto"));
        assertThat(row.get("0002"), is("tata"));
    }

    @Test
    public void create_result_should_call_execute_on_column() throws Exception {
        // given
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("scope", "column");
        parameters.put("column_id", "0001");

        final Map<String, String> rowValues = new HashMap<>();
        rowValues.put("0001", "toto");
        rowValues.put("0002", "tata");
        final DataSetRow row = new DataSetRow(rowValues);
        row.setTdpId(58L);

        final Action action = factory.create(columnTransformation, parameters);

        // when
        ActionTestWorkbench.test(row, actionRegistry, action);

        // then
        assertThat(row.get("0001"), is("TOTO"));
        assertThat(row.get("0002"), is("tata"));
    }

    @Test
    public void create_result_should_call_execute_on_table() throws Exception {
        // given
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("scope", "dataset");

        final Map<String, String> rowValues = new HashMap<>();
        rowValues.put("0000", "toto");
        rowValues.put("0001", "tata");
        final DataSetRow row = new DataSetRow(rowValues);

        final Action action = factory.create(tableTransformation, parameters);

        // when
        ActionTestWorkbench.test(row, actionRegistry, action);

        // then
        assertThat(row.get("0000"), is("TOTO"));
        assertThat(row.get("0001"), is("TATA"));
    }
}

// ------------------------------------------------------------------------------------------------------------------
// -----------------------------------------IMPLEMENTATIONS CLASSES--------------------------------------------------
// ------------------------------------------------------------------------------------------------------------------

@Component("action#CellTransformation")
@Profile("test")
class CellTransformation extends ActionMetadata implements CellAction {

    @Override
    public String getName() {
        return "CellTransformation";
    }

    @Override
    public String getCategory() {
        return null;
    }

    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return false;
    }

    @Override
    public void applyOnCell(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String value = row.get(columnId);
        row.set(columnId, value.toUpperCase());
    }

    @Override
    public Set<Behavior> getBehavior() {
        return Collections.emptySet();
    }
}

@Component("action#LineTransformation")
@Profile("test")
class LineTransformation extends ActionMetadata implements RowAction {

    @Override
    public String getName() {
        return "LineTransformation";
    }

    @Override
    public String getCategory() {
        return null;
    }

    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return false;
    }

    @Override
    public void applyOnLine(DataSetRow row, ActionContext context) {
        for (final Map.Entry<String, Object> entry : row.values().entrySet()) {
            row.set(entry.getKey(), entry.getValue().toString().toUpperCase());
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return Collections.emptySet();
    }
}

@Component("action#ColumnTransformation")
@Profile("test")
class ColumnTransformation extends ActionMetadata implements ColumnAction {

    @Override
    public String getName() {
        return "ColumnTransformation";
    }

    @Override
    public String getCategory() {
        return null;
    }

    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return false;
    }

    @Override
    public void applyOnColumn(DataSetRow row, ActionContext context) {
        final String columnId = context.getColumnId();
        final String value = row.get(columnId);
        row.set(columnId, value.toUpperCase());
    }

    @Override
    public Set<Behavior> getBehavior() {
        return Collections.emptySet();
    }
}

@Component("action#TableTransformation")
@Profile("test")
class TableTransformation extends ActionMetadata implements DataSetAction {

    @Override
    public String getName() {
        return "TableTransformation";
    }

    @Override
    public String getCategory() {
        return null;
    }

    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return false;
    }

    @Override
    public void applyOnDataSet(DataSetRow row, ActionContext context) {
        for (final Map.Entry<String, Object> entry : row.values().entrySet()) {
            row.set(entry.getKey(), entry.getValue().toString().toUpperCase());
        }
    }

    @Override
    public Set<Behavior> getBehavior() {
        return Collections.emptySet();
    }
}