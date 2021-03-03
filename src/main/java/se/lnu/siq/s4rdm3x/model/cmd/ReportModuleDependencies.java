package se.lnu.siq.s4rdm3x.model.cmd;

import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.Selector;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ReportModuleDependencies {

    public static class ModuleDependency {

        ArchDef.Component m_source, m_target;
        dmDependency m_d;

        public ModuleDependency(ArchDef.Component a_from, dmDependency a_dep, ArchDef.Component a_to) {
            m_source = a_from;
            m_target = a_to;
            m_d = a_dep;
        }
    }

    Map<dmDependency.Type, ArrayList<ModuleDependency>> m_moduleDeps;

    public void run(CGraph a_g, ArchDef a_arch) {

        Iterable<CNode> mappedNodes = a_arch.getMappedNodes(a_g.getNodes());

        m_moduleDeps = new HashMap<>();

        for (CNode from : mappedNodes) {
            mappedNodes.forEach(to -> from.getDependencies(to, new CNode.DependencyFilter() {
                @Override
                public boolean filter(CNode a_from, dmDependency a_dep, CNode a_to) {

                    addModuleDependency(a_arch.getMappedComponent(a_from), a_dep, a_arch.getMappedComponent(a_to));

                    return false;
                }

                private void addModuleDependency(ArchDef.Component a_from, dmDependency a_dep, ArchDef.Component a_to) {
                    ArrayList<ModuleDependency> target;
                    if (!m_moduleDeps.containsKey(a_dep.getType())) {
                        target = new ArrayList<>();
                        m_moduleDeps.put(a_dep.getType(), target);
                    } else {
                        target = m_moduleDeps.get(a_dep.getType());
                    }

                    target.add(new ModuleDependency(a_from, a_dep, a_to));
                }
            }));
        }
    }

    public int countInternalDeps(dmDependency.Type a_type) {
        return countDeps(a_type, moduleDependency -> moduleDependency.m_target == moduleDependency.m_source ? moduleDependency.m_d.getCount() : 0);
    }

    public int countExternalDeps(dmDependency.Type a_type) {
        return countDeps(a_type, moduleDependency -> moduleDependency.m_target != moduleDependency.m_source ? moduleDependency.m_d.getCount() : 0);
    }

    public int countInternalDeps() {
        return countDeps(moduleDependency -> moduleDependency.m_target == moduleDependency.m_source ? moduleDependency.m_d.getCount() : 0);
    }

    public int countExternalDeps() {
        return countDeps(moduleDependency -> moduleDependency.m_target != moduleDependency.m_source ? moduleDependency.m_d.getCount() : 0);
    }

    private int countDeps(dmDependency.Type a_type, Function<ModuleDependency, Integer> a_counter) {
        int ret = 0;
        ret = countDeps(m_moduleDeps.get(a_type), a_counter);

        return ret;
    }

    private int countDeps(Iterable<ModuleDependency> a_mds, Function<ModuleDependency, Integer> a_counter) {
        int ret = 0;
        if (a_mds != null) {
            for (ModuleDependency md : a_mds) {
                ret += a_counter.apply(md);
            }
        }
        return ret;
    }

    private int countDeps(Function<ModuleDependency, Integer> a_counter) {
        int ret = 0;
        for(Iterable<ModuleDependency> mds : m_moduleDeps.values()) {
            ret += countDeps(mds, a_counter);
        }

        return ret;
    }
}
