package se.lnu.siq.s4rdm3x.experiments.metric;

import se.lnu.siq.s4rdm3x.model.CNode;

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

    public void assignMetric(Iterable<CNode> a_nodes) {
        for(CNode n : a_nodes) {
            if (m_metrics.containsKey(n.getFileName())) {
                n.setMetric(getName(), m_metrics.get(n.getFileName()));
            } else {
                throw new IllegalArgumentException("No metric " + getName() + " found for: " + n.getFileName());
            }
        }
    }

    public void reassignMetric(Iterable<CNode> a_nodes){
        // this will not change so...
    }
}
