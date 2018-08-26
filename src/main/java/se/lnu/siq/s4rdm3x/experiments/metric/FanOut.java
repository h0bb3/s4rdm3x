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
        for(Node n : a_arch.getMappedNodes(a_g.getNodeSet())) {
            setMetric(n, getFanOut(n, au));
        }
    }

    public void reassignMetric(Graph a_g, HuGMe.ArchDef a_arch) {
        // the fan out will not change so...
    }

    public double getFanOut(Node a_n, AttributeUtil a_au) {
        double fanOut = 0;
        for (dmClass from : a_au.getClasses(a_n)) {
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
