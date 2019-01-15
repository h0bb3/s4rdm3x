package se.lnu.siq.s4rdm3x.experiments.metric;

import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.model.CNode;

public class ByteCodeInstructions extends Metric {

    @Override
    public String getName() {
        return "ByteCodeInstructions";
    }

    @Override
    public void assignMetric(Iterable<CNode> a_nodes) {

        for(CNode n : a_nodes) {
            double size = compute(n);
            n.setMetric(getName(), size);
        }
    }

    public double compute(CNode a_n) {
        double size = 0;
        for (dmClass c : a_n.getClasses()) {
            for (dmClass.Method m : c.getMethods()) {
                size += m.getInstructionCount();
            }
        }
        return size;
    }

    @Override
    public void reassignMetric(Iterable<CNode> a_nodes) {

    }
}
