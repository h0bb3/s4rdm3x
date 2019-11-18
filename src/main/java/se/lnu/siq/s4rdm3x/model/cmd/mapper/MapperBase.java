package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.stats;

import java.util.ArrayList;

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




    public static class OrphanNode {
        CNode m_node;
        OrphanNode(CNode a_node, ArchDef a_arch) {
            m_node = a_node;
            if (a_arch.getMappedComponent(m_node) == null) {
                throw new IllegalArgumentException("Orphan Node has no mapping");
            }
            if (a_arch.getClusteredComponent(m_node) != null) {
                throw new IllegalArgumentException("Clustered node is trying to pass as Orphan");
            }
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

        public int getDependencyCount(OrphanNode a_otherNode) {
            return m_node.getDependencyCount(a_otherNode.get());
        }

        public int getDependencyCount(ClusteredNode a_n) {
            return m_node.getDependencyCount(a_n.get());
        }
    }

    public static class ClusteredNode {
        CNode m_node;
        ArchDef m_arch;
        public ClusteredNode(CNode a_node, ArchDef a_arch) {
            m_node = a_node;
            m_arch = a_arch;
            if (a_arch.getMappedComponent(m_node) == null) {
                throw new IllegalArgumentException("Clustered Node has no mapping");
            }
            if (a_arch.getClusteredComponent(m_node) == null) {
                throw new IllegalArgumentException("Orphan node is trying to pass as Clustered");
            }
        }

        public String getClusteringComponentName() {
            return m_node.getClusteringComponentName();
        }

        public Iterable<dmDependency> getDependencies(CNode a_to) {
            return m_node.getDependencies(a_to);
        }

        public CNode get() {
            return m_node;
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

        public int getDependencyCount(OrphanNode a_n) {
            return m_node.getDependencyCount(a_n.get());
        }
    }

    protected ArrayList<ClusteredNode> getInitiallyMappedNodes(CGraph a_g) {
        ArrayList<ClusteredNode> ret = new ArrayList<>();
        for (CNode n : m_arch.getMappedNodes(a_g.getNodes())) {
            if (m_arch.getClusteredComponent(n) != null) {
                ret.add(new ClusteredNode(n, m_arch));
            }
        }

        return ret;
    }

    protected ArrayList<OrphanNode> getOrphanNodes(CGraph a_g) {

        ArrayList<OrphanNode> ret = new ArrayList<>();
        for (CNode n :  m_arch.getMappedNodes(a_g.getNodes())) {
            if (m_arch.getClusteredComponent(n) == null) {
                ret.add(new OrphanNode(n, m_arch));
            }
        }

        return ret;
    }

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
