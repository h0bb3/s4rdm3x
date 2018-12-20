package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.ArrayList;

public class NumberOfChildrenLevel0 extends Metric {

    public String getName() {
        return "NumberOfChildrenLevel0";
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

                    ret += 1;
                }
            }
        }

        return ret;
    }



    public void reassignMetric(Iterable<CNode> a_nodes) {
    }
}
