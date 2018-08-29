package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;

public class MinFan extends Metric {

    public String getName() {
        return "minfan";
    }

    public void assignMetric(Graph a_g, HuGMe.ArchDef a_arch) {
        FanHelper fh = new FanHelper(a_g, a_arch);

        for(Node n : a_arch.getMappedNodes(a_g.getNodeSet())) {
            setMetric(n, Math.max(fh.getFanIn(n),fh.getFanOut(n)));
        }
    }

    public void reassignMetric(Graph a_g, HuGMe.ArchDef a_arch) {
        // the fan in will not change so...
    }
}

