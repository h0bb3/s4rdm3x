package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;
import se.lnu.siq.s4rdm3x.cmd.util.FanInCache;

public class FanIn extends Metric {
    public String getName() {
        return "fanin";
    }

    public void assignMetric(Graph a_g, HuGMe.ArchDef a_arch) {
        FanInCache fic = new FanInCache(a_arch.getMappedNodes(a_g.getNodeSet()));

        for(Node n : a_arch.getMappedNodes(a_g.getNodeSet())) {
            setMetric(n, fic.getFanIn(n));
        }
    }

    public void reassignMetric(Graph a_g, HuGMe.ArchDef a_arch) {
        // the fan in will not change so...
    }
}
