package se.lnu.siq.asm_gs_test;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.view.ViewerListener;
import se.lnu.siq.asm_gs_test.cmd.AddRelationEdges;
import se.lnu.siq.asm_gs_test.cmd.NodeUtil;
//import se.lnu.siq.asm_gs_test.cmd.*;
import se.lnu.siq.asm_gs_test.cmd.Selector;
import se.lnu.siq.dmodel.dmProject;

/**
 * Created by tohto on 2017-08-17.
 */
public class ClickListener implements ViewerListener {

    Graph m_g;

    public ClickListener(Graph graph) {
        m_g = graph;
    }

    public void buttonPushed(String a_id) {
        NodeUtil nu = new NodeUtil(m_g);
        Node n = m_g.getNode(a_id);
        System.out.println("Button pushed on node " + nu.getName(n));

        //AddRelationEdges cmd = new AddRelationEdges("Dret", 17, new Selector.NodeCollection(n), new Selector.Not(new Selector.NodeCollection(n)));
        //cmd.run(m_g);
    }

    public void viewClosed(String id) {
        //loop = false;
    }

    public void buttonReleased(String a_id) {

        NodeUtil nu = new NodeUtil(m_g);
        Node n = m_g.getNode(a_id);
        System.out.println("Button released on node " + nu.getName(n));
    }
}
