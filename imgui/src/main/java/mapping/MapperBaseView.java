package mapping;

import gui.ImGuiWrapper;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;

import java.util.ArrayList;
import java.util.List;

public class MapperBaseView {

    protected List<CNode> m_selectedMappedNodes;   // this one is unmodifiable
    protected List<CNode> m_selectedOrphanNodes;  // this one is unmodifiable
    protected ArrayList<CNode> m_autoClusteredOrphans = new ArrayList<>();

    public MapperBaseView(List<CNode> a_mappedNodes, List<CNode> a_orphanNodes) {
        m_selectedMappedNodes = a_mappedNodes;
        m_selectedOrphanNodes = a_orphanNodes;

    }


    public Iterable<CNode> autoClusteredOrphans() {
        return m_autoClusteredOrphans;
    }

    public int autoClusteredOrphanCount() {
        return m_autoClusteredOrphans.size();
    }

    public void clearAutoClusteredOrphans() {
        m_autoClusteredOrphans.clear();
    }

    protected ArrayList<? extends CNode> getAllByName(Iterable<CNode> a_in, Iterable<CNode> a_by) {
        ArrayList<CNode> ret = new ArrayList<>();

        for (CNode b : a_by) {
            CNode found = getByName(a_in, b.getName());
            if (found != null) {
                ret.add(b);
            }
        }

        return ret;
    }

    public CNode getByName(Iterable<CNode> a_in, String a_name) {
        for (CNode n : a_in) {
            if (n.getName().equals(a_name)) {
                return n;
            }
        }
        return null;
    }

    protected CGraph createGraph() {
        CGraph g = new CGraph();

        for (CNode n : m_selectedMappedNodes) {
            CNode nodeCopy = g.createNode(n.getName());
            nodeCopy.shallowCopy(n);
            nodeCopy.setClustering(n.getMapping(), ArchDef.Component.ClusteringType.Initial.toString());
        }

        for (CNode n : m_selectedOrphanNodes) {
            CNode nodeCopy = g.createNode(n.getName());

            for (dmClass c : n.getClasses()) {
                nodeCopy.addClass(c);
            }
        }

        return g;
    }

    protected void setAutoClusteredNodes(Iterable<CNode>a_clustered) {
        m_autoClusteredOrphans.clear();
        m_autoClusteredOrphans.addAll(getAllByName(m_selectedOrphanNodes, a_clustered));
    }
}
