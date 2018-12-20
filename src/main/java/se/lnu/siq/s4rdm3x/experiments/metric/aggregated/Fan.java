package se.lnu.siq.s4rdm3x.experiments.metric.aggregated;

import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.experiments.metric.FanHelper;
import se.lnu.siq.s4rdm3x.experiments.metric.FanIn;
import se.lnu.siq.s4rdm3x.experiments.metric.FanOut;
import se.lnu.siq.s4rdm3x.experiments.metric.Metric;
import se.lnu.siq.s4rdm3x.model.CNode;

public class Fan extends Metric {

    public String getName() {
        return "fan";
    }

    public void assignMetric(Iterable<CNode> a_nodes) {

        FanIn fin = new FanIn();
        FanOut fout = new FanOut();

        for(CNode n : a_nodes) {
            n.setMetric(getName(), fin.compute(n, a_nodes) + fout.compute(n, a_nodes));
        }
    }

    public void reassignMetric(Iterable<CNode> a_nodes) {
        // the fan in will not change so...
    }
}