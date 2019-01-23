package se.lnu.siq.s4rdm3x.model.cmd.metrics;

import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.Selector;
import se.lnu.siq.s4rdm3x.experiments.metric.Metric;
import se.lnu.siq.s4rdm3x.experiments.metric.MetricFactory;

import java.util.ArrayList;

public class ComputeMetrics {
    static final String g_metricsMapKey = "MetricsMap";
    private Selector.ISelector m_selection;



    public ComputeMetrics(Selector.ISelector a_selection) {
        m_selection = a_selection;
    }

    public void run(CGraph a_g) {
        ArrayList<CNode> selectedSet = new ArrayList();
        for (CNode n : a_g.getNodes(m_selection)) {
            selectedSet.add(n);
        }

        computeAllMetrics(selectedSet);
    }

    private void computeAllMetrics(Iterable<CNode> a_nodes) {
        MetricFactory mf = new MetricFactory();

        for(Metric m : mf.getPrimitiveMetrics()) {
            m.assignMetric(a_nodes);

        }
    }
}
