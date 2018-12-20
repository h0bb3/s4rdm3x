package se.lnu.siq.s4rdm3x.cmd;

import se.lnu.siq.s4rdm3x.model.Selector;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.ArrayList;

public class GetNodes {

    private final Selector.ISelector m_selector;
    public ArrayList<CNode> m_nodes;

    public GetNodes(Selector.ISelector a_selector) {
        m_selector = a_selector;
    }

    public void run(CGraph a_g) {
        m_nodes = new ArrayList<>();
        for(CNode n: a_g.getNodes(m_selector)) {
            m_nodes.add(n);
        }
    }
}
