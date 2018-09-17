package se.lnu.siq.s4rdm3x.cmd.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class SystemModelReader {
    public static class Module {
        public String m_name;
    }
    public static class Mapping {
        public String m_moduleName;
        public String m_regexp;
    }
    public static class Relation {
        public String m_moduleNameFrom;
        public String m_moduleNameTo;
    }

    public ArrayList<Module> m_modules = new ArrayList<>();
    public ArrayList<Mapping> m_mappings = new ArrayList<>();
    public ArrayList<Relation> m_relations = new ArrayList<>();
    public String m_name = "undefined name";
    public ArrayList<String> m_jars = new ArrayList<>();
    public ArrayList<String> m_roots = new ArrayList<>();
    public String m_metrics = "undefined metrics file";

    private enum Context {
        Module,
        Mapping,
        Relation,
        Jar,
        Roots,
        Name,
        Metrics,
        None
    }

    private void handleLine(String a_line, Context a_context) {
        switch (a_context) {
            case Module: {
                Module m = new Module();
                m.m_name = a_line;
                m_modules.add(m);
            } break;
            case Mapping: {
                String [] parts = a_line.split(" ");
                Mapping m = new Mapping();
                m.m_moduleName = parts[0];
                m.m_regexp = parts[1];
                m_mappings.add(m);
            } break;
            case Relation: {
                String [] parts = a_line.split(" ");
                Relation r = new Relation();
                r.m_moduleNameFrom = parts[0];
                r.m_moduleNameTo = parts[1];
                m_relations.add(r);
            } break;
            case Name: {
                m_name = a_line;
            } break;
            case Jar: {
                m_jars.add(a_line);
            } break;
            case Roots: {
                m_roots.add(a_line);
            } break;
            case Metrics: {
                m_metrics = a_line;
            } break;
        }
    }

    public boolean readFile(String a_file) {
        Context context = Context.None;

        try (BufferedReader br = new BufferedReader(new FileReader(a_file))) {
            String line;
            while( (line = br.readLine()) != null) {
                if (line.startsWith("# modules")) {
                    context = Context.Module;
                } else if (line.startsWith("# mapping")) {
                    context = Context.Mapping;
                } else if (line.startsWith("# relations")) {
                    context = Context.Relation;
                } else if (line.startsWith("# jar")) {
                    context = Context.Jar;
                } else if (line.startsWith("# name")) {
                    context = Context.Name;
                } else if (line.startsWith("# root-packages")) {
                    context = Context.Roots;
                }  else if (line.startsWith("# metrics file")) {
                    context = Context.Metrics;
                } else if (line.startsWith("#")) {

                } else if (line.length() > 0){
                    handleLine(line, context);
                }
            }
            return true;
        } catch (IOException a_ioe) {
            return false;
        }
    }
}
