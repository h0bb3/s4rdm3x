package se.lnu.siq.s4rdm3x.experiments.metric.aggregated;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;
import se.lnu.siq.s4rdm3x.cmd.util.AttributeUtil;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
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

        for(Node n : a_nodes) {

            double size = 0;
            for (dmClass c : au.getClasses(n)) {
                size += c.getLineCount();
            }
            if (size == 0) {
                size = 1;
            }
            setMetric(n, getMetric(n) / size);
        }
    }

    public void reassignMetric(Iterable<Node> a_nodes) {

    }
}
