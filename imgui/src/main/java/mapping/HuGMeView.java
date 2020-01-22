package mapping;

import archviz.HNode;
import glm_.vec2.Vec2;
import glm_.vec4.Vec4;
import gui.ImGuiWrapper;
import hiviz.Tree;
import imgui.*;
import imgui.internal.ColumnsFlag;
import imgui.internal.Rect;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.HuGMe;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.HuGMeManual;
import se.lnu.siq.s4rdm3x.stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.sin;

public class HuGMeView extends MapperBaseView {
    private int[] m_viewSelection = {0};

    private final int g_hugMeParamsId = 0;
    private final int g_mappedEntitiesId = 1;
    private final int g_orphanEntitiesId = 2;
    private final int g_autoClusteredEntitiesId = 3;
    private final int g_humanClusteredEntitiesId = 4;

    private float[] m_omega = {0.5f};
    private float[] m_phi = {0.5f};

    HuGMe m_hugme;
    private float m_hugMeRectDrawerHeight = 0;


    public HuGMeView(List<CNode>a_mappedNodes, List<CNode>a_orphanNodes) {
        super(a_mappedNodes, a_orphanNodes);
    }

    public static class Action {
        public static class DoMap {
            public String a_whatNodeName;
            public String a_toComponentName;
        }

        DoMap m_doMapAction;
    }

    void doHugMeParamsView(ImGuiWrapper a_imgui, CGraph a_g, ArchDef a_arch, HNode.VisualsManager a_nvm, ResultView a_result) {

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
            m_hugme = new HuGMe((double)m_omega[0], (double)m_phi[0], false, a_arch);
            CGraph g = createGraph();

            m_hugme.run(g);

            //setAutoClusteredNodes(m_hugme.m_clusteredElements, m_selectedOrphanNodes);
            a_result.addResult("HuGMe", g);
        }

        if (m_hugme != null) {
            a_imgui.text("HuGMe automatically mapped nodes: " + m_hugme.getAutoClusteredOrphanCount());
            a_imgui.text("HuGMe considered nodes: " + m_hugme.m_consideredNodes);
            a_imgui.text("HuGMe mapped nodes from start: " + m_hugme.m_mappedNodesFromStart);
            a_imgui.text("HuGMe unmapped nodes from start: " + m_hugme.m_unmappedNodesFromStart);
        }

        a_imgui.imgui().nextColumn();

        if (m_selectedMappedNodes.size() + m_selectedOrphanNodes.size() > 17) {
            a_imgui.text("Visualization Disabled due to performance problems. Remove some nodes.");
            a_imgui.imgui().endColumns();
            return;
        }

        final float columnWidth = a_imgui.imgui().getColumnWidth(1);
        Vec2 columnSize = new Vec2(columnWidth - 10, (float) a_imgui.imgui().getContentRegionAvail().getY());
        a_imgui.imgui().beginChild("mappingandorphanpies", columnSize, true, 0);

        a_imgui.imgui().beginColumns("mappedorphanmappedcolumns", 5, 0);
        a_imgui.text("Mapped Nodes");
        a_imgui.imgui().nextColumn();
        a_imgui.text("Dependencies");
        a_imgui.imgui().nextColumn();
        a_imgui.text("Orphan Nodes");
        a_imgui.imgui().nextColumn();
        a_imgui.text("Dependencies");
        a_imgui.imgui().nextColumn();
        a_imgui.text("Orphan Nodes");



        final int columnCount = 5;
        float [] columnWidths = new float[columnCount];
        for (int cIx = 0; cIx < columnCount; cIx++) {
            columnWidths[cIx] = a_imgui.imgui().getColumnWidth(cIx);
        }

        columnWidths[columnCount - 1] -= 20;    // scrollbar
        a_imgui.imgui().endColumns();


        columnSize = new Vec2(columnWidth - 20, (float) a_imgui.imgui().getContentRegionAvail().getY());
        a_imgui.imgui().beginChild("mappingandorphanpies_body", columnSize, false, 0);
        columnSize.setY(m_hugMeRectDrawerHeight);
        a_imgui.imgui().beginChild("mappingandorphanpies_inner_body", columnSize, false, 0);

        Vec2 offset = a_imgui.imgui().getCurrentWindow().getPos();

        Tree mappedTree = new Tree();
        for (CNode n : m_selectedMappedNodes) {
            ArchDef.Component c = a_arch.getMappedComponent(n);
            mappedTree.addNode(c.getName() + "." + n.getLogicName(), n);
        }
        //mappedTree.doTree(a_imgui.imgui(), "mappedNodesTreeHuGMeViz");


        final float yPos = a_imgui.imgui().getCursorPosY();


        final int columnPadding = 7;    // TODO: find the correct value

        RectTreeDrawer rtdMapped = new RectTreeDrawer(a_imgui, a_nvm, offset.plus(new Vec2(0, yPos)), columnWidths[0] - columnPadding, m_selectedOrphanNodes, RectTreeDrawer.Align.Right, RectTreeDrawer.FanType.InOut);
        mappedTree.doVisit(rtdMapped);
        final float fromMappedHeight = rtdMapped.m_height;
        Iterable<RectTreeDrawer.LeafNodeDrawData> fromMappedDrawData = rtdMapped.getDrawData();

        Tree orphanTree = new Tree();
        for (CNode n : m_selectedOrphanNodes) {
            orphanTree.addNode(n.getLogicName(), n);
        }

        ArrayList<CNode> allSelected = new ArrayList<>();
        allSelected.addAll(m_selectedOrphanNodes);
        allSelected.addAll(m_selectedMappedNodes);

        RectTreeDrawer rtdOrphans = new RectTreeDrawer(a_imgui, a_nvm, offset.plus(new Vec2(columnWidths[0]+columnWidths[1]-columnPadding, yPos)), columnWidths[2], allSelected, RectTreeDrawer.Align.Center, RectTreeDrawer.FanType.InOut);
        orphanTree.doVisit(rtdOrphans);
        Iterable<RectTreeDrawer.LeafNodeDrawData> orphanDrawData = rtdOrphans.getDrawData();

        rtdMapped = new RectTreeDrawer(a_imgui, a_nvm, offset.plus(new Vec2(columnWidths[0]+columnWidths[1]+columnWidths[2]+columnWidths[3]-columnPadding, yPos)), columnWidths[4], m_selectedOrphanNodes, RectTreeDrawer.Align.Left, RectTreeDrawer.FanType.InOut);
        orphanTree.doVisit(rtdMapped);
        Iterable<RectTreeDrawer.LeafNodeDrawData> toMappedDrawData = rtdMapped.getDrawData();

        drawDependencies(a_imgui, fromMappedDrawData, orphanDrawData, a_nvm, true);
        drawDependencies(a_imgui, orphanDrawData, toMappedDrawData, a_nvm, false);

        //doPieVisualization(a_imgui, a_arch, a_nvm, columnsize);

        m_hugMeRectDrawerHeight = Math.max(Math.max(rtdMapped.getHeight(), rtdOrphans.getHeight()), fromMappedHeight);


        a_imgui.imgui().endChild();
        a_imgui.imgui().endChild();
        a_imgui.imgui().endChild();
        a_imgui.imgui().endColumns();
    }

    private void drawDependencies(ImGuiWrapper a_imgui, Iterable<RectTreeDrawer.LeafNodeDrawData> a_from, Iterable<RectTreeDrawer.LeafNodeDrawData> a_to, HNode.VisualsManager a_nvm, boolean a_useMappingColor) {
        final int white = a_imgui.toColor(new Vec4(1., 1., 1., 0.50));
        int outlineColor = white;

        class SelectedPath {
            ArrayList<Vec2> m_upperPoints = null;
            ArrayList<Vec2> m_lowerPoints = null;
            int m_color = 0;
            int m_outlineColor = 0;
        };

        ArrayList<SelectedPath> selectedPaths = new ArrayList<>();


        DrawList dl = a_imgui.imgui().getWindowDrawList();
        Vec2 whitePixelUV = a_imgui.imgui().getDrawListSharedData().getTexUvWhitePixel();

        for (RectTreeDrawer.LeafNodeDrawData fromDD : a_from) {
            CNode fromNode = (CNode)fromDD.m_node.getObject();
            ArrayList<RectTreeDrawer.LeafNodeDrawData> fanTo = new ArrayList<>();
            int totalFan = 0;

            Rect nodeRect = new Rect(fromDD.m_topLeft, fromDD.m_bottomRight);
            final boolean isInsideFromNode = a_imgui.isInside(nodeRect, a_imgui.getMousePos());



            for (RectTreeDrawer.LeafNodeDrawData toDD : a_to) {

                CNode toNode;

                toNode = (CNode)toDD.m_node.getObject();

                if (toNode == fromNode) {
                    continue;
                }

                if (fromNode.hasDependency(toNode)) {
                    fanTo.add(toDD);
                    totalFan += fromNode.getDependencyCount(toNode);
                }
                if (toNode.hasDependency(fromNode)) {
                    fanTo.add(toDD);
                    totalFan += toNode.getDependencyCount(fromNode);
                }
            }

            float heightStart = fromDD.getHeight();

            Vec2 endOffset = new Vec2(-75, 0);
            Vec2 startOffset = new Vec2(75, 0);

            Vec4 color = null;
            Vec4 outlineColorV4 = null;

            if (a_useMappingColor && a_nvm.hasBGColor(fromNode.getMapping())) {
                color =  a_nvm.getBGColor(fromNode.getMapping());
                outlineColorV4 = a_nvm.getTextColor(fromNode.getMapping());
            } else if (!a_useMappingColor) {
                //color = new Vec4(0.25, 0.25, 0.25, 1);
                color = a_imgui.imgui().getStyleColorVec4(Col.TitleBgActive);
                outlineColorV4 = a_imgui.imgui().getStyleColorVec4(Col.Separator);
            }


            float yOffsetStart = 0;
            for (RectTreeDrawer.LeafNodeDrawData toDD : fanTo) {
                CNode toNode = (CNode)toDD.m_node.getObject();
                int intColor = white;
                if (color == null) {
                    if (a_nvm.hasBGColor(toNode.getMapping())) {
                        color =  a_nvm.getBGColor(toNode.getMapping());
                        outlineColorV4 = a_nvm.getTextColor(toNode.getMapping());
                    }
                }

                if (color != null) {
                    intColor = a_imgui.toColor(color);
                }

                if (outlineColorV4 != null) {
                    outlineColor = a_imgui.toColor(outlineColorV4);
                }

                float heightEnd = toDD.getHeight();

                final int fan = fromNode.getDependencyCount(toNode) + toNode.getDependencyCount(fromNode);
                float yRatioStart = heightStart *  (fan / (float)fromDD.m_fan);
                float yRatioEnd = heightEnd * (fan / (float)toDD.m_fan);  // the fan out to toDD is the fanIn from the mapped node

                Vec2 start, end;
                ArrayList<Vec2> upper = new ArrayList<>();
                ArrayList<Vec2> lower = new ArrayList<>();

                //start = fromDD.calcMiddleRight();

                end = new Vec2((float)toDD.m_topLeft.getX(), toDD.m_topLeft.getY() + toDD.m_yOffset);
                start = new Vec2((float)fromDD.m_bottomRight.getX(), fromDD.m_topLeft.getY() + yOffsetStart);
                a_imgui.getCurvePoints(32, upper, start, start.plus(startOffset), end, end.plus(endOffset));

                start.setY(start.getY() + yRatioStart);
                end.setY(end.getY() + yRatioEnd);
                a_imgui.getCurvePoints(32, lower, start, start.plus(startOffset), end, end.plus(endOffset));
                toDD.m_yOffset += yRatioEnd;
                yOffsetStart += yRatioStart;


                int vtxOffset = dl.get_vtxCurrentIdx();

                boolean selectedNode = isInsideFromNode;
                if (selectedNode != true) {
                    nodeRect.setMin(toDD.m_topLeft);
                    nodeRect.setMax(toDD.m_bottomRight);
                    selectedNode = a_imgui.isInside(nodeRect, a_imgui.getMousePos());
                }
                if (selectedNode) {
                    // paths should be drawn as selected
                    SelectedPath sp = new SelectedPath();
                    sp.m_color = intColor;
                    sp.m_outlineColor = outlineColor;
                    sp.m_upperPoints = upper;
                    sp.m_lowerPoints = lower;
                    selectedPaths.add(sp);
                    continue;
                }

                dl.primReserve((upper.size() - 1) * 6, upper.size() * 2);
                addCurveDrawVerts(intColor, upper, lower, dl, whitePixelUV);

                if (selectedPaths.size() == 0) {
                    Rect curveBoundingRect = new Rect();

                    curveBoundingRect.setMin(new Vec2((float) upper.get(0).getX(), Math.min(upper.get(0).getY(), upper.get(upper.size() - 1).getY())));
                    curveBoundingRect.setMax(new Vec2((float) lower.get(lower.size() - 1).getX(), Math.max(lower.get(0).getY(), lower.get(lower.size() - 1).getY())));

                    Vec2 mousePos = a_imgui.getMousePos();
                    if (a_imgui.isInside(curveBoundingRect, mousePos)) {

                        /*a_imgui.beginTooltip();
                        a_imgui.text("inside curve rect");
                        a_imgui.endTooltip();*/

                        Rect quadRect = new Rect();
                        boolean isInsideQuad = false;
                        //addAllCurveQuadIndices(upper.size() - 1, dl, vtxOffset, quadRect, a_imgui.getMousePos());
                        if (addAllCurveQuadIndices(upper.size() - 1, dl, vtxOffset, quadRect, a_imgui.getMousePos()) && a_imgui.isInside(quadRect, a_imgui.getMousePos())) {
                            SelectedPath sp = new SelectedPath();
                            sp.m_color = intColor;
                            sp.m_upperPoints = upper;
                            sp.m_lowerPoints = lower;
                            selectedPaths.add(sp);
                        }
                    } else {
                        addAllCurveQuadIndices(upper.size() - 1, dl, vtxOffset, null, null);
                    }
                } else {
                    addAllCurveQuadIndices(upper.size() - 1, dl, vtxOffset, null, null);
                }

                a_imgui.imgui().getWindowDrawList().addPolyline(upper, outlineColor, false, 1);
                a_imgui.imgui().getWindowDrawList().addPolyline(lower, outlineColor, false, 1);
            }
        }

        for (SelectedPath sp : selectedPaths) {
            final int white25 = a_imgui.toColor(new Vec4(1., 1., 1., 0.25));

            // draw with original colors so we end up on top.
            int vtxOffset = dl.get_vtxCurrentIdx();
            dl.primReserve((sp.m_upperPoints.size() - 1) * 6, sp.m_upperPoints.size() * 2);
            addCurveDrawVerts(sp.m_color, sp.m_upperPoints, sp.m_lowerPoints, dl, whitePixelUV);
            addAllCurveQuadIndices(sp.m_upperPoints.size() - 1, dl,  vtxOffset, null, null);
            a_imgui.imgui().getWindowDrawList().addPolyline(sp.m_upperPoints, sp.m_outlineColor, false, 1);
            a_imgui.imgui().getWindowDrawList().addPolyline(sp.m_lowerPoints, sp.m_outlineColor, false, 1);

            // draw with overlay colors.
            vtxOffset = dl.get_vtxCurrentIdx();
            dl.primReserve((sp.m_upperPoints.size() - 1) * 6, sp.m_upperPoints.size() * 2);
            addCurveDrawVerts(white25, sp.m_upperPoints, sp.m_lowerPoints, dl, whitePixelUV);
            addAllCurveQuadIndices(sp.m_upperPoints.size() - 1, dl,  vtxOffset, null, null);
            a_imgui.imgui().getWindowDrawList().addPolyline(sp.m_upperPoints, sp.m_outlineColor, false, 1);
            a_imgui.imgui().getWindowDrawList().addPolyline(sp.m_lowerPoints, sp.m_outlineColor, false, 1);
        }
    }

    private void addCurveDrawVerts(int a_color, ArrayList<Vec2> a_upperCurvePoints, ArrayList<Vec2> a_lowerCurvePoints, DrawList dl, Vec2 a_uv) {
        for (int i = 0; i < a_upperCurvePoints.size(); i++) {
            dl.primWriteVtx(a_upperCurvePoints.get(i), a_uv, a_color);
            dl.primWriteVtx(a_lowerCurvePoints.get(i), a_uv, a_color);

        }
    }

    private boolean addAllCurveQuadIndices(int a_quadCount, DrawList dl, int vtxOffset, Rect a_quadRect, Vec2 a_checkPoint) {
        boolean isInsideQuad = a_quadRect == null;
        ArrayList<DrawVert> verts = dl.getVtxBuffer();
        for (int qIx = 0; qIx < a_quadCount; qIx++) {
            final int tIx = vtxOffset + qIx * 2;


            dl.primWriteIdx(tIx + 0);
            dl.primWriteIdx(tIx + 1);
            dl.primWriteIdx(tIx + 2);

            dl.primWriteIdx(tIx + 2);
            dl.primWriteIdx(tIx + 1);
            dl.primWriteIdx(tIx + 3);

            if (!isInsideQuad) {
                a_quadRect.setMin(new Vec2((float) verts.get(tIx + 0).getPos().getX(), Math.min(verts.get(tIx + 0).getPos().getY(), verts.get(tIx + 2).getPos().getY())));
                a_quadRect.setMax(new Vec2((float) verts.get(tIx + 3).getPos().getX(), Math.max(verts.get(tIx + 3).getPos().getY(), verts.get(tIx + 1).getPos().getY())));
                isInsideQuad = a_quadRect.contains(a_checkPoint);//a_imgui.isInside(quadRect, a_imgui.getMousePos());
            }
        }

        return isInsideQuad;
    }

    /*private void addCurveQuadIndices(DrawList dl, int vtxOffset) {


    }*/

    private void doPieVisualization(ImGuiWrapper a_imgui, ArchDef a_arch, HNode.VisualsManager a_nvm, Vec2 a_size) {
        final int white = a_imgui.toColor(new Vec4(1., 1., 1., 1));
        final int black = a_imgui.toColor(new Vec4(0., 0., 0., 1));
        Vec2 offset = a_imgui.imgui().getCurrentWindow().getPos();

        Vec2 center = offset.plus(a_size.times(0.5));

        Tree mappedTree = new Tree();
        Tree orphanTree = new Tree();

        for (CNode n : m_selectedMappedNodes) {
            ArchDef.Component c = a_arch.getMappedComponent(n);
            mappedTree.addNode(c.getName() + "." + n.getLogicName(), n);
        }

        for (CNode n : m_selectedOrphanNodes) {
            orphanTree.addNode(n.getLogicName(), n);
        }


        double outerRadius = Math.min(a_size.getX(), a_size.getY()) / 2 - 10;
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
    }


}
