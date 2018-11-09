package hiviz;

import glm_.vec2.Vec2;
import glm_.vec4.Vec4;
import gui.ImGuiWrapper;
import imgui.Col;
import imgui.ImGui;

import java.util.ArrayList;
import java.util.Arrays;

import static imgui.ImguiKt.COL32;

public class Tree {
    public static class TNode {
        private String m_name;
        ArrayList<TNode> m_children;
        TNode m_parent;
        boolean m_parentNodeRepresentation;
        boolean m_isLeaf;
        Mapping m_mapping;
        private static class Mapping {
            Vec4 m_color;
            String m_mappedTo;
        }

        public void setMapping(String a_name, Vec4 a_color) {
            m_mapping = new Mapping();
            m_mapping.m_mappedTo = a_name;
            m_mapping.m_color = a_color;
        }

        TNode(String a_name, TNode a_parent) {
            m_name = a_name;
            m_parent = a_parent;
            m_parentNodeRepresentation = false;
            m_children = new ArrayList<>();
            m_isLeaf = false;

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

        void doTree(ImGuiWrapper a_imgui) {
            if (m_name != null) {
                if (m_children.size() == 0) {

                    if (m_mapping != null) {

                        Vec2 pos = a_imgui.imgui().getCursorPos().plus(a_imgui.imgui().getWindowPos());

                        float radius = a_imgui.getTextLineHeightWithSpacing() / 2;
                        pos.setY(pos.getY() + radius - 1 - a_imgui.imgui().getScrollY());
                        pos.setX(pos.getX() + a_imgui.imgui().calcTextSize(m_name, false).getX() + radius);

                        doMapping(a_imgui, pos, radius, m_mapping);
                    }
                    a_imgui.text(m_name);

                } else if (m_children.size() > 0) {

                    ArrayList<Mapping> mappings = new ArrayList<>();

                    for (TNode c : m_children) {
                        c.getMappingColors(mappings);
                    }

                    Vec2 pos = a_imgui.imgui().getCursorPos().plus(a_imgui.imgui().getWindowPos());

                    float radius = a_imgui.getTextLineHeightWithSpacing() / 2;
                    pos.setY(pos.getY() + radius - 1 - a_imgui.imgui().getScrollY());
                    pos.setX(pos.getX() + a_imgui.imgui().calcTextSize(m_name, false).getX() + radius + a_imgui.imgui().getTreeNodeToLabelSpacing());


                    if (a_imgui.imgui().treeNode(m_name)) {
                        for (TNode c : m_children) {
                            c.doTree(a_imgui);
                        }
                        a_imgui.imgui().treePop();
                    }

                    for (int ix = 0; ix < mappings.size(); ix++) {
                        doMapping(a_imgui, pos, radius, mappings.get(ix));
                        pos.setX(pos.getX() + radius + 2);
                    }
                }
            } else {
                for (TNode c : m_children) {
                    c.doTree(a_imgui);
                }
            }
        }

        private void doMapping(ImGuiWrapper a_imgui, Vec2 pos, float radius, Mapping a_mapping) {
            a_imgui.addCircleFilled(pos, radius - 4, COL32((int) (a_mapping.m_color.getX() * 255), (int) (a_mapping.m_color.getY() * 255), (int) (a_mapping.m_color.getZ() * 255), 255), 16);
            if (a_imgui.isInside(pos, radius - 4, a_imgui.getMousePos())) {
                a_imgui.beginTooltip();
                a_imgui.text(a_mapping.m_mappedTo);
                a_imgui.endTooltip();
            }
        }

        private void getMappingColors(ArrayList<Mapping> a_mappings) {
            if (m_mapping != null) {
                boolean found = false;
                for (Mapping m : a_mappings) {
                    if (m.m_color == m_mapping.m_color) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    a_mappings.add(m_mapping);
                }
            }

            for (TNode c : m_children) {
                c.getMappingColors(a_mappings);
            }
        }

        public void setName(String a_name) {
            m_name = a_name;
        }
    }

    TNode m_root = new TNode(null, null);

    private TNode addNode(String a_name, TNode a_parent) {
        TNode n = new TNode(a_name, a_parent);
        a_parent.m_children.add(n);
        return n;
    }

    private TNode addNode(String [] a_names, TNode a_parent) {
        if (a_names.length == 1) {
            // the child may already exist
            for (TNode c : a_parent.m_children) {
                if (c.m_name.contentEquals(a_names[0]) && !c.isParentNodeRepresentation()) {
                    // we are adding a children to a virtual node so add the concrete node representation
                    if (c.m_children.size() > 0) {
                        TNode n = new TNode(c.m_name, c);
                        n.setToParentNodeRepresentation();   // add the leaf that represent the parent
                        c.m_children.add(0, n);
                    }
                    return c;
                }
            }
            if (a_parent.isConcreteNode() && a_parent.m_children.size() == 0) {
                addNode(a_parent.m_name, a_parent).setToParentNodeRepresentation();   // add the leaf that represent the parent
            }
            TNode leaf = addNode(a_names[0], a_parent);
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
                        addNode(nextParent.m_name, nextParent).setToParentNodeRepresentation();   // add the leaf that represent the parent
                    }
                    break;
                }
            }
            if (nextParent == null) {
                nextParent = addNode(a_names[0], a_parent);
            }
            return addNode(Arrays.copyOfRange(a_names, 1, a_names.length), nextParent);
        }
    }

    public TNode addNode(String a_name) {
        return addNode(a_name.split("\\."), m_root);
    }

    public void doTree(ImGui a_imgui) {
        ImGuiWrapper imgui = new ImGuiWrapper(a_imgui);

        m_root.doTree(imgui);
    }
}
