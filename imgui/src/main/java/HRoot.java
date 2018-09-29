import glm_.vec2.Vec2;
import imgui.ImGui;
import imgui.internal.DrawCornerFlag;
import imgui.internal.Rect;

import java.util.ArrayList;
import java.util.Arrays;

import static imgui.ImguiKt.COL32;

public class HRoot {

    static class AddDependencyAction {
        String a_source;
        String a_target;
    }

    static class HNode {
        public String m_name;
        ArrayList<HNode> m_children = new ArrayList<>();
        ArrayList<HNode> m_dependencies = new ArrayList<>();
        HNode m_parent;
        Rect m_rect;
        int m_leafNodeIx = -1;
        static final int g_rounding = 3;

        static class AddDependencyAction {
            HNode m_source;
            HNode m_target;
            int m_ix;
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

        AddDependencyAction render(Rect a_area, ImGui a_imgui, final int a_leafNodeCount) {
            m_rect = new Rect(a_area);
            AddDependencyAction ret = null;

            Rect childArea = new Rect(m_rect);
            if (m_name != null) {
                if (a_imgui.)
                a_imgui.getWindowDrawList().addRectFilled(m_rect.getTl(), m_rect.getBr(), COL32(75, 75, 75, 255), g_rounding, DrawCornerFlag.All.getI());
                a_imgui.getWindowDrawList().addRect(m_rect.getTl(), m_rect.getBr(), COL32(175, 175, 175, 255), g_rounding, DrawCornerFlag.All.getI(), 2);

                Vec2 textSize = a_imgui.calcTextSize(m_name, false);
                Vec2 textPos = m_rect.getTl().plus(m_rect.getSize().div(2).minus(textSize.div(2)));
                if (m_children.size() > 0) {
                    textPos.setY(m_rect.getTl().getY() + 3);
                }
                a_imgui.getWindowDrawList().addText(textPos, COL32(175, 175, 175, 255), m_name.toCharArray(), m_name.length());

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
            Vec2 mousePos = a_imgui.getMousePos();
            for (int ix = 0; ix < getMinLeafNodeIx(); ix++) {
                Vec2 p = new Vec2();
                p.setY(m_rect.getTl().getY());
                p.setX(m_rect.getTl().getX() + g_rounding + (m_rect.getWidth() - g_rounding * 2) / (getMinLeafNodeIx()) * (ix + 0.5f));
                a_imgui.getWindowDrawList().addCircle(p, radius, COL32(175, 175, 175, 255), 8, 1);
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
                a_imgui.getWindowDrawList().addCircle(p, 3, COL32(175, 175, 175, 255), 8, 1);

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
                a_imgui.getWindowDrawList().addCircle(p, 3, COL32(175, 175, 175, 255), 8, 1);

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
                a_imgui.getWindowDrawList().addCircle(p, 3, COL32(175, 175, 175, 255), 8, 1);

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

        void renderDependency(ImGui a_imgui, HNode a_dest, final int a_leafNodeCount) {
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
                a_imgui.getWindowDrawList().addLine(p1, p2, color, 1.0f);


                p1.setX(p2.getX()); p1.setY(dTL.getY());
                a_imgui.getWindowDrawList().addLine(p2, p1, color, 1.0f);

                p2.setX(p1.getX() - 5);
                p2.setY(p1.getY() - 10);
                a_imgui.getWindowDrawList().addLine(p1, p2, color, 1.0f);

                p2.setX(p1.getX() + 5);
                a_imgui.getWindowDrawList().addLine(p1, p2, color, 1.0f);

            } else {
                Vec2 p1, p2;

                p1 = new Vec2();
                p1.setX(sTL.getX());
                p1.setY(sTL.getY() + g_rounding + ((sSize.getY() - g_rounding * 2) / getMinLeafNodeIx()) * (getMinLeafNodeIx() - a_dest.getMaxLeafNodeIx() - 1 + 0.5f));

                p2 = new Vec2();
                p2.setX(dBR.getX() - g_rounding - (dSize.getX() - g_rounding * 2) / (a_leafNodeCount - a_dest.getMaxLeafNodeIx() - 1) * (getMinLeafNodeIx() - a_dest.getMaxLeafNodeIx() - 1 + 0.5f));
                p2.setY(p1.getY());
                a_imgui.getWindowDrawList().addLine(p1, p2, color, 1.0f);


                p1.setX(p2.getX()); p1.setY(dBR.getY());
                a_imgui.getWindowDrawList().addLine(p2, p1, color, 1.0f);

                p2.setX(p1.getX() - 5);
                p2.setY(p1.getY() + 10);
                a_imgui.getWindowDrawList().addLine(p1, p2, color, 1.0f);

                p2.setX(p1.getX() + 5);
                a_imgui.getWindowDrawList().addLine(p1, p2, color, 1.0f);
            }

        }

        void renderDependencies(ImGui a_imgui, final int a_leafNodeCount) {

            for(HNode dest : m_dependencies) {
                renderDependency(a_imgui, dest, a_leafNodeCount);
            }

            for (HNode c : m_children) {
                c.renderDependencies(a_imgui, a_leafNodeCount);
            }
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

    private String getNodeFullName(HNode a_node) {
        if (a_node == m_root) {
            return "";
        }
        String parentName = getNodeFullName(a_node.m_parent);
        if (parentName.length() > 0) {
            parentName += ".";
        }
        return  parentName +  a_node.m_name;
    }

    private void addNode(String [] a_names, HNode a_parent) {
        if (a_names.length == 1) {
            HNode leaf = addNode(a_names[0], a_parent);
            leaf.m_leafNodeIx = m_leafNodeCounter;
            m_leafNodeCounter++;
        } else {
            HNode nextParent = null;
            for (HNode ch : a_parent.m_children) {
                if (ch.m_name.contentEquals(a_names[0])) {
                    nextParent = ch;
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

    public void render(Rect a_rect, ImGui a_imgui) {
        HNode.AddDependencyAction action;
        action = m_root.render(a_rect, a_imgui, m_leafNodeCounter);
        if (action != null) {


            action.m_source = action.m_source != null ? action.m_source : m_root.findLeafNode(action.m_ix);
            action.m_target = action.m_target != null ? action.m_target : m_root.findLeafNode(action.m_ix);

            a_imgui.beginTooltip();
            a_imgui.text("Click to add dependency from " + getNodeFullName(action.m_source) + " to " + getNodeFullName(action.m_target));
            a_imgui.endTooltip();

            action.m_source.renderDependency(a_imgui, action.m_target, m_leafNodeCounter);
        }
        m_root.renderDependencies(a_imgui, m_leafNodeCounter);
    }

    HNode m_root = new HNode();
    int m_leafNodeCounter = 0;

}
