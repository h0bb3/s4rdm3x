package se.lnu.siq.s4rdm3x.model;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.util.AttributeUtil;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;

import java.util.regex.Pattern;

public class CNode {

    private Node m_n;

    private static AttributeUtil g_au = new AttributeUtil();


    CNode(Node a_node) {
        m_n = a_node;
    }

    public boolean matchesAnyPackageName(String a_package) {
        for (dmClass c : g_au.getClasses(m_n)) {
            if (c.getFileName().contains(a_package)) {
                return true;
            }
        }

        return false;
    }

    public boolean matchesAnyClassName(Pattern a_pattern) {
        for (dmClass c : g_au.getClasses(m_n)) {
            if (a_pattern.matcher(c.getName()).find()) {
                return true;
            }
        }
        return false;

    }

    public boolean hasAnyTag(String a_tag) {
        return g_au.hasAnyTag(m_n, a_tag);
    }

    public boolean hasEdgeTag(String a_tag) {
        for(Edge e : m_n.getEachEdge()) {
            if (g_au.hasAnyTag(e, a_tag)) {
                return true;
            }
        }

        return false;
    }

}
