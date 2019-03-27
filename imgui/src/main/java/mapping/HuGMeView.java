package mapping;

import archviz.HNode;
import glm_.vec2.Vec2;
import glm_.vec4.Vec4;
import gui.ImGuiWrapper;
import hiviz.Tree;
import imgui.ComboFlag;
import imgui.ImGui;
import imgui.SelectableFlag;
import imgui.internal.ColumnsFlag;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.HuGMeManual;
import se.lnu.siq.s4rdm3x.stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.Math.sin;

public class HuGMeView {
    private int[] m_viewSelection = {0};

    private final int g_hugMeParamsId = 0;
    private final int g_mappedEntitiesId = 1;
    private final int g_orphanEntitiesId = 2;
    private final int g_autoClusteredEntitiesId = 3;
    private final int g_humanClusteredEntitiesId = 4;

    private List<CNode> m_selectedMappedNodes;   // this one is unmodifiable
    private List<CNode> m_selectedOrphanNodes;  // this one is unmodifiable

    private ArrayList<CNode> m_autoClusteredOrphans = new ArrayList<>();
    private ArrayList<CNode> m_acceptedClusteredOrphans = new ArrayList<>();
    private float[] m_omega = {0.5f};
    private float[] m_phi = {0.5f};

    HuGMeManual m_hugme;


    public HuGMeView(List<CNode>a_mappedNodes, List<CNode>a_orphanNodes) {
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


    public static class Action {
        public static class DoMap {
            public String a_whatNodeName;
            public String a_toComponentName;
        }

        DoMap m_doMapAction;
    }

    void doHugMeParamsView(ImGuiWrapper a_imgui, ArchDef a_arch, HNode.VisualsManager a_nvm) {

        a_imgui.imgui().beginColumns("doHugMeParamsView", 2, 0);

        a_imgui.imgui().sliderFloat("Omega Threshold", m_omega, 0, 1, "%.2f", 1);
        a_imgui.imgui().sliderFloat("Phi Value", m_phi, 0, 1, "%.2f", 1);

        a_imgui.text("Mapped Entities: " + m_selectedMappedNodes.size());

        Tree tree = new Tree();
        for(ArchDef.Component c : a_arch.getComponents()) {
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

        a_imgui.imgui().nextColumn();

        Tree mappedTree = new Tree();
        for (CNode n : m_selectedMappedNodes) {
            ArchDef.Component c = a_arch.getMappedComponent(n);
            mappedTree.addNode(c.getName() + "." + n.getLogicName(), n);
        }
        //mappedTree.doTree(a_imgui.imgui(), "mappedNodesTreeHuGMeViz");

        class RectTreeDrawer implements Tree.TNodeVisitor {

            ImGuiWrapper m_imgui;
            HNode.VisualsManager m_nvm;
            Vec2 m_topLeft;

            RectTreeDrawer(ImGuiWrapper a_imgui, HNode.VisualsManager a_nvm, Vec2 a_topLeft) {
                m_nvm = a_nvm;
                m_imgui = a_imgui;
                m_topLeft = a_topLeft;
            }

            @Override
            public void visit(Tree.TNode a_node) {
                final int white = m_imgui.toColor(new Vec4(1., 1., 1., 1));

                Vec2 pos = new Vec2(m_topLeft);

                if (a_node.childCount() > 0 && a_node.getName() != null) {
                    a_imgui.addRect(m_topLeft, m_topLeft.plus(new Vec2(200, (a_node.fullChildCount() + 1) * a_imgui.getTextLineHeightWithSpacing())), white, 0, 0, 1);
                }

                if (a_node.getName() != null) {
                    a_imgui.addText(pos, white, a_node.getName());
                    pos.setY(pos.getY() + a_imgui.getTextLineHeightWithSpacing());
                }

                for (Tree.TNode c : a_node.children()) {
                    c.accept(new RectTreeDrawer(m_imgui, m_nvm, pos.plus(new Vec2(17, -1))));
                    pos.setY(pos.getY() + a_imgui.getTextLineHeightWithSpacing() * (1 + c.fullChildCount()));
                }
            }
        };
        mappedTree.doVisit(new RectTreeDrawer(a_imgui, a_nvm, a_imgui.imgui().getCursorPos().plus(new Vec2(150, 0))));


        //doPieVisualization(a_imgui, a_arch, a_nvm);



        a_imgui.imgui().endColumns();
    }

    private void doPieVisualization(ImGuiWrapper a_imgui, ArchDef a_arch, HNode.VisualsManager a_nvm) {
        final int white = a_imgui.toColor(new Vec4(1., 1., 1., 1));
        final int black = a_imgui.toColor(new Vec4(0., 0., 0., 1));
        Vec2 columnSize = new Vec2(a_imgui.imgui().getColumnWidth(1) - 10, (float) a_imgui.imgui().getContentRegionAvail().getY());
        a_imgui.imgui().beginChild("mappingandorphanpies", columnSize, true, 0);
        Vec2 offset = a_imgui.imgui().getCurrentWindow().getPos();
        Vec2 center = offset.plus(columnSize.times(0.5));

        Tree mappedTree = new Tree();
        Tree orphanTree = new Tree();

        for (CNode n : m_selectedMappedNodes) {
            ArchDef.Component c = a_arch.getMappedComponent(n);
            mappedTree.addNode(c.getName() + "." + n.getLogicName(), n);
        }

        for (CNode n : m_selectedOrphanNodes) {
            orphanTree.addNode(n.getLogicName(), n);
        }


        double outerRadius = Math.min(columnSize.getX(), columnSize.getY()) / 2 - 10;
        double innerRadius = outerRadius * 0.25;

        //
        Vec2 mousePos = a_imgui.getMousePos();
        Vec2 toMousePos = mousePos.minus(center);
        final float centerMousePosAngle = toMousePos.getY() > 0 ? (float) Math.atan2(toMousePos.getY(), toMousePos.getX()) : (float) (Math.PI * 2 + Math.atan2(toMousePos.getY(), toMousePos.getX()));
        CNode[] mouseHoverNode = {null};


        int nodeCount = m_selectedMappedNodes.size() + m_selectedOrphanNodes.size();
        double deltaAngle = (Math.PI * 2 ) / nodeCount;
        double spacingAngle = deltaAngle;

        HuGMePieDrawer mappedDrawer = new HuGMePieDrawer(a_imgui, a_nvm, spacingAngle, m_selectedMappedNodes.size() * deltaAngle - spacingAngle, outerRadius, innerRadius, center, mousePos, centerMousePosAngle);
        HuGMePieDrawer orphanDrawer = new HuGMePieDrawer(a_imgui, a_nvm, m_selectedMappedNodes.size() * deltaAngle + spacingAngle, 2 * Math.PI - deltaAngle, outerRadius, innerRadius, center, mousePos, centerMousePosAngle);
        mappedTree.doVisit(mappedDrawer);

        orphanTree.doVisit(orphanDrawer);


        for (HuGMePieDrawer.CurvePoints mcp : mappedDrawer.m_curvePoints) {
            for (HuGMePieDrawer.CurvePoints ocp : orphanDrawer.m_curvePoints) {

                if (mcp.m_node.hasDependency(ocp.m_node) || ocp.m_node.hasDependency(mcp.m_node)) {
                    Vec2 fromDir = ocp.m_end.minus(center).normalize();
                    Vec2 toDir = mcp.m_end.minus(center).normalize();


                    a_imgui.addCurve(ocp.m_end, center,
                            mcp.m_end, center,
                            white, 1);
                }
            }
        }

        a_imgui.imgui().endChild();
    }


}
