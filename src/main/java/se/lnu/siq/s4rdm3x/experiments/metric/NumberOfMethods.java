package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.model.AttributeUtil;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.model.CNode;

public class NumberOfMethods extends Metric {
    public String getName() {
        return "NumberOfMethods";
    }
    public void assignMetric(Iterable<CNode> a_nodes) {
        AttributeUtil au = new AttributeUtil();
        for (CNode n : a_nodes) {
            double nom = 0;
            for (dmClass c : n.getClasses()) {
                nom += c.getMethodCount();
            }

            n.setMetric(getName(), nom);
        }
    }
    public void reassignMetric(Iterable<CNode> a_nodes) {

    }
}
