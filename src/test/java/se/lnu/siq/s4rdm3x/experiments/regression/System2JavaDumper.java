package se.lnu.siq.s4rdm3x.experiments.regression;

import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.experiments.HuGMeExperimentRun;
import se.lnu.siq.s4rdm3x.experiments.regression.dumps.DumpBase;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class System2JavaDumper {

    private PrintStream m_out;
    private int m_tc;

    HashMap<dmClass, String> m_dmClassNames = new HashMap<>();
    HashMap<CNode, String> m_cNodeNames = new HashMap<>();

    DumpBase m_shadow;


    void dump(PrintStream a_out, String a_className, CGraph a_g, ArchDef a_a, DumpBase.HuGMeParams[] a_hugmeTests, DumpBase.NBParams[] a_nbTests, DumpBase.IRParams [] a_irTests, DumpBase.IRParams [] a_lsiTests) {

        m_out = a_out;
        m_tc = 0;

        ps("package se.lnu.siq.s4rdm3x.experiments.regression.dumps");
        ps("import se.lnu.siq.s4rdm3x.model.CGraph");
        ps("import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef");
        ps("import se.lnu.siq.s4rdm3x.model.CNode");
        ps("import se.lnu.siq.s4rdm3x.dmodel.dmClass");
        ps("import se.lnu.siq.s4rdm3x.dmodel.dmDependency");
        ps("import java.util.HashMap");

        createClassBlock(a_className, a_g, a_a, a_hugmeTests, a_nbTests, a_irTests, a_lsiTests);
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
        pms(a_methodName, false, "void");
    }
    private void pms(String a_methodName, boolean a_isPrivate, String a_returnType) {
        String access = a_isPrivate ? "private" : "public";
        pbs(access + " " + a_returnType + " " + a_methodName);
    }

    private void createClassBlock(String a_className, CGraph a_g, ArchDef a_a, DumpBase.HuGMeParams [] a_hugmeTests, DumpBase.NBParams[] a_nbTests, DumpBase.IRParams [] a_irTests, DumpBase.IRParams [] a_lsiTests) {
        pbs("public class " + a_className + " extends DumpBase");
        m_shadow = new DumpBase();

        createConstructorBlock(a_className);

        create_getHuGMeParams(a_hugmeTests);
        create_getNBParams(a_nbTests);
        create_getIRParams(a_irTests);
        create_getLSIParams(a_lsiTests);

        createNodesMethod(a_g.getNodes());

        createArchMethod(a_a);

        pbe();
    }

    private void create_getXParams(String a_xType, String a_xMethod, DumpBase.Params [] a_params) {
        pms(a_xMethod + "(int a_index)", false, a_xType);
        ps(a_xType + " r = null");
        pbs("switch (a_index)");

        for (int sIx = 0; sIx < a_params.length; sIx++) {
            DumpBase.Params p = a_params[sIx];
            pbs("case " + sIx + ":");
            ps("r = new "+ a_xType +"()");
            createParamFields(p);
            ps("break");
            pbe();
        }

        pbe();

        ps("return r");
        pbe();
    }

    private void create_getNBParams(DumpBase.NBParams [] a_scores) {
        create_getXParams("NBParams", "getNBParams", a_scores);
    }

    private void create_getHuGMeParams(DumpBase.HuGMeParams [] a_scores) {
        create_getXParams("HuGMeParams", "getHuGMeParams", a_scores);
    }

    private void create_getIRParams(DumpBase.IRParams [] a_scores) {
        create_getXParams("IRParams", "getIRParams", a_scores);
    }

    private void create_getLSIParams(DumpBase.IRParams [] a_scores) {
        create_getXParams("IRParams", "getLSIParams", a_scores);
    }

    private void createParamFields(DumpBase.Params a_params) {
        for (Field f : a_params.getClass().getFields()) {
            try {
                if (f.getType().equals(double.class)) {
                    ps("r." + f.getName() + " = " + f.getDouble(a_params));
                } else if (f.getType().equals(boolean.class)) {
                    ps("r." + f.getName() + " = " + f.getBoolean(a_params));
                } else if (f.getType().equals(double[].class)) {
                    double[] a = (double[]) f.get(a_params);
                    for (int i = 0; i < a.length; i++) {
                        ps("r." + f.getName() + "[" + i + "] = " + a[i]);
                    }
                } else if (f.getType().equals(int.class)) {
                    ps("r." + f.getName() + " = " + f.getInt(a_params));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
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


    public String escape(String s){
        return s.replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\f", "\\f")
                .replace("\'", "\\'")
                .replace("\"", "\\\"");
    }



    public String deEscape(String s) {

        // from: https://stackoverflow.com/questions/3537706/how-to-unescape-a-java-string-literal-in-java
        class Decoder {

            // The encoded character of each character escape.
            // This array functions as the keys of a sorted map, from encoded characters to decoded characters.
            final char[] ENCODED_ESCAPES = { '\"', '\'', '\\',  'b',  'f',  'n',  'r',  't' };

            // The decoded character of each character escape.
            // This array functions as the values of a sorted map, from encoded characters to decoded characters.
            final char[] DECODED_ESCAPES = { '\"', '\'', '\\', '\b', '\f', '\n', '\r', '\t' };

            // A pattern that matches an escape.
            // What follows the escape indicator is captured by group 1=character 2=octal 3=Unicode.
            final Pattern PATTERN = Pattern.compile("\\\\(?:(b|t|n|f|r|\\\"|\\\'|\\\\)|((?:[0-3]?[0-7])?[0-7])|u+(\\p{XDigit}{4}))");

            public CharSequence decodeString(CharSequence encodedString) {
                Matcher matcher = PATTERN.matcher(encodedString);
                StringBuffer decodedString = new StringBuffer();
                // Find each escape of the encoded string in succession.
                while (matcher.find()) {
                    char ch;
                    if (matcher.start(1) >= 0) {
                        // Decode a character escape.
                        ch = DECODED_ESCAPES[Arrays.binarySearch(ENCODED_ESCAPES, matcher.group(1).charAt(0))];
                    } else if (matcher.start(2) >= 0) {
                        // Decode an octal escape.
                        ch = (char)(Integer.parseInt(matcher.group(2), 8));
                    } else /* if (matcher.start(3) >= 0) */ {
                        // Decode a Unicode escape.
                        ch = (char)(Integer.parseInt(matcher.group(3), 16));
                    }
                    // Replace the escape with the decoded character.
                    matcher.appendReplacement(decodedString, Matcher.quoteReplacement(String.valueOf(ch)));
                }
                // Append the remainder of the encoded string to the decoded string.
                // The remainder is the longest suffix of the encoded string such that the suffix contains no escapes.
                matcher.appendTail(decodedString);
                return decodedString;
            }
        }

        Decoder d = new Decoder();
        return d.decodeString(s).toString();

        /*return s.replace("\\t", "\t")
                //.replace("\\b", "\b")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\f", "\f")
                .replace("\\'", "\'")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\"); // \\ -> \*/
        //return s.replace("\\\\", "\\");
    }

    private void createClassTextFunctions(CNode a_n) {
        CNode n = m_shadow.m_g.getNodeByName(a_n.getName());
        if (hasText(a_n)) {
            for (dmClass c : a_n.getClasses()) {
                ps("");
                pms(className(c) + "_texts" + "(dmClass a_c)");

                dmClass shadowClass = n.getClassByName(c.getName());

                for (String t : c.getTexts()) {
                    // TODO: there could be other things than needs to be escaped
                    String text = escape(t);//.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\\\"");
                    ps("a_c.addText(\"" + text + "\")");
                    shadowClass.addText(deEscape(text));
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

            CNode shadowNode = m_shadow.m_g.getNode(a_n.getName());

            for (dmClass c : a_n.getClasses()) {
                dmClass shadowClass = shadowNode.getClassByName(c.getName());
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

                    int [] shadowLines = new int[lineCount];
                    int ix = 0;
                    for (Integer i : d.lines()) {
                        shadowLines[ix] = i;
                    }

                    dmClass shadowTarget = getShadowClass(d.getTarget().getName());
                    if (shadowLines.length == 1) {
                        m_shadow.d(shadowClass, shadowTarget, d.getType(), shadowLines[0]);
                    } else {
                        m_shadow.d(shadowClass, shadowTarget, d.getType(), shadowLines);
                    }
                }

                pbe();
            }
        }
    }

    private dmClass getShadowClass(String a_className) {
        for (CNode n : m_shadow.m_g.getNodes()) {
            if (n.getClassByName(a_className) != null) {
                return n.getClassByName(a_className);
            }
        }
        return null;
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
        CNode n = m_shadow.m_g.createNode(a_n.getName());
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
            n.addClass(new dmClass(c.getName()));

            ps("m_classes.put(\"" + c.getName() + "\", " + className(c) + ")");
            if (hasText(a_n)) {
                ps(className(c) + "_texts(" + className(c) + ")");                              // call the node text functions
            }

            // methods are not added this may limit the usefullness of the regressions i.e. checking some metrics.
            // problem is that there is currently no way of adding a method without also adding the method name as a text
            // in addition the dependencies for the methods will require some special treatment.
            // possbily this could be fixed by adding the methods stuff last and removing the text from the node first, and having a lookup of dependencies.
            /*for (dmClass.Method m : c.getMethods()) {
                c.addDependency();

            }*/
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

        ps("createNodes()");
        ps("createArch()");

        pbe();
    }

}
