package se.lnu.siq.s4rdm3x.experiments.system;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class JITTACCModelReader {
    class Module {
        public String m_name;
    }
    class Mapping {
        public String m_moduleName;
        public String m_regexp;
    }
    class Relation {
        public String m_moduleNameFrom;
        public String m_moduleNameTo;
    }

    public ArrayList<Module> m_modules = new ArrayList<>();
    public ArrayList<Mapping> m_mappings = new ArrayList<>();
    public ArrayList<Relation> m_relations = new ArrayList<>();

    private enum Context {
        Module,
        Mapping,
        Relation,
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
