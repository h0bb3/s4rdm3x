package se.lnu.siq.s4rdm3x.model;

import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

import java.util.ArrayList;

public class CGraph {

    private NodeUtil m_nu;
    ArrayList<CNode> m_nodes = new ArrayList<>();

    public CGraph() {
    }

    public Iterable<CNode> getNodes(Selector.ISelector a_selector) {
        ArrayList<CNode> ret = new ArrayList<>();

        for(CNode n : getNodes()) {
            if (a_selector.isSelected(n)) {
                ret.add(n);
            }
        }

        return ret;
    }

    public Iterable<CNode> getNodes() {
        return m_nodes;
    }


    public CNode createNode(String a_name) {
        Node n = m_nu.createNode(a_name);
        return new CNode(n);
    }
}
