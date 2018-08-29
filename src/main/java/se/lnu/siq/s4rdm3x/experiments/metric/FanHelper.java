package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;
import se.lnu.siq.s4rdm3x.cmd.util.AttributeUtil;
import se.lnu.siq.s4rdm3x.cmd.util.FanInCache;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;

public class FanHelper {
    FanInCache m_fic;
    AttributeUtil m_au;
    public FanHelper(Graph a_g, HuGMe.ArchDef a_arch) {
        m_fic = new FanInCache(a_arch.getMappedNodes(a_g.getNodeSet()));
        m_au = new AttributeUtil();
    }

    double getFanIn(Node a_n) {
        return m_fic.getFanIn(a_n);
    }

    public double getFanOut(Node a_n) {
        double fanOut = 0;
        for (dmClass from : m_au.getClasses(a_n)) {
            fanOut += countDependenciesFrom(from);
        }

        return fanOut;
    }

    private double countDependenciesFrom(dmClass a_c) {
        double count = 0;
        // TODO: we should have some weight here

        for(dmDependency d : a_c.getDependencies()) {
            count += d.getCount();
        }

        return count;
    }
}
