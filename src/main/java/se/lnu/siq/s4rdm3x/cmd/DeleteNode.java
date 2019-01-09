package se.lnu.siq.s4rdm3x.cmd;

import org.graphstream.graph.Graph;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.Selector;

public class DeleteNode {
    Selector.ISelector m_selection;

    public DeleteNode(Selector.ISelector a_selection) {
        m_selection = a_selection;
    }

    public void run(CGraph a_g) {

        /*LinkedList<Node> toBeRemoved = new LinkedList<>();

        for (Node n : a_g.getNodeSet()) {
            if (m_selection.isSelected(n)) {
                toBeRemoved.add(n);
            }
        }*/

        //for (Node n : toBeRemoved) {
            //for (Edge e : n.getEdgeSet()) {
            //    a_g.removeEdge(e);
            //}
            //a_g.removeNode(n.getId());
        //}
    }
}
