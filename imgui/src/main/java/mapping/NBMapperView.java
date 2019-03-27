package mapping;

import archviz.HNode;
import gui.ImGuiWrapper;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.HuGMeManual;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.NBMapper;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.NBMapperManual;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

import java.util.ArrayList;
import java.util.List;

public class NBMapperView {

    NBMapperManual m_nbmapper;

    private List<CNode> m_selectedMappedNodes;   // this one is unmodifiable
    private List<CNode> m_selectedOrphanNodes;  // this one is unmodifiable

    private ArrayList<CNode> m_autoClusteredOrphans = new ArrayList<>();

    public Iterable<CNode> autoClusteredOrphans() {
        return m_autoClusteredOrphans;
    }

    public int autoClusteredOrphanCount() {
        return m_autoClusteredOrphans.size();
    }

    public void clearAutoClusteredOrphans() {
        m_autoClusteredOrphans.clear();
    }

    public NBMapperView(List<CNode>a_mappedNodes, List<CNode>a_orphanNodes) {
        m_selectedMappedNodes = a_mappedNodes;
        m_selectedOrphanNodes = a_orphanNodes;
    }

    void doNBMapperParamsView(ImGuiWrapper a_imgui, ArchDef a_arch, HNode.VisualsManager a_nvm) {

        a_imgui.imgui().beginColumns("doNBMapperParamsView", 2, 0);

        if (a_imgui.button("NBMap me Plz", 150)) {
            m_nbmapper = new NBMapperManual(a_arch);
            CGraph g = new CGraph();

            for (CNode n : m_selectedMappedNodes) {
                CNode nodeCopy = g.createNode(n.getName());

                for (dmClass c : n.getClasses()) {
                    nodeCopy.addClass(c);
                }

                ArchDef.Component c = a_arch.getMappedComponent(n);
                c.mapToNode(nodeCopy);
                c.clusterToNode(nodeCopy, ArchDef.Component.ClusteringType.Initial);
            }

            for (CNode n : m_selectedOrphanNodes) {
                CNode nodeCopy = g.createNode(n.getName());

                for (dmClass c : n.getClasses()) {
                    nodeCopy.addClass(c);
                }
            }

            m_nbmapper.run(g);

            m_autoClusteredOrphans.clear();
            m_autoClusteredOrphans.addAll(m_nbmapper.m_clusteredElements);
        }

        a_imgui.imgui().nextColumn();

        NBMapper mapper = new NBMapper(null);

        Instances td = mapper.getTrainingData(m_selectedMappedNodes, a_arch, mapper.getFilter());


        Filter filter = mapper.getFilter();

        a_imgui.text(filter.toString());

        for (Instance inst : td) {

            for (int aIx = 0; aIx < inst.numAttributes(); aIx++) {
                Attribute attrib = inst.attribute(aIx);

                String value = attrib.name() + "[";

                value += inst.toString(attrib);


                /*for (int vIx = 0; vIx < attrib.numValues(); vIx++) {
                    value += inst.toString()attrib.value(vIx) + ",";
                }*/

                value += "]";

                a_imgui.text(value);
            }


        }



        a_imgui.imgui().endColumns();
    }


}
