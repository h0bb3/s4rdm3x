package se.lnu.siq.s4rdm3x.model;

import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

import java.util.ArrayList;

public class CGraph {

    private MultiGraph m_g;

    public CGraph(String a_name) {
        m_g = new MultiGraph(a_name);
    }

    Iterable<CNode> getNodes() {
        ArrayList<CNode> nodes = new ArrayList<>();

        for (Node n : m_g.getEachNode()) {

            nodes.add(new CNode(n));
        }

        return nodes;
    }


}
