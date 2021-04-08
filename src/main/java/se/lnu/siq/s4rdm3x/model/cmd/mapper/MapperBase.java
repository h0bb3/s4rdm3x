package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates some basic mapper functionality
 */
public class MapperBase {

    public int m_manuallyMappedNodes = 0;
    public int m_failedMappings = 0;        // manual mappings that have have failed

    protected ArchDef m_arch;

    private ArrayList<CNode> m_autoClusteredOrphans = new ArrayList<>();

    protected void addAutoClusteredOrphan(OrphanNode a_node) {
        if (m_autoClusteredOrphans.contains(a_node.get())) {
            throw new IllegalArgumentException("Auto clustered orphan already added!");
        }
        // TODO: possibly make a copy of the node
        m_autoClusteredOrphans.add(a_node.get());
    }

    public Iterable<CNode> getAutoClusteredNodes() {
        return m_autoClusteredOrphans;
    }

    public int getAutoClusteredOrphanCount() {
        return m_autoClusteredOrphans.size();
    }

    private boolean m_doManualMapping;

    protected MapperBase(boolean a_doManualMapping, ArchDef a_arch) {
        m_doManualMapping = a_doManualMapping;
        m_arch = a_arch;
    }

    public void clearAutoClusterings() {
        m_autoClusteredOrphans.clear();
    }


    public static class DependencyWeights {
        Map<dmDependency.Type, Double> m_weights = new HashMap<>();

        public DependencyWeights(double a_initialWeight) {
            for(dmDependency.Type e : dmDependency.Type.values()) {
                m_weights.put(e, a_initialWeight);
            }
        }

        public DependencyWeights(DependencyWeights a_copy) {
            for(dmDependency.Type e : dmDependency.Type.values()) {
                m_weights.put(e, a_copy.getWeight(e));
            }
        }

        public boolean hasWeight(dmDependency.Type a_dep) {
            return m_weights.containsKey(a_dep);
        }

        public double getWeight(dmDependency.Type a_dep) {
            return m_weights.get(a_dep);
        }

        public void setWeight(dmDependency.Type a_dep, double a_w) {
            m_weights.replace(a_dep, a_w);
        }
    }

    protected static class NodeBase {
        protected CNode m_node;
        protected NodeBase(CNode a_node) {
            m_node = a_node;
        }

        public void setAttractions(double[] a_attractions) {
            m_node.setAttractions(a_attractions);
        }

        public double[] getAttractions() {
            return m_node.getAttractions();
        }

        public CNode get() {
            return m_node;
        }

        public String getMapping() {
            return m_node.getMapping();
        }

        public int getDependencyCount(NodeBase a_otherNode) {
            return m_node.getDependencyCount(a_otherNode.get());
        }

        public Iterable<dmDependency> getDependencies(CNode a_otherNode) {
            return m_node.getDependencies(a_otherNode);
        }

        public double getDependencyCount(NodeBase a_n, DependencyWeights a_dw, boolean a_countFileDeps) {
            double ret = 0;

            for (dmDependency d : m_node.getDependencies(a_n.get())) {
                if (a_countFileDeps || !d.getType().isFileBased) {
                    ret += d.getCount() * a_dw.getWeight(d.getType());
                }
            }
            return ret;
        }
    }

    public static class OrphanNode extends NodeBase {

        OrphanNode(CNode a_node, ArchDef a_arch) {
            super(a_node);
            if (a_arch.getMappedComponent(m_node) == null) {
                throw new IllegalArgumentException("Orphan Node has no mapping");
            }
            if (a_arch.getClusteredComponent(m_node) != null) {
                throw new IllegalArgumentException("Clustered node is trying to pass as Orphan");
            }
        }


    }

    public static class ClusteredNode extends NodeBase {

        ArchDef m_arch;
        public ClusteredNode(CNode a_node, ArchDef a_arch) {
            super(a_node);
            m_arch = a_arch;
            if (a_arch.getMappedComponent(m_node) == null) {
                throw new IllegalArgumentException("Clustered Node has no mapping");
            }
            if (a_arch.getClusteredComponent(m_node) == null) {
                throw new IllegalArgumentException("Orphan node is trying to pass as Clustered");
            }
        }

        @Override
        public boolean equals(Object a_obj) {
            if (a_obj instanceof ClusteredNode) {
                return ((ClusteredNode)a_obj).get() == this.get();
            }
            if (a_obj instanceof CNode) {
                return get() == a_obj;
            }
            return this == a_obj;
        }

        public ArchDef.Component getClusteredComponent() {
            return m_arch.getClusteredComponent(m_node);
        }

        public String getClusteringComponentName() {
            return getClusteredComponent().getName();
        }
    }

    /**
     * @param a_g contains all nodes (orphans and the initial set)
     * @return a shuffled list of the clustered nodes i.e. the initial set.
     */
    protected ArrayList<ClusteredNode> getInitiallyMappedNodes(CGraph a_g) {
        ArrayList<ClusteredNode> ret = new ArrayList<>();
        for (CNode n : m_arch.getMappedNodes(a_g.getNodes())) {
            if (m_arch.getClusteredComponent(n) != null) {
                ret.add(new ClusteredNode(n, m_arch));
            }
        }

        Collections.shuffle(ret);

        return ret;
    }

    /**
     * @param a_g contains all nodes (orphans and the initial set)
     * @return a shuffled list of the orphan nodes.
     */
    protected ArrayList<OrphanNode> getOrphanNodes(CGraph a_g) {

        ArrayList<OrphanNode> ret = new ArrayList<>();
        for (CNode n :  m_arch.getMappedNodes(a_g.getNodes())) {
            if (m_arch.getClusteredComponent(n) == null) {
                ret.add(new OrphanNode(n, m_arch));
            }
        }

        Collections.shuffle(ret);

        return ret;
    }

    /**
     * Cluster a node to an architectural module based on if the highest attraction value is over a threshold, and higher than the next highest attraction value (i.e. no equal attractions).
     * @param a_orphanNode The node to possibly cluster to, it needs to have the attractions set using the same indexing as the components in a_archDef, this node will get a clustering of type Automatic
     * @param a_archDef The components to cluster to
     * @param a_clusteringThreshold threshold that needs to be overcome, the value is mapper specific, e.g. if using probabilities 0-1 would be appropriate
     * @return the component that the node was clustered to, or null if it was not clustered.
     */
    public ArchDef.Component doAutoMappingAbsThreshold(OrphanNode a_orphanNode, ArchDef a_archDef, double a_clusteringThreshold) {
        double attractions[] = a_orphanNode.getAttractions();
        int[] maxes= getMaxIndices(attractions);
        int maxIx = maxes[0];
        int maxIx2 = maxes[1];

        if (attractions[maxIx] > a_clusteringThreshold && attractions[maxIx] > attractions[maxIx2]) {
            a_archDef.getComponent(maxIx).clusterToNode(a_orphanNode.get(), ArchDef.Component.ClusteringType.Automatic);
            return a_archDef.getComponent(maxIx);
        }

        return null;

    }

    /**
     * Cluster a node to an architectural module based on if the highest attraction value is a_clusteringThreshold times higher than the next highest attraction value.
     * @param a_orphanNode The node to possibly cluster to, it needs to have the attractions set using the same indexing as the components in a_archDef, this node will get a clustering of type Automatic
     * @param a_archDef The components to cluster to
     * @param a_clusteringThreshold the multiplier of the next highest threshold. Values under 1 would not make sense here.
     * @return the component that the node was clustered to, or null if it was not clustered.
     */
    public ArchDef.Component doAutoMapping(OrphanNode a_orphanNode, ArchDef a_archDef, double a_clusteringThreshold) {
        double attractions[] = a_orphanNode.getAttractions();
        int[] maxes= getMaxIndices(attractions);
        int maxIx = maxes[0];
        int maxIx2 = maxes[1];

        if (attractions[maxIx] > attractions[maxIx2] * a_clusteringThreshold) {
            a_archDef.getComponent(maxIx).clusterToNode(a_orphanNode.get(), ArchDef.Component.ClusteringType.Automatic);
            return a_archDef.getComponent(maxIx);
        }

        return null;

    }

    public int[] getMaxIndices(double[] a_values) {
        int[] ret = new int[] {0, 1};
        for (int cIx = 1; cIx < a_values.length; cIx++) {
            if (a_values[ret[0]] < a_values[cIx]) {
                ret[1] = ret[0];
                ret[0] = cIx;
            } else if (a_values[ret[1]] < a_values[cIx]) {
                ret[1] = cIx;
            }
        }

        return ret;
    }

    /**
     * Cluster a node to an architectural module based on if the highest attraction value is the only value one SD over the mean of all attraction values or the only value above the mean. This is the approach described in the HuGMe technique
     * @param a_orphanNode The node to possibly cluster to, it needs to have the attractions set using the same indexing as the components in a_archDef, this node will get a clustering of type Automatic
     * @param a_archDef The components to cluster to
     * @return the component that the node was clustered to, or null if it was not clustered.
     */
    public ArchDef.Component doAutoMapping(OrphanNode a_orphanNode, ArchDef a_archDef) {
        double attractions[] = a_orphanNode.getAttractions();
        double mean = stats.mean(attractions);
        double sd = stats.stdDev(attractions, mean);

        ArrayList<Integer> greaterThanMeanPlusSD = new ArrayList<>();
        ArrayList<Integer> greaterThanMean = new ArrayList<>();

        for(int i = 0; i < a_archDef.getComponentCount(); i++) {
            if (attractions[i] >= mean) {
                greaterThanMean.add(i);
            }
            if (attractions[i] - mean > sd) {   // could also stated as > mean + sd (i.e. the same)
                greaterThanMeanPlusSD.add(i);
            }
        }
        if (greaterThanMeanPlusSD.size() == 1) {
            ArchDef.Component clusteredComponent = a_archDef.getComponent(greaterThanMeanPlusSD.get(0));
            clusteredComponent.clusterToNode(a_orphanNode.get(), ArchDef.Component.ClusteringType.Automatic);
            return clusteredComponent;
        } else if (greaterThanMean.size() == 1) {
            ArchDef.Component clusteredComponent = a_archDef.getComponent(greaterThanMean.get(0));
            clusteredComponent.clusterToNode(a_orphanNode.get(), ArchDef.Component.ClusteringType.Automatic);
            return clusteredComponent;
        }

        return null;
    }


    public boolean doManualMapping() {
        return m_doManualMapping;
    }


    /**
     * Performs a correct manual mapping, however a mapping is considered a failure if the advice (attraction) to a user would not be regarded as good (see return).
     * @param a_n The orphan node to manually map. Attractions need to be set in this node, the mapping is ALWAYS done to the correct module.
     * @param a_arch The components used in the mapping.
     * @return true if the attraction of the correct module is higher than the median attraction (i.e. the module was presented in the upper half of a list of modules sorted on attraction).
     */
    protected boolean manualMapping(OrphanNode a_n, ArchDef a_arch) {
        ArchDef.Component targetC = a_arch.getMappedComponent(a_n.get());
        double[] attractions = a_n.getAttractions();
        for(int i = 0; i < a_arch.getComponentCount(); i++){
            if (a_arch.getComponent(i) == targetC) {
                boolean clustered = attractions[i] > stats.medianUnsorted(attractions);
                ArchDef.Component.ClusteringType type = clustered ? ArchDef.Component.ClusteringType.Manual : ArchDef.Component.ClusteringType.ManualFailed;
                targetC.clusterToNode(a_n.get(), type);
                m_manuallyMappedNodes++;
                if (!clustered) {
                    m_failedMappings++;
                }
                return true;
            }
        }

        return false;
    }
}
