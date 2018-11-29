package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.util.AttributeUtil;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;

public class LineCount extends Metric {
    @Override
    public String getName() {
        return "linecount";
    }


    @Override
    public void assignMetric(Iterable<Node> a_nodes) {
        AttributeUtil au = new AttributeUtil();
        ByteCodeInstructions bci = new ByteCodeInstructions();

        for(Node n : a_nodes) {


            double size = compute(n, au, bci);
            setMetric(n, size);
        }
    }

    public double compute(Node a_n, AttributeUtil a_au, ByteCodeInstructions a_bci) {
        double size = 0;
        for (dmClass c : a_au.getClasses(a_n)) {
            size += c.getLineCount();
        }

        if (size == 0) {
            size = a_bci.compute(a_n, a_au) * 0.2143;    // constant computed based on metrics from jabref, teammates, lucene & ant
        }

        return size;
    }

    public void reassignMetric(Iterable<Node> a_nodes) {

    }
}
