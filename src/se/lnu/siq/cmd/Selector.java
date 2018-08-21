package se.lnu.siq.asm_gs_test.cmd;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import se.lnu.siq.dmodel.dmClass;

import java.util.ArrayList;

public class Selector {

    private static AttributeUtil g_au = new AttributeUtil();
    private Selector() {};

    public interface ISelector {

        public boolean isSelected(Node a_node);

    }

    public static class All implements ISelector {
        public boolean isSelected(Node a_node) {return true;}
    }

    public static class NodeCollection implements ISelector {
        ArrayList<Node> m_nodes = new ArrayList<>();

        public NodeCollection() {

        }

        public NodeCollection(Node a_n) {
            m_nodes.add(a_n);
        }

        public NodeCollection(Node a_n0, Node a_n1) {
            m_nodes.add(a_n0);
            m_nodes.add(a_n1);
        }

        public NodeCollection(Node a_n0, Node a_n1, Node a_n2) {
            m_nodes.add(a_n0);
            m_nodes.add(a_n1);
            m_nodes.add(a_n2);
        }

        public void Add(Node a_n) {
            m_nodes.add(a_n);
        }

        public boolean isSelected(Node a_node) {
            return m_nodes.contains(a_node);
        }
    }

    public static class EdgTo implements ISelector {
        private String m_edgeTag;

        public EdgTo(String a_edgeTag) {m_edgeTag = a_edgeTag;}

        public boolean isSelected(Node a_node) {
            for(Edge e : a_node.getEachEdge()) {
                if (g_au.hasAnyTag(e, m_edgeTag)) {
                    return true;
                }
            }

            return false;
        }
    }

    public static class Tag implements ISelector {
        private String m_tag;

        public Tag(String a_tag) {
            m_tag = a_tag;
        }

        public boolean isSelected(Node a_node) {
            return g_au.hasAnyTag(a_node, m_tag);
        }
    }

    public static class Pkg implements ISelector {
        private String m_package;

        public Pkg(String a_package) {
            m_package = a_package;
        }

        public boolean isSelected(Node a_node) {
            for (dmClass c : g_au.getClasses(a_node)) {
                if (c.getFileName().contains(m_package)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class And implements ISelector {
        ISelector m_left, m_right;

        public And(ISelector a_left, ISelector a_right) {
            m_left = a_left;
            m_right = a_right;
        }

        public boolean isSelected(Node a_node) {
            return m_left.isSelected(a_node) && m_right.isSelected(a_node);
        }
    }

    public static class Or implements ISelector {
        ISelector m_left, m_right;

        public Or(ISelector a_left, ISelector a_right) {
            m_left = a_left;
            m_right = a_right;
        }

        public boolean isSelected(Node a_node) {
            return m_left.isSelected(a_node) || m_right.isSelected(a_node);
        }
    }

    public static class Not implements ISelector {
        ISelector m_right;

        public Not(ISelector a_right) {
            m_right = a_right;
        }

        public boolean isSelected(Node a_node) {
            return !m_right.isSelected(a_node);
        }
    }

}
