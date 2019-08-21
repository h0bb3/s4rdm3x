package se.lnu.siq.s4rdm3x.model;

import se.lnu.siq.s4rdm3x.model.AttributeUtil;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class Selector {

    private static AttributeUtil g_au = new AttributeUtil();
    private Selector() {};

    public interface ISelector {

        public boolean isSelected(CNode a_node);

    }

    public static class All implements ISelector {
        public boolean isSelected(CNode a_node) {return true;}
    }

    public static class NodeCollection implements ISelector {
        ArrayList<CNode> m_nodes = new ArrayList<>();

        public NodeCollection() {

        }

        public NodeCollection(CNode a_n) {
            m_nodes.add(a_n);
        }

        public NodeCollection(CNode a_n0, CNode a_n1) {
            m_nodes.add(a_n0);
            m_nodes.add(a_n1);
        }

        public NodeCollection(CNode a_n0, CNode a_n1, CNode a_n2) {
            m_nodes.add(a_n0);
            m_nodes.add(a_n1);
            m_nodes.add(a_n2);
        }

        public void Add(CNode a_n) {
            m_nodes.add(a_n);
        }

        public boolean isSelected(CNode a_node) {
            return m_nodes.contains(a_node);
        }
    }

    /*public static class EdgTo implements ISelector {
        private String m_edgeTag;

        public EdgTo(String a_edgeTag) {m_edgeTag = a_edgeTag;}

        public boolean isSelected(CNode a_node) {
            return a_node.hasEdgeTag(m_edgeTag);
        }
    }*/

    public static class Tag implements ISelector {
        private String m_tag;

        public Tag(String a_tag) {
            m_tag = a_tag;
        }

        public boolean isSelected(CNode a_node) {
            return a_node.hasAnyTag(m_tag.split(","));
        }
    }

    public static class Pat implements ISelector {
        private String m_sPattern;
        private Pattern m_cPattern;

        public Pat(String a_pattern) {
            m_sPattern = "^"+a_pattern+"$";
            m_cPattern = Pattern.compile(m_sPattern);
        }

        public boolean isSelected(CNode a_node) {
            return a_node.matchesAnyClassName(m_cPattern);
        }
    }

    public static class Pkg implements ISelector {
        private String m_package;

        public Pkg(String a_package) {
            m_package = a_package;
        }

        public boolean isSelected(CNode a_node) {

            return a_node.matchesAnyPackageName(m_package);
        }
    }

    public static class And implements ISelector {
        ISelector m_left, m_right;

        public And(ISelector a_left, ISelector a_right) {
            m_left = a_left;
            m_right = a_right;
        }

        public boolean isSelected(CNode a_node) {
            return m_left.isSelected(a_node) && m_right.isSelected(a_node);
        }
    }

    public static class Or implements ISelector {
        ISelector m_left, m_right;

        public Or(ISelector a_left, ISelector a_right) {
            m_left = a_left;
            m_right = a_right;
        }

        public boolean isSelected(CNode a_node) {
            return m_left.isSelected(a_node) || m_right.isSelected(a_node);
        }
    }

    public static class Not implements ISelector {
        ISelector m_right;

        public Not(ISelector a_right) {
            m_right = a_right;
        }

        public boolean isSelected(CNode a_node) {
            return !m_right.isSelected(a_node);
        }
    }

}
