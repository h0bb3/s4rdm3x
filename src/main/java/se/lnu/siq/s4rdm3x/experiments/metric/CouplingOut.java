package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.model.CNode;

public class CouplingOut extends Metric {

    public String getName() {
        return "CouplingOut";
    }

    public void assignMetric(Iterable<CNode> a_nodes) {
        for(CNode n : a_nodes) {
            double cout = 0;

            for (CNode m :  a_nodes) {
                if (m != n && n.hasDependency(m)) {
                    cout += 1;
                }
            }

            n.setMetric(getName(), cout);
        }
    }

    public void reassignMetric(Iterable<CNode> a_nodes) {

    }

}
