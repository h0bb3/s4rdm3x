package se.lnu.siq.s4rdm3x.dmodel;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.w3c.dom.Attr;
import se.lnu.siq.s4rdm3x.cmd.util.AttributeUtil;
import se.lnu.siq.s4rdm3x.cmd.util.NodeUtil;

import java.util.HashMap;

public class NodeGenerator {

    private void setInstructionCount(dmClass.Method a_m, int count) {
        for (int i = 0; i < count; i++) {
            a_m.incInstructionCount();
        }
    }

    private void setBranchStatementCount(dmClass.Method a_m, int count) {
        for (int i = 0; i < count; i++) {
            a_m.incBranchStatementCount();
        }
    }


    public Graph getGraph1() {
        Graph ret = new MultiGraph("graph1");

        dmClass c1 = new dmClass("c1");
        setInstructionCount(c1.addMethod("m1"), 17);
        setInstructionCount(c1.addMethod("m2"), 17);

        setBranchStatementCount(c1.addMethod("m3"), 8);
        setBranchStatementCount(c1.addMethod("m4"), 5);

        Node n1 = ret.addNode("n1");
        AttributeUtil au = new AttributeUtil();

        au.addClass(n1, c1);

        return ret;
    }

    public Graph getGraph2() {
        Graph ret = new MultiGraph("graph2");

        dmClass c1 = new dmClass("c1");
        dmClass.Method m1 = c1.addMethod("m1");
        setInstructionCount(m1, 17);
        setBranchStatementCount(m1, 9);

        dmClass c2 = new dmClass("c2");
        m1 = c2.addMethod("m1");
        setInstructionCount(m1, 17);
        setBranchStatementCount(m1, 6);

        Node n1 = ret.addNode("n1");
        AttributeUtil au = new AttributeUtil();

        au.addClass(n1, c1);
        au.addClass(n1, c2);

        return ret;
    }

    public Graph generateGraph(String [] a_edgesAsNodePairs) {
        return addToGraph(new MultiGraph("test"), dmDependency.Type.MethodCall, a_edgesAsNodePairs);
    }

    public Graph generateGraph(dmDependency.Type a_dependency, String [] a_edgesAsNodePairs) {
        return addToGraph(new MultiGraph("test"), a_dependency, a_edgesAsNodePairs);
    }


    public Graph addToGraph(Graph a_g, dmDependency.Type a_dependency, String [] a_edgesAsNodePairs) {
        Graph ret = a_g;
        String[] edgeIds = a_edgesAsNodePairs;
        HashMap<String, dmClass> classes = new HashMap<>();
        AttributeUtil au = new AttributeUtil();
        for (String id : edgeIds) {
            String first = id.substring(0, 1), second = id.substring(1, 2);
            if (!classes.containsKey(first)) {
                classes.put(first, new dmClass(first));
            }
            if (!classes.containsKey(second)) {
                classes.put(second, new dmClass(second));
            }

            dmClass cFirst = classes.get(first);
            dmClass cSecond = classes.get(second);
            cFirst.addDependency(cSecond, a_dependency, 0);

            if (a_g.getNode(first) == null) {
                Node n = ret.addNode(first);
                au.addClass(n, cFirst);
            }
            if (a_g.getNode(second) == null) {
                Node n = ret.addNode(second);
                au.addClass(n, cSecond);
            }
        }

        return ret;
    }
}
