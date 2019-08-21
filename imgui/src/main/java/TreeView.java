import archviz.HNode;
import archviz.HRoot;
import glm_.vec2.Vec2;
import glm_.vec4.Vec4;
import gui.ImGuiWrapper;
import gui.ZoomWindow;
import hiviz.Circle;
import hiviz.Tree;
import imgui.ImGui;
import imgui.internal.Rect;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.experiments.metric.Metric;
import se.lnu.siq.s4rdm3x.experiments.metric.MetricFactory;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.ArrayList;

import static java.lang.Math.sin;

public class TreeView  {
    private int[] m_treeViewSelection = {1};
    private int[] m_metricSelection = {1};
    private String[] treeViewRoots = {"", "", ""};
    ClassTreeNodeContextMenu m_selectedClassContextMenu = new ClassTreeNodeContextMenu("m_selectedClassContextMenu");
    CNode m_selectedClass;

    private final int g_archId = 0;
    private final int g_classesId = 1;
    private final int g_filesId = 2;

    private int m_statColor1, m_statColor2;

    private hiviz.Circle m_selectedPackage;
    ClassTreeNodeContextMenu m_selectedPackageContextMenu = new ClassTreeNodeContextMenu("m_selectedPackageContextMenu");
    private int[] m_colorSelection = {0};

    HRoot.State m_archState = new HRoot.State();

    private static class ClassTreeNodeContextMenu {
        private Tree.TNode m_selectedNode = null;
        private String m_idString;

        public ClassTreeNodeContextMenu(String a_idString) {
            m_idString = a_idString;
        }

        public Action doContextMenu(ImGui a_imgui, ArchDef a_arch, Tree.TNode a_selectedNode) {
            Action ret = null;
            if (m_selectedNode == null && a_selectedNode != null) {
                m_selectedNode = a_selectedNode;
            }
            if (m_selectedNode != null) {
                if (a_imgui.beginPopupContextWindow(m_idString, 1, true)) {
                    a_imgui.getCurrentWindow().getId();

                    Tree at = buildArchitectureTree(a_arch.getComponents(), "");
                    Object mappedObject = m_selectedNode.getMappedObject(0);
                    Tree.TNode selectedNode = at.doMenu(a_imgui, mappedObject);

                    if (selectedNode != null) {
                        if (selectedNode != mappedObject) {
                            ret = new Action();
                            ret.m_doMapAction = new Action.DoMap();
                            ret.m_doMapAction.a_whatNodeName = m_selectedNode.getFullName();
                            ret.m_doMapAction.a_toComponentName = selectedNode.getFullName();

                            System.out.println("what: " + ret.m_doMapAction.a_whatNodeName);
                            System.out.println("to: " + ret.m_doMapAction.a_toComponentName);
                        }
                    }

                    a_imgui.separator();
                    if (a_imgui.menuItem("Unmap##unmap", "", false, true)) {
                        ret = new Action();
                        ret.m_doMapAction = new Action.DoMap();
                        ret.m_doMapAction.a_whatNodeName = m_selectedNode.getFullName();
                        ret.m_doMapAction.a_toComponentName = null;
                    }

                    a_imgui.endPopup();
                } else {
                    m_selectedNode = null;
                }
            }
            return ret;
        }

        public Tree buildArchitectureTree(Iterable<ArchDef.Component> a_components, String a_filter) {
            Tree tree = new Tree();
            for(ArchDef.Component c : a_components) {
                if (a_filter.length() == 0 || c.getName().startsWith(a_filter)) {
                    tree.addNode(c.getName(), c);
                }
            }

            return tree;
        }

    }

    public static class Action {
        public static class DoMap {
            public String a_whatNodeName;
            public String a_toComponentName;
        }

        DoMap m_doMapAction;
    }

    float m_totalTime = 0;


    ZoomWindow m_packageZW = new ZoomWindow();


    Action doTreeView(ImGui a_imgui, ArchDef a_arch, CGraph a_g, HNode.VisualsManager a_nvm) {
        Action ret = null;
        hiviz.Tree tree = new hiviz.Tree();
        ArrayList<String> items = new ArrayList<>();
        items.add("Architecture");
        items.add("Classes");
        items.add("Files");

        m_archState.m_nvm = a_nvm;

        ImGuiWrapper iw = new ImGuiWrapper(a_imgui);

        m_statColor1 = iw.toColor(new Vec4(0.75, 0.5, 0.5, 1));
        m_statColor2 = iw.toColor(new Vec4(1.0, 0.25, 0.25, 1));

        if (a_imgui.combo("", m_treeViewSelection, items, items.size())) {
        }

        MetricFactory mf = new MetricFactory();
        Metric[] primitiveMetrics = mf.getPrimitiveMetrics();

        a_imgui.beginColumns("TreeViewColumns", 2, 0);


        // colors and mappings may have changed so we need to update them
        if (m_colorSelection[0] == 1) {
            colorizeCircles(m_selectedPackage, iw, a_arch, m_archState.m_nvm);
        }

        treeViewRoots[m_treeViewSelection[0]] = iw.inputTextSingleLine("Root", treeViewRoots[m_treeViewSelection[0]]);

        switch (m_treeViewSelection[0]) {
            case g_archId: {
                tree = m_selectedClassContextMenu.buildArchitectureTree(a_arch.getComponents(), getArchRootFilter());
            } break;
            case g_classesId: {
                for (CNode n : a_g.getNodes()) {

                    ArchDef.Component component = a_arch.getMappedComponent(n);

                    for (dmClass c : n.getClasses()) {
                        if (!c.isInner()) {
                            if (c.getName().startsWith(treeViewRoots[1])) {

                                Tree.TNode tn = tree.addNode(n.getName().replace("/", ".").replace(".java", ""), n);
                                if (component != null) {
                                    tn.setName(tn.getName());
                                    tn.setMapping(component.getName(), m_archState.m_nvm.getBGColor(component.getName()), component);
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

        if (selected != null && iw.isMouseClicked(0, false) && m_treeViewSelection[0] == g_classesId) {
            if (selected.getObject() != null) {
                m_selectedClass = (CNode) selected.getObject();
                //m_selectedPackage = null;
            } else {

                m_selectedPackage = getPackageCircles(iw, selected, new Vec2(0, 0), primitiveMetrics[m_metricSelection[0]]);
                colorizeCircles(m_selectedPackage, iw, a_arch, m_archState.m_nvm);
                m_selectedClass = null;
            }
        }

        if (selected != null) {

            iw.text(selected.getName());
        }




        if (m_treeViewSelection[0] == g_classesId) {
            ArrayList<String> metrics = new ArrayList<>();

            for (int i = 0; i < primitiveMetrics.length; i++) {
                metrics.add(primitiveMetrics[i].getName());
            }

            if (a_imgui.combo("Metric##metricscombo", m_metricSelection, metrics, metrics.size())) {
                if (m_selectedPackage != null) {
                    m_selectedPackage.computeLayout(m_selectedPackage.getPos(), primitiveMetrics[m_metricSelection[0]]);
                    colorizeCircles(m_selectedPackage, iw, a_arch, m_archState.m_nvm);

                }
            }

            ArrayList<String> colors = new ArrayList<>();
            colors.add("by Metric (mean, std dev)");
            colors.add("by MappingView");
            if (a_imgui.combo("Color##colorcombo", m_colorSelection, colors, colors.size())) {
                m_selectedPackage.computeLayout(m_selectedPackage.getPos(), primitiveMetrics[m_metricSelection[0]]);
                colorizeCircles(m_selectedPackage, iw, a_arch, m_archState.m_nvm);
            }

        }


        a_imgui.nextColumn();
            if (m_treeViewSelection[0] == g_classesId) {
                if (m_selectedClass != null) {
                    doClassView(iw, a_g, a_arch, m_archState.m_nvm, m_selectedClass);
                } else if (m_selectedPackage != null) {
                    Action a = doPackageView(iw, a_g, a_arch, m_archState.m_nvm, m_selectedPackage, primitiveMetrics[m_metricSelection[0]]);
                    if (ret == null && a != null) {
                        ret = a;
                    }
                }
                a_imgui.endColumns();
            } else if (m_treeViewSelection[0] == g_archId) {

                doArchStructure(a_arch, getArchRootFilter(), a_g, a_imgui, m_archState);
            } else {
                a_imgui.endColumns();
            }




        switch (m_treeViewSelection[0]) {
            case g_archId:
                /*if (a_imgui.beginPopupContextWindow("TreeViewContextMenuArch", 1, true)) {

                    a_imgui.menuItem("Some Arch Menu", "", false, true);

                    a_imgui.endPopup();
                } else {
                    m_selectedNode = null;
                }*/
                break;

            case g_classesId:
                Action a = m_selectedClassContextMenu.doContextMenu(a_imgui, a_arch, selected);
                if (ret == null && a != null) {
                    ret = a;
                }
                break;
        }

        return ret;
    }


    private Action doPackageView(ImGuiWrapper a_imgui, CGraph a_g, ArchDef a_arch, HNode.VisualsManager a_nvm, hiviz.Circle a_selectedNode, Metric a_metric) {

        Action ret = null;
        Vec2 columnSize = new Vec2(a_imgui.imgui().getColumnWidth(1) - 10, (float) a_imgui.imgui().getContentRegionAvail().getY());
        m_packageZW.setScale(m_packageZW.getScale() + a_imgui.imgui().getIo().getMouseWheel() * 0.1f);
        float packageScale = m_packageZW.getScale();



        Vec2 area = new Vec2(a_selectedNode.getRadius() * 2 * packageScale);
        final boolean clicked = m_packageZW.begin(a_imgui, columnSize.minus(10), area);

        hiviz.Circle selected = a_selectedNode.draw(a_imgui, m_packageZW.getULOffset(), packageScale, a_imgui.getMousePos(), new Rect(m_packageZW.getWindowPos(), m_packageZW.getWindowPos().plus(columnSize.minus(10))));

        final int white = a_imgui.toColor(new Vec4(1., 1., 1., 1));
        if (m_selectedPackage.getNode().getName() != null) {
            Vec2 textSize = a_imgui.calcTextSize(m_selectedPackage.getNode().getName(), false);
            a_imgui.addText(new Vec2(m_selectedPackage.getScreenPos().getX() - textSize.getX() * 0.5, m_selectedPackage.getScreenPos().getY() - m_selectedPackage.getScreenRadius() - textSize.getY()), white, m_selectedPackage.getNode().getName());
        }

        if (clicked) {
            if (selected != null) {
                if (selected.getNode().childCount() > 0) {
                    selected.computeLayout(m_selectedPackage.getChildPos(selected), a_metric);
                    colorizeCircles(m_selectedPackage, a_imgui, a_arch, a_nvm);
                    m_selectedPackage = selected;
                } else {
                    m_selectedClass = (CNode) selected.getNode().getObject();
                    //m_selectedPackage = null;
                }
            } else {
                Tree.TNode parent = m_selectedPackage.getNode().getParent();
                if (parent != null) {
                    Vec2 oldPos = m_selectedPackage.getPos();
                    Circle newSelected = getPackageCircles(a_imgui, parent, new Vec2(0, 0), a_metric);
                    colorizeCircles(newSelected, a_imgui, a_arch, a_nvm);

                    Circle oldSelected = newSelected.getCircle(m_selectedPackage.getNode());

                    newSelected.offsetPos((oldPos.minus(oldSelected.getPos())));

                    m_selectedPackage = newSelected;
                }
            }
        } else if (selected != null && selected.getNode() != null && selected.getNode().getName() != null) {
            a_imgui.beginTooltip();
            a_imgui.text(selected.getNode().getName());
            a_imgui.endTooltip();
        }

        if (selected != null) {
            ret = m_selectedPackageContextMenu.doContextMenu(a_imgui.imgui(), a_arch, selected.getNode());
        } else {
            ret = m_selectedPackageContextMenu.doContextMenu(a_imgui.imgui(), a_arch, null);
        }

        //a_imgui.addCircle( m_packageZW.getULOffset(), 50, white, 32, 2);
        //a_imgui.addRect(m_packageZW.getULOffset(), m_packageZW.getULOffset().plus(area), white, 0, 0, 2);
        m_packageZW.end(a_imgui);


        //a_imgui.imgui().endChild();

        return ret;
    }

    private Circle getPackageCircles(ImGuiWrapper a_imgui, Tree.TNode a_selected, Vec2 a_initialPos, Metric a_metric) {
        class CircleHierarchyBuilder implements Tree.TNodeVisitor {
            hiviz.Circle m_circle;

            @Override
            public void visit(Tree.TNode a_node) {
                m_circle = new hiviz.Circle(a_node);

                for(Tree.TNode c : a_node.children()) {
                    CircleHierarchyBuilder builder = new CircleHierarchyBuilder();
                    c.accept(builder);
                    m_circle.addChild(builder.m_circle);

                }
            }
        }



        CircleHierarchyBuilder rootBuilder = new CircleHierarchyBuilder();
        a_selected.accept(rootBuilder);
        rootBuilder.m_circle.computeLayout(a_initialPos, a_metric);

        return rootBuilder.m_circle;
    }

    private void doClassView(ImGuiWrapper a_imgui, CGraph a_g, ArchDef a_arch, HNode.VisualsManager a_nvm, CNode a_selectedNode) {

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
            ArchDef.Component mapped = a_arch.getMappedComponent(n);
            if (mapped == null) {
                archTree.addNode("unmapped." + n.getLogicNameSimple(), n);
            } else {
                archTree.addNode(mapped.getName() + "." + n.getLogicNameSimple(), n);
            }
        }

        for (CNode n : fanout) {
            ArchDef.Component mapped = a_arch.getMappedComponent(n);
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

        ArchDef.Component selectedComponent = a_arch.getMappedComponent(a_selectedNode);
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
                    ArchDef.Component from = a_arch.getMappedComponent(cp.m_node);
                    if (from != null && !from.allowedDependency(selectedComponent)) {
                        color = red;
                    }
                }

                offsetDistance += arrowSize;
                a_imgui.addArrow(cp.m_end.plus(dir.times(offsetDistance)), dir.times(-(arrowSize + 10.0f * cp.m_node.getDependencyCount(a_selectedNode) / (float) maxFanIn)), color);
                offsetDistance += 3;
            }

            if (fanout.contains(cp.m_node)) {

                int color = white;

                if (selectedComponent != null) {
                    ArchDef.Component to = a_arch.getMappedComponent(cp.m_node);
                    if (to != null && !selectedComponent.allowedDependency(to)) {
                        color = red;
                    }
                }

                a_imgui.addArrow(cp.m_end.plus(dir.times(offsetDistance)), dir.times(arrowSize + 10.0f * a_selectedNode.getDependencyCount(cp.m_node) / (float)maxFanOut), color);
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

        if (mouseHoverNode[0] != null) {
            a_imgui.beginTooltip();
            a_imgui.text(mouseHoverNode[0].getLogicName());


            a_imgui.text("Dependencies to " + m_selectedClass.getLogicNameSimple());
            for (dmDependency d : mouseHoverNode[0].getDependencies(m_selectedClass)) {
                a_imgui.text("\t" + d.getType().toString() + "(" + d.getCount() + ")");
            }

            a_imgui.text("Dependencies from " + m_selectedClass.getLogicNameSimple());
            for (dmDependency d : m_selectedClass.getDependencies(mouseHoverNode[0])) {
                a_imgui.text("\t" + d.getType().toString() + "(" + d.getCount() + ")");
            }



            a_imgui.endTooltip();


            if (a_imgui.isMouseClicked(0, false)) {
                m_selectedClass = mouseHoverNode[0];


            }
        } else if (a_imgui.isMouseClicked(0, false) && a_imgui.isInside(new Rect(offset, offset.plus(columnSize)), mousePos)) {
            // clicked outside
            //Tree.TNode parent = m_selectedNode.getParent();
            m_selectedClass = null;
            //m_selectedPackage =
        }



        a_imgui.imgui().endChild();
    }

    public String getArchRootFilter() {
        return treeViewRoots[0];
    }

    private void colorizeCircles(Circle a_root, ImGuiWrapper a_imgui, ArchDef a_arch, HNode.VisualsManager a_nvm) {
        if (m_colorSelection[0] == 0) {
            a_root.colorByMetric(m_statColor1, m_statColor2);
        } else {
            a_root.colorByMapping(a_imgui, a_arch, a_nvm);
        }

    }

    private void doArchStructure(ArchDef a_arch, String a_rootComponentFilter, CGraph a_g, ImGui a_imgui, HRoot.State a_vizState) {
        if (a_arch !=  null) {

            HRoot root = new HRoot();
            for (ArchDef.Component c : a_arch.getComponents()) {

                if (c.getName().startsWith(a_rootComponentFilter)) {
                    root.add(c.getName());
                }
            }

            for (ArchDef.Component from : a_arch.getComponents()) {
                for (ArchDef.Component to : a_arch.getComponents()) {
                    if (from.allowedDependency(to)) {
                        if (from.getName().startsWith(a_rootComponentFilter) && to.getName().startsWith(a_rootComponentFilter)) {
                            root.addDependency(from.getName(), to.getName());
                        }
                    }
                }
            }

            //Arrays.sort(components, (o1, o2) -> o2.getAllowedDependencyCount() - o1.getAllowedDependencyCount());

            //Rect r = a_imgui.getCurrentWindow().getContentsRegionRect();
            //Rect r = a_imgui.getCurrentWindow().getOuterRectClipped();

            Rect r = new Rect(a_imgui.getWindowPos().plus(a_imgui.getCursorPos()), a_imgui.getWindowPos().plus(a_imgui.getContentRegionMax()));

            a_imgui.setCursorPosY(a_imgui.getWindowContentRegionMax().getY());

            a_imgui.endColumns();   // columns cause crash if popup menus are used.
            HRoot.Action action = root.render(r, a_imgui, a_vizState);

            if (action != null && action.m_addComponent != null) {
                a_arch.addComponent(action.m_addComponent);
            }

            if (action != null && action.m_deletedComponents != null) {
                for (String cName : action.m_deletedComponents) {
                    a_arch.removeComponent(a_arch.getComponent(cName));
                }
            }

            // we need to do resorting before renaming
            if (action != null && action.m_nodeOrder != null) {
                ArrayList<ArchDef.Component> newOrder = new ArrayList<>();

                for(String name : action.m_nodeOrder) {
                    newOrder.add(a_arch.getComponent(name));
                }
                a_arch.clear();
                for(ArchDef.Component c : newOrder) {
                    a_arch.addComponent(c);
                }
            }

            // add dependenices
            if (action != null && action.m_addDependenices != null) {
                for(HRoot.Action.NodeNamePair pair : action.m_addDependenices.getPairs()) {
                    ArchDef.Component sC = a_arch.getComponent(pair.m_oldName);
                    ArchDef.Component tC = a_arch.getComponent(pair.m_newName);
                    if (tC == null) {
                        System.out.println("Could not find component named: " + pair.m_newName);
                    } else if (sC == null) {
                        System.out.println("Could not find component named: " + pair.m_oldName);
                    } else {
                        sC.addDependencyTo(tC);
                    }
                }
            }

            // remove dependenices
            if (action != null && action.m_removeDependencies != null) {
                for(HRoot.Action.NodeNamePair pair : action.m_removeDependencies.getPairs()) {
                    ArchDef.Component sC = a_arch.getComponent(pair.m_oldName);
                    ArchDef.Component tC = a_arch.getComponent(pair.m_newName);
                    if (tC == null) {
                        System.out.println("Could not find component named: " + pair.m_newName);
                    } else if (sC == null) {
                        System.out.println("Could not find component named: " + pair.m_oldName);
                    } else {
                        sC.removeDependencyTo(tC);
                    }
                }
            }

            // node renaming
            if (action != null && action.m_hiearchyMove != null) {
                for(HRoot.Action.NodeNamePair pair : action.m_hiearchyMove.getPairs()) {
                    ArchDef.Component c = a_arch.getComponent(pair.m_oldName);
                    if (c == null) {
                        System.out.println("Could not find component named: " + pair.m_oldName);
                    } else {
                        ArrayList<CNode> nodes = new ArrayList<>();
                        for(CNode n : a_g.getNodes()) {
                            if (c.isMappedTo(n)) {
                                nodes.add(n);
                            }
                        }
                        a_arch.setComponentName(c, pair.m_newName, nodes);
                    }
                }
            }
        }
    }
}
