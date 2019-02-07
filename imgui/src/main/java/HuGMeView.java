import archviz.HNode;
import glm_.vec2.Vec2;
import gui.ImGuiWrapper;
import hiviz.Tree;
import imgui.ComboFlag;
import imgui.ImGui;
import imgui.SelectableFlag;
import imgui.WindowFlag;
import imgui.internal.ColumnsFlag;
import se.lnu.siq.s4rdm3x.model.cmd.hugme.HuGMe;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.hugme.HuGMeManual;
import se.lnu.siq.s4rdm3x.stats;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Collection;

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

    private abstract static class TNodeCollectionWorker implements Tree.TNodeVisitor {

        private Collection<CNode> m_collection;

        protected TNodeCollectionWorker(Collection a_collection) {
            m_collection = a_collection;
        }


        static public Tree.TNodeVisitor doAdd(Collection<CNode> a_collection) {
            return new TNodeCollectionWorker(a_collection) {
                @Override
                protected void doWork(CNode a_node, Collection a_collection) {
                    a_collection.add(a_node);
                }
            };
        }

        static public Tree.TNodeVisitor doRemove(Collection<CNode> a_collection) {
            return new TNodeCollectionWorker(a_collection) {
                @Override
                protected void doWork(CNode a_node, Collection a_collection) {
                    a_collection.remove(a_node);
                }
            };
        }

        public void visit(Tree.TNode a_node) {
            for (Tree.TNode c : a_node.children()) {
                c.accept(this);
            }

            Object o = a_node.getObject();
            if (o != null) {
                doWork((CNode) o, m_collection);
            }
        }

        protected abstract void doWork(CNode a_node, Collection a_collection);

    }

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

        a_imgui.combo("", m_viewSelection, items, items.size());

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

        if (iw.button("Test Mapping 1", 150)) {
            String [] mappedNodes = {   "net/sf/jabref/collab/FileUpdatePanel.java",
                                        "net/sf/jabref/collab/InfoPane.java",
                                        "net/sf/jabref/collab/EntryDeleteChange.java",
                                        "net/sf/jabref/collab/GroupChange.java",
                                        "net/sf/jabref/collab/Change.java",
                                        "net/sf/jabref/collab/ChangeScanner.java",
                                        "net/sf/jabref/collab/StringRemoveChange.java",
                                        "net/sf/jabref/collab/PreambleChange.java",
                                        "net/sf/jabref/collab/ChangeDisplayDialog.java"};
            String [] orphanNodes = {"net/sf/jabref/collab/FileUpdateMonitor.java",
                                        "net/sf/jabref/collab/StringChange.java",
                                        "net/sf/jabref/collab/EntryChange.java",
                                        "net/sf/jabref/collab/MetaDataChange.java",
                                        "net/sf/jabref/collab/StringAddChange.java",
                                        "net/sf/jabref/collab/EntryAddChange.java",
                                        "net/sf/jabref/collab/FileUpdateListener.java",
                                        "net/sf/jabref/collab/StringNameChange.java"};

            m_selectedOrphanNodes.clear();
            for (String n : mappedNodes) {
                m_selectedMappedNodes.add(a_g.getNode(n));
            }
            m_selectedOrphanNodes.clear();
            for (String n : orphanNodes) {
                m_selectedOrphanNodes.add(a_g.getNode(n));
            }
        }

        if (iw.button("Random 20%", 150)) {
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
            hovered.accept(TNodeCollectionWorker.doAdd(m_selectedMappedNodes));
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
            hovered.accept(TNodeCollectionWorker.doRemove(m_selectedMappedNodes));
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
            hovered.accept(TNodeCollectionWorker.doAdd(m_selectedOrphanNodes));
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
            hovered.accept(TNodeCollectionWorker.doRemove(m_selectedOrphanNodes));
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

            Tree.TNode tn = tree.addNode(n.getLogicName(), n);

            HuGMe.ArchDef.Component component = a_arch.getMappedComponent(a_g.getNode(n.getName()));    // the orphan nodes are copies stripped from mapping information and the original node in the graph may contain the original mapping
            if (component != null) {
                tn.setMapping("Mapped to: " + component.getName(), a_nvm.getBGColor(component.getName()), component);
            }

            component = a_arch.getClusteredComponent(n);
            if (component != null) {
                tn.setMapping("Clustered to: " + component.getName(), a_nvm.getBGColor(component.getName()), component);
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

        // table of attraction values
        final int columnCount = a_arch.getComponentCount() + 5;
        Vec2 columnSize = new Vec2(a_imgui.imgui().getColumnWidth(1) - 10, (m_autoClusteredOrphans.size() + 2) * a_imgui.imgui().getFrameHeightWithSpacing());
        if (a_imgui.imgui().beginChild("doAcceptAutoClusteredEntitiesAttractionTable", columnSize, false, 0)) {

            // header
            a_imgui.imgui().beginColumns("doAcceptAutoClusteredEntitiesAttractionTableColumns", columnCount, ColumnsFlag.NoPreserveWidths.getI());
            a_imgui.text("Entity");
            a_imgui.imgui().nextColumn();

            a_imgui.text("Mapping");
            a_imgui.imgui().nextColumn();

            a_imgui.text("Clustering");
            a_imgui.imgui().nextColumn();

            int cIx  = 0;
            for (; cIx < a_arch.getComponentCount(); cIx++) {
                a_imgui.text(a_imgui.getLongestSubString(a_arch.getComponent(cIx).getName(), a_imgui.imgui().getColumnWidth(a_imgui.imgui().getColumnIndex()), "\\."));
                a_imgui.imgui().nextColumn();
            }

            a_imgui.text("Average");
            a_imgui.imgui().nextColumn();
            a_imgui.text("StdDev");


            float [] columnWidths = new float[columnCount];
            for (cIx = 0; cIx < columnCount; cIx++) {
                columnWidths[cIx] = a_imgui.imgui().getColumnWidth(cIx);
            }


            a_imgui.imgui().endColumns();
            a_imgui.imgui().separator();
            a_imgui.imgui().separator();

            // rows
            for (CNode n : m_autoClusteredOrphans) {
                a_imgui.imgui().beginColumns("doAcceptAutoClusteredEntitiesAttractionTableColumns", columnCount, ColumnsFlag.NoResize.getI());

                for (cIx = 0; cIx < columnCount; cIx++) {
                    a_imgui.imgui().setColumnWidth(cIx, columnWidths[cIx]);
                }

                double [] attractions = n.getAttractions();


                a_imgui.text(a_imgui.getLongestSubString(n.getLogicName(), columnWidths[a_imgui.imgui().getColumnIndex()], "\\."));
                a_imgui.imgui().nextColumn();


                HuGMe.ArchDef.Component mappedComponent = a_arch.getMappedComponent(a_g.getNode(n.getName()));
                if (mappedComponent != null) {
                    a_imgui.text(a_imgui.getLongestSubString(mappedComponent.getName(), columnWidths[a_imgui.imgui().getColumnIndex()] - (a_imgui.imgui().getCursorPos().getX() - a_imgui.imgui().getColumnOffset(a_imgui.imgui().getColumnIndex())), "\\."));
                } else {
                    a_imgui.text("");
                }
                a_imgui.imgui().nextColumn();


                {   // comboboxes for the clustering
                    int [] selectedComponent = {0};

                    HuGMe.ArchDef.Component clusteredTo = a_arch.getClusteredComponent(n);


                    Vec2 zeroSize = new Vec2(0, 0);
                    a_imgui.imgui().pushItemWidth(-1);
                    if (a_imgui.imgui().beginCombo("##"+n.getLogicName(), a_imgui.getLongestSubString(clusteredTo.getName(), columnWidths[a_imgui.imgui().getColumnIndex()] - a_imgui.imgui().getFrameHeightWithSpacing() - (a_imgui.imgui().getCursorPos().getX() - a_imgui.imgui().getColumnOffset(a_imgui.imgui().getColumnIndex())), "\\."), ComboFlag.None.getI())) {

                        for (int i = 0; i < a_arch.getComponentCount(); i++) {
                            HuGMe.ArchDef.Component c = a_arch.getComponent(i);
                            if (a_imgui.imgui().selectable(c.getName(), c == clusteredTo, SelectableFlag.None.getI(), zeroSize)) {
                                selectedComponent[0] = i;
                                c.clusterToNode(n, HuGMe.ArchDef.Component.ClusteringType.Manual);
                            }
                        }

                        a_imgui.imgui().endCombo();
                    }
                    a_imgui.imgui().popItemWidth();

                }
                a_imgui.imgui().nextColumn();

                double mean = stats.mean(attractions);
                double stddev = stats.stdDev(attractions, mean);

                cIx = 0;
                for (; cIx < a_arch.getComponentCount(); cIx++) {
                    String text = "" + attractions[cIx];
                    if (attractions[cIx] >= mean) {
                        text += " (>=m)";
                    }
                    if (attractions[cIx] - mean >= stddev) {
                        text += " (>=sd)";
                    }
                    a_imgui.text(text);
                    a_imgui.imgui().nextColumn();
                }


                a_imgui.text("" + mean);
                a_imgui.imgui().nextColumn();

                a_imgui.text("" + stddev);
                a_imgui.imgui().nextColumn();

                a_imgui.imgui().endColumns();
            }


            a_imgui.imgui().endChild();
        }

        if (a_imgui.button("Accept Auto Clustered Nodes", 150)) {
            for (CNode n : m_autoClusteredOrphans) {
                CNode graphNode = a_g.getNode(n.getName());
                a_arch.getClusteredComponent(n).mapToNode(graphNode);
            }

            m_autoClusteredOrphans.clear();
            m_selectedMappedNodes.clear();
            m_selectedOrphanNodes.clear();
        }

        a_imgui.imgui().endColumns();
    }
}
