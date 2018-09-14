package se.lnu.siq.s4rdm3x.cmd;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.util.AttributeUtil;
import se.lnu.siq.s4rdm3x.cmd.util.FanInCache;
import se.lnu.siq.s4rdm3x.cmd.util.Selector;
import se.lnu.siq.s4rdm3x.stats;

import java.util.ArrayList;

public class HuGMe {
    public static class ArchDef {

        public static class Component {
            private String m_name;
            private ArrayList<Component> m_allowedDependenciesTo;

            public enum ClusteringType {
                None,
                Manual,
                Automatic,
                Forced,
                Initial
            }

            public ClusteringType getClusteringType(Node a_n) {
                AttributeUtil au = new AttributeUtil();
                if (au.hasAnyTag(a_n, ClusteringType.Manual.toString())) {
                    return ClusteringType.Manual;
                }
                if (au.hasAnyTag(a_n, ClusteringType.Automatic.toString())) {
                    return ClusteringType.Automatic;
                }
                if (au.hasAnyTag(a_n, ClusteringType.Forced.toString())) {
                    return ClusteringType.Forced;
                }
                if (au.hasAnyTag(a_n, ClusteringType.Initial.toString())) {
                    return ClusteringType.Initial;
                }

                return ClusteringType.None;
            }

            public Component(String a_name) {
                m_name = a_name;
                m_allowedDependenciesTo = new ArrayList<>();
            }

            public void addDependencyTo(Component a_component) {
                if (a_component != this) {
                    m_allowedDependenciesTo.add(a_component);
                }
            }

            public boolean allowedDependency(Component a_component) {
                return m_allowedDependenciesTo.indexOf(a_component) >= 0;
            }

            public String getClusterName() {
                return m_name + "_c";
            }

            public String getName() {
                return m_name;
            }

            public void removeClustering(Node a_n, AttributeUtil a_au) {
                a_au.removeTag(a_n, getClusterName());
                for (ClusteringType ct : ClusteringType.values()) {
                    a_au.removeTag(a_n, ct.toString());
                }
            }

            public void clusterToNode(Node a_n, ClusteringType a_ct) {
                AttributeUtil au = new AttributeUtil();
                tagNode(a_n, getClusterName(), au);
                tagNode(a_n, a_ct.toString(), au);
            }

            public void unmap(Node a_n) {
                AttributeUtil au = new AttributeUtil();
                au.removeTag(a_n, m_name);
            }

            public void mapToNode(Node a_n) {
                AttributeUtil au = new AttributeUtil();
                tagNode(a_n, m_name, au);
            }

            public boolean isMappedTo(Node a_n, AttributeUtil a_au) {
                return a_au.hasAnyTag(a_n, m_name);
            }

            public boolean isMappedTo(Node a_n) {
                AttributeUtil au = new AttributeUtil();
                return isMappedTo(a_n, au);
            }

            private void tagNode(Node a_n, String a_tag, AttributeUtil a_au) {
                a_au.addTag(a_n, a_tag);
            }

            public void mapToNodes(Graph a_g, Selector.ISelector a_selector) {
                AttributeUtil au = new AttributeUtil();
                for (Node n : a_g) {
                    if (a_selector.isSelected(n)) {
                        tagNode(n, m_name, au);
                    }
                }
            }
        }

        private ArrayList<Component> m_components = new ArrayList<>();

        public Component addComponent(String a_componentName)
        {
            Component c = new Component(a_componentName);
            m_components.add(c);
            return c;
        }

        protected String[] getComponentNames() {
            String [] ret = new String[m_components.size()];

            for (int i = 0; i < ret.length; i++) {
                ret[i] = m_components.get(i).getName();
            }
            return ret;
        }

        protected String[] getClusterNames() {
            String [] ret = new String[m_components.size()];

            for (int i = 0; i < ret.length; i++) {
                ret[i] = m_components.get(i).getClusterName();
            }
            return ret;
        }

        public int getComponentCount() {
            return m_components.size();
        }

        public Component getComponent(int a_ix) {
            return m_components.get(a_ix);
        }
        public Component getComponent(String a_name) {
            for(Component c : m_components) {
                if (c.getName().compareTo(a_name) == 0) {
                    return c;
                }
            }

            return null;
        }

        public Iterable<Node> getMappedNodes(Iterable<Node> a_nodes) {
            AttributeUtil au = new AttributeUtil();
            return au.getNodesWithAnyTag(a_nodes, getComponentNames());
        }

        public void cleanNodeClusters(Iterable<Node> a_nodes) {
            AttributeUtil au = new AttributeUtil();
            for (Node n : a_nodes) {
                Component c = getClusteredComponent(n);
                while (c!= null) {
                    c.removeClustering(n, au);
                    c = getClusteredComponent(n);
                }
            }
        }

        public Component getClusteredComponent(Node a_n) {
            AttributeUtil au = new AttributeUtil();
            for(Component c : m_components) {
                if (au.hasAnyTag(a_n, c.getClusterName())) {
                    return c;
                }
            }
            return null;
        }

        public Component getMappedComponent(Node a_n) {
            AttributeUtil au = new AttributeUtil();
            for(Component c : m_components) {
                if (au.hasAnyTag(a_n, c.getName())) {
                    return c;
                }
            }
            return null;
        }

        public int getMappedNodeCount(Iterable<Node> a_nodes) {
            AttributeUtil au = new AttributeUtil();
            return au.getNodesWithAnyTag(a_nodes, getComponentNames()).size();
        }

        public int getClusteredNodeCount(Iterable<Node> a_nodes) {
            AttributeUtil au = new AttributeUtil();
            return au.getNodesWithAnyTag(a_nodes, getClusterNames()).size();
        }

        public Iterable<Component> getComponents() {
            return m_components;
        }
    }

    private double m_filterThreshold;   // omega in paper
    private double m_violationWeight;   // psi in paper
    private FanInCache m_fic;

    private boolean m_doManualMapping;

    private ArchDef m_arch;

    public ArrayList<Node> m_clusteredElements;

    public int m_consideredNodes = 0;           // all nodes that pass the filter
    public int m_automaticallyMappedNodes = 0;
    public int m_manuallyMappedNodes = 0;
    public int m_failedMappings = 0;
    public int m_autoWrong = 0;
    public int m_unmappedNodesFromStart = 0;
    public int m_mappedNodesFromStart = 0;

    public HuGMe(double a_filterThreshold, double a_violationWeight, boolean a_doManualMapping, ArchDef a_arch, FanInCache a_fic) {
        m_violationWeight = a_violationWeight;
        m_filterThreshold = a_filterThreshold;
        m_doManualMapping = a_doManualMapping;
        m_arch = a_arch;
        m_fic = a_fic;
    }




    public void run(Graph a_g) {
        AttributeUtil au = new AttributeUtil();

        final String [] originalMappingTags = m_arch.getComponentNames();

        m_clusteredElements = new ArrayList<>();

        // all considered nodes to unmapped
        java.util.ArrayList<Node> unmapped = new ArrayList<>();
        for (Node n : a_g.getEachNode()) {
            if (m_arch.getMappedComponent(n) != null && m_arch.getClusteredComponent(n) == null) {
                unmapped.add(n);
            }
        }

        // create the current clusters
        java.util.ArrayList<java.util.ArrayList<Node>> clusters = new ArrayList<>();
        for(int i = 0; i < m_arch.getComponentCount(); i++) {
            ArrayList<Node> c = new ArrayList<>();
            clusters.add(c);
            HuGMe.ArchDef.Component targetComponent = m_arch.getComponent(i);

            for(Node n : a_g.getNodeSet()) {
                if (m_arch.getClusteredComponent(n) == targetComponent) {
                    c.add(n);
                }
            }
            m_mappedNodesFromStart += c.size();
        }

        m_unmappedNodesFromStart = unmapped.size();

        java.util.ArrayList<Node> candidates = new ArrayList<>(unmapped);
        for (Node n : unmapped) {
            // count all dependencies to this class from all other unmapped classes
            int toMappedCountC = 0, totalCountC = 0;
            for (Node otherNode : unmapped) {
                if (n != otherNode) {
                    totalCountC += m_fic.getFanIn(n, otherNode);
                    totalCountC += m_fic.getFanIn(otherNode, n);
                }
            }

            for (ArrayList<Node> cluster : clusters) {
                for (Node nMapped : cluster) {

                    double fromClustered = m_fic.getFanIn(nMapped, n);
                    double toClustered = m_fic.getFanIn(n, nMapped);
                    toMappedCountC += toClustered;
                    toMappedCountC += fromClustered;
                    totalCountC += fromClustered;
                    totalCountC += toClustered;
                }
            }

            double ratio = 0.0;
            if (totalCountC > 0 && toMappedCountC > 0) {
                ratio = (double) toMappedCountC / (double) totalCountC;
            } else if (toMappedCountC > 0) {
                ratio = 1.0;
            }
            
            if (ratio < m_filterThreshold) {
                candidates.remove(n);
            }
        }

        m_consideredNodes = candidates.size();

        // 3 Count the attraction to the clusters, there will be one sum for each cluster
        for (Node n : candidates) {
            double attractions[] = new double[m_arch.getComponentCount()];
            for (int i = 0; i < m_arch.getComponentCount(); i++) {
                // TODO: Implement weights for different types of relations
                //attractions[i] = CountAttract(n, clusters.get(i));
                attractions[i] = CountAttractP(n, i, clusters);
            }
            n.setAttribute("attractions", attractions);
        }

        // 4 Find 2 candidate sets of attractions
        //      First set is based on >= mean of all attractions
        //      Second set is based on > standard deviation of all attractions

        for (Node n : candidates) {
            double attractions[] = n.getAttribute("attractions");
            double mean = stats.mean(attractions);
            double sd = stats.stdDev(attractions, mean);

            ArrayList<Integer> c1 = new ArrayList<>();
            ArrayList<Integer> c2 = new ArrayList<>();

            for(int i = 0; i < m_arch.getComponentCount(); i++) {
                if (attractions[i] >= mean) {
                    c2.add(i);
                }
                if (attractions[i] - mean > sd) {
                    c1.add(i);
                }
            }


            ArchDef.Component mappedC = m_arch.getMappedComponent(n);

            if (c1.size() == 1) {
                //au.addTag(n, clusterTags[c1.get(0)]);

                //Sys.out.println("Clustered to: " + g_clusterTags[c1.get(0)]);
                //au.addTag(n, ArchDef.Component.ClusteringType.Automatic.toString());
                ArchDef.Component clusteredComponent = m_arch.getComponent(c1.get(0));
                clusteredComponent.clusterToNode(n, ArchDef.Component.ClusteringType.Automatic);
                m_clusteredElements.add(n);
                if (mappedC != clusteredComponent) {
                    m_autoWrong++;
                }
                m_automaticallyMappedNodes++;
            } else if (c2.size() == 1) {
                ArchDef.Component clusteredComponent = m_arch.getComponent(c2.get(0));
                clusteredComponent.clusterToNode(n, ArchDef.Component.ClusteringType.Automatic);
                //Sys.out.println("Clustered to: " + g_clusterTags[c2.get(0)]);
                //au.addTag(n, ArchDef.Component.ClusteringType.Automatic.toString());
                m_clusteredElements.add(n);
                if (mappedC != clusteredComponent) {
                    m_autoWrong++;
                }
                m_automaticallyMappedNodes++;
            } else if (m_doManualMapping) {


                // we always map to the correct cluster using the oracle
                // we count the advice as a fail if the attraction is below the median attraction of the clusters
                // this is possibly more correct in relation to the paper
                boolean clustered = false;
                ArchDef.Component targetC = m_arch.getMappedComponent(n);
                for(int i = 0; i < m_arch.getComponentCount(); i++){
                    if (m_arch.getComponent(i) == targetC) {
                        targetC.clusterToNode(n, ArchDef.Component.ClusteringType.Manual);
                        clustered = attractions[i] > stats.medianUnsorted(attractions);
                        m_manuallyMappedNodes++;
                        break;
                    }
                }







                // this is the old conservative manual mapping
                // no clear answer so we must ask the oracle...
                // i.e. if the original mapping is present in one of the available options...
                /*boolean clustered = false;
                if (c1.size() > 0) {
                    for(Integer i : c1) {
                        if (mappedC == m_arch.getComponent(i)) {
                            mappedC.clusterToNode(n, ArchDef.Component.ClusteringType.Manual);
                            clustered = true;
                            //Sys.out.println("Clustered by Oracle to: " + g_clusterTags[i]);
                            m_manuallyMappedNodes++;
                            break;
                        }
                    }
                }
                if (!clustered && c2.size() > 0) {
                    for (Integer i : c2) {
                        if (mappedC == m_arch.getComponent(i)) {
                            mappedC.clusterToNode(n, ArchDef.Component.ClusteringType.Manual);
                            //Sys.out.println("Clustered by Oracle to: " + g_clusterTags[i]);
                            m_manuallyMappedNodes++;
                            clustered = true;
                            break;
                        }
                    }
                }*/


                if (clustered == false) {
                    //Sys.out.println("No attraction... this one is dead...");
                    m_failedMappings++;
                    // we force a mapping
                    /*for(int i =0; i < g_originalMappingTags.length; i++) {
                        if (au.hasAnyTag(n, g_originalMappingTags[i])) {
                            au.addTag(n, g_clusterTags[i]);
                            au.addTag(n, "manual");
                            au.addTag(n, "forced");
                        }
                    }*/
                }
            }
        }
    }


    private double CountAttractP(Node a_node, int a_cluster, ArrayList<ArrayList<Node>> a_clusters) {
        double overall = 0;
        double toOthers = 0;

        for (int i = 0; i < a_clusters.size(); i++) {
            final double allowedWeight = m_violationWeight;
            final double violationWeight = 1.0;
            double weightFrom, weightTo;

            if (i == a_cluster) {
                // internal dependencies are of course allowed
                weightFrom = 1.0f;
                weightTo = 1.0f;
            } else {
                ArchDef.Component from = m_arch.getComponent(a_cluster);
                ArchDef.Component to = m_arch.getComponent(i);
                // dependencies from a_cluster to other clusters (i) can incur violations
                if (!from.allowedDependency(to)) {
                    // violations have the highest weight
                    weightFrom = violationWeight;
                } else {
                    weightFrom = allowedWeight;
                }

                // dependencies from other clusters (i) to a_cluster can incur violations
                if (!to.allowedDependency(from)) {
                    weightTo = violationWeight;
                } else {
                    weightTo = allowedWeight;
                }

                toOthers += CountAttract(a_node, a_clusters.get(i), weightFrom, weightTo);
            }

            overall += CountAttract(a_node, a_clusters.get(i), 1.0, 1.0);
        }

        return overall - toOthers;
    }

    private double CountAttract(Node a_node, Iterable<Node> a_cluster, double a_weightFromNode, double a_weightFromCluster) {
        AttributeUtil au = new AttributeUtil();
        double count = 0;

        double cCount = 0;
        for (Node nTo:a_cluster) {
            cCount += m_fic.getFanIn(nTo, a_node) * a_weightFromNode;
            cCount += m_fic.getFanIn(a_node, nTo) * a_weightFromCluster;
        }

        count = cCount;

        return count;
    }

}
