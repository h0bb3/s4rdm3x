import glm_.vec2.Vec2;
import imgui.*;
import imgui.internal.DrawCornerFlag;
import imgui.internal.Rect;

import java.util.ArrayList;
import java.util.Arrays;

import static imgui.ImguiKt.COL32;

public class HRoot {

    // this class is for testing purposes mainly
    static class ImGuiWrapper {
        public ImGui m_imGui;

        public ImGuiWrapper(ImGui a_imgui) {
            m_imGui = a_imgui;
        }

        public void addRect(Vec2 a_tl, Vec2 a_br, int a_color, float a_rounding, int a_corners, float a_thickness) {
            m_imGui.getWindowDrawList().addRect(a_tl, a_br, a_color, a_rounding, a_corners, a_thickness);
        }
        public void addRectFilled(Vec2 a_tl, Vec2 a_br, int a_color, float a_rounding, int a_corners) {
            m_imGui.getWindowDrawList().addRectFilled(a_tl, a_br, a_color, a_rounding, a_corners);
        }

        public void addText(Vec2 a_pos, int a_color, String a_text) {
            m_imGui.getWindowDrawList().addText(a_pos, COL32(175, 175, 175, 255), a_text.toCharArray(), a_text.length());
        }

        public void addCircle(Vec2 a_center, float a_radius, int a_color, int a_segments, float a_thickness) {
            m_imGui.getWindowDrawList().addCircle(a_center, a_radius, a_color, a_segments, a_thickness);
        }

        public void addLine(Vec2 a_p1, Vec2 a_p2, int a_color, float a_thickness) {
            m_imGui.getWindowDrawList().addLine(a_p1, a_p2, a_color, a_thickness);
        }

        public Vec2 calcTextSize(String a_str, boolean a_hideTextAfterDoubleHash) {
            return m_imGui.calcTextSize(a_str, a_hideTextAfterDoubleHash);
        }

        public void beginTooltip() {
            m_imGui.beginTooltip();
        }

        public void endTooltip() {
            m_imGui.endTooltip();
        }

        public void text(String a_text) {
            m_imGui.text(a_text);
        }

        public Vec2 getMousePos() {
            return m_imGui.getMousePos();
        }

        public Vec2 getMouseDragDelta(int a_button, float a_lockThreshold) {
            return m_imGui.getMouseDragDelta(a_button, 1.0f);
        }

        public boolean isMouseDragging(int a_button, float a_lockThreshold) {
            return m_imGui.isMouseDragging(a_button, a_lockThreshold);
        }

        public void stopWindowDrag() {
            m_imGui.getCurrentWindow().setFlags(WindowFlag.NoMove.or(m_imGui.getCurrentWindow().getFlags()));
        }
    }

    static class Action {
        static class NodeNamePair {
            String m_oldName;
            String m_newName;
        }
        static class HierarchyMove {
            ArrayList<NodeNamePair> m_nodes;
        }

        ArrayList<String> m_nodeOrder;

        public HierarchyMove m_hiearchyMove;
    }

    static class DragNDropData {
        Rect m_dragRect;
        HNode m_staleSourceNode;
        HNode m_target;
    }

    static class HNode {
        public String m_name;
        ArrayList<HNode> m_children = new ArrayList<>();
        ArrayList<HNode> m_dependencies = new ArrayList<>();
        HNode m_parent;
        Rect m_rect;
        int m_leafNodeIx = -1;
        private boolean m_forcedConcreteNode = false;
        static final int g_rounding = 3;

        public HNode getRootParent() {
            if (m_parent.m_name != null) {
                return m_parent.getRootParent();
            }
            return this;
        }


        static class AddDependencyAction {
            HNode m_source;
            HNode m_target;
            int m_ix;
        }

        public void setAsConcrete() {
            m_forcedConcreteNode = true;
        }

        public boolean isConcreteNode() {
            return m_leafNodeIx > -1 || m_forcedConcreteNode;
        }

        private void getConcreteNodes(ArrayList<HNode> a_leafNodes) {
            if (m_children.size() > 0) {
                for (HNode c : m_children) {
                    c.getConcreteNodes(a_leafNodes);
                }
            }

            if (isConcreteNode()) {
                a_leafNodes.add(this);
            }
        }

        void getParentNodePath(ArrayList<HNode> a_outPath) {
            if (m_parent != null) {
                m_parent.getParentNodePath(a_outPath);
            }
            a_outPath.add(this);
        }

        Iterable<HNode> getConcreteNodes() {
            ArrayList<HNode> ret = new ArrayList<>();
            getConcreteNodes(ret);
            return ret;
        }

        HNode findLeafNode(int a_ix) {
            if (m_leafNodeIx == a_ix) {
                return this;
            }
            for(HNode c : m_children) {
                HNode n = c.findLeafNode(a_ix);
                if (n != null) {
                    return n;
                }
            }
            return null;
        }

        int getMinLeafNodeIx() {
            if (m_children.size() == 0) {
                return m_leafNodeIx;
            }
            return m_children.get(0).getMinLeafNodeIx();
        }

        int getMaxLeafNodeIx() {
            if (m_children.size() == 0) {
                return m_leafNodeIx;
            }
            return m_children.get(m_children.size() - 1).getMaxLeafNodeIx();
        }

        int getLeafNodeCount() {
            if (m_children.size() == 0) {
                return 1;
            } else {
                int sum = 0;
                for (HNode ch : m_children) {
                    sum += ch.getLeafNodeCount();
                }
                return sum;
            }
        }

        private HNode getNodeUnder(Vec2 a_point) {
            if (m_rect.contains(a_point)) {
                HNode child;
                for (HNode c : m_children) {
                    child = c.getNodeUnder(a_point);
                    if (child != null) {
                        return child;
                    }
                }
                return this.m_name != null ? this : null;   // don't return the root node
            }

            return null;
        }

        DragNDropData doDragNDrop(ImGuiWrapper a_imgui, DragNDropData a_dnd) {
            Vec2 mousePos = a_imgui.getMousePos();
            if (a_dnd!= null && a_imgui.isMouseDragging(0, 1.0f)) {
                a_dnd.m_target = getNodeUnder(mousePos);
                a_imgui.beginTooltip();
                String targetStr = a_dnd.m_target != null ? " into " + a_dnd.m_target.getFullName() : "";
                a_imgui.text("Dragging " + a_dnd.m_staleSourceNode.getFullName() + targetStr);
                a_imgui.endTooltip();
                Rect drawRect = new Rect(a_dnd.m_dragRect.getTl().plus(a_imgui.getMouseDragDelta(0, 1.0f)), a_dnd.m_dragRect.getBr().plus(a_imgui.getMouseDragDelta(0, 1.0f)));

                a_dnd.m_staleSourceNode.render(drawRect, a_imgui, a_dnd.m_staleSourceNode.getLeafNodeCount());
                a_imgui.addRect(drawRect.getTl(), drawRect.getBr(), COL32(175, 175, 175, 255), g_rounding, DrawCornerFlag.All.getI(), 2);



                return a_dnd;
            }

            // root node
            if (m_name == null) {
                for(HNode c : m_children) {
                    DragNDropData ret = c.doDragNDrop(a_imgui, a_dnd);
                    if (ret != null) {
                        return ret;
                    }
                }
                return null;
            }



            // we may start dragging
            if (m_rect.contains(mousePos)) {
                // first we check the children
                for(HNode c : m_children) {
                    a_dnd = c.doDragNDrop(a_imgui, a_dnd);
                    if (a_dnd != null) {
                        return a_dnd;
                    }
                }

                a_imgui.stopWindowDrag();


                if (a_imgui.isMouseDragging(0, 1.0f)) { ;
                    Rect dragRect = new Rect(m_rect.getTl().plus(a_imgui.getMouseDragDelta(0, 1.0f)), m_rect.getBr().plus(a_imgui.getMouseDragDelta(0, 1.0f)));

                    a_imgui.addRect(dragRect.getTl(), dragRect.getBr(), COL32(175, 175, 175, 255), g_rounding, DrawCornerFlag.All.getI(), 2);
                    a_dnd = new DragNDropData();
                    a_dnd.m_dragRect = dragRect;
                    a_dnd.m_staleSourceNode = this;
                    return a_dnd;
                }
            }

            return null;
        }

        AddDependencyAction render(Rect a_area, ImGuiWrapper a_imgui, final int a_leafNodeCount) {
            m_rect = new Rect(a_area);
            AddDependencyAction ret = null;
            Vec2 mousePos = a_imgui.getMousePos();
            Rect childArea = new Rect(m_rect);
            if (m_name != null) {

                a_imgui.addRectFilled(m_rect.getTl(), m_rect.getBr(), COL32(75, 75, 75, 255), g_rounding, DrawCornerFlag.All.getI());
                a_imgui.addRect(m_rect.getTl(), m_rect.getBr(), COL32(175, 175, 175, 255), g_rounding, DrawCornerFlag.All.getI(), 2);

                String name = isConcreteNode() ? m_name : "(" + m_name + ")";
                Vec2 textSize = a_imgui.calcTextSize(name, false);
                Vec2 textPos = m_rect.getTl().plus(m_rect.getSize().div(2).minus(textSize.div(2)));
                if (m_children.size() > 0) {
                    textPos.setY(m_rect.getTl().getY() + 3);
                }
                a_imgui.addText(textPos, COL32(175, 175, 175, 255), name);

                childArea.expand(-5);
                childArea.setMin(new Vec2((float)childArea.getMin().getX(), childArea.getMin().getY() + textSize.getY() + 5));

            }

            Vec2 size = childArea.getSize().div(getLeafNodeCount());
            int consumedLeafNodes = 0;
            for (int ix = 0; ix < m_children.size(); ix++) {
                HNode child = m_children.get(ix);
                Rect childRect = new Rect(childArea.getTl().plus(size.times(consumedLeafNodes)), childArea.getTl().plus(size.times(consumedLeafNodes + child.getLeafNodeCount())));
                AddDependencyAction childAction;
                childAction = child.render(childRect, a_imgui, a_leafNodeCount);
                if (ret == null) {
                    ret = childAction;
                }
                consumedLeafNodes += child.getLeafNodeCount();
            }

            final float radius = 3;

            for (int ix = 0; ix < getMinLeafNodeIx(); ix++) {
                Vec2 p = new Vec2();
                p.setY(m_rect.getTl().getY());
                p.setX(m_rect.getTl().getX() + g_rounding + (m_rect.getWidth() - g_rounding * 2) / (getMinLeafNodeIx()) * (ix + 0.5f));
                a_imgui.addCircle(p, radius, COL32(175, 175, 175, 255), 8, 1);
                if (pointInCircle(mousePos, p, radius)) {
                    ret = new AddDependencyAction();
                    ret.m_target = this;
                    ret.m_ix = getMinLeafNodeIx() - 1 - ix;
                }
            }

            for (int ix = 0; ix < a_leafNodeCount - getMaxLeafNodeIx() - 1; ix++) {
                Vec2 p = new Vec2();
                p.setY(m_rect.getBr().getY());
                p.setX(m_rect.getTl().getX() + g_rounding + (m_rect.getWidth() - g_rounding * 2) / (a_leafNodeCount - getMaxLeafNodeIx() - 1) * (ix + 0.5f));
                a_imgui.addCircle(p, 3, COL32(175, 175, 175, 255), 8, 1);

                if (pointInCircle(mousePos, p, radius)) {
                    ret = new AddDependencyAction();
                    ret.m_target = this;
                    ret.m_ix = a_leafNodeCount - ix - 1;
                }
            }

            for (int ix = 0; ix < a_leafNodeCount - getMaxLeafNodeIx() - 1; ix++) {
                Vec2 p = new Vec2();
                p.setX(m_rect.getBr().getX());
                p.setY(m_rect.getTl().getY() + g_rounding + (m_rect.getHeight() - g_rounding * 2) / (a_leafNodeCount - getMaxLeafNodeIx() - 1) * (ix + 0.5f));
                a_imgui.addCircle(p, 3, COL32(175, 175, 175, 255), 8, 1);

                if (pointInCircle(mousePos, p, radius)) {
                    ret = new AddDependencyAction();
                    ret.m_source = this;
                    ret.m_ix = a_leafNodeCount - 1 - ix;
                }
            }

            for (int ix = 0; ix < getMinLeafNodeIx(); ix++) {
                Vec2 p = new Vec2();
                p.setX(m_rect.getTl().getX());
                p.setY(m_rect.getTl().getY() + g_rounding + (m_rect.getHeight() - g_rounding * 2) / (getMinLeafNodeIx()) * (ix + 0.5f));
                a_imgui.addCircle(p, 3, COL32(175, 175, 175, 255), 8, 1);

                if (pointInCircle(mousePos, p, radius)) {
                    ret = new AddDependencyAction();
                    ret.m_source = this;
                    ret.m_ix = getMinLeafNodeIx() - 1 - ix;
                }
            }

            return ret;
        }

        private boolean pointInCircle(Vec2 a_point, Vec2 a_cPoint, float a_cRad) {
            return a_point.minus(a_cPoint).length2() < a_cRad * a_cRad;
        }

        void renderDependency(ImGuiWrapper a_imgui, HNode a_dest, final int a_leafNodeCount) {
            Vec2 sTL = m_rect.getTl();
            Vec2 sBR = m_rect.getBr();
            Vec2 sSize = m_rect.getSize();
            final int color = COL32(175, 175, 175, 255);
            Vec2 dTL = a_dest.m_rect.getTl();
            Vec2 dBR = a_dest.m_rect.getBr();
            Vec2 dSize = a_dest.m_rect.getSize();

            if (sBR.getX() <= dTL.getX()) {
                Vec2 p1, p2;

                p1 = new Vec2();
                p1.setX(sBR.getX());
                p1.setY(sBR.getY() - g_rounding - (sSize.getY() - g_rounding * 2) / (a_leafNodeCount - getMaxLeafNodeIx() - 1) * (a_dest.getMinLeafNodeIx() - getMaxLeafNodeIx() - 0.5f));

                p2 = new Vec2();
                p2.setX(dTL.getX() + g_rounding + ((dSize.getX() -  g_rounding * 2) / a_dest.getMinLeafNodeIx()) * (a_dest.getMinLeafNodeIx() - getMaxLeafNodeIx() - 0.5f));

                p2.setY(p1.getY());
                a_imgui.addLine(p1, p2, color, 1.0f);


                p1.setX(p2.getX()); p1.setY(dTL.getY());
                a_imgui.addLine(p2, p1, color, 1.0f);

                p2.setX(p1.getX() - 5);
                p2.setY(p1.getY() - 10);
                a_imgui.addLine(p1, p2, color, 1.0f);

                p2.setX(p1.getX() + 5);
                a_imgui.addLine(p1, p2, color, 1.0f);

            } else {
                Vec2 p1, p2;

                p1 = new Vec2();
                p1.setX(sTL.getX());
                p1.setY(sTL.getY() + g_rounding + ((sSize.getY() - g_rounding * 2) / getMinLeafNodeIx()) * (getMinLeafNodeIx() - a_dest.getMaxLeafNodeIx() - 1 + 0.5f));

                p2 = new Vec2();
                p2.setX(dBR.getX() - g_rounding - (dSize.getX() - g_rounding * 2) / (a_leafNodeCount - a_dest.getMaxLeafNodeIx() - 1) * (getMinLeafNodeIx() - a_dest.getMaxLeafNodeIx() - 1 + 0.5f));
                p2.setY(p1.getY());
                a_imgui.addLine(p1, p2, color, 1.0f);


                p1.setX(p2.getX()); p1.setY(dBR.getY());
                a_imgui.addLine(p2, p1, color, 1.0f);

                p2.setX(p1.getX() - 5);
                p2.setY(p1.getY() + 10);
                a_imgui.addLine(p1, p2, color, 1.0f);

                p2.setX(p1.getX() + 5);
                a_imgui.addLine(p1, p2, color, 1.0f);
            }

        }

        void renderDependencies(ImGuiWrapper a_imgui, final int a_leafNodeCount) {

            for(HNode dest : m_dependencies) {
                renderDependency(a_imgui, dest, a_leafNodeCount);
            }

            for (HNode c : m_children) {
                c.renderDependencies(a_imgui, a_leafNodeCount);
            }
        }

        private String getFullName() {
            if (m_name == null) {
                return "";
            }
            String parentName = m_parent.getFullName();
            if (parentName.length() > 0) {
                parentName += ".";
            }
            return  parentName +  m_name;
        }
    }

    private HNode findNode(String [] a_names) {
        HNode currentParent = m_root;

        for (int ix = 0; ix < a_names.length; ix++) {

            for(HNode n : currentParent.m_children) {
                if (n.m_name.contentEquals(a_names[ix])) {
                    if (ix + 1 < a_names.length) {
                        currentParent = n;
                        break;
                    } else {
                        return n;
                    }
                }
            }
        }

        return null;
    }

    public HNode liftDependencySource(HNode a_source, HNode a_dest) {
        HNode parent = a_source.m_parent;

        if(parent == m_root) {
            return a_source;
        }

        boolean foundInAllChildren = true;
        for (HNode c : parent.m_children) {
            boolean foundInChild = false;
            for (HNode d : c.m_dependencies) {
                if (d == a_dest) {
                    foundInChild = true;
                    break;
                }
            }

            if (!foundInChild) {
                foundInAllChildren = false;
                break;
            }
        }

        if (foundInAllChildren) {
            parent.m_dependencies.add(a_dest);
            for (HNode c : parent.m_children) {
                c.m_dependencies.remove(a_dest);
            }

            return liftDependencySource(parent, a_dest);
        }

        return a_source;
    }

    public HNode liftDependencyDest(HNode a_source, HNode a_dest) {
        HNode parent = a_dest.m_parent;
        if (parent == m_root) {
            return a_dest;
        }

        boolean foundDepToAllChildren = true;
        for(HNode c : parent.m_children) {

            boolean foundDepInChild = false;
            for(HNode d : a_source.m_dependencies) {
                if (d == c) {
                    foundDepInChild = true;
                    break;
                }
            }

            if (!foundDepInChild) {
                foundDepToAllChildren = false;
                break;
            }
        }

        if (foundDepToAllChildren) {
            for(HNode c : parent.m_children) {
                a_source.m_dependencies.remove(c);
            }
            a_source.m_dependencies.add(parent);

            return liftDependencyDest(a_source, parent);
        }
        return a_dest;
    }


    public void addDependency(String a_source, String a_dest) {
        HNode source = findNode(a_source.split("\\."));
        HNode dest = findNode(a_dest.split("\\."));

        source.m_dependencies.add(dest);

        // if all children of source.parent has dest, parent could get dest dependency instead
        HNode newSource = liftDependencySource(source, dest);

        // if source has dependency to all children in dest.parent, dest.parent should be the dest
        HNode newDest = liftDependencyDest(source, dest);

        // repeat the consolidation until no more changes...
        while (newSource != source || newDest != dest) {
            source = newSource;
            dest = newDest;

            newSource = liftDependencySource(source, dest);
            newDest = liftDependencyDest(source, dest);

        }

    }

    private void addNode(String [] a_names, HNode a_parent) {
        if (a_names.length == 1) {
            // the child may already exist
            for (HNode c : a_parent.m_children) {
                if (c.m_name.contentEquals(a_names[0])) {
                    c.setAsConcrete();
                    return;
                }
            }
            HNode leaf = addNode(a_names[0], a_parent);
            leaf.m_leafNodeIx = m_leafNodeCounter;
            m_leafNodeCounter++;
        } else {
            HNode nextParent = null;
            for (HNode ch : a_parent.m_children) {
                if (ch.m_name.contentEquals(a_names[0])) {
                    nextParent = ch;
                    break;
                }
            }
            if (nextParent == null) {
                nextParent = addNode(a_names[0], a_parent);
            }
            addNode(Arrays.copyOfRange(a_names, 1, a_names.length), nextParent);
        }
    }

    private HNode addNode(String a_name, HNode a_parent) {
        HNode n = new HNode();
        n.m_name = a_name;
        n.m_parent = a_parent;
        a_parent.m_children.add(n);
        return n;
    }

    public void add(String a_nodeName) {
        addNode(a_nodeName.split("\\."), m_root);
    }

    static DragNDropData g_dnd;

    public int getIndexOfFirstNonSimilarComponentInStr2(String a_str1, String a_str2) {
        int index = 0;
        String [] parts1 = a_str1.split("\\.");
        String [] parts2 = a_str2.split("\\.");

        for (int partIx = 0; partIx < parts1.length && partIx < parts2.length; partIx++) {
            if (parts1[partIx].contentEquals(parts2[partIx])) {
                index += parts1[partIx].length();
                if (partIx + 1 < parts2.length) {
                    index++;   // remove the .
                }
            }
        }

        return index;
    }

    private int getIndexRelativePosition(Iterable<HNode> a_nodes, Vec2 a_pos) {
        int ix = 0;
        for (HNode n : a_nodes) {
            //if (n.getFullName().contentEquals(g_dnd.m_staleSourceNode.getRootParent().getFullName())) { // we are operating relative the root
            //    sourceIx = ix;
            //}

            // we can check the mouse position like columns as we go from top left corner.
            Vec2 tl, br;
            tl = n.m_rect.getTl();
            br = n.m_rect.getBr();
            if (a_pos.getY() > tl.getY() && a_pos.getY() < br.getY()) {
                return ix;
            } else if (a_pos.getX() > tl.getX() && a_pos.getX() < br.getX()) {
                return ix;
            }
            ix++;
        }

        return -1;
    }

    public Action render(Rect a_area, ImGui a_imgui) {
        return render(a_area, new ImGuiWrapper(a_imgui));
    }

    public Action render(Rect a_rect, ImGuiWrapper a_imgui) {
        HNode.AddDependencyAction action;
        action = m_root.render(a_rect, a_imgui, m_leafNodeCounter);
        if (action != null) {


            action.m_source = action.m_source != null ? action.m_source : m_root.findLeafNode(action.m_ix);
            action.m_target = action.m_target != null ? action.m_target : m_root.findLeafNode(action.m_ix);

            a_imgui.beginTooltip();
            a_imgui.text("Click to add dependency from " + action.m_source.getFullName() + " to " + action.m_target.getFullName());
            a_imgui.endTooltip();

            action.m_source.renderDependency(a_imgui, action.m_target, m_leafNodeCounter);
        }
        m_root.renderDependencies(a_imgui, m_leafNodeCounter);

        if (g_dnd != null && !a_imgui.isMouseDragging(0, 1.0f)) {
            // convert g_dnd to action
            if (g_dnd.m_target != null) {

                // are we dropping into a child node?
                if (getIndexOfFirstNonSimilarComponentInStr2(g_dnd.m_target.getFullName(), g_dnd.m_staleSourceNode.getFullName()) == g_dnd.m_staleSourceNode.getFullName().length()) {
                    g_dnd = null;
                    return null;
                }
                Action a = new Action();
                a.m_hiearchyMove = new Action.HierarchyMove();

                a.m_hiearchyMove.m_nodes = new ArrayList<>();
                for (HNode leaf : g_dnd.m_staleSourceNode.getConcreteNodes()) {

                    Action.NodeNamePair pair = new Action.NodeNamePair();
                    pair.m_oldName = leaf.getFullName();
                    String oldName = pair.m_oldName;
                    if (g_dnd.m_staleSourceNode.m_children.size() == 0) {
                        // dragging leaf node so remove all of the old hierarchy
                        oldName = g_dnd.m_staleSourceNode.m_name;
                    }
                    // remove any common part of the old name
                    String targetFullName = g_dnd.m_target.getFullName();
                    String strippedOldName = oldName.substring(getIndexOfFirstNonSimilarComponentInStr2(g_dnd.m_staleSourceNode.m_parent.getFullName(), oldName));

                    if (targetFullName.length() > 0) {
                        pair.m_newName = strippedOldName.length() > 0 ? (targetFullName + "." + strippedOldName).replace("..", ".") : targetFullName;

                    } else {
                        pair.m_newName = strippedOldName;
                    }

                    a.m_hiearchyMove.m_nodes.add(pair);
                    System.out.println(pair.m_oldName + " -> " + pair.m_newName);
                }

                // we may have a move action here too... but as we are acting in a non root node we need to take better care
                ArrayList<HNode> sourceParents = new ArrayList<>();
                g_dnd.m_staleSourceNode.m_parent.getParentNodePath(sourceParents);
                ArrayList<HNode> targetParents = new ArrayList<>();
                g_dnd.m_target.m_parent.getParentNodePath(targetParents);

                HNode commonParent = null;
                // find the first common parent
                // move from the root towards each child
                for (int ix = 0; ix < targetParents.size() && ix < sourceParents.size(); ix++) {

                    if (targetParents.get(ix).m_name == null || targetParents.get(ix).m_name.contentEquals(sourceParents.get(ix).m_name)) {
                        commonParent = targetParents.get(ix);
                    }
                }

                commonParent = g_dnd.m_target;


                ArrayList<HNode> concreteNodes = new ArrayList<>();
                m_root.getConcreteNodes().forEach(n -> concreteNodes.add(n));
                //int sourceIx = getIndex(commonParent.m_children, g_dnd.m_staleSourceNode.getRootParent().getFullName());
                int mousePosIx = getIndexRelativePosition(concreteNodes, a_imgui.getMousePos());

                if (mousePosIx != -1) {
                        // ok we need to move the source to be after the mousePosIx
                        ArrayList<String> order = new ArrayList<>();
                        System.out.println("New node order:");
                        for (HNode n : m_root.getConcreteNodes()) {
                            // i we find the mouse index node we insert the source targets after that one
                            // we do not insert the nodes in the source, i.e. they are the concrete nodes we want to move
                            boolean found = false;
                            for(HNode sN : g_dnd.m_staleSourceNode.getConcreteNodes()) {
                                if (sN.getFullName().contentEquals(n.getFullName())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                order.add(n.getFullName());
                                System.out.println(n.getFullName());
                            }

                            if (n == concreteNodes.get(mousePosIx)) {   // the mouse ix is done using the commonParent children so we probably need to look at something special here.
                                for (HNode cNode : g_dnd.m_staleSourceNode.getConcreteNodes()) {
                                    order.add(cNode.getFullName());
                                    System.out.println(cNode.getFullName());
                                }
                            }

                        a.m_nodeOrder = order;
                    }
                }

                g_dnd = null;
                return a;
            } else {
                // move to root
                Action a = new Action();
                a.m_hiearchyMove = new Action.HierarchyMove();

                a.m_hiearchyMove.m_nodes = new ArrayList<>();
                for (HNode concrete : g_dnd.m_staleSourceNode.getConcreteNodes()) {
                    Action.NodeNamePair pair = new Action.NodeNamePair();
                    pair.m_oldName = concrete.getFullName();

                    String parentFullName = g_dnd.m_staleSourceNode.m_parent.m_name == null ? "" : g_dnd.m_staleSourceNode.m_parent.getFullName() + ".";   // +. as we know that there are children...
                    String strippedOldName = pair.m_oldName.substring(getIndexOfFirstNonSimilarComponentInStr2(parentFullName, pair.m_oldName));

                    pair.m_newName = strippedOldName;

                    System.out.println(pair.m_oldName);
                    System.out.println(parentFullName);

                    a.m_hiearchyMove.m_nodes.add(pair);
                }

                // we may also rearrange the order
                // as we don't have a drop target we are moving in the root
                int sourceIx = getIndex(m_root.m_children, g_dnd.m_staleSourceNode.getRootParent().getFullName());
                int mousePosIx = getIndexRelativePosition(m_root.m_children, a_imgui.getMousePos());

                if (mousePosIx != -1 && sourceIx != -1) {
                    if (mousePosIx + 1 != sourceIx && mousePosIx != sourceIx) {
                        // ok we need to move the source to be after the mousePosIx
                        ArrayList<String> order = new ArrayList<>();
                        System.out.println("New node order:");
                        for (HNode n : m_root.getConcreteNodes()) {
                            // i we find the mouse index node we insert the source targets after that one
                            // we do not insert the nodes in the source, i.e. they are the concrete nodes we want to move
                            boolean found = false;
                            for(HNode sN : g_dnd.m_staleSourceNode.getConcreteNodes()) {
                                if (sN.getFullName().contentEquals(n.getFullName())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                order.add(n.getFullName());
                                System.out.println(n.getFullName());
                            }

                            if (n.getRootParent() == m_root.m_children.get(mousePosIx)) {   // the mouse ix is done using the root children so we probably need to look at the n root parent here.
                                for (HNode cNode : g_dnd.m_staleSourceNode.getConcreteNodes()) {
                                    order.add(cNode.getFullName());
                                    System.out.println(cNode.getFullName());
                                }
                            }
                        }

                        a.m_nodeOrder = order;
                    }
                }

                g_dnd = null;
                return a;
            }
        } else {
            g_dnd = m_root.doDragNDrop(a_imgui, g_dnd);
        }

        return null;
    }

    private int getIndex(Iterable<HNode> a_nodes, String a_fullName) {
        int ix = 0;
        for (HNode n : a_nodes) {
            if (n.getFullName().contentEquals(a_fullName)) {
                return ix;
            }
        }
        return -1;
    }

    HNode m_root = new HNode();
    int m_leafNodeCounter = 0;

}
