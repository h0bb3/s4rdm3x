package mapping;

import archviz.HNode;
import glm_.vec2.Vec2;
import gui.ImGuiWrapper;
import hiviz.Tree;
import imgui.ComboFlag;
import imgui.ImGui;
import imgui.SelectableFlag;
import imgui.internal.ColumnsFlag;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class MappingView {
    private ArrayList<CNode> m_selectedMappedNodes = new ArrayList<>();
    private ArrayList<CNode> m_selectedOrphanNodes = new ArrayList<>();

    private int[] m_viewSelection = {0};
    private final int g_hugMeParamsId = 0;
    private final int g_nbmapperParamsId = 1;
    private final int g_mappedEntitiesId = 2;
    private final int g_orphanEntitiesId = 3;
    private final int g_autoClusteredEntitiesId = 4;
    private final int g_humanClusteredEntitiesId = 5;

    HuGMeView m_hugmeView = new HuGMeView(Collections.unmodifiableList(m_selectedMappedNodes), Collections.unmodifiableList(m_selectedOrphanNodes));
    NBMapperView m_nbmapperView = new NBMapperView(Collections.unmodifiableList(m_selectedMappedNodes), Collections.unmodifiableList(m_selectedOrphanNodes));


    private CNode m_selectedClusteredNode;

    public CNode getSelectedClusteredNode() {
        return m_selectedClusteredNode;
    }


    public HuGMeView.Action doView(ImGui a_imgui, ArchDef a_arch, CGraph a_g, HNode.VisualsManager a_nvm) {
        ArrayList<String> items = new ArrayList<>();

        items.add("HugMe Parameters");
        items.add("NBMapper Parameters");
        items.add("Mapped Entites");
        items.add("Orphan Entities");
        items.add("Auto Clustered Entities");
        items.add("Human Clustered Entities");

        ImGuiWrapper iw = new ImGuiWrapper(a_imgui);
        a_imgui.combo("", m_viewSelection, items, items.size());


        if (iw.button("Test MappingView 1", 150)) {
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


        switch (m_viewSelection[0]) {
            case g_hugMeParamsId:
                m_hugmeView.doHugMeParamsView(iw, a_arch, a_nvm);
                break;
            case g_nbmapperParamsId:
                m_nbmapperView.doNBMapperParamsView(iw, a_arch, a_nvm);
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

        return null;
    }


    private void doSelectMappedEntities(ImGuiWrapper a_imgui, CGraph a_g, ArchDef a_arch, HNode.VisualsManager a_nvm) {
        a_imgui.imgui().beginColumns("doSelectMappedEntitiesColumns", 2, 0);
        a_imgui.text("Unselected Mapped Nodes:");
        hiviz.Tree tree = new hiviz.Tree();
        for (CNode n : a_g.getNodes()) {

            if (!m_selectedMappedNodes.contains(n)) {
                ArchDef.Component component = a_arch.getMappedComponent(n);

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

            ArchDef.Component component = a_arch.getMappedComponent(n);

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

    private void doSelectOrphanEntities(ImGuiWrapper a_imgui, CGraph a_g, ArchDef a_arch, HNode.VisualsManager a_nvm) {
        a_imgui.imgui().beginColumns("doSelectOrphanEntitiesColumns", 2, 0);
        a_imgui.text("Unselected Entities:");
        hiviz.Tree tree = new hiviz.Tree();
        for (CNode n : a_g.getNodes()) {

            if (!m_selectedOrphanNodes.contains(n) && !m_selectedMappedNodes.contains(n)) {
                ArchDef.Component component = a_arch.getMappedComponent(n);

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

            ArchDef.Component component = a_arch.getMappedComponent(n);
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

    private Tree.TNode doClusteredTree(ImGuiWrapper a_imgui, Iterable<CNode> a_clusteredNodes, String a_treeId, ArchDef a_arch, CGraph a_originalNodes, HNode.VisualsManager a_nvm) {
        hiviz.Tree tree = new hiviz.Tree();
        for (CNode n : a_clusteredNodes) {

            Tree.TNode tn = tree.addNode(n.getLogicName(), n);

            ArchDef.Component component = a_arch.getMappedComponent(a_originalNodes.getNode(n.getName()));    // the orphan nodes are copies stripped from mapping information and the original node in the graph may contain the original mapping
            if (component != null) {
                tn.setMapping("Mapped to: " + component.getName(), a_nvm.getBGColor(component.getName()), component);
            }

            component = a_arch.getClusteredComponent(n);
            if (component != null) {
                tn.setMapping("Clustered to: " + component.getName(), a_nvm.getBGColor(component.getName()), component);
            }
        }

        return  tree.doTree(a_imgui.imgui(), a_treeId);
    }


    private void doAcceptAutoClusteredEntities(ImGuiWrapper a_imgui, CGraph a_g, ArchDef a_arch, HNode.VisualsManager a_nvm) {
        a_imgui.imgui().beginColumns("doAcceptAutoClusteredEntities", 2, 0);
        a_imgui.text("HugMe Auto Clustered Entities:");

        Tree.TNode hovered = doClusteredTree(a_imgui, m_hugmeView.autoClusteredOrphans(), "notSelectedHuGMeClusteredEntitiesTree", a_arch, a_g, a_nvm);
        if (hovered != null && a_imgui.isMouseDoubleClicked(0)) {
            hovered.accept(a_node -> {
                Object o = a_node.getObject();
                if (o != null) {
                }
            });
        }

        a_imgui.text("NBMapper Auto Clustered Entities:");
        hovered = doClusteredTree(a_imgui, m_nbmapperView.autoClusteredOrphans(), "notSelectedNBMapperClusteredEntitiesTree", a_arch, a_g, a_nvm);
        if (hovered != null && a_imgui.isMouseDoubleClicked(0)) {
            hovered.accept(a_node -> {
                Object o = a_node.getObject();
                if (o != null) {
                }
            });
        }

        a_imgui.imgui().nextColumn();

        a_imgui.text("HuGMe Mapping Results");
        doClusteringTable(m_hugmeView.autoClusteredOrphans(), m_hugmeView.autoClusteredOrphanCount(), a_imgui, a_g, a_arch, "HuGMEAutoClusteredOrphans");

        a_imgui.imgui().separator();
        a_imgui.text("NBMapper Mapping Results");
        doClusteringTable(m_nbmapperView.autoClusteredOrphans(), m_nbmapperView.autoClusteredOrphanCount(), a_imgui, a_g, a_arch, "NBMapperAutoClusteredOrphans");

        if (a_imgui.button("Accept Auto Clustered Nodes", 150)) {
            for (CNode n : m_hugmeView.autoClusteredOrphans()) {
                CNode graphNode = a_g.getNode(n.getName());
                a_arch.getClusteredComponent(n).mapToNode(graphNode);
            }

            m_hugmeView.clearAutoClusteredOrphans();
            m_selectedMappedNodes.clear();
            m_selectedOrphanNodes.clear();
        }

        a_imgui.imgui().endColumns();
    }

    private void doClusteringTable(Iterable<CNode> a_nodes, int a_nodeCount, ImGuiWrapper a_imgui, CGraph a_g, ArchDef a_arch, String a_tableId) {
        // table of attraction values
        final int columnCount = a_arch.getComponentCount() + 5;
        Vec2 columnSize = new Vec2(a_imgui.imgui().getColumnWidth(1) - 10, (a_nodeCount + 2) * a_imgui.imgui().getFrameHeightWithSpacing());
        if (a_imgui.imgui().beginChild(a_tableId+"Table", columnSize, false, 0)) {

            // header
            a_imgui.imgui().beginColumns(a_tableId+ "TableColumns", columnCount, ColumnsFlag.NoPreserveWidths.getI());
            a_imgui.text("Entity");
            a_imgui.imgui().nextColumn();

            a_imgui.text("MappingView");
            a_imgui.imgui().nextColumn();

            a_imgui.text("Clustering");
            a_imgui.imgui().nextColumn();

            int cIx  = 0;
            for (; cIx < a_arch.getComponentCount(); cIx++) {

                String text = a_imgui.getLongestSubString(a_arch.getComponent(cIx).getName(), a_imgui.imgui().getColumnWidth(a_imgui.imgui().getColumnIndex()), "\\.");

                a_imgui.text(text);
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
            for (CNode n : a_nodes) {
                a_imgui.imgui().beginColumns(a_tableId + "TableColumns", columnCount, ColumnsFlag.NoResize.getI());

                for (cIx = 0; cIx < columnCount; cIx++) {
                    a_imgui.imgui().setColumnWidth(cIx, columnWidths[cIx]);
                }

                double [] attractions = n.getAttractions();

                String logicName = a_imgui.getLongestSubString(n.getLogicName(), columnWidths[a_imgui.imgui().getColumnIndex()], "\\.");
                //a_imgui.text(logicName);
                if (a_imgui.imgui().selectable(logicName, false, 0, new Vec2(a_imgui.imgui().getColumnWidth(a_imgui.imgui().getColumnIndex()), a_imgui.imgui().getFrameHeightWithSpacing()))) {
                    m_selectedClusteredNode = a_g.getNode(n.getName());
                }
                a_imgui.imgui().nextColumn();


                ArchDef.Component mappedComponent = a_arch.getMappedComponent(a_g.getNode(n.getName()));
                if (mappedComponent != null) {
                    a_imgui.text(a_imgui.getLongestSubString(mappedComponent.getName(), columnWidths[a_imgui.imgui().getColumnIndex()] - (a_imgui.imgui().getCursorPos().getX() - a_imgui.imgui().getColumnOffset(a_imgui.imgui().getColumnIndex())), "\\."));
                } else {
                    a_imgui.text("");
                }
                a_imgui.imgui().nextColumn();


                {   // comboboxes for the clustering
                    int [] selectedComponent = {0};

                    ArchDef.Component clusteredTo = a_arch.getClusteredComponent(n);


                    Vec2 zeroSize = new Vec2(0, 0);
                    a_imgui.imgui().pushItemWidth(-1);
                    if (a_imgui.imgui().beginCombo("##"+n.getLogicName(), a_imgui.getLongestSubString(clusteredTo.getName(), columnWidths[a_imgui.imgui().getColumnIndex()] - a_imgui.imgui().getFrameHeightWithSpacing() - (a_imgui.imgui().getCursorPos().getX() - a_imgui.imgui().getColumnOffset(a_imgui.imgui().getColumnIndex())), "\\."), ComboFlag.None.getI())) {

                        for (int i = 0; i < a_arch.getComponentCount(); i++) {
                            ArchDef.Component c = a_arch.getComponent(i);
                            if (a_imgui.imgui().selectable(c.getName(), c == clusteredTo, SelectableFlag.None.getI(), zeroSize)) {
                                selectedComponent[0] = i;
                                c.clusterToNode(n, ArchDef.Component.ClusteringType.Manual);
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
                    String text = "" + Math.round(attractions[cIx] * 100D) / 100D;
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
    }


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
}
