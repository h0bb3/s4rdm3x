package se.lnu.siq.s4rdm3x.model.cmd;

import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;

import java.util.ArrayList;

public class ReportModuleDependencies {

    public static class Module {
        ArchDef.Component m_component;
        ArrayList<CNode> m_nodes;   // the nodes that are clustered to the component
        ArrayList<CNode> m_fanOut;  // the nodes that m_nodes have dependencies to
        ArrayList<CNode> m_fanIn;   // the nodes that have dependencies to m_nodes



        public int countInternalDependencies(dmDependency.Type a_type) {
            return count(a_type, m_nodes);
        }

        public int countOutgoingDependencies(dmDependency.Type a_type) {
            return count(a_type, m_fanOut);
        }

        public int countIncomingDependencies(dmDependency.Type a_type) {
            return count(a_type, m_fanIn);
        }

        private int count(dmDependency.Type a_type, Iterable<CNode> a_tos) {

            class Counter extends CNode.CountFilter {
                @Override
                public boolean filter(CNode a_from, dmDependency a_dep, CNode a_to) {
                    if (a_from != a_to && a_dep.getType() == a_type) {
                        m_count += a_dep.getCount();
                    }
                    return false;
                }
            }

            Counter c = new Counter();
            m_nodes.forEach(from -> {a_tos.forEach(to->{from.getDependencyCount(to, c);});});
            return c.m_count;
        }
    }

    public void run(CGraph a_g, ArchDef a_arch) {
        // iterate all components and get all nodes for each component
        // find all dependencies and construct the fan sets for each module
        // decide how to count i.e. internal vs outgoing and internal
        // |A -> B| B->|C|
        // AB has 2 internal deps A->B and B<-A
        // AB has 1 external fan out B->C
        // C has 0 internal
        // C has 1 external fan in C<-B
        // |AB| has thus 2/3 internal deps and 1/3 external deps
        // |C| has thus 0/1 internal deps 1/1 external deps
    }
}
