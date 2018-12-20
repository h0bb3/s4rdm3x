package se.lnu.siq.s4rdm3x.model;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.GraphicNode;
import org.graphstream.ui.view.Viewer;
import se.lnu.siq.s4rdm3x.model.AttributeUtil;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by tohto on 2017-09-22.
 */
public class NodeUtil {


    private static final String g_isPackageKey = "isPackage";

    private static final String g_hideKey = "ui.hide";
    private static final String g_edgeLengthKey = "layout.weight";
    private static final String g_packageEdgeKey = "layoutEdge";
    private static final String g_deflectionForceKey = "layout.weight";

    private Graph m_g;

    public NodeUtil(Graph a_g) {
        m_g = a_g;
    }

    public Node findNode(String a_name) {
        for (Node n : m_g.getNodeSet()) {
            if (getName(n).compareTo(a_name) == 0) {
                return n;
            }
        }
        return null;
    }


    public ArrayList<Node> searchNode(String a_pattern) {
        ArrayList<Node> ret = new ArrayList<>();
        Pattern p = Pattern.compile(a_pattern);
        for (Node n : m_g.getNodeSet()) {
            if (p.matcher(getName(n)).find()) {
                ret.add(n);
            }
        }
        return ret;
    }

    public Node createNode(String a_name) {
        Node n = findNode(a_name);
        if (n == null){
            String id = "" + m_g.getNodeCount();
            n = m_g.addNode(id);
            n.setAttribute(AttributeUtil.g_nameKey, a_name);
            n.setAttribute(g_deflectionForceKey, 17);
        } else {
           // Sys.out.println("Reusing graph node: " + n.getAttribute(AttributeUtil.g_nameKey));
        }
        return n;
    }

    public Node createPackage(String a_name) {
        Node n = createNode(a_name);
        n.setAttribute(g_isPackageKey);
        return n;
    }

    public GraphicNode getGraphicNode(Node a_n) {
        return ((Viewer)m_g.getAttribute("view")).getGraphicGraph().getNode(a_n.getId());
    }

    public void setLength(Edge a_e, double a_length) {
        a_e.setAttribute(g_edgeLengthKey, a_length);
    }
    public double getLength(Edge a_e) {
        return a_e.getAttribute(g_edgeLengthKey);
    }

    public boolean isPackageEdge(Edge a_e) {
        return a_e.hasAttribute(g_packageEdgeKey);
    }

    public Edge addToPackage(Node a_package, Node a_to) {
        String id = "" + m_g.getEdgeCount();
        Edge e = m_g.addEdge(id, a_package, a_to, true);
        hide(e);
        e.setAttribute(g_packageEdgeKey);
        return e;
    }

    public Edge createRelationEdge(Node a_from, Node a_to) {
        String id = "" + m_g.getEdgeCount();
        Edge e = m_g.addEdge(id, a_from, a_to, true);
        GraphicNode g1, g2;
        g1 = getGraphicNode(a_from);
        g2 = getGraphicNode(a_to);

        double x1 = g1.getX();
        double y1 = g1.getY();
        double x2 = g2.getX();
        double y2 = g2.getY();

        setLength(e, Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)));

        return e;
    }

    public boolean isPackage(Node a_n) {
        return a_n.hasAttribute(g_isPackageKey);
    }

    public String getName(Node a_n) {
        return a_n.getAttribute(AttributeUtil.g_nameKey);
    }

    public void show(Edge a_e) {
        a_e.removeAttribute(g_hideKey);
    }

    public void hide(Edge a_e) {
        a_e.setAttribute(g_hideKey);
    }

    public void hide(Node a_n) {
        a_n.setAttribute(g_hideKey);
        a_n.setAttribute("layout.ignore");
        a_n.setAttribute(g_deflectionForceKey, 0.0);
        //m_g.removeNode(a_n);
    }

    public void show(Node a_n) {
        a_n.removeAttribute(g_hideKey);
        a_n.setAttribute(g_deflectionForceKey, 1.0);

    }

    public boolean isVisible(Edge a_e) { return !a_e.hasAttribute(g_hideKey); }

    public boolean isVisible(Node a_n) { return !a_n.hasAttribute(g_hideKey); }
}
