package nl.esciencecenter.e3dchem.sstea;

import org.knime.core.node.NodeView;

public class ScoreNodeView extends NodeView<ScoreNodeModel> {

    protected ScoreNodeView(ScoreNodeModel nodeModel) {
        super(nodeModel);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onClose() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onOpen() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void modelChanged() {
        ScoreNodeModel nodeModel = getNodeModel();
        assert nodeModel != null;
    }

}
