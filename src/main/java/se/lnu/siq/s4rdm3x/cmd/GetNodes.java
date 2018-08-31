package se.lnu.siq.s4rdm3x.cmd;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.util.Selector;

import java.util.ArrayList;

public class GetNodes {

    private final Selector.ISelector m_selector;
    public ArrayList<Node> m_nodes;

    public GetNodes(Selector.ISelector a_selector) {
        m_selector = a_selector;
    }

    public void run(Graph a_g) {
        m_nodes = new ArrayList<>();
        for(Node n: a_g.getEachNode()) {
            if (m_selector.isSelected(n)) {
                m_nodes.add(n);
            }
        }
    }
}
