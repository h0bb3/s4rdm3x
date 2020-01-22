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
import se.lnu.siq.s4rdm3x.experiments.ExperimentRunData;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.Selector;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.util.SystemModelReader;
import se.lnu.siq.s4rdm3x.stats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class MappingView {
    private ArrayList<CNode> m_selectedMappedNodes = new ArrayList<>();
    private ArrayList<CNode> m_selectedOrphanNodes = new ArrayList<>();


    private final int g_hugMeParamsId = 0;
    private final int g_nbmapperParamsId = 1;
    private final int g_mappedEntitiesId = 2;
    private final int g_orphanEntitiesId = 3;
    private final int g_autoClusteredEntitiesId = 4;
    private final int g_humanClusteredEntitiesId = 5;
    private int[] m_viewSelection = {g_autoClusteredEntitiesId};

    HuGMeView m_hugmeView = new HuGMeView(Collections.unmodifiableList(m_selectedMappedNodes), Collections.unmodifiableList(m_selectedOrphanNodes));
    NBMapperView m_nbmapperView = new NBMapperView(Collections.unmodifiableList(m_selectedMappedNodes), Collections.unmodifiableList(m_selectedOrphanNodes));
    ResultView m_results = new ResultView();

    CGraph m_graph = new CGraph();


    private CNode m_selectedNode;
    private String m_mappingsFile = "C:\\hObbE\\projects\\coding\\research\\mappings.txt";

    public CNode getSelectedNode() {
        return m_selectedNode;
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

            m_graph = new CGraph();
            m_selectedMappedNodes.clear();
            for (String n : mappedNodes) {
                CNode node = a_g.getNode(n);
                CNode cpy = m_graph.createNode(n);
                cpy.shallowCopy(node);

                m_selectedMappedNodes.add(cpy);
            }
            m_selectedOrphanNodes.clear();
            for (String n : orphanNodes) {
                CNode node = a_g.getNode(n);
                CNode cpy = m_graph.createNode(n);
                cpy.shallowCopy(node);

                m_selectedOrphanNodes.add(cpy);
            }
        }

        if (iw.button("Random 20%", 150)) {
        }


        switch (m_viewSelection[0]) {
            case g_hugMeParamsId:
                m_hugmeView.doHugMeParamsView(iw, m_graph, a_arch, a_nvm, m_results);
                break;
            case g_nbmapperParamsId:
                m_nbmapperView.doNBMapperParamsView(iw, a_arch, a_nvm, a_g.getNodes(), m_results);
                if (m_nbmapperView.getSelectedNodeName().length() > 0) {
                    m_selectedNode = a_g.getNode(m_nbmapperView.getSelectedNodeName().replace(".", "/") + ".java");
                    m_nbmapperView.resetSelectedNodeName();
                }
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

        m_mappingsFile = a_imgui.inputTextSingleLine("##SaveMappingAs", m_mappingsFile);
        a_imgui.imgui().sameLine(0);
        if (a_imgui.button("Save Mapping", 0)) {
            SystemModelReader reader = new SystemModelReader();
            for (CNode n : m_selectedMappedNodes) {
                ArchDef.Component component = a_arch.getMappedComponent(n);
                SystemModelReader.Mapping m = new SystemModelReader.Mapping();
                m.m_moduleName = component.getName();
                m.m_regexp = n.getLogicName();
                reader.m_initialMappings.add(m);
            }
            try {
                reader.writeFile(m_mappingsFile);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        a_imgui.imgui().sameLine(0);
        if (a_imgui.button("Load Mapping", 0)) {
            SystemModelReader reader = new SystemModelReader();

            if (reader.readFile(m_mappingsFile)) {
                for (SystemModelReader.Mapping m : reader.m_initialMappings) {
                    for (CNode n : a_g.getNodes(new Selector.Pat(m.m_regexp))) {

                        if (!containsByName(m_selectedMappedNodes, n)) {
                            TNodeCollectionWorker.doAddNode(n, m_graph, m_selectedMappedNodes);
                        }
                    }
                }
            }
        }

        hiviz.Tree tree = new hiviz.Tree();
        int nodeCount = 0;
        for (CNode n : a_g.getNodes()) {
            if (!containsByName(m_selectedOrphanNodes, n) && !containsByName(m_selectedMappedNodes, n)) {
                ArchDef.Component component = a_arch.getMappedComponent(n);

                for (dmClass c : n.getClasses()) {
                    if (!c.isInner()) {
                        if (component != null) {
                            Tree.TNode tn = tree.addNode(n.getName().replace("/", ".").replace(".java", ""), n);
                            tn.setName(tn.getName());
                            tn.setMapping(component.getName(), a_nvm.getBGColor(component.getName()), component);
                            nodeCount++;
                        }
                    }
                }
            }
        }

        a_imgui.text("Unselected Mapped Nodes: " + nodeCount);
        Tree.TNode hovered = tree.doTree(a_imgui.imgui(), "notSelectedMappedEntitiesTree");
        if (hovered != null && a_imgui.isMouseDoubleClicked(0)) {
            hovered.accept(TNodeCollectionWorker.doAdd(m_graph, m_selectedMappedNodes));
        }

        a_imgui.imgui().nextColumn();


        a_imgui.text("Selected Mapped Nodes: " + m_selectedMappedNodes.size());
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
            hovered.accept(TNodeCollectionWorker.doRemove(m_graph, m_selectedMappedNodes));
        }

        a_imgui.imgui().endColumns();
    }

    private boolean containsByName(Iterable<CNode> a_collection, CNode a_node) {
        for (CNode n : a_collection) {
            if (n.getName().equals(a_node.getName())) {
                return true;
            }
        }
        return false;
    }

    private void doSelectOrphanEntities(ImGuiWrapper a_imgui, CGraph a_g, ArchDef a_arch, HNode.VisualsManager a_nvm) {
        a_imgui.imgui().beginColumns("doSelectOrphanEntitiesColumns", 2, 0);

        hiviz.Tree tree = new hiviz.Tree();
        int nodeCount = 0;
        for (CNode n : a_g.getNodes()) {

            if (!containsByName(m_selectedOrphanNodes, n) && !containsByName(m_selectedMappedNodes, n)) {
                ArchDef.Component component = a_arch.getMappedComponent(n);

                if (component != null) {
                    Tree.TNode tn = tree.addNode(n.getLogicName().replace("/", ".").replace(".java", ""), n);
                    tn.setName(tn.getName());
                    tn.setMapping(component.getName(), a_nvm.getBGColor(component.getName()), component);
                    nodeCount++;
                }
            }
        }

        a_imgui.text("Unselected Entities: " + nodeCount);
        Tree.TNode hovered = tree.doTree(a_imgui.imgui(), "notSelectedOrphanEntitiesTree");
        if (hovered != null && a_imgui.isMouseDoubleClicked(0)) {
            hovered.accept(TNodeCollectionWorker.doAdd(m_graph, m_selectedOrphanNodes));
        }

        a_imgui.imgui().nextColumn();


        a_imgui.text("Selected Orphan Entities: " + m_selectedOrphanNodes.size());
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
            hovered.accept(TNodeCollectionWorker.doRemove(m_graph, m_selectedOrphanNodes));
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
        a_imgui.text("Mapping Results:");

        Tree.TNode hovered = doClusteredTree(a_imgui, m_hugmeView.autoClusteredOrphans(), "notSelectedHuGMeClusteredEntitiesTree", a_arch, a_g, a_nvm);
        if (hovered != null && a_imgui.isMouseDoubleClicked(0)) {
            hovered.accept(a_node -> {
                Object o = a_node.getObject();
                if (o != null) {
                }
            });
        }

        ExperimentRunData.BasicRunData rd = new ExperimentRunData.BasicRunData();
        m_nbmapperView.fillRunData(rd, a_arch, a_g);
        //a_imgui.text(String.format("Performance %.2f, Precision: %.2f, Recall: %.2f", rd.calcAutoPerformance(), rd.calcAutoPrecision(), rd.calcAutoRecall()));

        a_imgui.text("NBMapper Auto Clustered Entities: " + m_nbmapperView.autoClusteredOrphanCount());
        hovered = doClusteredTree(a_imgui, m_nbmapperView.autoClusteredOrphans(), "notSelectedNBMapperClusteredEntitiesTree", a_arch, a_g, a_nvm);
        if (hovered != null && a_imgui.isMouseDoubleClicked(0)) {
            hovered.accept(a_node -> {
                Object o = a_node.getObject();
                if (o != null) {
                }
            });
        }

        a_imgui.imgui().nextColumn();

        final float columnWidth = a_imgui.imgui().getColumnWidth(a_imgui.imgui().getColumnIndex());




        /*a_imgui.text("HuGMe Mapping Results");
        doClusteringTable(m_hugmeView.autoClusteredOrphans(), m_hugmeView.autoClusteredOrphanCount(), a_imgui, a_g, a_arch, "HuGMEAutoClusteredOrphans", columnWidth);

        a_imgui.imgui().separator();
        a_imgui.text("NBMapper Mapping Results");*/


        //doClusteringTable(m_nbmapperView.autoClusteredOrphans(), m_nbmapperView.autoClusteredOrphanCount(), a_imgui, a_g, a_arch, "NBMapperAutoClusteredOrphans", columnWidth);

        m_results.doShow(a_imgui, a_g, a_arch, columnWidth);

        /*if (a_imgui.button("Accept to Main", 0)) {
            for (CNode n : m_nbmapperView.autoClusteredOrphans()) {
                CNode graphNode = a_g.getNode(n.getName());
                a_arch.getClusteredComponent(n).mapToNode(graphNode);
            }

            m_hugmeView.clearAutoClusteredOrphans();
            m_nbmapperView.clearAutoClusteredOrphans();
            //m_selectedMappedNodes.clear();
            //m_selectedOrphanNodes.clear();
        }

        a_imgui.imgui().sameLine(0);


        if (a_imgui.button("Accept to Mapped", 0)) {
            for (CNode n : m_nbmapperView.autoClusteredOrphans()) {
                a_arch.getClusteredComponent(n).mapToNode(n);
                m_selectedMappedNodes.add(n);
                m_selectedOrphanNodes.remove(m_nbmapperView.getByName(m_selectedOrphanNodes, n.getName()));
            }

            m_hugmeView.clearAutoClusteredOrphans();
            m_nbmapperView.clearAutoClusteredOrphans();
        }*/

        a_imgui.imgui().endColumns();
    }

    private void doClusteringTable(Iterable<CNode> a_nodes, int a_nodeCount, ImGuiWrapper a_imgui, CGraph a_g, ArchDef a_arch, String a_tableId, float a_width) {
        // table of attraction values
        final int columnCount = a_arch.getComponentCount() + 5;
        Vec2 columnSize = new Vec2( a_width - 20, (a_nodeCount) * a_imgui.imgui().getFrameHeightWithSpacing());

        final float maxHeight = columnSize.getY() + 2 * a_imgui.imgui().getFrameHeightWithSpacing() < a_imgui.imgui().getContentRegionMax().getY() - a_imgui.imgui().getCursorPosY() ? columnSize.getY() + 2 * a_imgui.imgui().getFrameHeightWithSpacing() : a_imgui.imgui().getContentRegionMax().getY() - a_imgui.imgui().getCursorPosY();

        // we need this to be able to handle columns in columns problems.
        a_imgui.imgui().beginChild(a_tableId + "OuterChildWindow", new Vec2(a_width - 10, maxHeight), false, 0);

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

        a_imgui.imgui().beginChild(a_tableId+"ScrollTable", new Vec2(a_width - 10, 0), false, 0);
        //a_imgui.imgui().beginChild(a_tableId+"RowTable", new Vec2(a_width - 55, (float)columnSize.getY()), true, 0);

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
                m_selectedNode = a_g.getNode(n.getName());
            }
            a_imgui.imgui().nextColumn();


            ArchDef.Component mappedComponent = a_arch.getMappedComponent(n);
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
        //a_imgui.imgui().endChild();
        a_imgui.imgui().endChild();
        a_imgui.imgui().endChild();
    }

    public void setInitialNBData(ExperimentRunData.NBMapperData a_data, CGraph a_system, ArchDef a_arch) {

        ArrayList<CNode> orphans = new ArrayList<>();
        CGraph creator = new CGraph();

        for (CNode n : a_system.getNodes()) {
            ArchDef.Component m = a_arch.getMappedComponent(n);
            if (m != null) {
                CNode initialNode = m_nbmapperView.getByName(a_data.getInitialClusteringNodes(), n.getName());
                if (initialNode == null) {
                    CNode cpy = creator.createNode(n.getName());
                    cpy.shallowCopy(n);
                    orphans.add(cpy);
                }
            }
        }

        setInitial(a_data.getInitialClusteringNodes(), orphans);
        m_nbmapperView.setAutoClusteredNodes(a_data.getAutoClusteredNodes(), a_system.getNodes());
        m_nbmapperView.setInitialParameters(a_data);

    }

    public void setInitial(Iterable<CNode> a_initialClustering, ArrayList<CNode> a_orphans) {
        m_selectedMappedNodes.clear();
        a_initialClustering.forEach(n -> m_selectedMappedNodes.add(n));
        m_selectedOrphanNodes.clear();
        m_selectedOrphanNodes.addAll(a_orphans);
    }


    private abstract static class TNodeCollectionWorker implements Tree.TNodeVisitor {

        private Collection<CNode> m_collection;
        private CGraph m_graph;

        protected TNodeCollectionWorker(CGraph a_graph, Collection a_collection){
            m_graph = a_graph;
            m_collection = a_collection;
        }

        static public void doAddNode(CNode a_nodeToAdd, CGraph a_graph, Collection<CNode> a_collection) {
            CNode cpy = a_graph.createNode(a_nodeToAdd.getName());
            cpy.shallowCopy(a_nodeToAdd);
            a_collection.add(cpy);
        }

        static public Tree.TNodeVisitor doAdd(CGraph a_graph, Collection<CNode> a_collection) {
            return new TNodeCollectionWorker(a_graph, a_collection) {
                @Override
                protected void doWork(CNode a_node, CGraph a_graph, Collection a_collection) {

                    doAddNode(a_node, a_graph, a_collection);
                }
            };
        }

        static public Tree.TNodeVisitor doRemove(CGraph a_graph, Collection<CNode> a_collection) {
            return new TNodeCollectionWorker(a_graph, a_collection) {
                @Override
                protected void doWork(CNode a_node,  CGraph a_graph, Collection a_collection) {
                    a_graph.removeNode(a_node);
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
                doWork((CNode) o, m_graph, m_collection);
            }
        }

        protected abstract void doWork(CNode a_node, CGraph a_graph, Collection a_collection);

    }
}
