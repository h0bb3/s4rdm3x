package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;
import se.lnu.siq.s4rdm3x.cmd.util.AttributeUtil;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;

public class LineCount extends Metric {
    @Override
    public String getName() {
        return "linecount";
    }


    @Override
    public void assignMetric(Graph a_g, HuGMe.ArchDef a_arch) {
        AttributeUtil au = new AttributeUtil();

        for(Node n : a_g.getEachNode()) {

            if (a_arch.getMappedComponent(n) != null) {
                double size = 0;
                for (dmClass c : au.getClasses(n)) {
                    size += c.getLineCount();
                }
                setMetric(n, size);
            }
        }
    }

    public void reassignMetric(Graph a_g, HuGMe.ArchDef a_arch) {

    }
}
