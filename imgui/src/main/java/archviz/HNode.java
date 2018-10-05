package archviz;

import glm_.vec2.Vec2;
import imgui.InputTextFlag;
import imgui.internal.DrawCornerFlag;
import imgui.internal.Rect;

import java.util.ArrayList;

import static imgui.ImguiKt.COL32;

class HNode {
    public String m_name;
    ArrayList<HNode> m_children = new ArrayList<>();
    ArrayList<HNode> m_dependencies = new ArrayList<>();
    HNode m_parent;
    Rect m_rect;
    int m_leafNodeIx = -1;
    private boolean m_parentNodeRepresentation = false;
    static final int g_rounding = 20;
    static final float g_margin = 10;

    public HNode getRootParent() {
        if (m_parent.m_name != null) {
            return m_parent.getRootParent();
        }
        return this;
    }

    public int assignRenderOrderLeafNodeIx(int a_ix) {
        if (m_children.size() == 0) {
            m_leafNodeIx = a_ix;
            return a_ix + 1;
        } else {
            m_leafNodeIx = -1;
            for (HNode n : m_children) {
                a_ix = n.assignRenderOrderLeafNodeIx(a_ix);
            }
        }

        return a_ix;
    }


    static class AddDependencyAction {
        HNode m_source;
        HNode m_target;
        int m_ix;
    }

    public void setToParentNodeRepresentation() {
        m_parentNodeRepresentation = true;
    }

    public boolean isConcreteNode() {
        return m_leafNodeIx >= 0 || m_parentNodeRepresentation;
        //return m_leafNodeIx > -1 || m_forcedConcreteNode;
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
        int index = Integer.MAX_VALUE;
        for(HNode n :  m_children) {
            int cIx = n.getMinLeafNodeIx();
            if (cIx < index) {
                index = cIx;
            }
        }
        return index;
    }

    int getMaxLeafNodeIx() {
        if (m_children.size() == 0) {
            return m_leafNodeIx;
        }
        int index = -1;
        for(HNode n :  m_children) {
            int cIx = n.getMaxLeafNodeIx();
            if (cIx > index) {
                index = cIx;
            }
        }
        return index;
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

    HRoot.DragNDropData doDragNDrop(ImGuiWrapper a_imgui, HRoot.DragNDropData a_dnd) {
        Vec2 mousePos = a_imgui.getMousePos();
        if (m_parentNodeRepresentation != true && a_dnd != null && a_imgui.isMouseDragging(0, 1.0f)) {
            a_dnd.m_target = getNodeUnder(mousePos);
            /*a_imgui.beginTooltip();
            String targetStr = a_dnd.m_target != null ? " into " + a_dnd.m_target.getFullName() : "";
            a_imgui.text("Dragging " + a_dnd.m_staleSourceNode.getFullName() + targetStr);
            a_imgui.endTooltip();*/
            Rect drawRect = new Rect(a_dnd.m_dragRect.getTl().plus(a_imgui.getMouseDragDelta(0, 1.0f)), a_dnd.m_dragRect.getBr().plus(a_imgui.getMouseDragDelta(0, 1.0f)));

            a_dnd.m_staleSourceNode.render(drawRect, a_imgui, a_dnd.m_staleSourceNode.getLeafNodeCount(), 100);
            a_imgui.addRect(drawRect.getTl(), drawRect.getBr(), COL32(175, 175, 175, 255), g_rounding, DrawCornerFlag.All.getI(), 2);


            return a_dnd;
        }

        // root node
        if (m_name == null) {
            for(HNode c : m_children) {
                HRoot.DragNDropData ret = c.doDragNDrop(a_imgui, a_dnd);
                if (ret != null) {
                    return ret;
                }
            }
            return null;
        }



        // we may start dragging
        if (m_parentNodeRepresentation != true && m_rect.contains(mousePos)) {
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
                a_dnd = new HRoot.DragNDropData();
                a_dnd.m_dragRect = dragRect;
                a_dnd.m_staleSourceNode = this;
                return a_dnd;
            }
        }

        return null;
    }

    AddDependencyAction render(Rect a_area, ImGuiWrapper a_imgui, final int a_leafNodeCount, int a_alpha) {
        m_rect = new Rect(a_area);
        AddDependencyAction ret = null;
        Vec2 mousePos = a_imgui.getMousePos();
        Rect childArea = new Rect(m_rect);
        if (m_name != null) {

            a_imgui.addRectFilled(m_rect.getTl(), m_rect.getBr(), COL32(75, 75, 75, a_alpha), g_rounding, DrawCornerFlag.All.getI());

            if (m_parentNodeRepresentation) {
                a_imgui.addDashedRect(m_rect.getTl(), m_rect.getBr(), COL32(175, 175, 175, a_alpha), 2, 5, 5, g_rounding);
            } else {
                a_imgui.addRect(m_rect.getTl(), m_rect.getBr(), COL32(175, 175, 175, a_alpha), g_rounding, DrawCornerFlag.All.getI(), 2);
            }

            String name = isConcreteNode() ? !m_parentNodeRepresentation ? m_name : "#"+m_name+"#" : "(" + m_name + ")";
            Vec2 textSize = a_imgui.calcTextSize(name, false);
            Vec2 textPos = m_rect.getTl().plus(m_rect.getSize().div(2).minus(textSize.div(2)));
            if (m_children.size() > 0) {
                textPos.setY(m_rect.getTl().getY() + 3);
            }
            a_imgui.addText(textPos, COL32(175, 175, 175, a_alpha), name);
            a_imgui.m_imGui.setCursorPos(textPos);
            char [] buffer = new char[256];
                if (a_imgui.m_imGui.inputText("", buffer, InputTextFlag.EnterReturnsTrue.getI()));

            //a_imgui.addDashedCircleSegment(m_rect.getCenter(), m_rect.getSize().length() / 2 , COL32(175, 175, 175, a_alpha), 64, 0, 3.14f, 2, 5, 10);

            childArea.expand(-g_margin);
            childArea.setMin(new Vec2((float)childArea.getMin().getX(), childArea.getMin().getY() + textSize.getY() + g_margin));

        }

        Vec2 size = childArea.getSize().div(getLeafNodeCount());
        int consumedLeafNodes = 0;
        for (int ix = 0; ix < m_children.size(); ix++) {
            HNode child = m_children.get(ix);
            Rect childRect = new Rect(childArea.getTl().plus(size.times(consumedLeafNodes)), childArea.getTl().plus(size.times(consumedLeafNodes + child.getLeafNodeCount())));
            AddDependencyAction childAction;
            childAction = child.render(childRect, a_imgui, a_leafNodeCount, a_alpha);
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
            a_imgui.addCircle(p, radius, COL32(175, 175, 175, a_alpha), 8, 1);
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
            a_imgui.addCircle(p, 3, COL32(175, 175, 175, a_alpha), 8, 1);

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
            a_imgui.addCircle(p, 3, COL32(175, 175, 175, a_alpha), 8, 1);

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
            a_imgui.addCircle(p, 3, COL32(175, 175, 175, a_alpha), 8, 1);

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

    public String getFullName() {
        if (m_name == null) {
            return "";
        }
        if (m_parentNodeRepresentation) {
            return m_parent.getFullName();
        }
        String parentName = m_parent.getFullName();
        if (parentName.length() > 0) {
            parentName += ".";
        }
        return  parentName +  m_name;
    }
}
