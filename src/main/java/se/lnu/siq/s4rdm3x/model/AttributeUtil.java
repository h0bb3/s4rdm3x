package se.lnu.siq.s4rdm3x.model;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;

import java.util.ArrayList;

/**
 * Created by tohto on 2017-08-22.
 */
class AttributeUtil {

    static final String g_nameKey = "name";
    static final String g_classKey = "dmClasses";
    static final String g_tagKey = "tags";
    static final String g_tagDelim = ",";

    public String getName(Node a_n) {
        return a_n.getAttribute(AttributeUtil.g_nameKey);
    }

    public ArrayList<Node> getNodesWithAnyTag(Iterable<Node> a_nodes, String[] a_tags) {
        ArrayList<Node> al = new ArrayList<>();
        for(Node n : a_nodes) {
            if (hasAnyTag(n, a_tags)) {
                al.add(n);
            }
        }

        return al;
    }

    public void clearAttributes(Node a_node) {
        if (a_node.hasAttribute(g_classKey)) {
            Object o = a_node.getAttribute(g_classKey);
            String name = a_node.getAttribute(g_nameKey);
            a_node.clearAttributes();
            a_node.setAttribute(g_classKey, o);
            a_node.setAttribute(g_nameKey, name);
        } else {
            a_node.clearAttributes();
        }
    }

    public boolean hasClass(Node a_node, dmClass a_class) {
        return getClasses(a_node).contains(a_class);
    }

    public void addClass(Node a_node, dmClass a_class) {
        if (!a_node.hasAttribute(g_classKey)) {
            a_node.setAttribute(g_classKey, new ArrayList<dmClass>());
        }
        ((ArrayList<dmClass>)a_node.getAttribute(g_classKey)).add(a_class);
    }

    public ArrayList<dmClass> getClasses(Node a_node) {
        if (a_node.hasAttribute(g_classKey)) {
            return (ArrayList<dmClass>) a_node.getAttribute(g_classKey);
        }
        return new ArrayList<dmClass>();
    }

    public void addTag(Node a_node, String a_tag) {
        String tags = a_tag;
        if (a_node.hasAttribute(g_tagKey)) {
            tags += g_tagDelim + a_node.getAttribute(g_tagKey);
        }
        a_node.setAttribute(g_tagKey, tags);
    }

    public void addTag(Edge a_edge, String a_tag) {
            String tags = a_tag;
            if (a_edge.hasAttribute(g_tagKey)) {
                tags += g_tagDelim + a_edge.getAttribute(g_tagKey);
            }
            a_edge.setAttribute(g_tagKey, tags);
    }

    public boolean hasAnyTag(Node a_node, String [] a_tags) {
        if (a_node.hasAttribute(g_tagKey)) {
            String [] nodeTags = ((String)a_node.getAttribute(g_tagKey)).split(g_tagDelim);
            for (String nodeTag : nodeTags) {
                for (String tag : a_tags) {
                    if (tag.compareTo(nodeTag) == 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean hasAllTags(Node a_node, String a_tags) {
        String[] tags = a_tags.split(g_tagDelim);
        for(String tag : tags) {
            if (!hasAnyTag(a_node, tag)) {
                return false;
            }
        }

        return true;
    }

    public void removeTag(Node a_node, String a_tag) {
        String tagStr = getTags(a_node);
        tagStr = tagStr.replace(a_tag, "");
        tagStr = tagStr.replace(g_tagDelim+g_tagDelim, g_tagDelim);
        a_node.setAttribute(g_tagKey, tagStr);
    }

    public String getTags(Node a_node) {
        return a_node.getAttribute(g_tagKey);
    }

    public boolean hasAnyTag(Node a_node, String a_tags) {
        return hasAnyTag(a_node, a_tags.split(g_tagDelim));
    }

    public boolean hasAnyTag(Edge a_edge, String a_tags) {
        if (a_edge.hasAttribute(g_tagKey)) {
            String [] edgeTags = ((String)a_edge.getAttribute(g_tagKey)).split(g_tagDelim);
            String [] tags = a_tags.split(g_tagDelim);
            for (String nodeTag : edgeTags) {
                for (String tag : tags) {
                    if (tag.compareTo(nodeTag) == 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
