package se.lnu.siq.s4rdm3x.model.cmd.metrics;

import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.Selector;

public class GetMetric {
    public double m_result;
    String m_metric;
    Selector.ISelector m_selection;

    public GetMetric(String a_metricName, Selector.ISelector a_selection) {
        m_selection = a_selection;
        m_metric = a_metricName;
    }

    public void run(CGraph a_g) {

        m_result = 0;
        for (CNode n : a_g.getNodes(m_selection)) {

            CNode.MetricMap map = n.getMetricMap();

            if (map == null) {
                throw new IllegalArgumentException("No metrics in node: " + n.getFileName());
            }

            if (!map.containsKey(m_metric)) {
                throw new IllegalArgumentException("No such metric: " + m_metric);
            }

            m_result += map.get(m_metric);
        }
    }

}
