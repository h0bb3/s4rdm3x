package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;
import se.lnu.siq.s4rdm3x.cmd.util.AttributeUtil;

import java.util.HashMap;
import java.util.Map;

public class CustomMetric extends Metric {
    private String m_name;
    private Map<String, Double> m_metrics;

    public CustomMetric(String a_name) {
        m_name = a_name;
        m_metrics = new HashMap<>();
    }

    public void addMetric(String a_fileName, double a_metric) {
        m_metrics.put(a_fileName, a_metric);
    }

    public String getName() {
        return m_name;
    }

    public void assignMetric(Graph a_g, HuGMe.ArchDef a_arch) {
        AttributeUtil au = new AttributeUtil();
        for(Node n : a_arch.getMappedNodes(a_g.getNodeSet())) {
            if (m_metrics.containsKey(au.getName(n))) {
                setMetric(n, m_metrics.get(au.getName(n)));
            } else {
                setMetric(n, -1);
            }
        }
    }

    public void reassignMetric(Graph a_g, HuGMe.ArchDef a_arch){
        // this will not change so...
    }
}
