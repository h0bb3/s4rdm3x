package se.lnu.siq.s4rdm3x.experiments.metric;

import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.ArrayList;

public class NumberOfParents extends Metric {

    public String getName() {
        return "NumberOfParents";
    }

    public void assignMetric(Iterable<CNode> a_nodes) {
        double nop = 0;
        for (CNode n : a_nodes) {
            ArrayList<CNode> path = new ArrayList<>();
            nop = nop(n, path, a_nodes);
            n.setMetric(getName(), nop);
        }
    }

    public void reassignMetric(Iterable<CNode> a_nodes) {

    }

    private double nop(CNode a_source, ArrayList<CNode> a_path, Iterable<CNode> a_nodes) {
        double ret = 0;
        a_path.add(a_source);

        for (CNode n : a_nodes) {
            if (!a_path.contains(n)) {
                if (a_source.isSpecializationOf(n)) {

                    ret += 1 + nop(n, a_path, a_nodes);
                }
            }
        }

        return ret;
    }
}
