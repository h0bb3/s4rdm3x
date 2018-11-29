package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Node;

public abstract class Metric {

    public enum Id {
        Rand("Random", "rand"),
        BCCC("Bytecode Cyclomatic Complexity", "bccc"),
        BCInsr("Bytecode Instructions", "bcinstr"),
        CIn("Coupling In", "bci"),
        COut("Coupling Out", "bco"),
        Custom("Custom Metric", "custom"),
        FanIn("Fan In", "fanin"),
        FanOut("Fan Out", "fanout"),
        LCOMHS("Lack of Cohesion of Methods HS", "lcomhs"),
        LineCount("LineCount", "lines"),
        NOC("Number of Children", "noc"),
        NOF("Number of Fields", "nof"),
        NOM("Number of Methods", "nom"),
        NOP("Number of Parents", "nop"),
        Rank("Rank", "rank");


        private String m_longName;
        private String m_shortName;

        Id(String a_longName, String a_shortName) {
            m_longName = a_longName;
            m_shortName = a_shortName;
        }
    }

    private static String g_metricName = "metric";
    protected void setMetric(Node a_node, double a_metric) {
        a_node.setAttribute(g_metricName, a_metric);
    }
    public double getMetric(Node a_node) {
        return a_node.getAttribute(g_metricName);
    }

    public abstract String getName();

    public abstract void assignMetric(Iterable<Node> a_nodes);
    public abstract void reassignMetric(Iterable<Node> a_nodes);
}
