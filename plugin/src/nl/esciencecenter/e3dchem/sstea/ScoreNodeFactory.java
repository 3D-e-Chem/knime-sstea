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
        return 0;
    }

    @Override
    public NodeView<ScoreNodeModel> createNodeView(int viewIndex, ScoreNodeModel nodeModel) {
        return null;
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
