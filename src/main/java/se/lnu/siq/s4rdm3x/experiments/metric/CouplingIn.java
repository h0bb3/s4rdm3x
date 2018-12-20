package se.lnu.siq.s4rdm3x.experiments.metric;

import se.lnu.siq.s4rdm3x.model.CNode;

public class CouplingIn extends Metric {

    public String getName() {
        return "CouplingIn";
    }

    public void assignMetric(Iterable<CNode> a_nodes) {
        for(CNode n : a_nodes) {
            double cin = 0;

            for (CNode m :  a_nodes) {
                if (m != n && m.hasDependency(n)) {
                    cin += 1;
                }
            }

            n.setMetric(getName(), cin);
        }
    }

    public void reassignMetric(Iterable<CNode> a_nodes) {

    }

}
