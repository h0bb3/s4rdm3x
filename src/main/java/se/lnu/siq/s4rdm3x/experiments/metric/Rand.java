package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Node;

import java.util.Random;

public class Rand extends Metric {
    private static Random g_rand = new Random();

    @Override
    public String getName() {
        return "rand";
    }


    @Override
    public void assignMetric(Iterable<Node> a_nodes) {

        for(Node n : a_nodes) {
            setMetric(n, g_rand.nextDouble());
        }
    }

    public void reassignMetric(Iterable<Node> a_nodes) {
        assignMetric(a_nodes);
    }
}
