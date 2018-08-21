package se.lnu.siq.asm_gs_test.cmd;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.GraphicNode;
import org.graphstream.ui.view.Viewer;
import se.lnu.siq.dmodel.dmClass;
import se.lnu.siq.dmodel.dmDependency;

import java.util.LinkedList;

/**
 * Created by tohto on 2017-08-25.
 */
public class AddRelationEdges {

    Selector.ISelector m_source;
    Selector.ISelector m_target;
    String m_edgeTag;
    float m_edgeLength;

    public AddRelationEdges(String a_edgeTag, float a_edgeLength, Selector.ISelector a_from, Selector.ISelector a_to) {
        m_edgeTag = a_edgeTag;
        m_edgeLength = a_edgeLength;
        m_source = a_from;
        m_target = a_to;

    }

    public void run(Graph a_g) {

        LinkedList<Node> from = new LinkedList();
        LinkedList<Node> to = new LinkedList();


        for (Node n : a_g.getEachNode()) {
            if (m_source.isSelected(n)) {
                from.add(n);
            }
        }

        for (Node n : a_g.getEachNode()) {
            if (m_target.isSelected(n)) {
                to.add(n);
            }
        }


        addEdges(from, to, a_g, m_edgeTag, m_edgeLength);
    }

    private int getDirectEdgeCount(Node a_from, Node a_to) {
        int ret = 0;
        for (Edge e : a_from.getEachLeavingEdge()) {
            if (e.getNode1() == a_to) {
                ret++;
            }
        }
        return ret;
    }

    private void addEdges(LinkedList<Node> a_from, LinkedList<Node> a_to, Graph a_graph, String a_edgeTag, float a_edgeLength) {
        AttributeUtil au = new AttributeUtil();
        NodeUtil nu = new NodeUtil(a_graph);

        for(Node from : a_from) {
            for(dmClass cFrom : au.getClasses(from)) {
                for (dmDependency d : cFrom.getDependencies()) {
                    for (Node to : a_to) {
                        if (getDirectEdgeCount(from, to) < 1) {
                            for (dmClass cTo : au.getClasses(to)) {
                                if (cTo == d.getTarget()) {


                                    String id = "" + a_graph.getEdgeCount();
                                    Edge e = nu.createRelationEdge(from, to);
                                    au.addTag(e, a_edgeTag);
                                    try {
                                        Thread.sleep(30);
                                    } catch (InterruptedException ex) {}
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
