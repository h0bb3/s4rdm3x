package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Node;

public class CouplingOut extends Metric {

    public String getName() {
        return "CouplingOut";
    }

    public void assignMetric(Iterable<Node> a_nodes) {
        FanHelper fh = new FanHelper(a_nodes);
        for(Node n : a_nodes) {
            double cin = 0;

            for (Node m :  a_nodes) {
                if (m != n && fh.hasDirectDependency(n, m)) {
                    cin += 1;
                }
            }

            setMetric(n, cin);
        }
    }

    public void reassignMetric(Iterable<Node> a_nodes) {

    }

}
