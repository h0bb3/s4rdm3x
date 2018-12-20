package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.Random;

public class Rand extends Metric {
    private static Random g_rand = new Random();

    @Override
    public String getName() {
        return "rand";
    }


    @Override
    public void assignMetric(Iterable<CNode> a_nodes) {

        for(CNode n : a_nodes) {
            n.setMetric(getName(), g_rand.nextDouble());
        }
    }

    public void reassignMetric(Iterable<CNode> a_nodes) {
        assignMetric(a_nodes);
    }
}
