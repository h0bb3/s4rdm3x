package hiviz;

import glm_.vec4.Vec4;
import gui.ImGuiWrapper;
import imgui.Col;
import imgui.ImGui;

import java.util.ArrayList;
import java.util.Arrays;

public class Tree {
    public static class TNode {
        private String m_name;
        ArrayList<TNode> m_children;
        TNode m_parent;
        boolean m_parentNodeRepresentation;
        boolean m_isLeaf;


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
                    //a_imgui.imgui().pushStyleColor(Col., new Vec4(1, 1, 1, 1));
                    a_imgui.text(m_name);
                    //a_imgui.imgui().popStyleColor(1);
                } else if (m_children.size() > 0) {
                    if (a_imgui.imgui().treeNode(m_name)) {
                        for (TNode c : m_children) {
                            c.doTree(a_imgui);
                        }
                        a_imgui.imgui().treePop();
                    }
                }
            } else {
                for (TNode c : m_children) {
                    c.doTree(a_imgui);
                }
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
