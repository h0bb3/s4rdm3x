package se.lnu.siq.s4rdm3x.g3n3z;

import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class System2JavaDumper {

    private PrintStream m_out;
    private int m_tc;

    void dump(PrintStream a_out, String a_className, CGraph a_g) {

        m_out = a_out;
        m_tc = 0;

        ps("package se.lnu.siq.s4rdm3x.g3n3z");
        ps("import se.lnu.siq.s4rdm3x.model.CGraph");
        ps("import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef");
        ps("import se.lnu.siq.s4rdm3x.model.CNode");
        ps("import se.lnu.siq.s4rdm3x.dmodel.dmClass");
        ps("import se.lnu.siq.s4rdm3x.dmodel.dmDependency");

        createClassBlock(a_className, a_g);
    }

    private void pln(String a_str) {
        String tabs = "";
        for(int i = 0; i < m_tc; i++) {tabs += "\t";}
        m_out.println(tabs + a_str);
    }

    private void ps(String a_statement) {
        pln(a_statement + ";");
    }

    //print block start
    private void pbs(String a_block) {
        pln(a_block + " {");
        m_tc++;
    }

    // print block end
    private void pbe() {
        m_tc--;
        pln("}");
    }

    // print method start
    private void pms(String a_methodName) {
        pbs("private void " + a_methodName);
    }

    private void createClassBlock(String a_className, CGraph a_g) {
        pbs("class " + a_className);

        ps("public CGraph m_g");
        ps("public ArchDef m_a");

        createConstructorBlock(a_className);

        createNodesMethod(a_g.getNodes());

        pbe();
    }

    private String nodeFunctionName(CNode a_n) {
        // TODO: last replace should not be needed as this should be fixed when loading
        // _java/awt/geom/RoundRectangle2D_java()
        return "node_" + a_n.getLogicName().replace(".", "_").replace("/", "_");
    }

    private void createNodesMethod(Iterable<CNode> a_nodes) {
        pms("createNodes");
        int max = 10;

        // create all nodes and classes
        for(CNode n : a_nodes) {
            createNode(n);
            max--;
            if (max < 0) {
                break;
            }
        }
        pbe();

        // create all dependencies
    }

    private void createNode(CNode a_n) {
        ps("CNode " + nodeFunctionName(a_n) + " = m_g.createNode(\"" + a_n.getName() + "\")");

        for (dmClass c : a_n.getClasses()) {

            ps("\tdmClass " + className(c) + " = new dmClass(\"" + c.getName() + "\")");
            ps("\t" + nodeFunctionName(a_n) + ".addClass(" + className(c) + ")");
            for(String t : c.getTexts()) {
                // TODO: there could be other things than needs to be escaped
                ps("\t" + className(c) + ".addText(\"" + t.replace("\n", "\\n").replace("\"", "\\\")") +"\")");
            }

        }
    }

    private String className(dmClass c) {
        return c.getName().replace(".", "_").replace("$", "-i-");
    }

    private void createConstructorBlock(String a_className) {
        pbs("public " + a_className +"()");

        ps("m_g = new CGraph()");
        ps("m_a = new ArchDef()");

        ps("createNodes()");
        //ps("createArch()");


        pbe();
    }

}
