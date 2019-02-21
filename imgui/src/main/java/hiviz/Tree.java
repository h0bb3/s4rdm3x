package hiviz;

import glm_.vec2.Vec2;
import glm_.vec4.Vec4;
import gui.ImGuiWrapper;
import imgui.ImGui;
import imgui.internal.Rect;

import java.util.ArrayList;
import java.util.Arrays;

import static imgui.ImguiKt.COL32;

public class Tree {

    public interface TContextMenuHandler {
        boolean doContextMenu(ImGui a_imgui);
    }

    public interface TNodeVisitor {
        void visit(TNode a_node);
    }

    public static class TNode {
        private String m_name;
        private ArrayList<TNode> m_children;
        private TNode m_parent;
        private boolean m_parentNodeRepresentation;
        private boolean m_isLeaf;
        private ArrayList<Mapping> m_mappings = new ArrayList<>();
        private Object m_nodeObject;


        public Iterable<TNode> children() {
            return m_children;
        }

        public void accept(TNodeVisitor a_visitor) {
            a_visitor.visit(this);
        }

        public TNode doMenu(ImGui a_imgui, Object a_selectedObject) {
            TNode ret = null;
            if (m_name != null) {
                if (m_children.size() > 0) {
                    if (a_imgui.beginMenu(m_name,  true)) {

                        for (TNode tn : m_children) {
                            TNode r = tn.doMenu(a_imgui, a_selectedObject);
                            if (ret == null) {
                                ret = r;
                            }
                        }

                        a_imgui.endMenu();
                    }
                } else {
                    boolean [] selected = {m_nodeObject == a_selectedObject};
                    if (a_imgui.menuItem(m_name, "", selected, true)) {
                        return this;
                    }
                }
            } else {
                for (TNode tn : m_children) {
                    TNode r = tn.doMenu(a_imgui, a_selectedObject);
                    if (ret == null) {
                        ret = r;
                    }
                }
            }

            return ret;
        }

        public TNode getParent() {
            return m_parent;
        }

        public Object getObject() {
            if (m_isLeaf) {
                return m_nodeObject;
            } else {
                return null;
            }
        }

        public int childCount() {
            return m_children.size();
        }

        public int concreteChildCount() {
            int count = 0;

            for (TNode n : m_children) {
                if (n.isConcreteNode()) {
                    count++;
                }
            }

            return count;
        }

        private static class Mapping {
            Vec4 m_color;
            String m_mappedToName;
            Object m_mappedToObject;
        }

        public void setMapping(String a_name, Vec4 a_color, Object a_object) {
            Mapping m = new Mapping();
            m.m_mappedToName = a_name;
            m.m_mappedToObject = a_object;
            m.m_color = a_color;
            m_mappings.add(m);
        }

        public Object getMappedObject(int a_index) {
            if (m_mappings != null && a_index < m_mappings.size()) {
                return m_mappings.get(a_index).m_mappedToObject;
            }

            return null;
        }

        TNode(String a_name, TNode a_parent, Object a_nodeObject) {
            m_name = a_name;
            m_parent = a_parent;
            m_parentNodeRepresentation = false;
            m_children = new ArrayList<>();
            m_isLeaf = false;
            m_nodeObject = a_nodeObject;

        }

        public String getFullName() {
            if (m_parent != null) {
                String pName = m_parent.getFullName();
                if (pName != null) {
                    return pName + "." + m_name;
                }
            }
            return m_name;
        }

        public String getName() {
            return m_name;
        }

        public void setToParentNodeRepresentation() {
            m_parentNodeRepresentation = true;
        }

        public boolean isParentNodeRepresentation() {
            return m_parentNodeRepresentation;
        }

        public boolean isConcreteNode() {
            // during construction we can not trust the number of children to be an accurate representation
            return (m_children.size() == 0 && m_isLeaf) || m_parentNodeRepresentation;
            //return m_leafNodeIx > -1 || m_forcedConcreteNode;
        }

        TNode doTree(ImGuiWrapper a_imgui, String a_treeId) {
            TNode ret = null;
            if (m_name != null) {
                if (m_children.size() == 0) {

                    if (m_mappings != null) {
                        int xoffset = 3;
                        float textWidth = a_imgui.imgui().calcTextSize(m_name, false).getX();
                        for (Mapping m : m_mappings) {

                            Vec2 pos = a_imgui.imgui().getCursorPos().plus(a_imgui.imgui().getWindowPos());

                            float radius = a_imgui.getTextLineHeightWithSpacing() / 2;
                            pos.setY(pos.getY() + radius - 1 - a_imgui.imgui().getScrollY());
                            pos.setX(pos.getX() + textWidth + radius + xoffset);

                            xoffset += radius * 2 + 3;
                            doMapping(a_imgui, pos, radius, m);
                        }
                    }


                    Vec2 pos = a_imgui.imgui().getCursorPos().plus(a_imgui.imgui().getWindowPos());
                    pos.setY(pos.getY() - 1 - a_imgui.imgui().getScrollY());
                    Rect r = new Rect();

                    r.setMin(pos);
                    r.setMax( pos.plus((float)a_imgui.imgui().calcTextSize(m_name, false).getX() + a_imgui.imgui().getTreeNodeToLabelSpacing(), a_imgui.getTextLineHeightWithSpacing()));

                    a_imgui.text(m_name);

                    if (a_imgui.isInside(r, a_imgui.getMousePos())) {
                        ret = this;
                    }


                } else if (m_children.size() > 0) {

                    ArrayList<Mapping> mappings = new ArrayList<>();

                    for (TNode c : m_children) {
                        c.addUniqueMappings(mappings);
                    }

                    Vec2 pos = a_imgui.imgui().getCursorPos().plus(a_imgui.imgui().getWindowPos());

                    float radius = a_imgui.getTextLineHeightWithSpacing() / 2;
                    pos.setY(pos.getY() + radius - 1 - a_imgui.imgui().getScrollY());
                    pos.setX(pos.getX() + a_imgui.imgui().calcTextSize(m_name, false).getX() + radius + a_imgui.imgui().getTreeNodeToLabelSpacing());

                    {
                        Vec2 tpos = a_imgui.imgui().getCursorPos().plus(a_imgui.imgui().getWindowPos());
                        tpos.setY(tpos.getY() - 1 - a_imgui.imgui().getScrollY());
                        Rect r = new Rect();
                        r.setMin(tpos);

                        r.setMax(r.getMin().plus((float) a_imgui.imgui().calcTextSize(m_name, false).getX() + a_imgui.imgui().getTreeNodeToLabelSpacing(), a_imgui.getTextLineHeightWithSpacing()));
                        if (a_imgui.isInside(r, a_imgui.getMousePos())) {
                            ret = this;
                        }
                    }


                    if (a_imgui.imgui().treeNode(a_treeId + m_name, "%s", m_name)) {
                        for (TNode c : m_children) {
                            TNode r = c.doTree(a_imgui, a_treeId);
                            if (ret == null && r != null) {
                                ret = r;
                            }
                        }
                        a_imgui.imgui().treePop();
                    }

                    for (int ix = 0; ix < mappings.size(); ix++) {
                        doMapping(a_imgui, pos, radius, mappings.get(ix));
                        pos.setX(pos.getX() + radius + 3);
                    }
                }
            } else {
                for (TNode c : m_children) {
                    TNode r = c.doTree(a_imgui, a_treeId);
                    if (ret == null && r != null) {
                        ret = r;
                    }
                }
            }

            return ret;
        }

        private void doMapping(ImGuiWrapper a_imgui, Vec2 pos, float radius, Mapping a_mapping) {
            a_imgui.addCircleFilled(pos, radius - 4, COL32((int) (a_mapping.m_color.getX() * 255), (int) (a_mapping.m_color.getY() * 255), (int) (a_mapping.m_color.getZ() * 255), 255), 16);
            if (a_imgui.isInside(pos, radius - 4, a_imgui.getMousePos())) {
                a_imgui.beginTooltip();
                a_imgui.text(a_mapping.m_mappedToName);
                a_imgui.endTooltip();
            }
        }

        private void addUniqueMappings(ArrayList<Mapping> a_mappings) {
            if (m_mappings != null) {
                boolean found = false;
                for (Mapping myMapping : m_mappings) {
                    for (Mapping m : a_mappings) {
                        if (m.m_color == myMapping.m_color) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        a_mappings.add(myMapping);
                    }
                }
            }

            for (TNode c : m_children) {
                c.addUniqueMappings(a_mappings);
            }
        }

        public void setName(String a_name) {
            m_name = a_name;
        }
    }

    TNode m_root = new TNode(null, null, null);

    private TNode addNode(String a_name, TNode a_parent, Object a_nodeObject) {
        TNode n = new TNode(a_name, a_parent, a_nodeObject);
        a_parent.m_children.add(n);
        return n;
    }

    private TNode addNode(String [] a_names, TNode a_parent, Object a_nodeObject) {
        if (a_names.length == 1) {
            // the child may already exist
            for (TNode c : a_parent.m_children) {
                if (c.m_name.contentEquals(a_names[0]) && !c.isParentNodeRepresentation()) {
                    // we are adding a children to a virtual node so add the concrete node representation
                    if (c.m_children.size() > 0) {
                        TNode n = new TNode(c.m_name, c, a_nodeObject);
                        n.setToParentNodeRepresentation();   // add the leaf that represent the parent
                        c.m_children.add(0, n);
                    }
                    return c;
                }
            }
            if (a_parent.isConcreteNode() && a_parent.m_children.size() == 0) {
                addNode(a_parent.m_name, a_parent, a_nodeObject).setToParentNodeRepresentation();   // add the leaf that represent the parent
            }
            TNode leaf = addNode(a_names[0], a_parent, a_nodeObject);
            leaf.m_isLeaf = true;
            //leaf.m_leafNodeIx = m_leafNodeCounter;
            //m_leafNodeCounter++;
            return leaf;
        } else {
            TNode nextParent = null;
            for (TNode ch : a_parent.m_children) {
                if (ch.m_name.contentEquals(a_names[0]) && ch.isParentNodeRepresentation() != true) {
                    nextParent = ch;
                    if (nextParent.m_children.size() == 0) {
                        addNode(nextParent.m_name, nextParent, a_nodeObject).setToParentNodeRepresentation();   // add the leaf that represent the parent
                    }
                    break;
                }
            }
            if (nextParent == null) {
                nextParent = addNode(a_names[0], a_parent, a_nodeObject);
            }
            return addNode(Arrays.copyOfRange(a_names, 1, a_names.length), nextParent, a_nodeObject);
        }
    }

    public TNode addNode(String a_name, Object a_nodeObject) {
        return addNode(a_name.split("\\."), m_root, a_nodeObject);
    }

    public TNode doTree(ImGui a_imgui, String a_treeId) {
        ImGuiWrapper imgui = new ImGuiWrapper(a_imgui);

        return m_root.doTree(imgui, a_treeId);
    }

    public TNode doMenu(ImGui a_imgui, Object a_selectedObject) {
        return m_root.doMenu(a_imgui, a_selectedObject);

    }

    public void doVisit(TNodeVisitor a_visitor) {
        m_root.accept(a_visitor);
    }
}
