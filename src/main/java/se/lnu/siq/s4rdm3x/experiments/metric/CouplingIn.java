package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.util.AttributeUtil;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;

import java.util.HashSet;

public class CouplingIn extends Metric {

    public String getName() {
        return "CouplingIn";
    }

    public void assignMetric(Iterable<Node> a_nodes) {
        FanHelper fh = new FanHelper(a_nodes);
        for(Node n : a_nodes) {
            double cin = 0;

            for (Node m :  a_nodes) {
                if (m != n && fh.hasDirectDependency(m, n)) {
                    cin += 1;
                }
            }

            setMetric(n, cin);
        }
    }

    public void reassignMetric(Iterable<Node> a_nodes) {

    }

}
