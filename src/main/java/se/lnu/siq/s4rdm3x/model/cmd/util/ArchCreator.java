package se.lnu.siq.s4rdm3x.model.cmd.util;

import se.lnu.siq.s4rdm3x.experiments.system.System;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.Selector;

public class ArchCreator {

    public void mapArch(ArchDef a_arch, SystemModelReader a_model, CGraph a_g) throws System.NoMappedNodesException{
        for (SystemModelReader.Mapping mapping : a_model.m_mappings) {
            ArchDef.Component c = a_arch.getComponent(mapping.m_moduleName);
            if (c != null) {
                Selector.Pat p = new Selector.Pat(mapping.m_regexp);

                for (CNode n : a_g.getNodes()) {
                    if (p.isSelected(n)) {
                        ArchDef.Component oldMapping = a_arch.getMappedComponent(n);
                        if (oldMapping != null) {
                            if (c != oldMapping) {
                                java.lang.System.err.println("Warning: Old mapping exists for node: was " + n.getName() + " -> " + oldMapping.getName() + " now mapped to: " + c.getName() + " (Make sure this is what you want...)");
                            }
                        }
                        c.mapToNode(n);
                    }
                }
            } else {
                java.lang.System.err.println("Error: Could not find component with name: " + mapping.m_moduleName + " for system: " + a_model.m_name);
            }
        }

        // check that all components actually have some nodes mapped
        System.NoMappedNodesException exception = new System.NoMappedNodesException(a_arch);
        for (ArchDef.Component c : a_arch.getComponents()) {
            if (a_arch.getMappedNodeCount(a_g.getNodes(), c) == 0) {
                exception.m_components.add(c);
            }
        }

        if (exception.m_components.size() > 0) {
            throw exception;
        }
    }

    public int countNodesToBeMapped(ArchDef a_arch, Iterable<SystemModelReader.Mapping>a_mappings, CGraph a_g) {
        int count = 0;
        for (SystemModelReader.Mapping mapping : a_mappings) {
            ArchDef.Component c = a_arch.getComponent(mapping.m_moduleName);
            Selector.Pat p = new Selector.Pat(mapping.m_regexp);

            for (CNode n : a_g.getNodes()) {
                if (p.isSelected(n)) {
                    count++;
                }
            }
        }
        return count;
    }

    public void setInitialMapping(ArchDef a_arch, SystemModelReader a_model, CGraph a_g) {
        for (SystemModelReader.Mapping mapping : a_model.m_initialMappings) {
            ArchDef.Component c = a_arch.getComponent(mapping.m_moduleName);
            Selector.Pat p = new Selector.Pat(mapping.m_regexp);

            for (CNode n : a_g.getNodes()) {
                if (p.isSelected(n)) {
                    ArchDef.Component oldMapping = a_arch.getClusteredComponent(n);
                    if (oldMapping != null && oldMapping.getClusteringType(n) == ArchDef.Component.ClusteringType.Initial) {
                        // we already have an initial clustering
                        if (!mapping.m_regexp.contains(".*")) {
                            oldMapping.removeClustering(n);
                            c.clusterToNode(n, ArchDef.Component.ClusteringType.Initial);
                        }
                    } else {
                        c.clusterToNode(n, ArchDef.Component.ClusteringType.Initial);
                    }
                }
            }
        }
    }

    public ArchDef createArch(SystemModelReader a_model) {
        ArchDef arch = new ArchDef();

        for (SystemModelReader.Module module : a_model.m_modules) {
            arch.addComponent(module.m_name);
        }

        for (SystemModelReader.Relation relation : a_model.m_relations) {
            ArchDef.Component from, to;
            from = arch.getComponent(relation.m_moduleNameFrom);
            to = arch.getComponent(relation.m_moduleNameTo);
            if (from == null) {
                throw new NullPointerException("Could not find component: " + relation.m_moduleNameFrom + " -> " + a_model.m_id + ": " + relation.m_line);
            }
            if (to == null) {
                throw new NullPointerException("Could not find component: " + relation.m_moduleNameTo + " -> " + a_model.m_id + ": " + relation.m_line);
            }
            from.addDependencyTo(to);
        }

        return arch;
    }

    public SystemModelReader createSystemModel(ArchDef a_arch, Iterable<CNode> a_nodesToMap) {
        SystemModelReader ret = new SystemModelReader();

        ret.m_name = "created system";

        for (ArchDef.Component c : a_arch.getComponents()) {

            SystemModelReader.Module module = new SystemModelReader.Module();
            module.m_name = c.getName();
            ret.m_modules.add(module);



            for (ArchDef.Component to : a_arch.getComponents()) {
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
                    //m.m_regexp = n.getName();
                    m.m_regexp = n.getLogicName();

                    ret.m_mappings.add(m);
                }
            }
        }

        return ret;

    }
}
