package se.lnu.siq.s4rdm3x.cmd.util;

import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;

import java.util.HashMap;

public class FanInCache {

    HashMap<Node, HashMap<Node, Double>> m_nodeFanInMap = null;

    public FanInCache(Iterable<Node> a_nodes) {
        AttributeUtil au = new AttributeUtil();
        m_nodeFanInMap = new HashMap<>();

        for (Node to : a_nodes) {

            HashMap<Node, Double> toMap = new HashMap<>();
            m_nodeFanInMap.put(to, toMap);

            for (Node from : a_nodes) {
                if (to != from) {
                    double fanIn = 0;

                    for (dmClass cTo : au.getClasses(to)) {
                        for (dmClass cFrom : au.getClasses(from)) {
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

    public double getFanIn(Node a_to) {
        /*HashMap<Node, Double> toMap = m_nodeFanInMap.get(a_to);
        double ret = 0;
        for(Double d : toMap.values()) {
            ret += d;
        }*/

        double ret = 0;
        AttributeUtil au = new AttributeUtil();
        for (dmClass c : au.getClasses(a_to)) {
            for (dmDependency d : c.getIncomingDependencies()) {
                if (!au.hasClass(a_to, d.getSource())) {    // we do not count self references
                    ret += d.getCount();
                }
            }
        }

        return ret;
    }

    public double getFanIn(Node a_to, Node a_from) {
        /*HashMap<Node, Double> toMap = m_nodeFanInMap.get(a_to);

        assert(a_to != null);
        if (toMap.containsKey(a_from)) {
            return toMap.get(a_from);
        }*/

        double ret = 0;
        AttributeUtil au = new AttributeUtil();
        for (dmClass c : au.getClasses(a_to)) {
            for (dmDependency d : c.getIncomingDependencies()) {
                if (au.hasClass(a_from, d.getSource())) {
                    ret += d.getCount();
                }
            }
        }

        return ret;
    }
}
