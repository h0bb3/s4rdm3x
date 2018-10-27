package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.util.AttributeUtil;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;

public class LCOMHS extends Metric {
    public String getName() {
        return "LackOfCohesionOfMethods_HS";
    }

    public void assignMetric(Iterable<Node> a_nodes) {
        AttributeUtil au = new AttributeUtil();


        for(Node n : a_nodes) {



            for (dmClass c : au.getClasses(n)) {
                if (!c.isInner()) {
                    double m = c.getMethodCount();
                    double f = c.getFieldCount();
                    double lcom = 0;
                    if (m - 1 > 0 && f > 0) {
                        double sum_fm = calcMethodFieldUseSum(c);
                        lcom = (m - (sum_fm / f)) / (m - 1);
                    }

                    setMetric(n, lcom);

                    break;
                }
            }
        }
    }

    private double calcMethodFieldUseSum(dmClass a_class) {
        double sum = 0;

        for (dmClass.Method m : a_class.getMethods()) {
            sum += m.getUsedFieldCount();
        }

        return sum;
    }

    public void reassignMetric(Iterable<Node> a_nodes) {

    }
}
