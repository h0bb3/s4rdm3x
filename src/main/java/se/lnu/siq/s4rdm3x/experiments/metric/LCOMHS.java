package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.model.AttributeUtil;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.model.CNode;

public class LCOMHS extends Metric {
    public String getName() {
        return "LackOfCohesionOfMethods_HS";
    }

    public void assignMetric(Iterable<CNode> a_nodes) {


        for(CNode n : a_nodes) {
            for (dmClass c : n.getClasses()) {
                if (!c.isInner()) {
                    double m = c.getConcreteMethodCount();
                    double f = c.getFieldCount();
                    double lcom = 0;
                    if (m - 1 > 0 && f > 0) {
                        double sum_fm = calcMethodFieldUseSum(c);
                        lcom = (m - (sum_fm / f)) / (m - 1);
                    }

                    n.setMetric(getName(), lcom);

                    break;
                }
            }
        }
    }

    private double calcMethodFieldUseSum(dmClass a_class) {
        double sum = 0;

        for (dmClass.Method m : a_class.getMethods()) {
            if (m.isConcrete()) {
                sum += m.getUsedFieldCount();
            }
        }

        return sum;
    }

    public void reassignMetric(Iterable<CNode> a_nodes) {

    }
}
