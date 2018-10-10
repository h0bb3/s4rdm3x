package archviz;

import glm_.vec2.Vec2;
import glm_.vec4.Vec4;
import imgui.*;
import imgui.internal.DrawCornerFlag;
import imgui.internal.Rect;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static imgui.ImguiKt.COL32;

public class HRoot {

    public static class State {






        HNode.VisualsManager m_nvm = new HNode.VisualsManager();
        HNode.NodeNameEdit m_nne = new HNode.NodeNameEdit();
        HNode.DragNDropData m_dNd = null;
        HNode m_underPopUp = null;
    }

    public static class Action {
        public static class NodeNamePair {
            NodeNamePair() {
            }
            NodeNamePair(String a_oldName, String a_newName) {
                m_oldName = a_oldName;
                m_newName = a_newName;
            }
            public String m_oldName;
            public String m_newName;
        }
        public static class HierarchyMove {
            public ArrayList<NodeNamePair> m_nodes = new ArrayList<>();

            void addPair(NodeNamePair a_pair) {
                if (!a_pair.m_oldName.contentEquals(a_pair.m_newName)) {
                    m_nodes.add(a_pair);
                }
            }
        }

        public ArrayList<String> m_nodeOrder;
        public HierarchyMove m_hiearchyMove;
        public HierarchyMove m_addDependenices;
        public HierarchyMove m_removeDependencies;

        public String m_addComponent;
        public ArrayList<String> m_deletedComponents;
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

        if(parent == m_root || parent.m_children.size() < 2) {
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
        if (parent == m_root || parent.m_children.size() < 2) {
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

        if (dest.getConcreteRepresentation() != null) {
            dest = dest.getConcreteRepresentation();
        }

        if (source.getConcreteRepresentation() != null) {
            source = source.getConcreteRepresentation();
        }

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

    private HNode addNode(String [] a_names, HNode a_parent) {
        if (a_names.length == 1) {
            // the child may already exist
            for (HNode c : a_parent.m_children) {
                if (c.m_name.contentEquals(a_names[0]) && !c.isParentNodeRepresentation()) {
                    // we are adding a children to a virtual node so add the concrete node representaion
                    if (c.m_children.size() > 0) {
                        HNode n = new HNode();
                        n.m_name = c.m_name;
                        n.m_parent = c;
                        n.setToParentNodeRepresentation();   // add the leaf that represent the parent
                        c.m_children.add(0, n);
                    }
                    return c;
                }
            }
            if (a_parent.isConcreteNode() && a_parent.m_children.size() == 0) {
                addNode(a_parent.m_name, a_parent).setToParentNodeRepresentation();   // add the leaf that represent the parent
            }
            HNode leaf = addNode(a_names[0], a_parent);
            leaf.m_leafNodeIx = m_leafNodeCounter;
            m_leafNodeCounter++;
            return leaf;
        } else {
            HNode nextParent = null;
            for (HNode ch : a_parent.m_children) {
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

    private HNode addNode(String a_name, HNode a_parent) {
        HNode n = new HNode();
        n.m_name = a_name;
        n.m_parent = a_parent;
        // we are adding into a concrete parent so we always add an extra node here to handle the case of the
        a_parent.m_children.add(n);
        return n;
    }

    public HNode add(String a_nodeName) {
        return addNode(a_nodeName.split("\\."), m_root);
    }

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
        int ret = 0;
        Vec2 firstTl = null;
        System.out.println("Node Checking order:");
        for (HNode n : a_nodes) {
            //if (n.getFullName().contentEquals(g_dnd.m_staleSourceNode.getRootParent().getFullName())) { // we are operating relative the root
            //    sourceIx = ix;
            //}

            System.out.println(n.getFullName());
            // we skip concrete parent nodes as these render their children
            if (n.m_children.size() > 0 && n.isConcreteNode() && n.m_rect.contains(a_pos)) {
                continue;
            }

            // we can check the mouse position like rows and columns as we go from top left corner.
            Vec2 tl, br;
            tl = n.m_rect.getTl();
            br = n.m_rect.getBr();
            if (firstTl == null) {
                firstTl = tl;
            }
            if (a_pos.getY() > tl.getY() && a_pos.getX() > tl.getX()) {
                ret = ix;
            }
            ix++;
        }

        if (a_pos.getX() < firstTl.getX() || a_pos.getY() < firstTl.getY()) {
            return -1;
        }

        return ret;
    }

    public Action render(Rect a_area, ImGui a_imgui, State a_state) {
        return render(a_area, new ImGuiWrapper(a_imgui), a_state);
    }


    private ArrayList<String> changeNodeOrder(Vec2 a_dropPos, ImGuiWrapper a_imgui, HNode.DragNDropData a_dnd) {
        ArrayList<HNode> concreteNodes = new ArrayList<>();
        m_root.getConcreteNodes().forEach(n -> concreteNodes.add(n));
        //int sourceIx = getIndex(commonParent.m_children, g_dnd.m_staleSourceNode.getRootParent().getFullName());
        int mousePosIx = getIndexRelativePosition(concreteNodes, a_dropPos);

        System.out.println("index: " + mousePosIx);

        a_imgui.beginTooltip();
        a_imgui.text("mousePosIx:" + mousePosIx);
        a_imgui.endTooltip();

        // ok we need to move the source to be after the mousePosIx
        ArrayList<String> order = new ArrayList<>();
        System.out.println("New node order:");
        boolean added = false;
        for (HNode n : concreteNodes) {
            // i we find the mouse index node we insert the source targets after that one
            // we do not insert the nodes in the source, i.e. they are the concrete nodes we want to move
            boolean found = false;
            for(HNode sN : a_dnd.m_staleSourceNode.getConcreteNodes()) {
                if (sN.getFullName().contentEquals(n.getFullName())) {
                    found = true;
                    break;
                }
            }

            // dragging to the top left : add before anything
            if (!added && mousePosIx < 0) {
                for (HNode cNode : a_dnd.m_staleSourceNode.getConcreteNodes()) {
                    order.add(cNode.getFullName());
                    System.out.println(cNode.getFullName());
                }
                added = true;
            }

            if (!found) {
                order.add(n.getFullName());
                System.out.println(n.getFullName());
            }

            // dragging in the area
            if (!added && mousePosIx >= 0 && mousePosIx < concreteNodes.size() && n == concreteNodes.get(mousePosIx)) {   // the mouse ix is done using the commonParent children so we probably need to look at something special here.
                for (HNode cNode : a_dnd.m_staleSourceNode.getConcreteNodes()) {
                    order.add(cNode.getFullName());
                    System.out.println(cNode.getFullName());
                }
                added = true;
            }
        }

        // dragging to the bottom right : add last
        if (!added) {
            for (HNode cNode : a_dnd.m_staleSourceNode.getConcreteNodes()) {
                order.add(cNode.getFullName());
                System.out.println(cNode.getFullName());
            }
        }

        return order;
    }

    public Action render(Rect a_rect, ImGuiWrapper a_imgui, State a_state) {
        HNode.Action action;
        m_leafNodeCounter = m_root.assignRenderOrderLeafNodeIx(0);  // leaf node indices need to be in rendering order and not in adding order.

        // fix aney missing states

        /*

            targetLayOut = m_root.calculateLayout(a_rect, m_leadNodeCount);
            if (g_oldLayout != null) {
                layout = g_oldLayout.interpolate(targetLayout, 0-1);
            } else {
                layout = targetLayout;
            }

            m_root.render(layout, a_imgui, m_leafNodeCounter, 255);

         */

        action = m_root.render(a_rect, a_imgui, m_leafNodeCounter, 255, a_state.m_nvm, a_state.m_nne);
        if (action != null) {
            if (action.m_addDependencyAction != null) {

                action.m_addDependencyAction.m_source = action.m_addDependencyAction.m_source != null ? action.m_addDependencyAction.m_source : m_root.findLeafNode(action.m_addDependencyAction.m_ix);
                action.m_addDependencyAction.m_target = action.m_addDependencyAction.m_target != null ? action.m_addDependencyAction.m_target : m_root.findLeafNode(action.m_addDependencyAction.m_ix);

                boolean hasDependency = action.m_addDependencyAction.m_source.hasDependencyTo(action.m_addDependencyAction.m_target);
                /*for (HNode d : action.m_addDependencyAction.m_source.m_dependencies) {
                    if (d == action.m_addDependencyAction.m_target) {
                        hasDependency = true;
                        break;
                    }
                }*/

                a_imgui.beginTooltip();
                if (!hasDependency) {
                    a_imgui.text("Click to add dependency from " + action.m_addDependencyAction.m_source.getFullName() + " to " + action.m_addDependencyAction.m_target.getFullName());
                } else {
                    a_imgui.text("Click to remove dependency from " + action.m_addDependencyAction.m_source.getFullName() + " to " + action.m_addDependencyAction.m_target.getFullName());
                }
                a_imgui.endTooltip();

                action.m_addDependencyAction.m_source.renderDependency(a_imgui, action.m_addDependencyAction.m_target, m_leafNodeCounter);

                if (a_imgui.isMouseClicked(0, false)) {
                    Action a = createDependencyAction(action.m_addDependencyAction.m_source, action.m_addDependencyAction.m_target, hasDependency);

                    // as we return we do this to avoid flicker...
                    m_root.renderDependencies(a_imgui, m_leafNodeCounter);
                    return a;
                }
            }
            if (action.m_renameNodeAction != null) {
                Action a = new Action();
                a.m_hiearchyMove = new Action.HierarchyMove();
                String originalName = action.m_renameNodeAction.m_node.m_name;

                for (HNode n : action.m_renameNodeAction.m_node.getConcreteNodes()) {
                    Action.NodeNamePair p = new Action.NodeNamePair();
                    p.m_oldName = n.getFullName();
                    action.m_renameNodeAction.m_node.m_name = action.m_renameNodeAction.a_newName;
                    p.m_newName = n.getFullName();
                    action.m_renameNodeAction.m_node.m_name = originalName;
                    a.m_hiearchyMove.m_nodes.add(p);
                }

                // as we return we do this to avoid flicker...
                m_root.renderDependencies(a_imgui, m_leafNodeCounter);

                return a;
            }
        }
        m_root.renderDependencies(a_imgui, m_leafNodeCounter);

        if (a_state.m_dNd != null && !a_imgui.isMouseDragging(0, 1.0f)) {
            // convert g_dnd to action
            if (a_state.m_dNd.m_target != null) {

                // are we dropping into a child node?
                if (getIndexOfFirstNonSimilarComponentInStr2(a_state.m_dNd.m_target.getFullName(), a_state.m_dNd.m_staleSourceNode.getFullName()) == a_state.m_dNd.m_staleSourceNode.getFullName().length()) {
                    a_state.m_dNd = null;
                    return null;
                }
                Action a = new Action();
                a.m_hiearchyMove = new Action.HierarchyMove();

                for (HNode leaf : a_state.m_dNd.m_staleSourceNode.getConcreteNodes()) {

                    Action.NodeNamePair pair = new Action.NodeNamePair();
                    pair.m_oldName = leaf.getFullName();
                    String oldName = pair.m_oldName;
                    if (a_state.m_dNd.m_staleSourceNode.m_children.size() == 0) {
                        // dragging leaf node so remove all of the old hierarchy
                        oldName = a_state.m_dNd.m_staleSourceNode.m_name;
                    }
                    // remove any common part of the old name
                    String targetFullName = a_state.m_dNd.m_target.getFullName();
                    String strippedOldName = oldName.substring(getIndexOfFirstNonSimilarComponentInStr2(a_state.m_dNd.m_staleSourceNode.m_parent.getFullName(), oldName));

                    if (targetFullName.length() > 0) {
                        pair.m_newName = strippedOldName.length() > 0 ? (targetFullName + "." + strippedOldName).replace("..", ".") : targetFullName;

                    } else {
                        pair.m_newName = strippedOldName;
                    }

                    a.m_hiearchyMove.addPair(pair);
                    System.out.println(pair.m_oldName + " -> " + pair.m_newName);
                }

                // we may have a move action here too... but as we are acting in a non root node we need to take better care
                a.m_nodeOrder = changeNodeOrder(a_imgui.getMousePos(), a_imgui, a_state.m_dNd);

                a_state.m_dNd = null;
                return a;
            } else {
                // move to root
                Action a = new Action();
                a.m_hiearchyMove = new Action.HierarchyMove();

                for (HNode concrete : a_state.m_dNd.m_staleSourceNode.getConcreteNodes()) {
                    Action.NodeNamePair pair = new Action.NodeNamePair();
                    pair.m_oldName = concrete.getFullName();

                    String parentFullName = a_state.m_dNd.m_staleSourceNode.m_parent.m_name == null ? "" : a_state.m_dNd.m_staleSourceNode.m_parent.getFullName() + ".";   // +. as we know that there are children...
                    String strippedOldName = pair.m_oldName.substring(getIndexOfFirstNonSimilarComponentInStr2(parentFullName, pair.m_oldName));

                    pair.m_newName = strippedOldName;

                    System.out.println(pair.m_oldName);
                    System.out.println(parentFullName);
                    a.m_hiearchyMove.addPair(pair);
                }

                a.m_nodeOrder = changeNodeOrder(a_imgui.getMousePos(), a_imgui, a_state.m_dNd);

                a_state.m_dNd = null;
                return a;
            }
        } else {
            if (a_state.m_dNd != null) {
               // changeNodeOrder(a_imgui.getMousePos(), a_imgui);
            }
            a_state.m_dNd = m_root.doDragNDrop(a_imgui, a_state.m_dNd);
            if (a_state.m_dNd != null) {

                Rect drawRect = new Rect(a_state.m_dNd.m_dragRect.getTl().plus(a_imgui.getMouseDragDelta(0, 1.0f)), a_state.m_dNd.m_dragRect.getBr().plus(a_imgui.getMouseDragDelta(0, 1.0f)));
                //a_imgui.addRect(drawRect.getTl(), drawRect.getBr(), COL32(175, 175, 175, 255), g_rounding, DrawCornerFlag.All.getI(), 2);
                a_state.m_dNd.m_staleSourceNode.render(drawRect, a_imgui, a_state.m_dNd.m_staleSourceNode.getLeafNodeCount(), 100, a_state.m_nvm, new HNode.NodeNameEdit());
            }
        }

        //if (a_imgui.isMouseDown(1)) {
            if (a_imgui.beginPopupContextWindow("popup", 1, true)) {
                a_state.m_dNd = null;
                Action a = null;
                if (a_state.m_underPopUp == null) {
                    a_state.m_underPopUp = m_root.getNodeUnder(a_imgui.getMousePos());
                    if (a_state.m_underPopUp == null) {
                        a_state.m_underPopUp = m_root;
                    }
                    //if (a_state.m_underPopUp.isParentNodeRepresentation()) {
                    //    a_state.m_underPopUp = a_state.m_underPopUp.m_parent;
                    //}
                }

                if (a_state.m_underPopUp.m_name != null && a_imgui.m_imGui.beginMenu("Manage Dependencies", true)) {
                    HNode to = m_root.doNameMenu(a_imgui, a_state.m_underPopUp);
                    if (to != null)  {
                        //a = new Action();
                        boolean addDependency = a_state.m_underPopUp.hasDependencyTo(to);
                        /*for (HNode n : a_state.m_underPopUp.m_dependencies) {
                            if (to.getFullName().contentEquals(n.getFullName())) {
                                addDependency = true;
                            }
                        }*/

                        a = createDependencyAction(a_state.m_underPopUp, to, addDependency);

                    }

                    a_imgui.m_imGui.endMenu();
                }

                if (a_imgui.m_imGui.menuItem("Add Component", "CTRL+A", false, true)) {
                    a = new Action();

                    a.m_addComponent = a_state.m_underPopUp.getFullName();
                    if (a.m_addComponent.length() > 0) {
                        a.m_addComponent += ".";
                    }
                    a.m_addComponent += "component_" + m_root.countNodes();
                    //a_imgui.closeCurrentPopup();
                }
                if (a_state.m_underPopUp.m_name != null && a_imgui.m_imGui.menuItem("Add Parent", "", false, true)) {
                    a = new Action();

                    a.m_hiearchyMove = new Action.HierarchyMove();
                    String parentName = a_state.m_underPopUp.m_parent.getFullName();
                    if (parentName.length() > 0) {
                        parentName += ".";
                    }
                    a.m_hiearchyMove.m_nodes.add(new Action.NodeNamePair(a_state.m_underPopUp.getFullName(), parentName + "virtual_" + m_root.countNodes() + "." + a_state.m_underPopUp.m_name));
                }
                if (a_state.m_underPopUp.m_name != null && a_imgui.m_imGui.menuItem("Delete " + a_state.m_underPopUp.m_name, "del", false, true)) {
                    a = new Action();
                    a.m_deletedComponents = new ArrayList<>();

                    for (HNode n : a_state.m_underPopUp.getConcreteNodes()) {
                        a.m_deletedComponents.add(n.getFullName());
                    }

                }

                HNode.Visuals v = a_state.m_nvm.getNodeState(a_state.m_underPopUp);
                if (v != null) {
                    a_imgui.m_imGui.separator();
                    if (a_imgui.m_imGui.beginMenu("Set Color", true)) {

                        Vec4 col = v.m_color;
                        a_imgui.m_imGui.colorEdit3("color", col, ColorEditFlag.PickerHueWheel.getI());
                        a_imgui.m_imGui.endMenu();
                    }
                    if (a != null) {
                        return a;
                    }
                }
            } else {
                a_state.m_underPopUp = null;
            }
        //}

        return null;
    }

    @NotNull
    private Action createDependencyAction(HNode a_source, HNode a_target, boolean a_doAdd) {
        Action a = new Action();
        ArrayList<Action.NodeNamePair> pairs;
        if (!a_doAdd) {
            a.m_addDependenices = new Action.HierarchyMove();
            pairs = a.m_addDependenices.m_nodes;
        } else {
            a.m_removeDependencies = new Action.HierarchyMove();
            pairs = a.m_removeDependencies.m_nodes;
        }
        for (HNode sN : a_source.getConcreteNodes()) {
            for (HNode tN : a_target.getConcreteNodes()) {
                Action.NodeNamePair p = new Action.NodeNamePair();
                p.m_oldName = sN.getFullName();
                p.m_newName = tN.getFullName();
                pairs.add(p);
            }
        }
        return a;
    }

    HNode m_root = new HNode();
    int m_leafNodeCounter = 0;

}
