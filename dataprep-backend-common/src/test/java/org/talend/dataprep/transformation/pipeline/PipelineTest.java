package org.talend.dataprep.transformation.pipeline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.DisposableBean;
import org.talend.dataprep.api.dataset.*;
import org.talend.dataprep.api.dataset.statistics.StatisticsAdapter;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.transformation.api.action.DataSetRowAction;
import org.talend.dataprep.transformation.api.action.context.ActionContext;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.transformer.json.NullAnalyzer;
import org.talend.dataprep.transformation.pipeline.link.BasicLink;
import org.talend.dataprep.transformation.pipeline.link.CloneLink;
import org.talend.dataprep.transformation.pipeline.link.NullLink;
import org.talend.dataprep.transformation.pipeline.node.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PipelineTest {

    private TestOutput output;

    @Before
    public void setUp() throws Exception {
        output = new TestOutput();
    }

    @Test
    public void testCompileAction() throws Exception {
        // Given
        final Action mockAction = new Action() {

            @Override
            public DataSetRowAction getRowAction() {
                return new DataSetRowAction() {

                    @Override
                    public void compile(ActionContext actionContext) {
                        actionContext.get("ExecutedCompile", p -> true);
                    }

                    @Override
                    public DataSetRow apply(DataSetRow dataSetRow, ActionContext context) {
                        return dataSetRow;
                    }
                };
            }
        };
        final ActionContext actionContext = new ActionContext(new TransformationContext());
        final Node node = NodeBuilder.source().to(new CompileNode(mockAction, actionContext)).to(output).build();
        final RowMetadata rowMetadata = new RowMetadata();
        final DataSetRow row = new DataSetRow(rowMetadata);

        // When
        assertFalse(actionContext.has("ExecutedCompile"));
        node.exec().receive(row, rowMetadata);

        // Then
        assertTrue(actionContext.has("ExecutedCompile"));
        assertTrue(actionContext.get("ExecutedCompile"));
    }

    @Test
    public void testActionNode() throws Exception {
        final ActionContext actionContext = new ActionContext(new TransformationContext());
        final Action mockAction = new Action();
        ActionNode compileNode = new ActionNode(mockAction, actionContext);

        assertEquals(actionContext, compileNode.getActionContext());
        assertEquals(mockAction, compileNode.getAction());
    }

    @Test
    public void testCompileNode() throws Exception {
        final ActionContext actionContext = new ActionContext(new TransformationContext());
        final Action mockAction = new Action();
        CompileNode compileNode = new CompileNode(mockAction, actionContext);

        assertEquals(actionContext, compileNode.getActionContext());
        assertEquals(mockAction, compileNode.getAction());
    }

    @Test
    public void testRecompileAction() throws Exception {
        // Given
        AtomicInteger compileCount = new AtomicInteger();
        final Action mockAction = new Action() {

            @Override
            public DataSetRowAction getRowAction() {
                return new DataSetRowAction() {

                    @Override
                    public void compile(ActionContext actionContext) {
                        compileCount.incrementAndGet();
                        actionContext.setActionStatus(ActionContext.ActionStatus.OK);
                    }

                    @Override
                    public DataSetRow apply(DataSetRow dataSetRow, ActionContext context) {
                        return dataSetRow;
                    }
                };
            }
        };
        final ActionContext actionContext = new ActionContext(new TransformationContext());
        final Node node = NodeBuilder.source().to(new CompileNode(mockAction, actionContext)).to(output).build();
        final RowMetadata rowMetadata = new RowMetadata();
        final DataSetRow row = new DataSetRow(rowMetadata);

        // When
        assertEquals(0, compileCount.get());
        node.exec().receive(row, rowMetadata);
        rowMetadata.addColumn(new ColumnMetadata()); // Change row metadata in middle of the transformation (to trigger
                                                     // new compile).
        node.exec().receive(row, rowMetadata);

        // Then
        assertEquals(2, compileCount.get());
    }

    @Test
    public void testAction() throws Exception {
        // Given
        final Action mockAction = new Action() {

            @Override
            public DataSetRowAction getRowAction() {
                return (r, context) -> {
                    context.get("ExecutedApply", p -> true);
                    return r;
                };
            }
        };
        final ActionContext actionContext = new ActionContext(new TransformationContext());
        final Node node = NodeBuilder.source().to(new ActionNode(mockAction, actionContext)).to(output).build();
        final RowMetadata rowMetadata = new RowMetadata();
        final DataSetRow row = new DataSetRow(rowMetadata);

        // When
        assertFalse(actionContext.has("ExecutedApply"));
        node.exec().receive(row, rowMetadata);

        // Then
        assertTrue(actionContext.has("ExecutedApply"));
        assertTrue(actionContext.get("ExecutedApply"));
    }

    @Test
    public void testCanceledAction() throws Exception {
        // Given
        final Action mockAction = new Action() {

            @Override
            public DataSetRowAction getRowAction() {
                return (r, context) -> {
                    context.get("Executed", p -> true);
                    return r;
                };
            }
        };
        final ActionContext actionContext = new ActionContext(new TransformationContext());
        actionContext.setActionStatus(ActionContext.ActionStatus.CANCELED);
        final Node node = NodeBuilder.source().to(new ActionNode(mockAction, actionContext)).to(output).build();
        final RowMetadata rowMetadata = new RowMetadata();
        final DataSetRow row = new DataSetRow(rowMetadata);

        // When
        node.exec().receive(row, rowMetadata);

        // Then
        assertFalse(actionContext.has("Executed"));
    }

    @Test
    public void testCloneLink() throws Exception {
        // Given
        final TestOutput output2 = new TestOutput();
        final Node node = NodeBuilder.source().toMany(output, output2).build();
        final RowMetadata rowMetadata = new RowMetadata();
        final DataSetRow row1 = new DataSetRow(rowMetadata);
        final DataSetRow row2 = row1.clone();
        row1.setTdpId(1L);
        row2.setTdpId(2L);

        // When
        node.exec().receive(row1, rowMetadata);
        node.exec().receive(row2, rowMetadata);
        node.exec().signal(Signal.END_OF_STREAM);

        // Then
        assertEquals(2, output.getCount());
        assertEquals(2, output2.getCount());
        assertEquals(row2, output.getRow());
        assertEquals(row2, output2.getRow());
        assertEquals(rowMetadata, output.getMetadata());
        assertEquals(rowMetadata, output2.getMetadata());
        assertEquals(Signal.END_OF_STREAM, output.getSignal());
        assertEquals(Signal.END_OF_STREAM, output2.getSignal());
    }

    @Test
    public void testSourceNode() throws Exception {
        // Given
        final Node node = NodeBuilder.source().to(output).build();
        final RowMetadata rowMetadata = new RowMetadata();
        final DataSetRow row1 = new DataSetRow(rowMetadata);
        final DataSetRow row2 = row1.clone();
        row1.setTdpId(1L);
        row2.setTdpId(2L);

        // When
        node.exec().receive(row1, rowMetadata);
        node.exec().receive(row2, rowMetadata);

        // Then
        assertEquals(2, output.getCount());
        assertEquals(row2, output.getRow());
        assertEquals(rowMetadata, output.getMetadata());
    }

    @Test
    public void testFilteredSourceNode() throws Exception {
        // Given
        final Node node = NodeBuilder.filteredSource(r -> r.getTdpId() == 2).to(output).build();
        final RowMetadata rowMetadata = new RowMetadata();
        final DataSetRow row1 = new DataSetRow(rowMetadata);
        final DataSetRow row2 = row1.clone();
        row1.setTdpId(1L);
        row2.setTdpId(2L);

        // When
        node.exec().receive(row1, rowMetadata);
        node.exec().receive(row2, rowMetadata);

        // Then
        assertEquals(1, output.getCount());
        assertEquals(row1, output.getRow());
        assertEquals(rowMetadata, output.getMetadata());
    }

    @Test
    public void testPipeline() throws Exception {
        // Given
        final Pipeline pipeline = new Pipeline(NodeBuilder.source().to(output).build());
        final RowMetadata rowMetadata = new RowMetadata();
        final DataSetRow row = new DataSetRow(rowMetadata);

        // When
        final DataSet dataSet = new DataSet();
        final DataSetMetadata metadata = new DataSetMetadata();
        metadata.setRowMetadata(rowMetadata);
        dataSet.setMetadata(metadata);
        dataSet.setRecords(Collections.singletonList(row).stream());
        pipeline.execute(dataSet);

        // Then
        assertEquals(1, output.getCount());
        assertEquals(row, output.getRow());
        assertEquals(rowMetadata, output.getMetadata());
        assertEquals(Signal.END_OF_STREAM, output.getSignal());
    }

    @Test
    public void testSignals() throws Exception {
        // Given
        final TestOutput output = new TestOutput();
        final Node node = NodeBuilder.source().to(output).build();

        for (final Signal signal : Signal.values()) {
            // When
            node.exec().signal(signal);

            // Then
            assertEquals(signal, output.getSignal());
        }
    }

    @Test
    public void testSink() throws Exception {
        // Given
        final Node node = NodeBuilder.source().sink().build();
        final RowMetadata rowMetadata = new RowMetadata();
        final DataSetRow row = new DataSetRow(rowMetadata);

        // When
        node.exec().receive(row, rowMetadata);

        // Then
        assertEquals(0, output.getCount());
        assertEquals(null, output.getRow());
        assertEquals(null, output.getMetadata());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void terminalNodeLink() throws Exception {
        Node terminalNode = new TerminalNode();
        terminalNode.setLink(null);
    }

    @Test
    public void terminalNode() throws Exception {
        Node terminalNode = new TerminalNode();
        assertEquals(terminalNode.getLink(), NullLink.INSTANCE);
    }

    @Test(expected = TDPException.class)
    public void testOutputAfterTerminalNode() throws Exception {
        // When
        // It is illegal to continue pipeline after a TerminalNode
        NodeBuilder.source().to(NullNode.INSTANCE).to(output).build();
    }

    @Test
    public void testVisitorAndToString() throws Exception {
        final Node node = NodeBuilder.source() //
                .to(new BasicNode()) //
                .toMany(new BasicNode()) //
                .to(new ActionNode(new Action(), new ActionContext(new TransformationContext()))) //
                .to(new DelayedAnalysisNode(c -> NullAnalyzer.INSTANCE, c -> true, new StatisticsAdapter())) //
                .to(output) //
                .build();
        final Pipeline pipeline = new Pipeline(node);
        final TestVisitor visitor = new TestVisitor();

        // When
        pipeline.accept(visitor);

        // Then
        final Class[] expectedClasses = { Pipeline.class, SourceNode.class, BasicLink.class, BasicNode.class, CloneLink.class,
                ActionNode.class, DelayedAnalysisNode.class };
        Assert.assertThat(visitor.traversedClasses, CoreMatchers.hasItems(expectedClasses));
        Assert.assertNotNull(pipeline.toString());
    }

    @Test
    public void testCleanUp() throws Exception {
        // Given
        final TransformationContext transformationContext = new TransformationContext();
        final ActionContext actionContext = transformationContext.create((r, ac) -> r);
        final AtomicInteger wasDestroyed = new AtomicInteger(0);
        actionContext.get("test1", p -> (DisposableBean) wasDestroyed::incrementAndGet);
        actionContext.get("test2", p -> (DisposableBean) wasDestroyed::incrementAndGet);
        final Node node = NodeBuilder.source() //
                .to(new BasicNode()) //
                .to(new CleanUpNode(transformationContext))
                .to(output) //
                .build();

        // When
        node.exec().signal(Signal.END_OF_STREAM);

        // Then
        assertEquals(2, wasDestroyed.get());
    }

    private static class TestOutput extends TerminalNode {

        private DataSetRow row;

        private RowMetadata metadata;

        private int count;

        private Signal signal;

        @Override
        public void receive(DataSetRow row, RowMetadata metadata) {
            count++;
            this.row = row;
            this.metadata = metadata;
        }

        @Override
        public void signal(Signal signal) {
            this.signal = signal;
            super.signal(signal);
        }

        Signal getSignal() {
            return signal;
        }

        DataSetRow getRow() {
            return row;
        }

        RowMetadata getMetadata() {
            return metadata;
        }

        int getCount() {
            return count;
        }
    }

    private static class TestVisitor extends Visitor {

        List<Class> traversedClasses = new ArrayList<>();

        @Override
        public void visitAction(ActionNode actionNode) {
            traversedClasses.add(actionNode.getClass());
            super.visitAction(actionNode);
        }

        @Override
        public void visitCompile(CompileNode compileNode) {
            traversedClasses.add(compileNode.getClass());
            super.visitCompile(compileNode);
        }

        @Override
        public void visitSource(SourceNode sourceNode) {
            traversedClasses.add(sourceNode.getClass());
            super.visitSource(sourceNode);
        }

        @Override
        public void visitBasicLink(BasicLink basicLink) {
            traversedClasses.add(basicLink.getClass());
            super.visitBasicLink(basicLink);
        }

        @Override
        public void visitDelayedAnalysis(DelayedAnalysisNode delayedAnalysisNode) {
            traversedClasses.add(delayedAnalysisNode.getClass());
            super.visitDelayedAnalysis(delayedAnalysisNode);
        }

        @Override
        public void visitPipeline(Pipeline pipeline) {
            traversedClasses.add(pipeline.getClass());
            super.visitPipeline(pipeline);
        }

        @Override
        public void visitNode(Node node) {
            traversedClasses.add(node.getClass());
            super.visitNode(node);
        }

        @Override
        public void visitCloneLink(CloneLink cloneLink) {
            traversedClasses.add(cloneLink.getClass());
            super.visitCloneLink(cloneLink);
        }
    }
}