package org.talend.dataprep.transformation.pipeline.node;

import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;

import java.util.function.Predicate;

public class FilterNode extends BasicNode {

    private final Predicate<DataSetRow> filter;

    public FilterNode(Predicate<DataSetRow> filter) {
        this.filter = filter;
    }

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        if (filter.test(row)) {
            super.receive(row, metadata);
        }
    }

}
