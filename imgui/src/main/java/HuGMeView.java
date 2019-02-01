import archviz.HNode;
import gui.ImGuiWrapper;
import hiviz.Tree;
import imgui.ImGui;
import se.lnu.siq.s4rdm3x.model.cmd.hugme.HuGMe;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.hugme.HuGMeManual;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;

public class HuGMeView {
    private int[] m_viewSelection = {0};
    Tree.TNode m_selectedMappedNode;

    private final int g_hugMeParamsId = 0;
    private final int g_mappedEntitiesId = 1;
    private final int g_orphanEntitiesId = 2;
    private final int g_autoClusteredEntitiesId = 3;
    private final int g_humanClusteredEntitiesId = 4;

    private ArrayList<CNode> m_selectedMappedNodes = new ArrayList<>();
    private ArrayList<CNode> m_selectedOrphanNodes = new ArrayList<>();
    private ArrayList<CNode> m_autoClusteredOrphans = new ArrayList<>();
    private ArrayList<CNode> m_acceptedClusteredOrphans = new ArrayList<>();
    private float[] m_omega = {0.5f};
    private float[] m_phi = {0.5f};

    HuGMeManual m_hugme;

    public static class Action {
        public static class DoMap {
            public String a_whatNodeName;
            public String a_toComponentName;
        }

        DoMap m_doMapAction;
    }



    Action doHugMeView(ImGui a_imgui, HuGMe.ArchDef a_arch, CGraph a_g, HNode.VisualsManager a_nvm) {
        Action ret = null;

        ArrayList<String> items = new ArrayList<>();
        items.add("HugMe Parameters");
        items.add("Mapped Entites");
        items.add("Orphan Entities");
        items.add("Auto Clustered Entities");
        items.add("Human Clustered Entities");

        if (a_imgui.combo("", m_viewSelection, items, items.size())) {
        }

        ImGuiWrapper iw = new ImGuiWrapper(a_imgui);

        switch (m_viewSelection[0]) {
            case g_hugMeParamsId:
                doHugMeParamsView(iw, a_arch);
            break;
            case g_mappedEntitiesId:
                doSelectMappedEntities(iw, a_g, a_arch, a_nvm);
            break;

            case g_orphanEntitiesId:
                doSelectOrphanEntities(iw, a_g, a_arch, a_nvm);
                break;
            case g_autoClusteredEntitiesId:
                doAcceptAutoClusteredEntities(iw, a_g, a_arch, a_nvm);
        }

        return ret;
    }

    private void doSelectMappedEntities(ImGuiWrapper a_imgui, CGraph a_g, HuGMe.ArchDef a_arch, HNode.VisualsManager a_nvm) {
        a_imgui.imgui().beginColumns("doSelectMappedEntitiesColumns", 2, 0);
        a_imgui.text("Unselected Mapped Nodes:");
        hiviz.Tree tree = new hiviz.Tree();
        for (CNode n : a_g.getNodes()) {

            if (!m_selectedMappedNodes.contains(n)) {
                HuGMe.ArchDef.Component component = a_arch.getMappedComponent(n);

                for (dmClass c : n.getClasses()) {
                    if (!c.isInner()) {
                        if (component != null) {
                            Tree.TNode tn = tree.addNode(n.getName().replace("/", ".").replace(".java", ""), n);
                            tn.setName(tn.getName());
                            tn.setMapping(component.getName(), a_nvm.getBGColor(component.getName()), component);
                        }
                    }
                }
            }
        }

        Tree.TNode hovered = tree.doTree(a_imgui.imgui(), "notSelectedMappedEntitiesTree");
        if (hovered != null && a_imgui.isMouseDoubleClicked(0)) {
           hovered.accept(new Tree.TNodeVisitor() {
               @Override
               public void visit(Tree.TNode a_node) {

                   for (Tree.TNode c : a_node.children()) {
                       c.accept(this);
                   }

                   Object o = a_node.getObject();
                   if (o != null) {
                       m_selectedMappedNodes.add((CNode) o);
                   }
               }
           });
        }

        a_imgui.imgui().nextColumn();


        a_imgui.text("Selected Mapped Nodes:");
        tree = new hiviz.Tree();
        for (CNode n : m_selectedMappedNodes) {

            HuGMe.ArchDef.Component component = a_arch.getMappedComponent(n);

            for (dmClass c : n.getClasses()) {
                if (!c.isInner()) {
                    if (component != null) {
                        Tree.TNode tn = tree.addNode(n.getName().replace("/", ".").replace(".java", ""), n);
                        tn.setName(tn.getName());
                        tn.setMapping(component.getName(), a_nvm.getBGColor(component.getName()), component);
                    }
                }
            }
        }

        hovered = tree.doTree(a_imgui.imgui(), "selectedMappedEntitiesTree");
        if (hovered != null && a_imgui.isMouseDoubleClicked(0)){
            hovered.accept(a_node -> {
                Object o = a_node.getObject();
                if (o != null) {
                    m_selectedMappedNodes.remove(o);
                }
            });
        }

        a_imgui.imgui().endColumns();
    }

    private void doSelectOrphanEntities(ImGuiWrapper a_imgui, CGraph a_g, HuGMe.ArchDef a_arch, HNode.VisualsManager a_nvm) {
        a_imgui.imgui().beginColumns("doSelectOrphanEntitiesColumns", 2, 0);
        a_imgui.text("Unselected Entities:");
        hiviz.Tree tree = new hiviz.Tree();
        for (CNode n : a_g.getNodes()) {

            if (!m_selectedOrphanNodes.contains(n) && !m_selectedMappedNodes.contains(n)) {
                HuGMe.ArchDef.Component component = a_arch.getMappedComponent(n);

                if (component != null) {
                    Tree.TNode tn = tree.addNode(n.getLogicName().replace("/", ".").replace(".java", ""), n);
                    tn.setName(tn.getName());
                    tn.setMapping(component.getName(), a_nvm.getBGColor(component.getName()), component);
                }
            }
        }

        Tree.TNode hovered = tree.doTree(a_imgui.imgui(), "notSelectedOrphanEntitiesTree");
        if (hovered != null && a_imgui.isMouseDoubleClicked(0)) {
            hovered.accept(a_node -> {
                Object o = a_node.getObject();
                if (o != null) {
                    m_selectedOrphanNodes.add((CNode)o);
                }
            });
        }

        a_imgui.imgui().nextColumn();


        a_imgui.text("Selected Orphan Entities:");
        tree = new hiviz.Tree();
        for (CNode n : m_selectedOrphanNodes) {

            HuGMe.ArchDef.Component component = a_arch.getMappedComponent(n);
            if (component != null) {
                Tree.TNode tn = tree.addNode(n.getLogicName(), n);
                tn.setName(tn.getName());
                tn.setMapping(component.getName(), a_nvm.getBGColor(component.getName()), component);
            }
        }

        hovered = tree.doTree(a_imgui.imgui(), "selectedOrphanEntitiesTree");
        if (hovered != null && a_imgui.isMouseDoubleClicked(0)){
            hovered.accept(a_node -> {
                Object o = a_node.getObject();
                if (o != null) {
                    m_selectedOrphanNodes.remove(o);
                }
            });
        }

        a_imgui.imgui().endColumns();
    }

    private void doHugMeParamsView(ImGuiWrapper a_imgui, HuGMe.ArchDef a_arch) {

        a_imgui.imgui().sliderFloat("Omega Threshold", m_omega, 0, 1, "%.2f", 1);
        a_imgui.imgui().sliderFloat("Phi Value", m_phi, 0, 1, "%.2f", 1);

        a_imgui.text("Mapped Entities: " + m_selectedMappedNodes.size());

        Tree tree = new Tree();
        for(HuGMe.ArchDef.Component c : a_arch.getComponents()) {
            Tree.TNode tn = null;
            for (CNode n : m_selectedMappedNodes) {
                if (c.isMappedTo(n)) {
                    tn = tree.addNode(c.getName() + "." + n.getLogicName(), n);
                }
            }

            if (tn == null) {
                tree.addNode(c.getName(), c);
            }
        }

        tree.doTree(a_imgui.imgui(), "doHugMeParamsViewMappedEntities");

        if (a_imgui.button("HuGMe Plz", 150)) {
            m_hugme = new HuGMeManual((double)m_omega[0], (double)m_phi[0], a_arch);
            CGraph g = new CGraph();

            for (CNode n : m_selectedMappedNodes) {
                CNode nodeCopy = g.createNode(n.getName());

                for (dmClass c : n.getClasses()) {
                    nodeCopy.addClass(c);
                }

                HuGMe.ArchDef.Component c = a_arch.getMappedComponent(n);
                c.mapToNode(nodeCopy);
                c.clusterToNode(nodeCopy, HuGMe.ArchDef.Component.ClusteringType.Initial);
            }

            for (CNode n : m_selectedOrphanNodes) {
                CNode nodeCopy = g.createNode(n.getName());

                for (dmClass c : n.getClasses()) {
                    nodeCopy.addClass(c);
                }
            }

            m_hugme.run(g);

            m_autoClusteredOrphans.clear();
            m_autoClusteredOrphans.addAll(m_hugme.m_clusteredElements);
        }

        if (m_hugme != null) {
            a_imgui.text("HuGMe automatically mapped nodes: " + m_hugme.m_automaticallyMappedNodes);
            a_imgui.text("HuGMe considered nodes: " + m_hugme.m_consideredNodes);
            a_imgui.text("HuGMe mapped nodes from start: " + m_hugme.m_mappedNodesFromStart);
            a_imgui.text("HuGMe unmapped nodes from start: " + m_hugme.m_unmappedNodesFromStart);
        }
    }

    private void doAcceptAutoClusteredEntities(ImGuiWrapper a_imgui, CGraph a_g, HuGMe.ArchDef a_arch, HNode.VisualsManager a_nvm) {
        a_imgui.imgui().beginColumns("doAcceptAutoClusteredEntities", 2, 0);
        a_imgui.text("Auto Clustered Entities:");
        hiviz.Tree tree = new hiviz.Tree();
        for (CNode n : m_autoClusteredOrphans) {
            HuGMe.ArchDef.Component component = a_arch.getClusteredComponent(n);
            if (component != null) {
                Tree.TNode tn = tree.addNode(n.getLogicName().replace("/", ".").replace(".java", ""), n);
                tn.setName(tn.getName());
                tn.setMapping(component.getName(), a_nvm.getBGColor(component.getName()), component);
            }
        }

        Tree.TNode hovered = tree.doTree(a_imgui.imgui(), "notSelectedAutoClusteredEntitiesTree");
        if (hovered != null && a_imgui.isMouseDoubleClicked(0)) {
            hovered.accept(a_node -> {
                Object o = a_node.getObject();
                if (o != null) {
                }
            });
        }

        a_imgui.imgui().nextColumn();

        a_imgui.button("Accept Auto Clustered Nodes", 150);

        a_imgui.imgui().endColumns();
    }
}
