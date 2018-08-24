package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;

import java.util.Random;

public class Rand extends Metric {
    private static Random g_rand = new Random();

    @Override
    public String getName() {
        return "rand";
    }


    @Override
    public void assignMetric(Graph a_g, HuGMe.ArchDef a_arch) {

        for(Node n : a_g.getEachNode()) {
            if (a_arch.getMappedComponent(n) != null) {
                setMetric(n, g_rand.nextDouble());
            }
        }
    }
}
