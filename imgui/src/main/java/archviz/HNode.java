package archviz;

import glm_.vec2.Vec2;
import glm_.vec4.Vec4;
import imgui.internal.DrawCornerFlag;
import imgui.internal.Rect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static imgui.ImguiKt.COL32;

class HNode {

    public String m_name;
    ArrayList<HNode> m_children = new ArrayList<>();
    ArrayList<HNode> m_dependencies = new ArrayList<>();
    HNode m_parent;
    Rect m_rect;
    int m_leafNodeIx = -1;
    private boolean m_parentNodeRepresentation = false;
    static final int g_rounding = 7;
    static final float g_margin = 7;

    static class Action {
        static class AddDependency {
            HNode m_source;
            HNode m_target;
            int m_ix;
        }

        static class RenameNode {
            HNode m_node;
            String a_newName;
        }

        AddDependency m_addDependencyAction;
        RenameNode m_renameNodeAction;
    }

    static class DragNDropData {
        Rect m_dragRect;
        HNode m_staleSourceNode;
        HNode m_target;
    }

    static class Visuals {
        Vec4 m_bgColor = new Vec4(0.25, 0.25, 0.25, 1.0);
        Vec4 m_textColor = new Vec4(0.85, 0.85, 0.85, 1.0);
        Rect m_startRect;
        Rect m_lastRenderedRect;
        float m_timer;

        boolean isEqual(Rect a_r1, Rect a_r2) {
            Vec2 v1 = a_r1.getMax(), v2 = a_r2.getMax();

            if (Math.abs(v1.getX() - v2.getX()) < 0.5 & Math.abs(v1.getY() - v2.getY()) < 0.5) {
                v1 = a_r1.getMin(); v2 = a_r2.getMin();
                if (Math.abs(v1.getX() - v2.getX()) < 0.5 & Math.abs(v1.getY() - v2.getY()) < 0.5) {
                   return true;
                }
            }
            return false;
        }

        void interpolateRect(float a_elapsedTime, Rect a_goal) {
            final float timerStart = 1;
            if (m_startRect == null || (m_timer < 0 && !isEqual(m_lastRenderedRect, a_goal))) {
                if (m_startRect == null) {
                    m_startRect = new Rect(a_goal.getCenter(), a_goal.getCenter());
                    m_lastRenderedRect = new Rect(a_goal.getCenter(), a_goal.getCenter());
                }
                m_timer = timerStart;
            } else {
                m_timer -= a_elapsedTime;
                if (m_timer <= 0) {
                    m_lastRenderedRect.setMax(a_goal.getMax());
                    m_lastRenderedRect.setMin(a_goal.getMin());

                    m_startRect.setMax(a_goal.getMax());
                    m_startRect.setMin(a_goal.getMin());

                } else {
                    float percent = 1.0f - (m_timer / timerStart);

                    // smooth step it
                    percent = (percent * percent * (3-2*percent));

                    Vec2 maxDir = a_goal.getMax().minus(m_startRect.getMax());
                    Vec2 minDir = a_goal.getMin().minus(m_startRect.getMin());

                    m_lastRenderedRect.setMax(m_startRect.getMax().plus(maxDir.times(percent)));
                    m_lastRenderedRect.setMin(m_startRect.getMin().plus(minDir.times(percent)));
                }
            }
        }

        int getBgColorAsInt(int a_alpha)  {
            return getColorAsInt(m_bgColor, a_alpha);
        }

        int getTextColorAsInt(int a_alpha) {
            return getColorAsInt(m_textColor, a_alpha);
        }

        private int getColorAsInt(Vec4 a_color, int a_alpha) {
            return COL32((int)(a_color.getX() * 255), (int)(a_color.getY() * 255), (int)(a_color.getZ() * 255), a_alpha);
        }

        public void copyColors(Visuals a_v) {
            m_bgColor = new Vec4(a_v.m_bgColor);
            m_textColor = new Vec4(a_v.m_textColor);
        }
    }


    static class VisualsManager {
        HNode.Visuals addNew(HNode a_node) {
            HNode.Visuals ret = new HNode.Visuals();
            copyColorsFromParent(a_node, ret);
            m_nodeState.put(a_node.getUniqueName(), ret);

            return ret;
        }

        private void copyColorsFromParent(HNode a_node, Visuals a_v) {
            if (a_node.m_parent != null && a_node.m_parent.m_name != null) {
                Visuals parentV = getNodeState(a_node.m_parent);
                if (parentV != null) {
                    a_v.copyColors(parentV);

                } else {
                    copyColorsFromParent(a_node.m_parent, a_v);
                }
            }
        }

        HNode.Visuals getNodeState(HNode a_node) {
            return m_nodeState.get(a_node.getUniqueName());
        }

        public void copyColorsToNewNode(String a_from, String a_to) {
            Visuals f = m_nodeState.get(a_from);
            if (f != null) {
                Visuals t = new Visuals();
                t.copyColors(f);
                m_nodeState.put(a_to, t);
            }
        }

        HashMap<String, Visuals> m_nodeState = new HashMap<>();
    }

    static class NodeNameEdit {
        private HNode m_staleEditingNode;
        char [] m_buffer = new char[256];

        public enum EditingStatus {
            None,
            Editing,
            Changing
        }

        public void setEditingNode(HNode a_node) {
            m_staleEditingNode = a_node;
            copyToBuffer(a_node.m_name);
        }

        EditingStatus getEditingStatus(HNode a_node) {
            if (m_staleEditingNode != null && m_staleEditingNode.getFullName().contentEquals(a_node.getFullName())) {
                if (m_staleEditingNode.m_parentNodeRepresentation == a_node.m_parentNodeRepresentation) {
                    return EditingStatus.Editing;
                } else {
                    return EditingStatus.Changing;
                }
            }

            return EditingStatus.None;
        }


        public void copyToBuffer(String a_str) {
            Arrays.fill(m_buffer, '\0');
            for (int i = 0; i < a_str.length() && i < m_buffer.length; i++) {
                m_buffer[i] = a_str.charAt(i);
            }
        }

        public String getBufferString() {
            String txt = new String();
            for (int i = 0; i < m_buffer.length; i++) {
                if (m_buffer[i] == '\0') {
                    break;
                }
                txt += m_buffer[i];
            }

            return txt;
        }

        public void reset() {
            m_staleEditingNode = null;
            Arrays.fill(m_buffer, '\0');
        }
    }

    public int countChildren() {
        int sum = 0;
        for (HNode n : m_children) {
            sum += 1 + n.countChildren();
        }

        return sum;
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

    public void setToParentNodeRepresentation() {
        m_parentNodeRepresentation = true;
    }

    public boolean isParentNodeRepresentation() {
        return m_parentNodeRepresentation;
    }

    public boolean isAbstract() {
        return m_children.size() > 0 && m_children.get(0).m_parentNodeRepresentation != true;
    }

    public HNode getConcreteRepresentation() {
        if (m_children.size() > 0 && m_children.get(0).m_parentNodeRepresentation) {
            return m_children.get(0);
        }
        return null;
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

    Iterable<HNode> getConcreteNodes() {
        ArrayList<HNode> ret = new ArrayList<>();
        getConcreteNodes(ret);
        return ret;
    }

    private void getAllNodes(ArrayList<HNode> a_nodes) {
        if (m_children.size() > 0) {
            for (HNode c : m_children) {
                c.getAllNodes(a_nodes);
            }
        }

        if (m_name != null) {
            a_nodes.add(this);
        }
    }

    Iterable<HNode> getAllNodes() {
        ArrayList<HNode> ret = new ArrayList<>();
        getAllNodes(ret);
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

    public HNode getNodeUnder(Vec2 a_point) {
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
        if (m_parentNodeRepresentation != true && a_dnd != null && a_imgui.isMouseDragging(0, 1.0f)) {
            a_dnd.m_target = getNodeUnder(mousePos);

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



        // we may start dragging or editing
        if (a_imgui.isInside(m_rect, mousePos)) {
            // first we check the children
            for (HNode c : m_children) {
                a_dnd = c.doDragNDrop(a_imgui, a_dnd);
                if (a_dnd != null) {
                    return a_dnd;
                }
            }
            a_imgui.stopWindowDrag();


            if (m_parentNodeRepresentation != true && a_imgui.isMouseDragging(0, 1.0f)) {

                Rect dragRect = new Rect(m_rect.getTl().plus(a_imgui.getMouseDragDelta(0, 1.0f)), m_rect.getBr().plus(a_imgui.getMouseDragDelta(0, 1.0f)));

                //a_imgui.addRect(dragRect.getTl(), dragRect.getBr(), COL32(175, 175, 175, 255), g_rounding, DrawCornerFlag.All.getI(), 2);
                a_dnd = new DragNDropData();
                a_dnd.m_dragRect = dragRect;
                a_dnd.m_staleSourceNode = this;
                return a_dnd;
            }
        }

        return null;
    }

    Action render(Rect a_area, ImGuiWrapper a_imgui, final int a_leafNodeCount, int a_alpha, VisualsManager a_nvm, NodeNameEdit a_nne) {
        m_rect = new Rect(a_area);
        Action ret = null;
        Vec2 mousePos = a_imgui.getMousePos();
        Rect childArea = new Rect(m_rect);
        if (m_name != null) {

            Visuals v = a_nvm.getNodeState(this);
            if (v == null) {
                v = a_nvm.addNew(this);
            }

            //v.interpolateRect((float)a_imgui.m_imGui.getIo().getDeltaTime(), m_rect);
            //a_imgui.addRectFilled(v.m_lastRenderedRect.getTl(), v.m_lastRenderedRect.getBr(), v.getBgColorAsInt(a_alpha), g_rounding, DrawCornerFlag.All.getI());

            a_imgui.addRectFilled(m_rect.getTl(), m_rect.getBr(), v.getBgColorAsInt(a_alpha), g_rounding, DrawCornerFlag.All.getI());


            if (m_parentNodeRepresentation) {
                a_imgui.addDashedRect(m_rect.getTl(), m_rect.getBr(), v.getTextColorAsInt(a_alpha), 2, 5, 5, g_rounding);
            } else {
                a_imgui.addRect(m_rect.getTl(), m_rect.getBr(), v.getTextColorAsInt(a_alpha), g_rounding, DrawCornerFlag.All.getI(), 2);
            }

            Rect textRect = new Rect();

            String name = !isAbstract() ? m_name : "(" + m_name + ")";
            Vec2 textSize = a_imgui.calcTextSize(name, false);
            Vec2 textPos = m_rect.getTl().plus(m_rect.getSize().div(2).minus(textSize.div(2)));
            if (m_children.size() > 0) {
                textPos.setY(m_rect.getTl().getY() + 3);
            }
            textRect.setMin(textPos);
            textRect.setMax(textPos.plus(textSize));

            if (a_imgui.isMouseDoubleClicked(0) && textRect.contains(mousePos)) {
                a_nne.setEditingNode(this);
            }

            switch (a_nne.getEditingStatus(this)) {
                case Editing: {
                    String txt = a_nne.getBufferString();

                    textSize = a_imgui.calcTextSize(txt, false);
                    textPos = m_rect.getTl().plus(m_rect.getSize().div(2).minus(textSize.div(2)));
                    if (m_children.size() > 0) {
                        textPos.setY(m_rect.getTl().getY() + 3);
                    }
                    switch (a_imgui.inputTextSingleLine(textPos, textSize.getX(), "", a_nne.m_buffer)) {
                        case Done: {
                            ret = new Action();
                            ret.m_renameNodeAction = new Action.RenameNode();
                            ret.m_renameNodeAction.m_node = this.m_parentNodeRepresentation ? this.m_parent : this;
                            ret.m_renameNodeAction.a_newName = new String(txt);
                        }
                        case Canceled: {
                            a_nne.reset();
                        } break;
                    }
                } break;
                case Changing: {
                    String txt = a_nne.getBufferString();
                    name = !isAbstract() ?  txt : "(" + txt + ")";
                    textSize = a_imgui.calcTextSize(name, false);
                    textPos = m_rect.getTl().plus(m_rect.getSize().div(2).minus(textSize.div(2)));
                    if (m_children.size() > 0) {
                        textPos.setY(m_rect.getTl().getY() + 3);
                    }
                    a_imgui.addText(textPos, v.getTextColorAsInt(a_alpha), name);
                } break;
                case None: {

                    a_imgui.addText(textRect.getTl(), v.getTextColorAsInt(a_alpha), name);
                    if (textRect.contains(mousePos)) {
                        a_imgui.beginTooltip();
                        a_imgui.text(getFullName());
                        a_imgui.endTooltip();
                    }

                } break;
            }

            childArea.expand(-g_margin);
            childArea.setMin(new Vec2((float)childArea.getMin().getX(), childArea.getMin().getY() + textRect.getHeight()));

        }

        Vec2 size = childArea.getSize().div(getLeafNodeCount());
        int consumedLeafNodes = 0;
        for (int ix = 0; ix < m_children.size(); ix++) {
            HNode child = m_children.get(ix);
            Rect childRect = new Rect(childArea.getTl().plus(size.times(consumedLeafNodes)), childArea.getTl().plus(size.times(consumedLeafNodes + child.getLeafNodeCount())));
            Action childAction;
            childAction = child.render(childRect, a_imgui, a_leafNodeCount, a_alpha, a_nvm, a_nne);
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
            /*a_imgui.addCircle(p, radius, COL32(175, 175, 175, a_alpha), 8, 1);
            if (a_imgui.isInside(p, radius, mousePos)) {
                ret = new Action();
                ret.m_addDependencyAction = new Action.AddDependency();
                ret.m_addDependencyAction.m_target = this;
                ret.m_addDependencyAction.m_ix = getMinLeafNodeIx() - 1 - ix;
            }*/
        }

        for (int ix = 0; ix < a_leafNodeCount - getMaxLeafNodeIx() - 1; ix++) {
            Vec2 p = new Vec2();
            p.setY(m_rect.getBr().getY());
            p.setX(m_rect.getTl().getX() + g_rounding + (m_rect.getWidth() - g_rounding * 2) / (a_leafNodeCount - getMaxLeafNodeIx() - 1) * (ix + 0.5f));
            /*a_imgui.addCircle(p, 3, COL32(175, 175, 175, a_alpha), 8, 1);
            if (a_imgui.isInside(p, radius, mousePos)) {
                ret = new Action();
                ret.m_addDependencyAction = new Action.AddDependency();
                ret.m_addDependencyAction.m_target = this;
                ret.m_addDependencyAction.m_ix = a_leafNodeCount - ix - 1;
            }*/
        }

        for (int ix = 0; ix < a_leafNodeCount - getMaxLeafNodeIx() - 1; ix++) {
            Vec2 p = new Vec2();
            p.setX(m_rect.getBr().getX());
            p.setY(m_rect.getTl().getY() + g_rounding + (m_rect.getHeight() - g_rounding * 2) / (a_leafNodeCount - getMaxLeafNodeIx() - 1) * (ix + 0.5f));
            /*a_imgui.addCircle(p, 3, COL32(175, 175, 175, a_alpha), 8, 1);

            if (a_imgui.isInside(p, radius, mousePos)) {
                ret = new Action();
                ret.m_addDependencyAction = new Action.AddDependency();
                ret.m_addDependencyAction.m_source = this;
                ret.m_addDependencyAction.m_ix = a_leafNodeCount - 1 - ix;
            }*/
        }

        for (int ix = 0; ix < getMinLeafNodeIx(); ix++) {
            Vec2 p = new Vec2();
            p.setX(m_rect.getTl().getX());
            p.setY(m_rect.getTl().getY() + g_rounding + (m_rect.getHeight() - g_rounding * 2) / (getMinLeafNodeIx()) * (ix + 0.5f));
            /*a_imgui.addCircle(p, 3, COL32(175, 175, 175, a_alpha), 8, 1);

            if (a_imgui.isInside(p, radius, mousePos)) {
                ret = new Action();
                ret.m_addDependencyAction = new Action.AddDependency();
                ret.m_addDependencyAction.m_source = this;
                ret.m_addDependencyAction.m_ix = getMinLeafNodeIx() - 1 - ix;
            }*/
        }

        return ret;
    }

    boolean hasDependencyTo(HNode a_target) {
        if (m_name == null) {
            return false;
        }
        for (HNode d : m_dependencies) {
            if (d.getUniqueName().contentEquals(a_target.getUniqueName())) {
                return true;
            }
        }


        // check all the on my own side
        if (m_parent.m_name !=  null) {
            if (m_parent.hasDependencyTo(a_target)) {
                return true;
            }
        }

        // check all the parents on target side
        if (a_target.m_parent.m_name != null) {
            if (hasDependencyTo(a_target.m_parent)) {
                return true;
            }
        }

        return false;
    }

    HNode doNameMenu(ImGuiWrapper a_imgui, HNode a_staleSourceNode) {
        if (m_children.size() > 0) {
            if (m_name != null) {
                if (a_imgui.imgui().beginMenu(m_name, true)) {
                    if (!a_staleSourceNode.getFullName().contentEquals(getFullName())) {
                        boolean [] selected = {a_staleSourceNode.hasDependencyTo(this)};
                        /*for (HNode d : a_staleSourceNode.m_dependencies) {
                            if (d.getFullName().contentEquals(getFullName())) {
                                selected[0] = true;
                                break;
                            }
                        }*/

                        if (a_imgui.imgui().menuItem("*", "", selected, true)) {
                            a_imgui.imgui().endMenu();
                            return this;
                        }

                        if (getConcreteRepresentation() == null) {
                            a_imgui.imgui().separator();
                        }
                    }

                    for (HNode n : m_children) {
                        HNode ret = n.doNameMenu(a_imgui, a_staleSourceNode);
                        if (ret !=  null) {
                            a_imgui.imgui().endMenu();
                            return ret;
                        }
                    }
                    a_imgui.imgui().endMenu();
                }
            } else {
                for (HNode n : m_children) {
                    HNode ret = n.doNameMenu(a_imgui, a_staleSourceNode);
                    if (ret !=  null) {
                        return ret;
                    }
                }
            }

        } else if (!a_staleSourceNode.getFullName().contentEquals(getFullName())) {
            boolean [] selected = new boolean[] {a_staleSourceNode.hasDependencyTo(this)};

            /*for (HNode d : a_staleSourceNode.m_dependencies) {
                if (d.getFullName().contentEquals(getFullName())) {
                    selected[0] = true;
                    break;
                }
            }*/

            if (a_imgui.imgui().menuItem(m_name, "", selected, true)) {
                return this;
            }
            if (m_parentNodeRepresentation) {
                a_imgui.imgui().separator();
            }
        }

        return null;
    }

    void renderDependency(ImGuiWrapper a_imgui, HNode a_dest, final int a_leafNodeCount, VisualsManager a_vm) {
        Vec2 sTL = m_rect.getTl();
        Vec2 sBR = m_rect.getBr();
        Vec2 sSize = m_rect.getSize();

        Vec2 dTL = a_dest.m_rect.getTl();
        Vec2 dBR = a_dest.m_rect.getBr();
        Vec2 dSize = a_dest.m_rect.getSize();

        float lineThickness = 2.0f;

        Visuals v = a_vm.getNodeState(this);
        if (v == null) {
            v = a_vm.addNew(this);
        }
        int color = v.getTextColorAsInt(255);


        if (sBR.getX() <= dTL.getX()) {
            Vec2 p1, p2;

            p1 = new Vec2();
            p1.setX(sBR.getX());
            p1.setY(sBR.getY() - g_rounding - (sSize.getY() - g_rounding * 2) / (a_leafNodeCount - getMaxLeafNodeIx() - 1) * (a_dest.getMinLeafNodeIx() - getMaxLeafNodeIx() - 0.5f));

            p2 = new Vec2();
            p2.setX(dTL.getX() + g_rounding + ((dSize.getX() -  g_rounding * 2) / a_dest.getMinLeafNodeIx()) * (a_dest.getMinLeafNodeIx() - getMaxLeafNodeIx() - 0.5f));

            p2.setY(p1.getY());
            a_imgui.addLine(p1, p2, color, lineThickness);


            p1.setX(p2.getX()); p1.setY(dTL.getY());
            a_imgui.addLine(p2, p1, color, lineThickness);

            p2.setX(p1.getX() - 5);
            p2.setY(p1.getY() - 10);
            a_imgui.addLine(p1, p2, color, lineThickness);

            p2.setX(p1.getX() + 5);
            a_imgui.addLine(p1, p2, color, lineThickness);

        } else {
            Vec2 p1, p2;

            p1 = new Vec2();
            p1.setX(sTL.getX());
            p1.setY(sTL.getY() + g_rounding + ((sSize.getY() - g_rounding * 2) / getMinLeafNodeIx()) * (getMinLeafNodeIx() - a_dest.getMaxLeafNodeIx() - 1 + 0.5f));

            p2 = new Vec2();
            p2.setX(dBR.getX() - g_rounding - (dSize.getX() - g_rounding * 2) / (a_leafNodeCount - a_dest.getMaxLeafNodeIx() - 1) * (getMinLeafNodeIx() - a_dest.getMaxLeafNodeIx() - 1 + 0.5f));
            p2.setY(p1.getY());
            a_imgui.addLine(p1, p2, color, lineThickness);


            p1.setX(p2.getX()); p1.setY(dBR.getY());
            a_imgui.addLine(p2, p1, color, lineThickness);

            p2.setX(p1.getX() - 5);
            p2.setY(p1.getY() + 10);
            a_imgui.addLine(p1, p2, color, lineThickness);

            p2.setX(p1.getX() + 5);
            a_imgui.addLine(p1, p2, color, lineThickness);
        }

    }

    void renderDependencies(ImGuiWrapper a_imgui, final int a_leafNodeCount, VisualsManager a_vm) {

        for(HNode dest : m_dependencies) {
            renderDependency(a_imgui, dest, a_leafNodeCount, a_vm);
        }

        for (HNode c : m_children) {
            c.renderDependencies(a_imgui, a_leafNodeCount, a_vm);
        }
    }

    public String getUniqueName() {
        String name = getFullName();

        if (m_parentNodeRepresentation) {
            name += "$";
        }

        return name;
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
