package org.talend.dataprep.transformation.pipeline.node;

import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.transformation.pipeline.Visitor;

public class SourceNode extends BasicNode {

    @Override
    public void receive(DataSetRow row, RowMetadata metadata) {
        link.exec().emit(row, metadata);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitSource(this);
    }

}
