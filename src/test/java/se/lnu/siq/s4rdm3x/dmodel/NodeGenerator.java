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

    public Graph getGraph1() {
        Graph ret = new MultiGraph("graph1");

        dmClass c1 = new dmClass("c1");
        setInstructionCount(c1.addMethod("m1"), 17);
        setInstructionCount(c1.addMethod("m2"), 17);

        Node n1 = ret.addNode("n1");
        AttributeUtil au = new AttributeUtil();

        au.addClass(n1, c1);

        return ret;
    }

    public Graph getGraph2() {
        Graph ret = new MultiGraph("graph2");

        dmClass c1 = new dmClass("c1");
        setInstructionCount(c1.addMethod("m1"), 17);

        dmClass c2 = new dmClass("c2");
        setInstructionCount(c2.addMethod("m1"), 17);

        Node n1 = ret.addNode("n1");
        AttributeUtil au = new AttributeUtil();

        au.addClass(n1, c1);
        au.addClass(n1, c2);

        return ret;
    }


    public Graph generateGraph(String [] a_edgesAsNodePairs) {
        Graph ret = new MultiGraph("test");
        String[] edgeIds = a_edgesAsNodePairs;
        HashMap<String, dmClass> classes = new HashMap<>();
        HashMap<String, Node> nodes = new HashMap<>();
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
            cFirst.addDependency(cSecond, dmDependency.Type.MethodCall, 0);

            if (nodes.get(first) == null) {
                Node n = ret.addNode(first);
                nodes.put(first, n);
                au.addClass(n, cFirst);
            }
            if (nodes.get(second) == null) {
                Node n = ret.addNode(second);
                nodes.put(second, n);
                au.addClass(n, cSecond);
            }
        }

        return ret;
    }
}
