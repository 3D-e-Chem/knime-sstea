package nl.esciencecenter.e3dchem.sstea;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class ScoreNodeFactory extends NodeFactory<ScoreNodeModel> {

    @Override
    public ScoreNodeModel createNodeModel() {
        return new ScoreNodeModel();
    }

    @Override
    protected int getNrNodeViews() {
        return 1;
    }

    @Override
    public NodeView<ScoreNodeModel> createNodeView(int viewIndex, ScoreNodeModel nodeModel) {
        return new ScoreNodeView(nodeModel);
    }

    @Override
    protected boolean hasDialog() {
        return true;
    }

    @Override
    protected NodeDialogPane createNodeDialogPane() {
        return new ScoreNodeDialog();
    }

}
