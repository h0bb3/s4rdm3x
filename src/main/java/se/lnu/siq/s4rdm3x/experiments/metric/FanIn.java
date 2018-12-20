package se.lnu.siq.s4rdm3x.experiments.metric;

import se.lnu.siq.s4rdm3x.model.CNode;

public class FanIn extends Metric {
    public String getName() {
        return "fanin";
    }

    public void assignMetric(Iterable<CNode> a_nodes) {
        for(CNode n : a_nodes) {
            double fin = compute(n, a_nodes);
            n.setMetric(getName(), fin);
        }
    }

    public void reassignMetric(Iterable<CNode> a_nodes) {
        // the fan in will not change so...
    }

    public double compute(CNode n, Iterable<CNode> a_nodes) {
        double fin = 0;
        for (CNode m : a_nodes) {
            if (n != m) {
                fin += m.getDependencyCount(n);
            }
        }

        return fin;
    }
}
