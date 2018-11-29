package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.util.AttributeUtil;
import se.lnu.siq.s4rdm3x.cmd.util.FanInCache;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;

import java.util.ArrayList;

public class FanHelper {
    FanInCache m_fic;
    AttributeUtil m_au;
    public FanHelper(Iterable<Node> a_nodes) {
        m_fic = new FanInCache(a_nodes);
        m_au = new AttributeUtil();
    }

    public double getFanIn(Node a_n) {
        return m_fic.getFanIn(a_n);
    }

    public double getFanIn(Node a_source, Node a_target) {
        return m_fic.getFanIn(a_source, a_target);
    }

    public double getFanIn(Node a_n, Iterable<Node> a_targets) {
        double ret = 0;

        for (Node t : a_targets) {
            if (a_n != t) {
                ret += m_fic.getFanIn(a_n, t);
            }
        }

        return ret;
    }

    public double getFanIn(Iterable<Node> a_sources, Iterable<Node> a_targets) {

        double ret = 0;

        for (Node s : a_sources) {
            ret += getFanIn(s, a_targets);
        }

        return ret;
    }

    public double getFanOut(Node a_n, Iterable<Node> a_targets) {
        double ret = 0;

        for (Node t : a_targets) {
            if (a_n != t) {
                ret += getFanOut(a_n, t);
            }
        }

        return ret;
    }

    public double getFanOut(Iterable<Node> a_sources, Iterable<Node> a_targets) {

        double ret = 0;

        for (Node s : a_sources) {
            ret += getFanOut(s, a_targets);
        }

        return ret;
    }



    public double getFanOut(Node a_n, Node a_target) {
        double fanOut = 0;
        for (dmClass from : m_au.getClasses(a_n)) {
            for (dmDependency d: from.getDependencies()) {
                if (m_au.hasClass(a_target, d.getTarget())) {
                    fanOut += d.getCount();
                }
            }
        }

        return fanOut;
    }

    public double getFanOut(Node a_n) {
        double fanOut = 0;
        for (dmClass from : m_au.getClasses(a_n)) {
            fanOut += countDependenciesFrom(a_n, from);
        }

        return fanOut;
    }

    private double countDependenciesFrom(Node a_source, dmClass a_c) {
        double count = 0;
        // TODO: we should have some weight here

        for(dmDependency d : a_c.getDependencies()) {
            if (!m_au.hasClass(a_source, d.getTarget())) {  // node internal dependencies do not count
                count += d.getCount();
            }
        }

        return count;
    }

    public boolean hasDirectDependency(Node a_from, Node a_to) {
        for (dmClass c : m_au.getClasses(a_from)) {
            for (dmDependency d : c.getDependencies()) {
                if (m_au.hasClass(a_to, d.getTarget())) {
                    return true;
                }
            }
        }

        return false;
    }

    public ArrayList<Node> getCoupledNodes(Node a_n, Iterable<Node> a_nodes) {
        ArrayList<Node> ret = new ArrayList<>();

        for (dmClass c : m_au.getClasses(a_n)) {
            for (dmDependency d : c.getDependencies()) {
                Node n = findNode(d.getTarget(), a_nodes);
                if (n != null && n != a_n && !ret.contains(n)) {
                    ret.add(n);
                }
            }

            for (dmDependency d : c.getIncomingDependencies()) {
                Node n = findNode(d.getSource(), a_nodes);
                if (n != null && n != a_n && !ret.contains(n)) {
                    ret.add(n);
                }
            }
        }

        return ret;
    }

    private Node findNode(dmClass a_class, Iterable<Node> a_nodes) {

        for (Node n : a_nodes) {
            for (dmClass c : m_au.getClasses(n)) {
                if (c == a_class) {
                    return n;
                }
            }
        }

        return null;
    }

    public boolean hasDirectDependency(Node a_from, Node a_to, dmDependency.Type a_type) {
        for (dmClass c : m_au.getClasses(a_from)) {
            for (dmDependency d : c.getDependencies()) {
                if (d.getType() == a_type) {
                    if (m_au.hasClass(a_to, d.getTarget())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
