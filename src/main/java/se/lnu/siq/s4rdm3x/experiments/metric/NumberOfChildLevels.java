package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;

import java.util.ArrayList;

public class NumberOfChildLevels  extends Metric {

    public String getName() {
        return "NumberOfChildLevels";
    }

    public void assignMetric(Iterable<Node> a_nodes) {
        FanHelper fh = new FanHelper(a_nodes);
        double noc = 0;
        for (Node n : a_nodes) {
            ArrayList<Node> path = new ArrayList<>();
            noc = noc(n, path, a_nodes, fh);
            setMetric(n, noc);
        }
    }

    private double noc(Node a_source, ArrayList<Node> a_path, Iterable<Node> a_nodes, FanHelper a_fh) {
        double ret = 0;
        a_path.add(a_source);

        for (Node n : a_nodes) {
            if (!a_path.contains(n)) {
                if (a_fh.hasDirectDependency(n, a_source, dmDependency.Type.Extends) ||
                        a_fh.hasDirectDependency(n, a_source, dmDependency.Type.Implements)) {

                        double childDepth = 1 + noc(n, a_path, a_nodes, a_fh);
                        if (childDepth > ret) {
                            ret = childDepth;
                        }
                }
            }
        }

        a_path.remove(a_source);
        return ret;
    }



    public void reassignMetric(Iterable<Node> a_nodes) {
    }
}
