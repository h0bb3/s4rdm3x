package se.lnu.siq.s4rdm3x.experiments.metric;

import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.ArrayList;

public class NumberOfChildLevels  extends Metric {

    public String getName() {
        return "NumberOfChildLevels";
    }

    public void assignMetric(Iterable<CNode> a_nodes) {
        double noc = 0;
        for (CNode n : a_nodes) {
            ArrayList<CNode> path = new ArrayList<>();
            noc = noc(n, path, a_nodes);
            n.setMetric(getName(), noc);
        }
    }

    private double noc(CNode a_source, ArrayList<CNode> a_path, Iterable<CNode> a_nodes) {
        double ret = 0;
        a_path.add(a_source);

        for (CNode n : a_nodes) {
            if (!a_path.contains(n)) {
                if (n.isSpecializationOf(a_source)) {

                        double childDepth = 1 + noc(n, a_path, a_nodes);
                        if (childDepth > ret) {
                            ret = childDepth;
                        }
                }
            }
        }

        a_path.remove(a_source);
        return ret;
    }



    public void reassignMetric(Iterable<CNode> a_nodes) {
    }
}
