package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.ArrayList;

public class NBMapperManual extends NBMapper {

    public NBMapperManual(ArchDef a_arch, double [] a_initialDistribution) {
        super(a_arch, a_initialDistribution);
    }


    // in this version an orphan node does not have a mapping and no clustering
    @Override
    protected java.util.ArrayList<CNode> getOrphanNodes(CGraph a_g) {

        java.util.ArrayList<CNode> ret = new ArrayList<>();
        for (CNode n : a_g.getNodes()) {
            if (m_arch.getMappedComponent(n) == null && m_arch.getClusteredComponent(n) == null) {
                ret.add(n);
            }
        }

        return ret;
    }
}
