package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.util.AttributeUtil;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;

public class NumberOfFields extends Metric {
    public String getName() {
        return "NumberOfMethods";
    }
    public void assignMetric(Iterable<Node> a_nodes) {
        AttributeUtil au = new AttributeUtil();
        for (Node n : a_nodes) {
            double nom = 0;
            for (dmClass c : au.getClasses(n)) {
                nom += c.getFieldCount();
            }

            setMetric(n, nom);
        }
    }
    public void reassignMetric(Iterable<Node> a_nodes) {

    }
}
