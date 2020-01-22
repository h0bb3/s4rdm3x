package mapping;

import gui.ImGuiWrapper;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.experiments.ExperimentRunData;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MapperBaseView {

    protected List<CNode> m_selectedMappedNodes;   // this one is unmodifiable
    protected List<CNode> m_selectedOrphanNodes;  // this one is unmodifiable
    protected ArrayList<CNode> m_autoClusteredOrphans = new ArrayList<>();

    public MapperBaseView(List<CNode> a_mappedNodes, List<CNode> a_orphanNodes) {
        m_selectedMappedNodes = a_mappedNodes;
        m_selectedOrphanNodes = a_orphanNodes;

    }

    public void fillRunData(ExperimentRunData.BasicRunData a_rd, ArchDef a_arch, CGraph a_originalMappings) {
        m_autoClusteredOrphans.forEach(n -> a_rd.addAutoClusteredNode(n));
        m_selectedMappedNodes.forEach(n -> a_rd.addInitialClusteredNode(n));
        a_rd.m_id = 1;
        a_rd.m_totalMapped = m_selectedMappedNodes.size() + m_selectedOrphanNodes.size();
        a_rd.m_initialClusteringPercent = m_selectedMappedNodes.size() / (double)a_rd.m_totalMapped;

        for (CNode n : m_autoClusteredOrphans) {
            ArchDef.Component c = a_arch.getClusteredComponent(n);

            if (c != a_arch.getMappedComponent(a_originalMappings.getNode(n.getName()))) {
                a_rd.m_totalAutoWrong++;
            }
        }
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
            nodeCopy.setMapping(n.getMapping());
        }

        for (CNode n : m_selectedOrphanNodes) {
            CNode nodeCopy = g.createNode(n.getName());
            nodeCopy.setMapping(n.getMapping());

            for (dmClass c : n.getClasses()) {
                nodeCopy.addClass(c);
            }
        }

        return g;
    }

    /*protected CGraph createGraph() {
        CGraph g = new CGraph();
        HashMap<dmClass, dmClass> sourceClasses = new HashMap<>();  // maps from created copy class (key) to

        for (CNode n : m_selectedMappedNodes) {
            CNode nodeCopy = g.createNode(n.getName());
            nodeCopy.setClustering(n.getMapping(), ArchDef.Component.ClusteringType.Initial.toString());
            for (dmClass sourceClass : n.getClasses()) {
                dmClass c = new dmClass(sourceClass.getName());
                nodeCopy.addClass(c);
                sourceClasses.put(c, sourceClass);
            }
        }

        for (CNode n : m_selectedOrphanNodes) {
            CNode nodeCopy = g.createNode(n.getName());

            for (dmClass sourceClass : n.getClasses()) {
                dmClass c = new dmClass(sourceClass.getName());
                nodeCopy.addClass(c);
                sourceClasses.put(c, sourceClass);
            }
        }

        // now we need to set up the dependencies correctly
        for (CNode n : g.getNodes()) {
            for (dmClass c : n.getClasses()) {
                dmClass sourceClass = sourceClasses.get(c);
                for (dmDependency srcDep : sourceClass.getDependencies()) {
                    if (sourceClasses.containsValue(srcDep.getTarget()) {
                        dmDependency newDep
                    }
                }
            }
        }



        return g;
    }*/


    protected void setAutoClusteredNodes(Iterable<CNode>a_clustered, Iterable<CNode> a_nodemappings) {
        m_autoClusteredOrphans.clear();
        //m_autoClusteredOrphans.addAll(getAllByName(m_selectedOrphanNodes, a_clustered));
        for (CNode n : a_clustered) {
            n.setMapping(getByName(a_nodemappings, n.getName()).getMapping());
            m_autoClusteredOrphans.add(n);
        }
    }
}
