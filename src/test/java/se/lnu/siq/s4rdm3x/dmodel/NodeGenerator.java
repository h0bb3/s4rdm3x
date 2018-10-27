package se.lnu.siq.s4rdm3x.dmodel;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.objectweb.asm.ClassReader;
import org.w3c.dom.Attr;
import se.lnu.siq.s4rdm3x.cmd.util.AttributeUtil;
import se.lnu.siq.s4rdm3x.cmd.util.NodeUtil;

import java.io.IOException;
import java.io.InputStream;
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

    private ASMdmProjectBuilder getAsMdmProjectBuilder(String name) throws IOException {
        InputStream in = ASMdmProjectBuilder.class.getResourceAsStream(name);
        ASMdmProjectBuilder pb = new ASMdmProjectBuilder();
        pb.getProject().doTrackConstantDeps(true);
        ClassReader classReader = new ClassReader(in);
        classReader.accept(pb, 0);
        return pb;
    }

    public Graph loadGraph(String a_javaClassName) {
        try {
            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(a_javaClassName);

            Graph g = new MultiGraph("Test");
            NodeUtil nu = new NodeUtil(g);
            AttributeUtil au = new AttributeUtil();

            for (dmClass c : pb.getProject().getClasses()) {
                String cName = c.getFileName();
                Node n = nu.createNode(cName);
                au.addClass(n, c);
            }

            return g;

        } catch (IOException e) {
            return null;
        }
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
                ret.addNode(first);
            }
            Node n = a_g.getNode(first);
            if (!au.hasClass(n, cFirst)) {
                au.addClass(n, cFirst);
            }

            if (a_g.getNode(second) == null) {
                ret.addNode(second);
            }
            n = a_g.getNode(second);
            if (!au.hasClass(n, cSecond)) {
                au.addClass(n, cSecond);
            }

        }

        return ret;
    }
}
