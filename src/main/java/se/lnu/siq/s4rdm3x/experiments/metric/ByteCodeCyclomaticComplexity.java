package se.lnu.siq.s4rdm3x.experiments.metric;

import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.model.CNode;

public class ByteCodeCyclomaticComplexity extends Metric {

    @Override
    public String getName() {
        return "ByteCodeCyclomaticComplexity";
    }

    @Override
    public void assignMetric(Iterable<CNode> a_nodes) {

        for(CNode n : a_nodes) {
            double cc = 0;
            for (dmClass c : n.getClasses()) {
                for (dmClass.Method m : c.getMethods()) {
                    cc += m.getBranchStatementCount();
                    if (!m.isAbstract()) {
                        cc++;
                    }
                }
            }
            n.setMetric(getName(), cc);
        }
    }

    @Override
    public void reassignMetric(Iterable<CNode> a_nodes) {

    }
}
