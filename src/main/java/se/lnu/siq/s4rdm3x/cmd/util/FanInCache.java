package se.lnu.siq.s4rdm3x.cmd.util;

import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.AttributeUtil;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.HashMap;

public class FanInCache {

    HashMap<CNode, HashMap<CNode, Double>> m_nodeFanInMap = null;

    public FanInCache(Iterable<CNode> a_nodes) {
        m_nodeFanInMap = new HashMap<>();

        for (CNode to : a_nodes) {

            HashMap<CNode, Double> toMap = new HashMap<>();
            m_nodeFanInMap.put(to, toMap);

            for (CNode from : a_nodes) {
                if (to != from) {
                    double fanIn = 0;

                    for (dmClass cTo : to.getClasses()) {
                        for (dmClass cFrom : from.getClasses()) {
                            fanIn += countDependenciesTo(cFrom, cTo);
                        }
                    }
                    if (fanIn > 0) {
                        toMap.put(from, fanIn);
                    }
                }
            }
        }

        m_nodeFanInMap = new HashMap<>();
    }

    private double countDependenciesTo(dmClass a_from, dmClass a_to) {
        double count = 0;
        // TODO: we should have some weight here

        for(dmDependency d : a_from.getDependencies()) {
            if (d.getTarget() == a_to) {
                count += d.getCount();
            }
        }

        return count;
    }

    public double getFanIn(CNode a_to) {
        /*HashMap<Node, Double> toMap = m_nodeFanInMap.get(a_to);
        double ret = 0;
        for(Double d : toMap.values()) {
            ret += d;
        }*/

        double ret = 0;
        for (dmClass c : a_to.getClasses()) {
            for (dmDependency d : c.getIncomingDependencies()) {
                if (!a_to.containsClass(d.getSource())) {    // we do not count self references
                    ret += d.getCount();
                }
            }
        }

        return ret;
    }

    public double getFanIn(CNode a_to, CNode a_from) {
        /*HashMap<Node, Double> toMap = m_nodeFanInMap.get(a_to);

        assert(a_to != null);
        if (toMap.containsKey(a_from)) {
            return toMap.get(a_from);
        }*/

        double ret = 0;
        for (dmClass c : a_to.getClasses()) {
            for (dmDependency d : c.getIncomingDependencies()) {
                if (a_from.containsClass(d.getSource())) {
                    ret += d.getCount();
                }
            }
        }

        return ret;
    }
}
