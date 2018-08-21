package se.lnu.siq.s4rdm3x.cmd;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.LinkedList;

/**
 * Created by tohto on 2017-08-22.
 */
public class ShowNode {

    Selector.ISelector m_selection;
    boolean m_doShow;

    public ShowNode(Selector.ISelector a_selection, boolean a_doShow) {
        m_selection = a_selection;
        m_doShow = a_doShow;
    }

    public void run(Graph a_g) {
        NodeUtil nu = new NodeUtil(a_g);
        LinkedList<Node> shownNodes = new LinkedList<>();

        for (Node n : a_g.getEachNode()) {

            if (m_selection.isSelected(n)) {
                if (m_doShow) {
                    nu.show(n);
                    shownNodes.add(n);
                } else {
                    nu.hide(n);
                    for (Edge e : n.getEdgeSet()) {
                        nu.hide(e);
                    }
                }
            }
        }

        // show edges again if both nodes are shown
        for (Node n : shownNodes) {
            for (Edge e : n.getEdgeSet()) {
                if (!nu.isPackageEdge(e) && nu.isVisible((Node)e.getNode0()) && nu.isVisible((Node)e.getNode1())) {
                    nu.show(e);
                }
            }
        }
    }
}
