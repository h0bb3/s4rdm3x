package se.lnu.siq.s4rdm3x.cmd.util;

import se.lnu.siq.s4rdm3x.cmd.hugme.HuGMe;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.Selector;

public class ArchCreator {

    public void mapArch(HuGMe.ArchDef  a_arch, SystemModelReader a_model, CGraph a_g) {
        for (SystemModelReader.Mapping mapping : a_model.m_mappings) {
            HuGMe.ArchDef.Component c = a_arch.getComponent(mapping.m_moduleName);
            Selector.Pat p = new Selector.Pat(mapping.m_regexp);

            for (CNode n : a_g.getNodes()) {
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
            if (from == null) {
                throw new NullPointerException("Could not find component: " + relation.m_moduleNameFrom + " on line: " + relation.m_line);
            }
            if (to == null) {
                throw new NullPointerException("Could not find component: " + relation.m_moduleNameTo + " on line: " + relation.m_line);
            }
            from.addDependencyTo(to);
        }

        return arch;
    }

    public SystemModelReader createSystemModel(HuGMe.ArchDef a_arch, Iterable<CNode> a_nodesToMap) {
        SystemModelReader ret = new SystemModelReader();

        ret.m_name = "created system";

        for (HuGMe.ArchDef.Component c : a_arch.getComponents()) {

            SystemModelReader.Module module = new SystemModelReader.Module();
            module.m_name = c.getName();
            ret.m_modules.add(module);



            for (HuGMe.ArchDef.Component to : a_arch.getComponents()) {
                if (c.allowedDependency(to)) {
                    SystemModelReader.Relation relation = new SystemModelReader.Relation();
                    relation.m_moduleNameFrom = c.getName();
                    relation.m_moduleNameTo = to.getName();

                    ret.m_relations.add(relation);
                }
            }


            for (CNode n : a_nodesToMap) {
                if (c.isMappedTo(n)) {
                    SystemModelReader.Mapping m = new SystemModelReader.Mapping();

                    m.m_moduleName = c.getName();
                    m.m_regexp = n.getName();

                    ret.m_mappings.add(m);
                }
            }
        }

        return ret;

    }
}
