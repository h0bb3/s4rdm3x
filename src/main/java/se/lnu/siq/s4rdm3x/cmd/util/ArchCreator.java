package se.lnu.siq.s4rdm3x.cmd.util;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;

public class ArchCreator {

    public void mapArch(HuGMe.ArchDef  a_arch, SystemModelReader a_model, Graph a_g) {
        for (SystemModelReader.Mapping mapping : a_model.m_mappings) {
            HuGMe.ArchDef.Component c = a_arch.getComponent(mapping.m_moduleName);
            Selector.Pat p = new Selector.Pat(mapping.m_regexp);

            for (Node n : a_g.getEachNode()) {
                if (p.isSelected(n)) {
                    HuGMe.ArchDef.Component oldMapping = a_arch.getMappedComponent(n);
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

    public HuGMe.ArchDef createArch(SystemModelReader a_model) {
        HuGMe.ArchDef arch = new HuGMe.ArchDef();

        for (SystemModelReader.Module module : a_model.m_modules) {
            arch.addComponent(module.m_name);
        }

        for (SystemModelReader.Relation relation : a_model.m_relations) {
            HuGMe.ArchDef.Component from, to;
            from = arch.getComponent(relation.m_moduleNameFrom);
            to = arch.getComponent(relation.m_moduleNameTo);
            from.addDependencyTo(to);
        }



        return arch;
    }
}
