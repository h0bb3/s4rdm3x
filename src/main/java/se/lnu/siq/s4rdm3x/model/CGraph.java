package se.lnu.siq.s4rdm3x.model;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class CGraph {

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

        CNode ret = getNode(a_name);

        if (ret != null) {
            return ret;
        }

        ret = new CNode(a_name, m_nodes.size());
        m_nodes.add(ret);

        return ret;
    }

    public void clear() {
        m_nodes.clear();
    }

    public CNode getNode(String a_name) {
        for (CNode n : m_nodes) {
            if (n.getName().contentEquals(a_name)) {
                return n;
            }
        }

        return null;
    }

    public CNode getNodeByName(String a_name) {
        for (CNode n : m_nodes) {
            if (n.getName().contentEquals(a_name)) {
                return n;
            }
        }

        return null;
    }

    public CNode getNodeByLogicName(String a_logicName) {
        for (CNode n : m_nodes) {
            if (n.getLogicName().contentEquals(a_logicName)) {
                return n;
            }
        }

        return null;
    }

    public ArrayList<CNode> searchNode(String a_namePattern) {
        ArrayList<CNode> ret = new ArrayList<>();
        Pattern p = Pattern.compile(a_namePattern);
        for (CNode n : m_nodes) {
            if (p.matcher(n.getName()).find()) {
                ret.add(n);
            }
        }
        return ret;
    }

    public void removeNode(CNode a_node) {
        m_nodes.remove(a_node);
    }
}
