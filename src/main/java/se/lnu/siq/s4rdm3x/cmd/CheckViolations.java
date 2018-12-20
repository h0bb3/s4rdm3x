package se.lnu.siq.s4rdm3x.cmd;

import se.lnu.siq.s4rdm3x.cmd.hugme.HuGMe;
import se.lnu.siq.s4rdm3x.model.AttributeUtil;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.ArrayList;
import java.util.HashMap;

public class CheckViolations {

    public ArrayList<Violation> m_divergencies = new ArrayList<>();

    public static class Violation {
        public static class Part {
            public HuGMe.ArchDef.Component m_component;
            public CNode m_node;
            public dmClass m_class;
        }
        public Part m_source = new Part(), m_dest = new Part();
        public dmDependency m_dependency;
    }

    public void run(CGraph a_g, HuGMe.ArchDef a_arch) {
        HashMap<HuGMe.ArchDef.Component, ArrayList<CNode>> nodesPerComponent = new HashMap<>();

        for (HuGMe.ArchDef.Component c : a_arch.getComponents()) {
            nodesPerComponent.put(c, new ArrayList<>());
        }
        for(CNode n : a_arch.getMappedNodes(a_g.getNodes())) {
            HuGMe.ArchDef.Component c = a_arch.getMappedComponent(n);
            nodesPerComponent.get(c).add(n);
        }

        for (HuGMe.ArchDef.Component cFrom : a_arch.getComponents()) {

            for (HuGMe.ArchDef.Component cTo : a_arch.getComponents()) {
                if (cFrom != cTo && !cFrom.allowedDependency(cTo)) {
                    addDivergencies(cFrom, nodesPerComponent.get(cFrom), cTo, nodesPerComponent.get(cTo));
                }
            }
        }
    }

    private void addDivergencies(HuGMe.ArchDef.Component a_cFrom, Iterable<CNode> a_from, HuGMe.ArchDef.Component a_cTo, Iterable<CNode> a_to) {
        AttributeUtil au = new AttributeUtil();
        for(CNode from : a_from) {
            for (CNode to : a_to) {
                for (dmDependency d : from.getDependencies(to)) {
                    // divergency found
                    Violation v = new Violation();
                    v.m_source.m_component = a_cFrom;
                    v.m_source.m_class = d.getSource();
                    v.m_source.m_node = from;
                    v.m_dest.m_component = a_cTo;
                    v.m_dest.m_class = d.getTarget();
                    v.m_dest.m_node = to;
                    v.m_dependency = d;
                    m_divergencies.add(v);
                }
            }
        }
    }
}