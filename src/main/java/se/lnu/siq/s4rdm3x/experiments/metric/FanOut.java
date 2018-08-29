package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;
import se.lnu.siq.s4rdm3x.cmd.util.AttributeUtil;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;

public class FanOut extends Metric {

    @Override
    public String getName() {
        return "fanout";
    }

    @Override
    public void assignMetric(Graph a_g, HuGMe.ArchDef a_arch) {
        AttributeUtil au = new AttributeUtil();
        FanHelper fh = new FanHelper(a_g, a_arch);
        for(Node n : a_arch.getMappedNodes(a_g.getNodeSet())) {
            setMetric(n, fh.getFanOut(n));
        }
    }

    public void reassignMetric(Graph a_g, HuGMe.ArchDef a_arch) {
        // the fan out will not change so...
    }


}
