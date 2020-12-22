package se.lnu.siq.s4rdm3x.g3n3z;

import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

public class System2JavaDumper {

    private PrintStream m_out;
    private int m_tc;

    HashMap<dmClass, String> m_dmClassNames = new HashMap<>();
    HashMap<CNode, String> m_cNodeNames = new HashMap<>();

    void dump(PrintStream a_out, String a_className, CGraph a_g, ArchDef a_a) {

        m_out = a_out;
        m_tc = 0;

        ps("package se.lnu.siq.s4rdm3x.g3n3z");
        ps("import se.lnu.siq.s4rdm3x.model.CGraph");
        ps("import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef");
        ps("import se.lnu.siq.s4rdm3x.model.CNode");
        ps("import se.lnu.siq.s4rdm3x.dmodel.dmClass");
        ps("import se.lnu.siq.s4rdm3x.dmodel.dmDependency");
        ps("import java.util.HashMap");

        createClassBlock(a_className, a_g, a_a);
    }

    private void pln(String a_str) {
        String tabs = "";
        if (a_str.length() > 0) {
            for (int i = 0; i < m_tc; i++) {
                tabs += "\t";
            }
        }
        m_out.println(tabs + a_str);
    }

    private void ps(String a_statement) {
        if (a_statement.length() > 0) {
            pln(a_statement + ";");
        } else {
            pln("");
        }
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

    private void createClassBlock(String a_className, CGraph a_g, ArchDef a_a) {
        pbs("class " + a_className);

        ps("public CGraph m_g");
        ps("public ArchDef m_a");
        ps("public HashMap<String, dmClass> m_classes");

        createConstructorBlock(a_className);

        createNodesMethod(a_g.getNodes());

        createArchMethod(a_a);

        pbe();
    }

    private void createArchMethod(ArchDef a_a) {
        pms("createArch()");
        ps("m_a = new ArchDef()");
        for (ArchDef.Component c : a_a.getComponents()) {
            ps("ArchDef.Component c" + a_a.getComponentIx(c) + " = m_a.addComponent(\"" + c.getName() + "\")");
        }

        for (ArchDef.Component c : a_a.getComponents()) {
            for (ArchDef.Component to : getAllowedDependenciesFrom(c, a_a.getComponents())) {
                ps("c" + a_a.getComponentIx(c) + ".addDependencyTo(c" + a_a.getComponentIx(to)+")");
            }
        }
        pbe();
    }

    private Iterable<ArchDef.Component> getAllowedDependenciesFrom(ArchDef.Component a_from, Iterable<ArchDef.Component> a_components) {
        ArrayList<ArchDef.Component> ret = new ArrayList<>();
        for (ArchDef.Component to : a_components) {
            if (a_from != to && a_from.allowedDependency(to)) {
                ret.add(to);
            }
        }

        return ret;
    }

    private String nodeFunctionName(CNode a_n) {
        if (!m_cNodeNames.containsKey(a_n)) {
            m_cNodeNames.put(a_n, "n" + m_cNodeNames.values().size());
        }

        return m_cNodeNames.get(a_n);
    }

    private void createNodesMethod(Iterable<CNode> a_nodes) {
        final int maxNodeCount = 100;
        final int maxClassCount = 100;
        /*public void d(dmClass a_source, dmClass a_target, dmDependency.Type a_dt, int[] a_lines) {
            for (int i : a_lines) {
                a_source.addDependency(a_target, a_dt, i);
            }
        }*/

        pms("d(dmClass a_source, dmClass a_target, dmDependency.Type a_dt, int[] a_lines)");
        pbs("for (int i : a_lines)");
        ps("a_source.addDependency(a_target, a_dt, i)");
        pbe();
        pbe();

        pms("d(dmClass a_source, dmClass a_target, dmDependency.Type a_dt, int a_line)");
        ps("a_source.addDependency(a_target, a_dt, a_line)");
        pbe();

        pms("createNodes()");

        // create all nodes and classes
        int count = 0;
        int mCount = 0;
        for(CNode n : a_nodes) {
            createNode(n);
            count++;
            if (count >= maxNodeCount) {
                ps("nextNodes_" + mCount +"()");
                pbe();
                pms("nextNodes_" + mCount +"()");
                mCount++;
                count = 0;
            }
        }

        ps("dmDependency.Type [] dt = dmDependency.Type.values()");
        // create all dependencies

        count = 0;
        mCount = 0;
        for(CNode n : a_nodes) {
            createClassDependencies(n);
            count++;
            if (count >= maxClassCount) {
                ps("nextDeps_" + mCount +"()");
                pbe();
                pms("nextDeps_" + mCount +"()");
                mCount++;
                count = 0;
            }
        }

        pbe();

        for(CNode n : a_nodes) {
            createClassTextFunctions(n);
            createClassDependencyFunctions(n);
        }
    }



    private void createClassTextFunctions(CNode a_n) {
        if (hasText(a_n)) {
            for (dmClass c : a_n.getClasses()) {
                ps("");
                pms(className(c) + "_texts" + "(dmClass a_c)");

                for (String t : c.getTexts()) {
                    // TODO: there could be other things than needs to be escaped
                    ps("a_c.addText(\"" + t.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\\\"") + "\")");
                }

                pbe();
            }
        }
    }

    private boolean hasText(CNode a_n) {
        for (dmClass c : a_n.getClasses()) {
            for (String t : c.getTexts()) {
                return true;
            }
        }
        return false;
    }

    private void createClassDependencyFunctions(CNode a_n) {
        if (hasDependency(a_n)) {
            for (dmClass c : a_n.getClasses()) {
                pms(className(c) + "_deps()");

                for (dmDependency d : c.getDependencies()) {
                    String source = "m_classes.get(\"" + c.getName() + "\")";;
                    String target = "m_classes.get(\"" + d.getTarget().getName() + "\")";
                    String type = "dmDependency.Type." + d.getType();
                    String lines  = "new int[]{";
                    int lineCount = 0;
                    int lastLine = 0;
                    for (Integer line : d.lines()) {
                        lines += line + ", ";
                        lineCount++;
                        lastLine = line;
                    }
                    if (lineCount > 1) {
                        lines = lines.substring(0, lines.length() - 2);
                        lines += "}";
                    } else {
                        lines = "" + lastLine;
                    }

                    ps("d(" + source + ", " + target + ", " + type + ", " + lines +")");
                }

                pbe();
            }
        }
    }

    private void createClassDependencies(CNode a_n) {
        if (hasDependency(a_n)) {
            for (dmClass c : a_n.getClasses()) {
                ps(className(c) + "_deps()");
            }
        }
    }

    private boolean hasDependency(CNode a_n) {
        for (dmClass c : a_n.getClasses()) {
            for (dmDependency d : c.getDependencies()) {
                return true;
            }
        }
        return false;
    }

    private void createNode(CNode a_n) {
        ps("");
        ps("CNode " + nodeFunctionName(a_n) + " = m_g.createNode(\"" + a_n.getName() + "\")");
        a_n.setMapping(a_n.getMapping());
        if (a_n.getMapping().length() > 0) {
            ps(nodeFunctionName(a_n) + ".setMapping(\"" + a_n.getMapping() + "\")");
        }
        if (a_n.getClusteringComponentName().length() > 0) {
            ps(nodeFunctionName(a_n) + ".setClustering(\"" + a_n.getClusteringComponentName() + "\", \"" + a_n.getClusteringType() + "\")");
        }

        for (dmClass c : a_n.getClasses()) {
            ps("dmClass " + className(c) + " = new dmClass(\"" + c.getName() + "\")");    // create class
            ps(nodeFunctionName(a_n) + ".addClass(" + className(c) + ")");           // add class to node

            ps("m_classes.put(\"" + c.getName() + "\", " + className(c) + ")");
            if (hasText(a_n)) {
                ps(className(c) + "_texts(" + className(c) + ")");                              // call the node text functions
            }
        }
    }

    private String shortName(String a_name) {
        String [] parts = a_name.split("_");
        String ret = "";

        // keep 3 characters for everything except last part
        for (int i = 0; i < parts.length; i++) {
            if (i < parts.length -1) {
                if (parts[i].length() > 3) {
                    parts[i] = parts[i].substring(0, 3);
                }
            }
            ret += "_" + parts[i];
        }

        return ret;
    }

    private String className(dmClass a_c) {
        if (!m_dmClassNames.containsKey(a_c)) {
            m_dmClassNames.put(a_c, "c" + m_dmClassNames.values().size());
        }

        return m_dmClassNames.get(a_c);
    }

    private void createConstructorBlock(String a_className) {
        pbs("public " + a_className +"()");

        ps("m_g = new CGraph()");
        ps("m_a = new ArchDef()");
        ps("m_classes = new HashMap<>()");

        ps("createNodes()");
        ps("createArch()");


        pbe();
    }

}
