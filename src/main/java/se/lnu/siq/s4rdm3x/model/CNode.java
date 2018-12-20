package se.lnu.siq.s4rdm3x.model;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.metrics.ComputeMetrics;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class CNode {



    public static class MetricMap extends HashMap<String, Double> {}

    private MetricMap m_metrics = new MetricMap();

    private Node m_n;

    private static AttributeUtil g_au = new AttributeUtil();

    double[] m_attractions = null;


    CNode(Node a_node) {
        m_n = a_node;
    }

    public boolean matchesAnyPackageName(String a_package) {
        for (dmClass c : g_au.getClasses(m_n)) {
            if (c.getFileName().contains(a_package)) {
                return true;
            }
        }

        return false;
    }

    public boolean matchesAnyClassName(Pattern a_pattern) {
        for (dmClass c : g_au.getClasses(m_n)) {
            if (a_pattern.matcher(c.getName()).find()) {
                return true;
            }
        }
        return false;

    }

    public boolean hasAnyTag(String a_tag) {
        return g_au.hasAnyTag(m_n, a_tag);
    }

    public boolean hasEdgeTag(String a_tag) {
        for(Edge e : m_n.getEachEdge()) {
            if (g_au.hasAnyTag(e, a_tag)) {
                return true;
            }
        }

        return false;
    }

    public void addTag(String a_tag) {
        g_au.addTag(m_n, a_tag);
    }

    public boolean hasDependency(CNode a_to) {
        for (dmClass cFrom : g_au.getClasses(m_n)) {
            for(dmDependency dFrom : cFrom.getDependencies()) {
                for (dmClass cTo : g_au.getClasses(a_to.m_n)) {
                    if (dFrom.getTarget() == cTo) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public int getDependencyCount(CNode a_to) {
        int count = 0;
        for (dmClass cFrom : g_au.getClasses(m_n)) {
            for(dmDependency dFrom : cFrom.getDependencies()) {
                for (dmClass cTo : g_au.getClasses(a_to.m_n)) {
                    if (dFrom.getTarget() == cTo) {
                        count++;
                    }
                }
            }
        }

        return count;
    }

    public Iterable<dmDependency> getDependencies(CNode a_to) {
        ArrayList<dmDependency> ret = new ArrayList<>();
        for (dmClass cFrom : g_au.getClasses(m_n)) {
            for(dmDependency dFrom : cFrom.getDependencies()) {
                for (dmClass cTo : g_au.getClasses(a_to.m_n)) {
                    if (dFrom.getTarget() == cTo) {
                        ret.add(dFrom);
                    }
                }
            }
        }

        return ret;
    }

    public String getFileName() {
        return g_au.getClasses(m_n).get(0).getFileName();
    }

    public Iterable<dmClass> getClasses() {
        return g_au.getClasses(m_n);
    }
    public int getClassCount() {
        return g_au.getClasses(m_n).size();
    }

    public void addClass(dmClass a_c) {
        g_au.addClass(m_n, a_c);
    }

    public void removeTag(String a_tag) {
        g_au.removeTag(m_n, a_tag);
    }

    public boolean hasAnyTag(String[] a_tags) {
        for (String tag : a_tags) {
            if (hasAnyTag(tag)) {
                return true;
            }
        }

        return false;
    }

    public void setAttractions(double[] a_attractions) {
        m_attractions = a_attractions;
    }

    public double[] getAttractions() {
        return m_attractions;
    }

    public void setMetric(String a_metric, double a_value) {
        m_metrics.put(a_metric, a_value);
    }

    public double getMetric(String a_metric) {
        return m_metrics.get(a_metric);
    }

    public MetricMap getMetricMap() {
        return m_metrics;
    }

    public boolean isSpecializationOf(CNode a_source) {
        for (dmDependency d : getDependencies(a_source)) {
            if (d.getType() == dmDependency.Type.Extends || d.getType() == dmDependency.Type.Implements) {
                return true;
            }
        }

        return false;
    }
}
