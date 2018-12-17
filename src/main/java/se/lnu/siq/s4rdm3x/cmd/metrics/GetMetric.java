package se.lnu.siq.s4rdm3x.cmd.metrics;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.util.AttributeUtil;
import se.lnu.siq.s4rdm3x.cmd.util.Selector;
import se.lnu.siq.s4rdm3x.experiments.metric.Metric;
import se.lnu.siq.s4rdm3x.experiments.metric.MetricFactory;

public class GetMetric {
    public double m_result;
    String m_metric;
    Selector.ISelector m_selection;

    public GetMetric(String a_metricName, Selector.ISelector a_selection) {
        m_selection = a_selection;
        m_metric = a_metricName;
    }

    public void run(Graph a_g) {

        m_result = 0;
        for (Node n : a_g.getNodeSet()) {
            if (m_selection.isSelected(n)) {
                ComputeMetrics.Map map = n.getAttribute(ComputeMetrics.g_metricsMapKey);

                if (map == null) {
                    AttributeUtil au = new AttributeUtil();
                    throw new IllegalArgumentException("No metrics in node: " + au.getName(n));
                }

                if (!map.containsKey(m_metric)) {
                    throw new IllegalArgumentException("No such metric: " + m_metric);
                }

                m_result += map.get(m_metric);
            }
        }
    }

}
