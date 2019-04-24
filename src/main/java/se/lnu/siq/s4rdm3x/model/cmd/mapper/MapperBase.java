package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.stats;

public class MapperBase {

    public int m_manuallyMappedNodes = 0;
    public int m_failedMappings = 0;        // manual mappings that have have failed

    private boolean m_doManualMapping;

    protected MapperBase(boolean a_doManualMapping) {
        m_doManualMapping = a_doManualMapping;
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
