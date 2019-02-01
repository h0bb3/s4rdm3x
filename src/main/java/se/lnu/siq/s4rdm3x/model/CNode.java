package se.lnu.siq.s4rdm3x.model;

import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class CNode {

    private int m_index;
    String m_name;
    private ArrayList<String> m_tags = new ArrayList<>();
    private ArrayList<dmClass> m_classes = new ArrayList<>();
    double[] m_attractions = null;
    private String m_mappedToComponent = "";

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

    public int getIndex() {
        return m_index;
    }




    CNode(String a_name, int a_index) {
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

    public int getDependencyCount(CNode a_to) {
        int count = 0;
        for (dmClass cFrom : m_classes) {
            for(dmDependency dFrom : cFrom.getDependencies()) {
                for (dmClass cTo : a_to.getClasses()) {
                    if (dFrom.getTarget() == cTo) {
                        count += dFrom.getCount();
                    }
                }
            }
        }

        return count;
    }

    public Iterable<dmDependency> getDependencies(CNode a_to) {
        ArrayList<dmDependency> ret = new ArrayList<>();
        for (dmClass cFrom : m_classes) {
            for(dmDependency dFrom : cFrom.getDependencies()) {
                for (dmClass cTo : a_to.getClasses()) {
                    if (dFrom.getTarget() == cTo) {
                        ret.add(dFrom);
                    }
                }
            }
        }

        return ret;
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

    public boolean containsClass(dmClass a_className) {
        return m_classes.contains(a_className);
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
