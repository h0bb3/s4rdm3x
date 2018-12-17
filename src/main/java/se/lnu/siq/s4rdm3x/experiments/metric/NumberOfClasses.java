package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.util.AttributeUtil;

public class NumberOfClasses extends Metric {
    public String getName() {
        return "NumberOfClasses";
    }

    public void assignMetric(Iterable<Node> a_nodes) {
        AttributeUtil au = new AttributeUtil();

        for(Node n : a_nodes) {
            setMetric(n, au.getClasses(n).size());
        }
    }

    public void reassignMetric(Iterable<Node> a_nodes) {
        // the fan in will not change so...
    }



}
