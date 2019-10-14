package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.stats;

import java.util.ArrayList;

public class MapperBase {

    public int m_manuallyMappedNodes = 0;
    public int m_failedMappings = 0;        // manual mappings that have have failed

    protected ArchDef m_arch;

    private ArrayList<CNode> m_autoClusteredOrphans = new ArrayList<>();

    protected void addAutoClusteredOrphan(CNode a_node) {
        if (m_autoClusteredOrphans.contains(a_node)) {
            throw new IllegalArgumentException("Auto clustered orphan already added!");
        }
        // TODO: possibly make a copy of the node
        m_autoClusteredOrphans.add(a_node);
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

    protected java.util.ArrayList<CNode> getOrphanNodes(CGraph a_g) {

        java.util.ArrayList<CNode> ret = new ArrayList<>();
        for (CNode n :  m_arch.getMappedNodes(a_g.getNodes())) {
            if (m_arch.getClusteredComponent(n) == null) {
                ret.add(n);
            }
        }

        return ret;
    }

    protected java.util.ArrayList<CNode> getInitiallyMappedNodes(CGraph a_g) {
        java.util.ArrayList<CNode> ret = new ArrayList<>();
        for (CNode n : m_arch.getMappedNodes(a_g.getNodes())) {
            if (m_arch.getClusteredComponent(n) != null) {
            //if (m_arch.getClusteredComponent(n) != null && m_arch.getClusteredComponent(n).getClusteringType(n) == ArchDef.Component.ClusteringType.Initial) {
                ret.add(n);
            }
        }

        return ret;
    }


    public boolean doManualMapping() {
        return m_doManualMapping;
    }

    protected boolean manualMapping(CNode a_n, ArchDef a_arch) {
        ArchDef.Component targetC = a_arch.getMappedComponent(a_n);
        double[] attractions = a_n.getAttractions();
        for(int i = 0; i < a_arch.getComponentCount(); i++){
            if (a_arch.getComponent(i) == targetC) {
                boolean clustered = attractions[i] > stats.medianUnsorted(attractions);
                ArchDef.Component.ClusteringType type = clustered ? ArchDef.Component.ClusteringType.Manual : ArchDef.Component.ClusteringType.ManualFailed;
                targetC.clusterToNode(a_n, type);
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
