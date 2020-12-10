package se.lnu.siq.s4rdm3x.model;

import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import weka.classifiers.functions.SGDText;
import weka.core.DenseInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

public class CNode {

    private int m_index;
    String m_name;
    private ArrayList<String> m_tags = new ArrayList<>();
    private ArrayList<dmClass> m_classes = new ArrayList<>();
    double[] m_attractions = null;


    private String m_mappedToComponent = ""; // This is the mapping (i.e. done by expert)
    private String m_clusteredToComponent = ""; // This is the clustering (e.g. done by technique)
    private String m_clusteringType = ""; // This is the type of clustering done, (automatic, initial or maual)

    public CNode(CNode a_node) {
        shallowCopy(a_node);
    }

    public void clearAttributes() {
        m_tags = new ArrayList<>();
        m_metrics = new MetricMap();
        m_attractions = null;
    }

    public String getLogicName() {
        for (dmClass c : getClasses()) {
            if (!c.isInner()) {
                return c.getName();
            }
        }

        return getFileName();
    }

    public String getLogicNameSimple() {
        String logicName = getLogicName();

        if (logicName.contains(".")) {
            return logicName.substring(logicName.lastIndexOf(".") + 1);
        } else {
            return logicName;
        }
    }

    public void shallowCopy(CNode a_node) {
        m_name = a_node.m_name;
        m_attractions = a_node.m_attractions;
        m_mappedToComponent = a_node.m_mappedToComponent;
        m_clusteredToComponent = a_node.m_clusteredToComponent;
        m_clusteringType = a_node.m_clusteringType;
        m_classes.addAll(a_node.m_classes);
        m_tags.addAll(a_node.m_tags);
    }



    public static class MetricMap extends HashMap<String, Double> {}

    private MetricMap m_metrics = new MetricMap();

    public String getTags() {
        return getTags(",");
    }

    public String getTags(String a_separator) {
        String ret = "";

        for (String tag : m_tags) {
            ret += tag + a_separator;
        }

        if (m_tags.size() > 0) {
            ret = ret.substring(0, ret.length() - a_separator.length());
        }

        return ret;
    }

    /*public String getId() {
        return m_id;
    }*/

    /*public int getIndex() {
        return m_index;
    }*/




    public CNode(String a_name, int a_index) {
        m_name = a_name;
        m_index = a_index;
    }

    public boolean matchesAnyPackageName(String a_package) {
        for (dmClass c : m_classes) {
            if (c.getFileName().contains(a_package)) {
                return true;
            }
        }

        return false;
    }

    public boolean matchesAnyClassName(Pattern a_pattern) {
        for (dmClass c : m_classes) {
            if (a_pattern.matcher(c.getName()).find()) {
                return true;
            }
        }
        return false;

    }

    public boolean hasAnyTag(String [] a_tags) {
        for (String tag : a_tags) {
            if (hasTag(tag)) {
                return true;
            }
        }
        return false;
    }

    public void setClustering(String a_componentName, String a_clusteringType) {
        m_clusteredToComponent = a_componentName;
        m_clusteringType = a_clusteringType;
    }

    public String getClusteringComponentName() {
        return m_clusteredToComponent;
    }

    public String getClusteringType() {
        return m_clusteringType;
    }

    public void setMapping(String a_componentName) {
        m_mappedToComponent = a_componentName;
    }

    public String getMapping() {
        return m_mappedToComponent;
    }

    public boolean hasTag(String a_tag) {
        for (String nodeTag : m_tags) {
            if (a_tag.compareTo(nodeTag) == 0) {
                return true;
            }
        }
        return false;
    }

    /*public boolean hasEdgeTag(String a_tag) {
        for(Edge e : m_n.getEachEdge()) {
            if (g_au.hasAnyTag(e, a_tag)) {
                return true;
            }
        }

        return false;
    }*/

    public void addTag(String a_tag) {
        for (String t :  m_tags) {
            if (t.contentEquals(a_tag)) {
                return;
            }
        }
        m_tags.add(a_tag);
    }

    public boolean hasDependency(CNode a_to) {
        for (dmClass cFrom : m_classes) {
            for(dmDependency dFrom : cFrom.getDependencies()) {
                for (dmClass cTo : a_to.getClasses()) {
                    if (dFrom.getTarget() == cTo) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static interface DependencyFilter {
        boolean filter(CNode a_from, dmDependency a_dep, CNode a_to);
    }
    public static class NoFilter implements DependencyFilter {
        public boolean filter(CNode a_from, dmDependency a_dep, CNode a_to) { return true; }
    }

    public static class CountFilter implements DependencyFilter {
        public int m_count = 0;
        public boolean filter(CNode a_from, dmDependency a_dep, CNode a_to) { m_count += a_dep.getCount(); return false; }
    }

    public int getDependencyCount(CNode a_to) {
        return getDependencyCount(a_to, new CountFilter());
    }

    public int getDependencyCount(CNode a_to, CountFilter a_filter) {
        getDependencies(a_to, a_filter);
        return a_filter.m_count;
    }

    public int getDependencyCount(Iterable<CNode> a_tos, CountFilter a_filter) {
        a_tos.forEach(n->{getDependencyCount(n, a_filter);});
        return a_filter.m_count;
    }

    public Iterable<dmDependency> getDependencies(Iterable<CNode> a_tos) {
        return getDependencies(a_tos, new NoFilter());
    }

    public Iterable<dmDependency> getDependencies(Iterable<CNode> a_tos, DependencyFilter a_filter) {
        return getDependencies(a_tos, new ArrayList<>(), a_filter);
    }

    public Iterable<dmDependency> getDependencies(Iterable<CNode> a_tos, List<dmDependency> a_result) {
        return getDependencies(a_tos, a_result, new NoFilter());
    }

    public Iterable<dmDependency> getDependencies(Iterable<CNode> a_tos, List<dmDependency> a_result, DependencyFilter a_filter) {
        for (CNode to : a_tos) {
            getDependencies(to, a_result, a_filter);
        }
        return a_result;
    }

    public Iterable<dmDependency> getDependencies(CNode a_to) {
        return getDependencies(a_to, new ArrayList<>());
    }

    public Iterable<dmDependency> getDependencies(CNode a_to, DependencyFilter a_filter) {
        return getDependencies(a_to, new ArrayList<>(), a_filter);
    }

    public Iterable<dmDependency> getDependencies(CNode a_to, List<dmDependency> a_result) {
        return getDependencies(a_to, a_result, new NoFilter());
    }

    public Iterable<dmDependency> getDependencies(CNode a_to, List<dmDependency> a_result, DependencyFilter a_filter) {

        for (dmClass cFrom : m_classes) {
            for(dmDependency dFrom : cFrom.getDependencies()) {
                for (dmClass cTo : a_to.getClasses()) {
                    if (dFrom.getTarget() == cTo && a_filter.filter(this, dFrom, a_to)) {
                        a_result.add(dFrom);
                    }
                }
            }
        }

        return a_result;
    }



    public String getFileName() {
        return m_classes.get(0).getFileName();
    }
    public String getName() { return m_name; }

    public Iterable<dmClass> getClasses() {
        return m_classes;
    }
    public int getClassCount() {
        return m_classes.size();
    }

    public boolean containsClass(dmClass a_class) {
        return m_classes.contains(a_class);
    }

    public void addClass(dmClass a_c) {

        m_classes.add(a_c);
    }

    public void removeTag(String a_tag) {
        m_tags.removeIf(t->t.contentEquals(a_tag));
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
