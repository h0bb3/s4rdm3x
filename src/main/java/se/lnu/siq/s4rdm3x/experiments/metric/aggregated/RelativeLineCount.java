package se.lnu.siq.s4rdm3x.experiments.metric.aggregated;

import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.util.AttributeUtil;
import se.lnu.siq.s4rdm3x.experiments.metric.ByteCodeInstructions;
import se.lnu.siq.s4rdm3x.experiments.metric.LineCount;
import se.lnu.siq.s4rdm3x.experiments.metric.Metric;

public class RelativeLineCount extends Metric {

    private Metric m_rel;

    public RelativeLineCount(Metric a_rel) {
        m_rel = a_rel;
    }

    @Override
    public String getName() {
        return m_rel.getName() + "_linecount";
    }


    @Override
    public void assignMetric(Iterable<Node> a_nodes) {

        m_rel.assignMetric(a_nodes);

        AttributeUtil au = new AttributeUtil();
        LineCount lc = new LineCount();
        ByteCodeInstructions bci = new ByteCodeInstructions();

        for(Node n : a_nodes) {
            double lineCount = lc.compute(n, au, bci);
            if (lineCount == 0) {
                lineCount = 1;
            }
            setMetric(n, getMetric(n) / lineCount);
        }
    }

    public void reassignMetric(Iterable<Node> a_nodes) {

    }
}
