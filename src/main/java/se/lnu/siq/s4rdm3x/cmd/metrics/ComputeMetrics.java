package se.lnu.siq.s4rdm3x.cmd.metrics;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.util.Selector;
import se.lnu.siq.s4rdm3x.experiments.metric.Metric;
import se.lnu.siq.s4rdm3x.experiments.metric.MetricFactory;

import java.util.ArrayList;
import java.util.HashMap;

public class ComputeMetrics {
    static final String g_metricsMapKey = "MetricsMap";
    private Selector.ISelector m_selection;

    public static class Map extends HashMap<String, Double> {}

    public ComputeMetrics(Selector.ISelector a_selection) {
        m_selection = a_selection;
    }

    public void run(Graph a_g) {
        ArrayList<Node> selectedSet = new ArrayList();
        for (Node n : a_g.getEachNode()) {
            if (m_selection.isSelected(n)) {
                selectedSet.add(n);
            }
        }

        computeAllMetrics(selectedSet);
    }

    private void computeAllMetrics(Iterable<Node> a_nodes) {
        MetricFactory mf = new MetricFactory();
        for (Node n : a_nodes) {
            n.setAttribute(g_metricsMapKey, new Map());
        }

        for(Metric m : mf.getPrimitiveMetrics()) {
            m.assignMetric(a_nodes);

            assignMetricToMetricsMap(m, a_nodes);
        }
    }

    private void assignMetricToMetricsMap(Metric a_m, Iterable<Node> a_nodes) {
        String metricKey = a_m.getName();
        for (Node n : a_nodes) {
            Map metricsMap = n.getAttribute(g_metricsMapKey);
            metricsMap.put(metricKey, a_m.getMetric(n));
        }
    }
}
