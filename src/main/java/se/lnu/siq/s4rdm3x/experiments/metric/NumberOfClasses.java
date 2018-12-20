package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.model.AttributeUtil;
import se.lnu.siq.s4rdm3x.model.CNode;

public class NumberOfClasses extends Metric {
    public String getName() {
        return "NumberOfClasses";
    }

    public void assignMetric(Iterable<CNode> a_nodes) {

        for(CNode n : a_nodes) {
            n.setMetric(getName(), n.getClassCount());
        }
    }

    public void reassignMetric(Iterable<CNode> a_nodes) {
        // the fan in will not change so...
    }



}
