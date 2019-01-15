package se.lnu.siq.s4rdm3x.experiments.metric;

import se.lnu.siq.s4rdm3x.cmd.util.FanInCache;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.ArrayList;

public class FanHelper {
    FanInCache m_fic;
    public FanHelper(Iterable<CNode> a_nodes) {
        m_fic = new FanInCache(a_nodes);
    }

    public double getFanIn(CNode a_n) {
        return m_fic.getFanIn(a_n);
    }

    public double getFanIn(CNode a_source, CNode a_target) {
        return m_fic.getFanIn(a_source, a_target);
    }

    public double getFanIn(CNode a_n, Iterable<CNode> a_targets) {
        double ret = 0;

        for (CNode t : a_targets) {
            if (a_n != t) {
                ret += m_fic.getFanIn(a_n, t);
            }
        }

        return ret;
    }

    public double getFanIn(Iterable<CNode> a_sources, Iterable<CNode> a_targets) {

        double ret = 0;

        for (CNode s : a_sources) {
            ret += getFanIn(s, a_targets);
        }

        return ret;
    }

    public double getFanOut(CNode a_n, Iterable<CNode> a_targets) {
        double ret = 0;

        for (CNode t : a_targets) {
            if (a_n != t) {
                ret += getFanOut(a_n, t);
            }
        }

        return ret;
    }

    public double getFanOut(Iterable<CNode> a_sources, Iterable<CNode> a_targets) {

        double ret = 0;

        for (CNode s : a_sources) {
            ret += getFanOut(s, a_targets);
        }

        return ret;
    }



    public double getFanOut(CNode a_n, CNode a_target) {
        double fanOut = 0;
        for (dmDependency d: a_n.getDependencies(a_target)) {
            fanOut += d.getCount();
        }

        return fanOut;
    }

    public ArrayList<CNode> getCoupledNodes(CNode a_n, Iterable<CNode> a_nodes) {
        ArrayList<CNode> ret = new ArrayList<>();

        /*for (dmClass c : m_au.getClasses(a_n)) {
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
        }*/

        return ret;
    }

    private CNode findNode(dmClass a_class, Iterable<CNode> a_nodes) {

        for (CNode n : a_nodes) {
            for (dmClass c : n.getClasses()) {
                if (c == a_class) {
                    return n;
                }
            }
        }

        return null;
    }

    /*public boolean hasDirectDependency(CNode a_from, CNode a_to, dmDependency.Type a_type) {

        a_from.hasDependency(a_to, a_type);

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
    }*/
}
