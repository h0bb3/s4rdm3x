package se.lnu.siq.asm_gs_test.cmd;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;


/**
 * Created by tohto on 2017-09-22.
 */
public class ContractNode {
    Selector.ISelector m_selection;
    boolean m_doContract;

    public ContractNode(boolean a_doContract, Selector.ISelector a_selection) {
        m_selection = a_selection;
        m_doContract = a_doContract;
    }

    public void run(Graph a_g) {
        NodeUtil nu = new NodeUtil(a_g);

        for (Node n : a_g.getNodeSet()) {
            if (m_selection.isSelected(n)) {
                contract(n, nu);
            }
        }
    }

    public void contract(Node a_n, NodeUtil a_nu) {
        final String g_originalLengthKey = "original.length";
        final String g_isContractedKey = "isContracted";
        if (a_nu.isPackage(a_n)) {

            if (m_doContract && !a_n.hasAttribute(g_isContractedKey)) {
                a_n.setAttribute(g_isContractedKey);
                for (Edge e : a_n.getEachLeavingEdge()) {
                    contract(e.getNode1(), a_nu);
                    e.setAttribute(g_originalLengthKey, a_nu.getLength(e));
                    a_nu.setLength(e, 0);
                    a_nu.hide((Node)e.getNode1());
                }
            } else if (!m_doContract && a_n.hasAttribute(g_isContractedKey)){
                a_n.removeAttribute(g_isContractedKey);
                for (Edge e : a_n.getEachLeavingEdge()) {
                    contract(e.getNode1(), a_nu);
                    a_nu.show((Node)e.getNode1());
                    if (e.hasAttribute(g_originalLengthKey)) {
                        a_nu.setLength(e, e.getAttribute(g_originalLengthKey));
                    }
                }
            }
        }
    }
}
