package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.ArrayList;

public class NBMapperManual extends NBMapper {

    public NBMapperManual(ArchDef a_arch, boolean a_doUseCDA, boolean a_doUseNodeText, boolean a_doUseNodeName, boolean a_doUseArchComponentName, int a_minWordLength, double [] a_initialDistribution) {
        // TODO: fixme by adding parameters
        super(a_arch, false, a_doUseCDA, a_doUseNodeText, a_doUseNodeName, a_doUseArchComponentName, a_minWordLength, a_initialDistribution);
    }


    // in this version an orphan node does not have a mapping and no clustering
    /*@Override
    protected java.util.ArrayList<CNode> getOrphanNodes(CGraph a_g) {

        java.util.ArrayList<CNode> ret = new ArrayList<>();
        for (CNode n : a_g.getNodes()) {
            //if (m_arch.getMappedComponent(n) == null && m_arch.getClusteredComponent(n) == null) {
                ret.add(n);
            //}
        }

        return ret;
    }*/
}
