package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.util.AttributeUtil;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;

public class ByteCodeInstructions extends Metric {

    @Override
    public String getName() {
        return "ByteCodeInstructions";
    }

    @Override
    public void assignMetric(Iterable<Node> a_nodes) {
        AttributeUtil au = new AttributeUtil();

        for(Node n : a_nodes) {
            double size = 0;
            for (dmClass c : au.getClasses(n)) {
                for (dmClass.Method m : c.getMethods()) {
                    size += m.getInstructionCount();
                }
            }
            setMetric(n, size);
        }
    }

    @Override
    public void reassignMetric(Iterable<Node> a_nodes) {

    }
}
