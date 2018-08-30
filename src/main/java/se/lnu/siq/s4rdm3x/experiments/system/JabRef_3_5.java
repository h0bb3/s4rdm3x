package se.lnu.siq.s4rdm3x.experiments.system;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;
import se.lnu.siq.s4rdm3x.cmd.LoadJar;
import se.lnu.siq.s4rdm3x.cmd.Selector;

import java.io.IOException;



public class JabRef_3_5 extends System {

    @Override
    public String getName() {
        return "JabRef_3_5";
    }

    public HuGMe.ArchDef createAndMapArch(Graph a_g)  {


        JITTACCModelReader jmr = new JITTACCModelReader();
        if (jmr.readFile("data/jabref-3_5-archmodel.txt")) {
            HuGMe.ArchDef arch = new HuGMe.ArchDef();

            for (JITTACCModelReader.Module module : jmr.m_modules) {
                arch.addComponent(module.m_name);
            }

            for (JITTACCModelReader.Relation relation : jmr.m_relations) {
                HuGMe.ArchDef.Component from, to;
                from = arch.getComponent(relation.m_moduleNameFrom);
                to = arch.getComponent(relation.m_moduleNameTo);
                from.addDependencyTo(to);
            }

            for (JITTACCModelReader.Mapping mapping : jmr.m_mappings) {
                HuGMe.ArchDef.Component c = arch.getComponent(mapping.m_moduleName);
                Selector.Pat p = new Selector.Pat(mapping.m_regexp);

                for(Node n : a_g.getEachNode()) {
                    if (p.isSelected(n)) {
                        HuGMe.ArchDef.Component oldMapping = arch.getMappedComponent(n);
                        if (oldMapping != null) {
                            // we already have a mapping
                            if (!mapping.m_regexp.contains(".*")) {
                                oldMapping.unmap(n);
                                c.mapToNode(n);
                            }
                        } else {
                            c.mapToNode(n);
                        }
                    }
                }
            }

            return arch;
        }


        return null;
    }

    public boolean load(Graph a_g) {
        LoadJar c = new LoadJar("data/JabRef-3.5.jar", "net/sf/jabref/");
        try {
            c.run(a_g);
        } catch (IOException e) {
            java.lang.System.out.println(e);
            return false;
        }
        return true;
    }

}