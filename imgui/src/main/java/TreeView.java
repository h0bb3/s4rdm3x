import archviz.HNode;
import glm_.vec2.Vec2;
import glm_.vec4.Vec4;
import gui.ImGuiWrapper;
import hiviz.Tree;
import imgui.ImGui;
import jogamp.opengl.glu.nurbs.Curve;
import org.graphstream.graph.Graph;
import se.lnu.siq.s4rdm3x.model.cmd.hugme.HuGMe;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.ArrayList;

import static java.lang.Math.sin;

public class TreeView {
    private int[] m_treeViewSelection = {1};
    private String[] treeViewRoots = {"", "", ""};
    Tree.TNode m_selectedNode;
    CNode m_selectedClass;

    private final int g_archId = 0;
    private final int g_classesId = 1;
    private final int g_filesId = 2;

    public static class Action {
        public static class DoMap {
            public String a_whatNodeName;
            public String a_toComponentName;
        }

        DoMap m_doMapAction;
    }

    float m_totalTime = 0;



    Action doTreeView(ImGui a_imgui, HuGMe.ArchDef a_arch, CGraph a_g, HNode.VisualsManager a_nvm) {
        Action ret = null;
        hiviz.Tree tree = new hiviz.Tree();
        ArrayList<String> items = new ArrayList<>();
        items.add("Architecture");
        items.add("Classes");
        items.add("Files");

        if (a_imgui.combo("", m_treeViewSelection, items, items.size())) {
        }

        a_imgui.beginColumns("TreeViewColumns", 2, 0);

        ImGuiWrapper iw = new ImGuiWrapper(a_imgui);
        treeViewRoots[m_treeViewSelection[0]] = iw.inputTextSingleLine("Root", treeViewRoots[m_treeViewSelection[0]]);

        switch (m_treeViewSelection[0]) {
            case g_archId: {
                tree = buildArchitectureTree(a_arch.getComponents(), getArchRootFilter());
            } break;
            case g_classesId: {
                for (CNode n : a_g.getNodes()) {

                    HuGMe.ArchDef.Component component = a_arch.getMappedComponent(n);

                    for (dmClass c : n.getClasses()) {
                        if (!c.isInner()) {
                            if (c.getName().startsWith(treeViewRoots[1])) {

                                Tree.TNode tn = tree.addNode(n.getName().replace("/", ".").replace(".java", ""), n);
                                if (component != null) {
                                    tn.setName(tn.getName());
                                    tn.setMapping(component.getName(), a_nvm.getBGColor(component.getName()), component);
                                }
                            }
                        } else {
                            // tree.addNode(c.getName().replace("$", ".") + " (inner class)");
                        }
                    }
                }
            } break;
            case g_filesId: {
            } break;
        }


        //NodeUtil nu = new NodeUtil(a_g);

        Tree.TNode selected = tree.doTree(a_imgui, null);

        if (m_selectedNode == null && selected != null) {
            m_selectedNode = selected;
        }

        if (selected != null && selected.getObject() != null && iw.isMouseClicked(0, false) && m_treeViewSelection[0] == g_classesId) {
            m_selectedClass = (CNode) selected.getObject();
        }

        if (selected != null) {
            iw.text(selected.getName());
        }

        if (m_selectedNode != null) {
            switch (m_treeViewSelection[0]) {
                case g_archId:
                    if (a_imgui.beginPopupContextWindow("TreeViewContextMenuArch", 1, true)) {

                        a_imgui.menuItem("Some Arch Menu", "", false, true);

                        a_imgui.endPopup();
                    } else {
                        m_selectedNode = null;
                    }
                    break;

                case g_classesId:
                    if (a_imgui.beginPopupContextWindow("TreeViewContextMenuClasses", 1, true)) {

                        Tree at = buildArchitectureTree(a_arch.getComponents(), "");
                        Object mappedObject = m_selectedNode.getMappedObject(0);
                        Tree.TNode selectedNode = at.doMenu(a_imgui, mappedObject);

                        if (selectedNode != null) {
                            if (selectedNode != mappedObject) {
                                ret = new Action();
                                ret.m_doMapAction = new Action.DoMap();
                                ret.m_doMapAction.a_whatNodeName = m_selectedNode.getFullName().replace(".", "/");
                                ret.m_doMapAction.a_toComponentName = selectedNode.getFullName();

                                System.out.println("what: " + ret.m_doMapAction.a_whatNodeName);
                                System.out.println("to: " + ret.m_doMapAction.a_toComponentName);
                            }
                        }

                        a_imgui.endPopup();
                    } else {
                        m_selectedNode = null;
                    }


                break;
            }
        }


        a_imgui.nextColumn();
            if (m_treeViewSelection[0] == g_classesId && m_selectedClass != null) {
                doClassView(iw, a_g, a_arch, a_nvm, m_selectedClass);
            }

        a_imgui.endColumns();

        return ret;
    }

    private Tree buildArchitectureTree(Iterable<HuGMe.ArchDef.Component> a_components, String a_filter) {
        Tree tree = new Tree();
        for(HuGMe.ArchDef.Component c : a_components) {
            if (a_filter.length() == 0 || c.getName().startsWith(a_filter)) {
                tree.addNode(c.getName(), c);
            }
        }

        return tree;
    }

    private void doClassView(ImGuiWrapper a_imgui, CGraph a_g, HuGMe.ArchDef a_arch, HNode.VisualsManager a_nvm, CNode a_selectedNode) {

        Vec2 columnSize = new Vec2(a_imgui.imgui().getColumnWidth(1) - 10, (float) a_imgui.imgui().getContentRegionAvail().getY());
        a_imgui.imgui().beginChild("classcenterview", columnSize, true, 0);
        Vec2 offset = a_imgui.imgui().getCurrentWindow().getPos();
        Vec2 center = offset.plus(columnSize.times(0.5));


        final int white = a_imgui.toColor(new Vec4(1., 1., 1., 1));
        final int black = a_imgui.toColor(new Vec4(0., 0., 0., 1));

        int maxFanIn = 0, maxFanOut = 0;
        ArrayList<CNode> fanin = new ArrayList<>();
        for (CNode n : a_g.getNodes()) {
            if (n != a_selectedNode && n.hasDependency(a_selectedNode)) {

                fanin.add(n);
                int count = n.getDependencyCount(a_selectedNode);
                if (count > maxFanIn) {
                    maxFanIn = count;
                }
            }
        }

        ArrayList<CNode> fanout = new ArrayList<>();
        for (CNode n : a_g.getNodes()) {
            if (n != a_selectedNode && a_selectedNode.hasDependency(n)) {
                a_imgui.text(n.getLogicName());
                fanout.add(n);
                int count = a_selectedNode.getDependencyCount(n);
                if (count > maxFanOut) {
                    maxFanOut = count;
                }
            }
        }

        //Tree archTree = buildArchitectureTree(a_arch.getComponents(), "");

        Tree archTree = new Tree();

        for (CNode n : fanin) {
            HuGMe.ArchDef.Component mapped = a_arch.getMappedComponent(n);
            if (mapped == null) {
                archTree.addNode("unmapped." + n.getLogicNameSimple(), n);
            } else {
                archTree.addNode(mapped.getName() + "." + n.getLogicNameSimple(), n);
            }
        }

        for (CNode n : fanout) {
            HuGMe.ArchDef.Component mapped = a_arch.getMappedComponent(n);
            if (mapped == null) {
                archTree.addNode("unmapped." + n.getLogicNameSimple(), n);
            } else {
                archTree.addNode(mapped.getName() + "." + n.getLogicNameSimple(), n);
            }
        }


        double outerRadius = Math.min(columnSize.getX(), columnSize.getY()) / 2 - 10;
        double innerRadius = outerRadius * 0.25;

        //
        Vec2 mousePos = a_imgui.getMousePos();
        Vec2 toMousePos = mousePos.minus(center);
        float cosa = toMousePos.getX() / (toMousePos.length());
        final float centerMousePosAngle = toMousePos.getY() > 0 ? (float)Math.atan2(toMousePos.getY(), toMousePos.getX()) : (float)(Math.PI * 2 + Math.atan2(toMousePos.getY(), toMousePos.getX()));
        CNode [] mouseHoverNode = {null};


        class CurvePoints {
            Vec2 m_start;
            Vec2 m_startControl;
            Vec2 m_end;
            Vec2 m_endControl;
            CNode m_node;
        }

        ArrayList<CurvePoints> g_curvePoints = new ArrayList<>();

        class PieDrawer implements Tree.TNodeVisitor {

            double m_fromAngle, m_toAngle;
            double m_radius;

            PieDrawer(double a_fromAngle, double a_toAngle, double a_radius) {
                m_fromAngle = a_fromAngle;
                m_toAngle = a_toAngle;
                m_radius = a_radius;
            }

            @Override
            public void visit(Tree.TNode a_node) {

                final boolean isRoot = a_node.getName() == null;
                double angleSpan = m_toAngle - m_fromAngle;

                class ChildCounterVisitor implements Tree.TNodeVisitor {

                    public int m_childCount = -1; // -1 since the first parent node is counted in visit

                    @Override
                    public void visit(Tree.TNode a_node1) {
                        m_childCount += 1; for (Tree.TNode c : a_node1.children()){c.accept(this);}
                    }
                }

                ChildCounterVisitor childCounter = new ChildCounterVisitor();

                final double angleBorder = isRoot ? 0.0 : 0.01;
                childCounter.visit(a_node);
                double angleDelta = (angleSpan - 2 * angleBorder) / childCounter.m_childCount;


                if (!isRoot) {

                    int segments = Math.max(8, childCounter.m_childCount * 8);
                    CNode n = null;

                    if (a_nvm.hasBGColor(a_node.getFullName())) {
                        Vec4 bgColor = a_nvm.getBGColor(a_node.getFullName());
                        a_imgui.addFilledCircleSegment(center, (float) m_radius, a_imgui.toColor(bgColor), segments, (float) m_fromAngle, (float) m_toAngle);
                        a_imgui.addCircleSegment(center, (float) m_radius, white, segments, (float) m_fromAngle, (float) m_toAngle, 2);
                        a_imgui.addCircleSegment(center, (float) innerRadius, white, segments, (float) m_fromAngle, (float) m_toAngle, 2);

                        Vec2 p1 = new Vec2(Math.cos(m_fromAngle) * m_radius, sin(m_fromAngle) * m_radius).plus(center);
                        Vec2 p2 = new Vec2(Math.cos(m_toAngle) * m_radius, sin(m_toAngle) * m_radius).plus(center);
                        Vec2 p3 = new Vec2(Math.cos(m_fromAngle) * innerRadius, sin(m_fromAngle) * innerRadius).plus(center);
                        Vec2 p4 = new Vec2(Math.cos(m_toAngle) * innerRadius, sin(m_toAngle) * innerRadius).plus(center);

                        a_imgui.addLine(p1, p3, white, 2);
                        a_imgui.addLine(p2, p4, white, 2);
                    } else {
                        // we now have a code node... or the unmapped category...
                        n = (CNode)(a_node.getObject());
                        if (n != null) {

                            String name = a_imgui.getLongestSubString(n.getLogicName(), (float)(m_radius - innerRadius - 10), "\\.");
                            Vec2 textSize = a_imgui.calcTextSize(name, false);

                            float midAngle = (float)(m_fromAngle + (m_toAngle - m_fromAngle) / 2);

                            Vec2 p1 = new Vec2(Math.cos(midAngle) * (m_radius - textSize.getX()), sin(midAngle) * (m_radius - textSize.getX())).plus(center);
                            Vec2 textEndPos = a_imgui.text(name, p1, white, (float) midAngle);


                            Vec2 endControl = new Vec2(Math.cos(midAngle) * innerRadius, Math.sin(midAngle) * innerRadius).plus(center);

                            Vec2 start = center;

                            Vec2 centerControl = new Vec2(start);

                            if (center.getY() > p1.getY()) {
                                centerControl.setY(start.getY() - 150);
                            } else {
                                centerControl.setY(start.getY() + 150);
                            }

                            CurvePoints cp = new CurvePoints();
                            cp.m_start = start;
                            cp.m_end = textEndPos;
                            cp.m_startControl = centerControl;
                            cp.m_endControl = endControl;
                            cp.m_node = n;
                            g_curvePoints.add(cp);

                        }
                    }

                    if (centerMousePosAngle > m_fromAngle && centerMousePosAngle < m_toAngle) {
                        if (a_imgui.isInside(center, (float)m_radius, mousePos) && !a_imgui.isInside(center, (float)innerRadius, mousePos)) {
                            mouseHoverNode[0] = n;
                            a_imgui.beginTooltip();
                                a_imgui.text(a_node.getFullName());
                            a_imgui.endTooltip();
                        }
                    }


                }

                int cIx = 0;
                double childRadius = isRoot ? m_radius : m_radius - 20;
                double fromAngle = m_fromAngle + angleBorder;
                for (Tree.TNode c : a_node.children()) {

                    childCounter.m_childCount = -1;
                    childCounter.visit(c);

                    double angle = angleDelta * (childCounter.m_childCount + 1);
                    double toAngle = fromAngle + angle;

                    c.accept(new PieDrawer(fromAngle, toAngle, childRadius));

                    fromAngle += angle;

                    cIx++;
                }
            }
        }

        archTree.doVisit(new PieDrawer(0, 2 * Math.PI, outerRadius));

        HuGMe.ArchDef.Component selectedComponent = a_arch.getMappedComponent(a_selectedNode);
        if (selectedComponent != null) {
            a_imgui.addCircleFilled(center, (float)innerRadius - 1, a_imgui.toColor(a_nvm.getBGColor(selectedComponent.getName())), 32);
        } else {
            a_imgui.addCircleFilled(center, (float)innerRadius - 1, black, 32);
        }

        for (CurvePoints from : g_curvePoints) {
            if (from.m_node == mouseHoverNode[0]) {
                for (CurvePoints to : g_curvePoints) {
                    if (from != to) {
                        if (from.m_node.hasDependency(to.m_node) || to.m_node.hasDependency(from.m_node)) {

                            Vec2 fromDir = from.m_end.minus(center).normalize();
                            Vec2 toDir = to.m_end.minus(center).normalize();


                            a_imgui.addCurve(center.plus(fromDir.times(innerRadius)), center.plus(fromDir.times(innerRadius)).plus(fromDir.times(-25)),
                                    center.plus(toDir.times(innerRadius)), center.plus(toDir.times(innerRadius)).plus(toDir.times(-25)),
                                    white, 1);
                        }
                    }
                }
            }
        }


        for (CurvePoints cp : g_curvePoints) {
            //a_imgui.addCurve(cp.m_start, cp.m_startControl, cp.m_end, cp.m_endControl, white, 1);
            final int red = a_imgui.toColor(new Vec4(1, 0.25, 0.25, 1));
            Vec2 dir = center.minus(cp.m_end).normalize();
            float offsetDistance = 10;
            float arrowSize = 7;
            if (fanin.contains(cp.m_node)) {
                int color = white;

                if (selectedComponent != null) {
                    HuGMe.ArchDef.Component from = a_arch.getMappedComponent(cp.m_node);
                    if (from != null && !from.allowedDependency(selectedComponent)) {
                        color = red;
                    }
                }

                offsetDistance += arrowSize;
                a_imgui.addArrow(cp.m_end.plus(dir.times(offsetDistance)), dir.times(-(arrowSize + 10 * a_selectedNode.getDependencyCount(cp.m_node) / maxFanIn)), color);
                offsetDistance += 3;
            }

            if (fanout.contains(cp.m_node)) {

                int color = white;

                if (selectedComponent != null) {
                    HuGMe.ArchDef.Component to = a_arch.getMappedComponent(cp.m_node);
                    if (to != null && !selectedComponent.allowedDependency(to)) {
                        color = red;
                    }
                }

                a_imgui.addArrow(cp.m_end.plus(dir.times(offsetDistance)), dir.times(arrowSize + 10 * cp.m_node.getDependencyCount(a_selectedNode) / maxFanOut), color);
            }
        }

        //a_imgui.addCircleFilled(center, (float)50, black, 32);

        a_imgui.text(a_selectedNode.getLogicName());

        m_totalTime += a_imgui.imgui().getIo().getDeltaTime();
        a_imgui.addText(center.minus(a_imgui.imgui().calcTextSize(a_selectedNode.getLogicName(), false).times(0.5)), white, a_selectedNode.getLogicName());
        if (selectedComponent != null) {
            a_imgui.addText(center.minus(a_imgui.imgui().calcTextSize(selectedComponent.getName(), false).times(0.5)).plus(new Vec2(0, 15)), white, selectedComponent.getName());
        }

        a_imgui.text("Fan in: ");
        for (CNode n : a_g.getNodes()) {
            if (n.hasDependency(a_selectedNode)) {
                a_imgui.text(n.getLogicName());
            }
        }

        a_imgui.text("Fan out: ");
        for (CNode n : a_g.getNodes()) {
            if (a_selectedNode.hasDependency(n)) {
                a_imgui.text(n.getLogicName());
            }
        }

        if (a_imgui.isMouseClicked(0, false) && mouseHoverNode[0] != null) {
            m_selectedClass = mouseHoverNode[0];
        }

        a_imgui.imgui().endChild();
    }

    public String getArchRootFilter() {
        return treeViewRoots[0];
    }
}
