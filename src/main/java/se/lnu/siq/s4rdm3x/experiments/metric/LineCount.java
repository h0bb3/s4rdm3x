package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.model.AttributeUtil;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.model.CNode;

public class LineCount extends Metric {
    @Override
    public String getName() {
        return "linecount";
    }


    @Override
    public void assignMetric(Iterable<CNode> a_nodes) {
        ByteCodeInstructions bci = new ByteCodeInstructions();

        for(CNode n : a_nodes) {
            double size = compute(n, bci);
            n.setMetric(getName(), size);
        }
    }

    public double compute(CNode a_n, ByteCodeInstructions a_bci) {
        double size = 0;
        for (dmClass c : a_n.getClasses()) {
            size += c.getLineCount();
        }

        if (size == 0) {
            size = a_bci.compute(a_n) * 0.2143;    // constant computed based on metrics from jabref, teammates, lucene & ant
        }

        return size;
    }

    public void reassignMetric(Iterable<CNode> a_nodes) {

    }
}
