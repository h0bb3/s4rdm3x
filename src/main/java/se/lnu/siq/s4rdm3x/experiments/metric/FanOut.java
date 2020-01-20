package se.lnu.siq.s4rdm3x.experiments.metric;

import se.lnu.siq.s4rdm3x.model.CNode;

public class FanOut extends Metric {

    @Override
    public String getName() {
        return "fanout";
    }

    @Override
    public void assignMetric(Iterable<CNode> a_nodes) {
        for(CNode n : a_nodes) {
            double fout = compute(n, a_nodes);
            n.setMetric(getName(), fout);
        }
    }

    public void reassignMetric(Iterable<CNode> a_nodes) {
        // the fan out will not change so...
    }


    public double compute(CNode a_n, Iterable<CNode> a_nodes) {
        double fout = 0;
        for (CNode m : a_nodes) {

            if (a_n != m) {
                fout += a_n.getDependencyCount(m);
            }
        }

        return fout;
    }
}
