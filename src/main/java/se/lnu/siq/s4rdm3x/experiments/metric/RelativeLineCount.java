package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;
import se.lnu.siq.s4rdm3x.cmd.util.AttributeUtil;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;

public class RelativeLineCount extends Metric {

    private Metric m_rel;

    public RelativeLineCount(Metric a_rel) {
        m_rel = a_rel;
    }

    @Override
    public String getName() {
        return m_rel.getName() + "_linecount";
    }


    @Override
    public void assignMetric(Graph a_g, HuGMe.ArchDef a_arch) {

        m_rel.assignMetric(a_g, a_arch);

        AttributeUtil au = new AttributeUtil();

        for(Node n : a_g.getEachNode()) {

            if (a_arch.getMappedComponent(n) != null) {
                double size = 0;
                for (dmClass c : au.getClasses(n)) {
                    size += c.getLineCount();
                }
                if (size == 0) {
                    size = 1;
                }
                setMetric(n, getMetric(n) / size);
            }
        }
    }

    public void reassignMetric(Graph a_g, HuGMe.ArchDef a_arch) {

    }
}
