package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.ArrayList;

public class HuGMeManual extends HuGMe {


    public HuGMeManual(double a_filterThreshold, double a_violationWeight, ArchDef a_arch) {
        super(a_filterThreshold, a_violationWeight, false, a_arch, null);
    }



    // in this version an orphan node does not have a mapping and no clustering
    /*@Override
    protected java.util.ArrayList<CNode> getOrphanNodes(CGraph a_g) {

        java.util.ArrayList<CNode> ret = new ArrayList<>();
        for (CNode n : a_g.getNodes()) {
            if (m_arch.getMappedComponent(n) == null && m_arch.getClusteredComponent(n) == null) {
                ret.add(n);
            }
        }

        return ret;
    }*/
}
