package se.lnu.siq.s4rdm3x.experiments.metric.aggregated;

import se.lnu.siq.s4rdm3x.experiments.metric.ByteCodeInstructions;
import se.lnu.siq.s4rdm3x.experiments.metric.LineCount;
import se.lnu.siq.s4rdm3x.experiments.metric.Metric;
import se.lnu.siq.s4rdm3x.model.CNode;

public class RelativeLineCount extends Metric {

    public static final String g_nameSuffix = "_linecount";
    private Metric m_rel;

    public RelativeLineCount(Metric a_rel) {
        m_rel = a_rel;
    }

    @Override
    public String getName() {
        return m_rel.getName() + g_nameSuffix;
    }


    @Override
    public void assignMetric(Iterable<CNode> a_nodes) {

        m_rel.assignMetric(a_nodes);

        LineCount lc = new LineCount();
        ByteCodeInstructions bci = new ByteCodeInstructions();

        for(CNode n : a_nodes) {
            double lineCount = lc.compute(n, bci);
            if (lineCount == 0) {
                lineCount = 1;
            }
            n.setMetric(getName(), n.getMetric(m_rel.getName()) / lineCount);
        }
    }

    public void reassignMetric(Iterable<CNode> a_nodes) {

    }
}
