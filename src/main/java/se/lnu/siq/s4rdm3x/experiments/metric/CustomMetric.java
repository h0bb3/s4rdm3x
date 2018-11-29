package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Node;
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

    public void assignMetric(Iterable<Node> a_nodes) {
        AttributeUtil au = new AttributeUtil();
        for(Node n : a_nodes) {
            if (m_metrics.containsKey(au.getName(n))) {
                setMetric(n, m_metrics.get(au.getName(n)));
            } else {
                System.out.println("No metric " + getName() + " found for: " + au.getName(n));
                setMetric(n, -1);
            }
        }
    }

    public void reassignMetric(Iterable<Node> a_nodes){
        // this will not change so...
    }
}
