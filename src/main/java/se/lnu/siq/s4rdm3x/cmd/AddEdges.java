package se.lnu.siq.s4rdm3x.cmd;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.util.AttributeUtil;
import se.lnu.siq.s4rdm3x.cmd.util.NodeUtil;
import se.lnu.siq.s4rdm3x.cmd.util.Selector;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by tohto on 2017-08-22.
 */
public class AddEdges {
    Selector.ISelector m_selection;
    String m_edgeTag;
    float m_edgeLength;

    public AddEdges(String a_edgeTag, float a_edgeLength, Selector.ISelector a_selection) {
        m_edgeTag = a_edgeTag;
        m_edgeLength = a_edgeLength;
        m_selection = a_selection;
    }

    public void run(Graph a_g) {
        AttributeUtil au = new AttributeUtil();
        NodeUtil nu = new NodeUtil(a_g);
        LinkedList<Node> packageNodes = new LinkedList();

        Node packageNode = nu.createPackage(m_edgeTag);

        au.addTag(packageNode, m_edgeTag);

        for (Node n : a_g.getEachNode()) {
            if (m_selection.isSelected(n)) {
                packageNodes.add(n);
            }
        }

        double circ = 1.1f;
        double maxPR = 0;
        ArrayList<Double> pNodeRadiis = new ArrayList<>();
        for (Node n : packageNodes) {
            if (nu.isPackage(n) != true) {
                circ += 1.1;
            } else {
                double pr = getRadiiForPackageNode(n, nu);
                pNodeRadiis.add(pr);
                if (pr > maxPR) {
                    maxPR = pr;
                }
            }
        }

        // get the largest radii, the diameter is the diameter of the encompassing circle
        // then remove each radii to put everything "inside" this circle
        maxPR = 2 * maxPR;

        double radii = circ / (2.0f * Math.PI);

        if (radii < m_edgeLength) {
            radii = m_edgeLength;
        }

        for (Node n : packageNodes) {
            Edge e = nu.addToPackage(packageNode, n);

            if (nu.isPackage(n) != true) {
                nu.setLength(e, radii);
            } else {
                nu.setLength(e, radii + maxPR - pNodeRadiis.get(0));
            }
            au.addTag(e, m_edgeTag);

            try {
                Thread.sleep(30);
            } catch (InterruptedException ex) {}
        }




       // assignPackageEdges(packageNodes, a_g, m_edgeTag, m_edgeLength);
    }

    private double getRadiiForPackageNode(Node a_n, NodeUtil a_nu) {
        //AttributeUtil au = new AttributeUtil();
        double maxRadii = 0;
        for(Edge e: a_n.getEachLeavingEdge()) {
            double radii = a_nu.getLength(e);
            if (radii > maxRadii) {
                maxRadii = radii;
            }
        }

        return maxRadii;
    }

    /*
    private void assignPackageEdges(LinkedList<Node> a_nodes, Graph a_graph, String a_edgeTag, float a_edgeLength) {

        // assign an edge to between every pair in the list
        ListIterator rootIt = a_nodes.listIterator();
        AttributeUtil tu = new AttributeUtil();

        while(rootIt.hasNext()) {

            Node root = (Node)rootIt.next();
            ListIterator tailIt = a_nodes.listIterator(rootIt.nextIndex());
            //root.setAttribute("ui.class", a_edgeTag);

            while(tailIt.hasNext()) {
                Node tailNode = (Node)tailIt.next();

                String id = "" + a_graph.getEdgeCount();
                Edge e = a_graph.addEdge(id, root, tailNode);
                tu.addTag(e, a_edgeTag);
                e.setAttribute("ui.hide");
                e.setAttribute("layoutEdge");

                e.setAttribute("layout.weight", a_edgeLength);
            }
        }
    }*/
}
