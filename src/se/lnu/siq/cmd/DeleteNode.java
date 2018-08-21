package se.lnu.siq.asm_gs_test.cmd;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.LinkedList;

public class DeleteNode {
    Selector.ISelector m_selection;

    public DeleteNode(Selector.ISelector a_selection) {
        m_selection = a_selection;
    }

    public void run(Graph a_g) {

        LinkedList<Node> toBeRemoved = new LinkedList<>();

        for (Node n : a_g.getNodeSet()) {
            if (m_selection.isSelected(n)) {
                toBeRemoved.add(n);
            }
        }

        //for (Node n : toBeRemoved) {
            //for (Edge e : n.getEdgeSet()) {
            //    a_g.removeEdge(e);
            //}
            //a_g.removeNode(n.getId());
        //}
    }
}
