package se.lnu.siq.s4rdm3x.experiments.metric.aggregated;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;
import se.lnu.siq.s4rdm3x.experiments.metric.FanHelper;
import se.lnu.siq.s4rdm3x.experiments.metric.Metric;

public class Fan extends Metric {

    public String getName() {
        return "fan";
    }

    public void assignMetric(Iterable<Node> a_nodes) {
        FanHelper fh = new FanHelper(a_nodes);

        for(Node n : a_nodes) {
            setMetric(n, fh.getFanIn(n) + fh.getFanOut(n));
        }
    }

    public void reassignMetric(Iterable<Node> a_nodes) {
        // the fan in will not change so...
    }
}