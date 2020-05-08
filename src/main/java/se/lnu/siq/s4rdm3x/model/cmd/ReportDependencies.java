package se.lnu.siq.s4rdm3x.model.cmd;

import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.Selector;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;

import java.util.ArrayList;
import java.util.HashMap;

public class ReportDependencies {
    public ArrayList<CheckViolations.Violation> m_divergencies = new ArrayList<>();
    Selector.ISelector m_sourceNodes;

    public static class Dependency {
        public CNode m_node;
        ArrayList<dmDependency> m_internalDependencies; // dependencies to/from other nodes within the same architectural module, dependencies to self are not counted
        ArrayList<dmDependency> m_externalDependencies; // dependencies to/from nodes in other architectural modules
        ArrayList<dmDependency> m_unmappedDependenices; // dependencies to/from nodes that are not mapped to an architectural module

        public int getInternalDependencyCount() {
            return getDCount(m_internalDependencies);
        }

        public int getExternalDependencyCount() {
            return getDCount(m_externalDependencies);
        }

        public int getUnmappedDependencyCount() {
            return getDCount(m_unmappedDependenices);
        }

        private int getDCount(Iterable<dmDependency> a_dependencies) {
            int [] ret = new int[1];
            ret[0] = 0;
            a_dependencies.forEach(d -> ret[0] += d.getCount());
            return ret[0];
        }
    }

    public ArrayList<Dependency> m_dependencyReport;

    public ReportDependencies() {
        m_sourceNodes = new Selector.All();
    }

    public ReportDependencies(Selector.ISelector a_nodesToCheck) {
        m_sourceNodes = a_nodesToCheck;
    }

    public void run(CGraph a_g, ArchDef a_arch) {
        HashMap<ArchDef.Component, Iterable<CNode>> nodesPerComponent = new HashMap<>();
        Iterable<CNode> unmapped = a_arch.getUnmappedNodes(a_g.getNodes(m_sourceNodes));
        m_dependencyReport = new ArrayList<>();

        for (ArchDef.Component c : a_arch.getComponents()) {
            nodesPerComponent.put(c, c.getMappedNodes(a_g.getNodes(m_sourceNodes)));
        }

        for (ArchDef.Component cFrom : a_arch.getComponents()) {

            for (CNode n : nodesPerComponent.get(cFrom)) {
                Dependency d = new Dependency();
                m_dependencyReport.add(d);
                d.m_node = n;
                d.m_externalDependencies = new ArrayList<>();
                for (ArchDef.Component cTo : a_arch.getComponents()) {
                    if (cFrom == cTo) {
                        d.m_internalDependencies = getDependencies(n, nodesPerComponent.get(cTo));
                    } else {
                        d.m_externalDependencies.addAll(getDependencies(n, nodesPerComponent.get(cTo)));
                    }
                }
                d.m_unmappedDependenices = getDependencies(n, unmapped);
            }
        }
    }

    private ArrayList<dmDependency> getDependencies(CNode a_from, Iterable<CNode> a_targets) {
        ArrayList<dmDependency> ret = new ArrayList<>();
        for (CNode to : a_targets) {
            if (a_from != to) {
                for (dmDependency d : a_from.getDependencies(to)) {
                    ret.add(d);
                }
                for (dmDependency d : to.getDependencies(a_from)) {
                    ret.add(d);
                }
            }
        }
        return ret;
    }
}
