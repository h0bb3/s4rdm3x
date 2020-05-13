package se.lnu.siq.s4rdm3x.model.cmd;

import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.Selector;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ReportDependencies {
    public ArrayList<CheckViolations.Violation> m_divergencies = new ArrayList<>();
    Selector.ISelector m_sourceNodes;

    public static class Dependency {
        public CNode m_node;

        Set<CNode> m_internalDependencies; // dependencies to/from other nodes within the same architectural module, dependencies to self are not counted
        Set<CNode> m_externalDependencies; // dependencies to/from nodes in other architectural modules
        Set<CNode> m_unmappedDependenices; // dependencies to/from nodes that are not mapped to an architectural module

        // Fan methods refer to counting all dependencies between nodes, i.e. you can get multiple dependencies between the same two nodes
        // Coupling methods refer to counting only unique nodes, i.e. you will get a maximum of one dependency between the same two nodes

        public int getInternalFan() {
            return getFan(m_internalDependencies);
        }

        public int getExternalFan() {
            return getFan(m_externalDependencies);
        }

        public int getUnmappedFan() {
            return getFan(m_unmappedDependenices);
        }

        public int getInternalFanOut() {
            return getFanOut(m_internalDependencies);
        }

        public int getExternalFanOut() {
            return getFanOut(m_externalDependencies);
        }

        public int getUnmappedFanOut() {
            return getFanOut(m_unmappedDependenices);
        }

        public int getInternalCouplingOut() {
            return getCouplingOut(m_internalDependencies);
        }

        public int getExternalCouplingOut() {
            return getCouplingOut(m_externalDependencies);
        }

        public int getUnmappedCouplingOut() {
            return getCouplingOut(m_unmappedDependenices);
        }

        private int getFanOut(Iterable<CNode> a_dependencies) {
            int [] ret = new int[1];
            ret[0] = 0;
            a_dependencies.forEach(d -> ret[0] += m_node.getDependencyCount(d));
            return ret[0];
        }

        private int getCouplingOut(Iterable<CNode> a_dependencies) {
            Set ret = new HashSet<CNode>();

            a_dependencies.forEach(d -> {if (m_node.hasDependency(d)) {ret.add(d);}});
            return ret.size();
        }

        private int getFan(Iterable<CNode> a_dependencies) {
            int [] ret = new int[1];
            ret[0] = 0;
            a_dependencies.forEach(d -> ret[0] += m_node.getDependencyCount(d) + d.getDependencyCount(m_node));
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
                d.m_externalDependencies = new HashSet<>();
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

    private Set<CNode> getDependencies(CNode a_from, Iterable<CNode> a_targets) {
        Set<CNode> ret = new HashSet<>();
        for (CNode to : a_targets) {
            if (a_from != to) {
                if (a_from.hasDependency(to) || to.hasDependency(a_from)) {
                    ret.add(to);
                }
            }
        }
        return ret;
    }
}
