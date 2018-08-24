package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;

public abstract class Metric {
    private static String g_metricName = "metric";
    protected void setMetric(Node a_node, double a_metric) {
        a_node.setAttribute(g_metricName, a_metric);
    }
    public double getMetric(Node a_node) {
        return a_node.getAttribute(g_metricName);
    }

    public abstract String getName();

    public abstract void assignMetric(Graph a_g, HuGMe.ArchDef a_arch);
}
