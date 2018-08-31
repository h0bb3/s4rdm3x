package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;

public class AvgFan extends Metric {

    public String getName() {
        return "avgfan";
    }

    public void assignMetric(Graph a_g, HuGMe.ArchDef a_arch) {
        FanHelper fh = new FanHelper(a_g, a_arch);

        for(Node n : a_arch.getMappedNodes(a_g.getNodeSet())) {
            setMetric(n, (fh.getFanIn(n) + fh.getFanOut(n)) * 0.5);
        }
    }

    public void reassignMetric(Graph a_g, HuGMe.ArchDef a_arch) {
        // the fan in will not change so...
    }
}