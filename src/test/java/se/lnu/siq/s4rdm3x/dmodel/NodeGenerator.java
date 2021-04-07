package se.lnu.siq.s4rdm3x.dmodel;

import org.objectweb.asm.ClassReader;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class NodeGenerator {

    static final String g_classesPkg = "se.lnu.siq.s4rdm3x.dmodel.classes.";
    static final String g_classesDir = "se/lnu/siq/s4rdm3x/dmodel/classes/";

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

    public static dmClass createClass(final String a_name, dmFile.dmDirectory a_root) {
        return new dmClass(a_name, a_root.createFile(dmClass.toJavaSourceFile(a_name)));
    }

    public static dmClass createClass(final String a_name) {
        dmFile.dmDirectory root = new dmFile.dmDirectory("root", null);
        dmClass ret = new dmClass(a_name, root.createFile(dmClass.toJavaSourceFile(a_name)));
        return ret;
    }


    public CGraph getGraph1() {
        CGraph ret = new CGraph();

        dmClass c1 = createClass("c1");
        setInstructionCount(c1.addMethod("m1", false, false), 17);
        setInstructionCount(c1.addMethod("m2", false, false), 17);

        setBranchStatementCount(c1.addMethod("m3", false, false), 8);
        setBranchStatementCount(c1.addMethod("m4", false, false), 5);

        CNode n1 = ret.createNode("n1");
        n1.addClass(c1);

        return ret;
    }

    public CGraph getGraph2() {
        CGraph ret = new CGraph();

        dmClass c1 = createClass("c1");
        dmClass.Method m1 = c1.addMethod("m1", false, false);
        setInstructionCount(m1, 17);
        setBranchStatementCount(m1, 9);

        dmClass c2 = createClass("c2");
        m1 = c2.addMethod("m1", false, false);
        setInstructionCount(m1, 17);
        setBranchStatementCount(m1, 6);

        CNode n1 = ret.createNode("n1");

        n1.addClass(c1);
        n1.addClass(c2);

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

    public CNode loadNode(String a_javaClassName) {
        CGraph g = loadGraph("/" + g_classesDir + a_javaClassName + ".class");

        // We need to find the correct node
        dmClass c = createClass(g_classesDir + a_javaClassName);
        String searchPattern =  c.getFileName();
        CNode a = g.searchNode(searchPattern).get(0);

        return a;
    }

    public CGraph loadGraph(String a_javaClassName) {
        try {
            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(a_javaClassName);

            CGraph g = new CGraph();

            for (dmClass c : pb.getProject().getClasses()) {
                String cName = c.getFileName();
                CNode n = g.createNode(cName);
                n.addClass(c);
            }

            return g;

        } catch (IOException e) {
            return null;
        }
    }

    public CGraph generateGraph(String [] a_edgesAsNodePairs) {
        return addToGraph(new CGraph(), dmDependency.Type.MethodCall, a_edgesAsNodePairs);
    }

    public CGraph generateGraph(dmDependency.Type a_dependency, String [] a_edgesAsNodePairs) {
        return addToGraph(new CGraph(), a_dependency, a_edgesAsNodePairs);
    }


    public CGraph addToGraph(CGraph a_g, dmDependency.Type a_dependency, String [] a_edgesAsNodePairs) {
        CGraph ret = a_g;
        String[] edgeIds = a_edgesAsNodePairs;
        HashMap<String, dmClass> classes = new HashMap<>();

        dmFile.dmDirectory root = null;
        if (a_g.getNodeCount() > 0) {
            for (CNode n : a_g.getNodes()) {
                for (dmClass c : n.getClasses()) {
                    root = c.getFile().getRoot();
                }
            }
        }
        if (root == null) {
            root = new dmFile.dmDirectory("root", null);
        }
        for (String id : edgeIds) {
            String first = id.substring(0, 1), second = id.substring(1, 2);
            if (!classes.containsKey(first)) {
                classes.put(first, createClass(first, root));
            }
            if (!classes.containsKey(second)) {
                classes.put(second, createClass(second, root));
            }

            dmClass cFirst = classes.get(first);
            dmClass cSecond = classes.get(second);
            cFirst.addDependency(cSecond, a_dependency, 0);

            if (a_g.getNode(first) == null) {
                ret.createNode(first);
            }
            CNode n = a_g.getNode(first);
            if (!n.containsClass(cFirst)) {
                n.addClass(cFirst);
            }

            if (a_g.getNode(second) == null) {
                ret.createNode(second);
            }
            n = a_g.getNode(second);
            if (!n.containsClass(cSecond)) {
                n.addClass(cSecond);
            }

        }

        return ret;
    }
}
