package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Node;

public class AvgFan extends Metric {

    public String getName() {
        return "avgfan";
    }

    public void assignMetric(Iterable<Node> a_nodes) {
        FanHelper fh = new FanHelper(a_nodes);

        for(Node n : a_nodes) {
            setMetric(n, (fh.getFanIn(n) + fh.getFanOut(n)) * 0.5);
        }
    }

    public void reassignMetric(Iterable<Node> a_nodes) {
        // the fan in will not change so...
    }
}
