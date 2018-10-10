package archviz;

import glm_.vec2.Vec2;
import glm_.vec4.Vec4;
import imgui.InputTextFlag;
import imgui.StyleVar;
import imgui.TextEditState;
import imgui.internal.DrawCornerFlag;
import imgui.internal.Rect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static imgui.ImguiKt.COL32;
import static imgui.ImguiKt.getNUL;

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
        Vec4 m_color = new Vec4(0.5, 0.5, 0.5, 1.0);

        int getIntColor(int a_alpha)  {
            return COL32((int)(m_color.getX() * 255), (int)(m_color.getY() * 255), (int)(m_color.getZ() * 255), a_alpha);
        }
    }

    static class VisualsManager {
        HNode.Visuals addNew(HNode a_node) {
            HNode.Visuals ret = new HNode.Visuals();
            m_nodeState.put(a_node.getFullName(), ret);

            ret.m_color = new Vec4(0.5, 0.5, 0.5, 1.0);

            return ret;
        }

        HNode.Visuals getNodeState(HNode a_node) {
            return m_nodeState.get(a_node.getFullName());
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

    public int countNodes() {
        int sum = m_name == null ? 0 : 1;
        for (HNode n : m_children) {
            sum += n.countNodes();
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
                c.getConcreteNodes(a_nodes);
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

                a_imgui.addRect(dragRect.getTl(), dragRect.getBr(), COL32(175, 175, 175, 255), g_rounding, DrawCornerFlag.All.getI(), 2);
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


            a_imgui.addRectFilled(m_rect.getTl(), m_rect.getBr(), v.getIntColor(a_alpha), g_rounding, DrawCornerFlag.All.getI());

            if (m_parentNodeRepresentation) {
                a_imgui.addDashedRect(m_rect.getTl(), m_rect.getBr(), COL32(175, 175, 175, a_alpha), 2, 5, 5, g_rounding);
            } else {
                a_imgui.addRect(m_rect.getTl(), m_rect.getBr(), COL32(175, 175, 175, a_alpha), g_rounding, DrawCornerFlag.All.getI(), 2);
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
                    a_imgui.addText(textPos, COL32(175, 175, 175, a_alpha), name);
                } break;
                case None: {

                    a_imgui.addText(textRect.getTl(), COL32(175, 175, 175, a_alpha), name);
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
            if (d.getFullName().contentEquals(a_target.getFullName())) {
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
                if (a_imgui.m_imGui.beginMenu(m_name, true)) {
                    if (!a_staleSourceNode.getFullName().contentEquals(getFullName())) {
                        boolean [] selected = {a_staleSourceNode.hasDependencyTo(this)};
                        /*for (HNode d : a_staleSourceNode.m_dependencies) {
                            if (d.getFullName().contentEquals(getFullName())) {
                                selected[0] = true;
                                break;
                            }
                        }*/

                        if (a_imgui.m_imGui.menuItem("*", "", selected, true)) {
                            a_imgui.m_imGui.endMenu();
                            return this;
                        }

                        if (getConcreteRepresentation() == null) {
                            a_imgui.m_imGui.separator();
                        }
                    }

                    for (HNode n : m_children) {
                        HNode ret = n.doNameMenu(a_imgui, a_staleSourceNode);
                        if (ret !=  null) {
                            a_imgui.m_imGui.endMenu();
                            return ret;
                        }
                    }
                    a_imgui.m_imGui.endMenu();
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

            if (a_imgui.m_imGui.menuItem(m_name, "", selected, true)) {
                return this;
            }
            if (m_parentNodeRepresentation) {
                a_imgui.m_imGui.separator();
            }
        }

        return null;
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
