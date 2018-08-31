package se.lnu.siq.s4rdm3x.cmd.util;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;

public class LoadArch {
    String m_file;
    public HuGMe.ArchDef m_arch;

    public LoadArch(String a_file) {
        m_file = a_file;
    }

    public void run(Graph a_g) {
        JITTACCModelReader jmr = new JITTACCModelReader();
        if (jmr.readFile(m_file)) {
            m_arch = new HuGMe.ArchDef();

            for (JITTACCModelReader.Module module : jmr.m_modules) {
                m_arch.addComponent(module.m_name);
            }

            for (JITTACCModelReader.Relation relation : jmr.m_relations) {
                HuGMe.ArchDef.Component from, to;
                from = m_arch.getComponent(relation.m_moduleNameFrom);
                to = m_arch.getComponent(relation.m_moduleNameTo);
                from.addDependencyTo(to);
            }

            for (JITTACCModelReader.Mapping mapping : jmr.m_mappings) {
                HuGMe.ArchDef.Component c = m_arch.getComponent(mapping.m_moduleName);
                Selector.Pat p = new Selector.Pat(mapping.m_regexp);

                for(Node n : a_g.getEachNode()) {
                    if (p.isSelected(n)) {
                        HuGMe.ArchDef.Component oldMapping = m_arch.getMappedComponent(n);
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
        }
    }
}
