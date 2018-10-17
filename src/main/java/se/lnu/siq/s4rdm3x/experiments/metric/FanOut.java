package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.util.AttributeUtil;

public class FanOut extends Metric {

    @Override
    public String getName() {
        return "fanout";
    }

    @Override
    public void assignMetric(Iterable<Node> a_nodes) {
        AttributeUtil au = new AttributeUtil();
        FanHelper fh = new FanHelper(a_nodes);
        for(Node n : a_nodes) {
            setMetric(n, fh.getFanOut(n));
        }
    }

    public void reassignMetric(Iterable<Node> a_nodes) {
        // the fan out will not change so...
    }


}
