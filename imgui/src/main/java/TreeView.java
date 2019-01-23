import archviz.HNode;
import gui.ImGuiWrapper;
import hiviz.Tree;
import imgui.ImGui;
import se.lnu.siq.s4rdm3x.model.cmd.hugme.HuGMe;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.ArrayList;

public class TreeView {
    private int[] treeViewSelection = {0};
    private String[] treeViewRoots = {"", "", ""};
    Tree.TNode m_selectedNode;

    private final int g_archId = 0;
    private final int g_classesId = 1;
    private final int g_filesId = 2;

    public static class Action {
        public static class DoMap {
            public String a_whatNodeName;
            public String a_toComponentName;
        }

        DoMap m_doMapAction;
    }



    Action doTreeView(ImGui a_imgui, HuGMe.ArchDef a_arch, CGraph a_g, HNode.VisualsManager a_nvm) {
        Action ret = null;
        hiviz.Tree tree = new hiviz.Tree();
        ArrayList<String> items = new ArrayList<>();
        items.add("Architecture");
        items.add("Classes");
        items.add("Files");

        if (a_imgui.combo("", treeViewSelection, items, items.size())) {
        }

        ImGuiWrapper iw = new ImGuiWrapper(a_imgui);
        treeViewRoots[treeViewSelection[0]] = iw.inputTextSingleLine("Root", treeViewRoots[treeViewSelection[0]]);

        switch (treeViewSelection[0]) {
            case g_archId: {
                tree = buildArchitectureTree(a_arch.getComponents(), getArchRootFilter());
            } break;
            case g_classesId: {
                for (CNode n : a_g.getNodes()) {

                    HuGMe.ArchDef.Component component = a_arch.getMappedComponent(n);

                    for (dmClass c : n.getClasses()) {
                        if (!c.isInner()) {
                            if (c.getName().startsWith(treeViewRoots[1])) {

                                Tree.TNode tn = tree.addNode(n.getName().replace("/", ".").replace(".java", ""), n);
                                if (component != null) {
                                    tn.setName(tn.getName());
                                    tn.setMapping(component.getName(), a_nvm.getBGColor(component.getName()), component);
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

        if (m_selectedNode == null && selected != null) {
            m_selectedNode = selected;
        }

        if (m_selectedNode != null) {
            switch (treeViewSelection[0]) {
                case g_archId:
                    if (a_imgui.beginPopupContextWindow("TreeViewContextMenuArch", 1, true)) {

                        a_imgui.menuItem("Some Arch Menu", "", false, true);

                        a_imgui.endPopup();
                    } else {
                        m_selectedNode = null;
                    }
                    break;

                case g_classesId:
                if (a_imgui.beginPopupContextWindow("TreeViewContextMenuClasses", 1, true)) {

                    Tree at = buildArchitectureTree(a_arch.getComponents(), "");
                    Object mappedObject = m_selectedNode.getMappedObject();
                    Tree.TNode selectedNode = at.doMenu(a_imgui, mappedObject);

                    if (selectedNode != null) {
                        if (selectedNode != mappedObject) {
                            ret = new Action();
                            ret.m_doMapAction = new Action.DoMap();
                            ret.m_doMapAction.a_whatNodeName = m_selectedNode.getFullName().replace(".", "/");
                            ret.m_doMapAction.a_toComponentName = selectedNode.getFullName();

                            System.out.println("what: " + ret.m_doMapAction.a_whatNodeName);
                            System.out.println("to: " + ret.m_doMapAction.a_toComponentName);
                        }
                    }

                    a_imgui.endPopup();
                } else {
                    m_selectedNode = null;
                }
                break;
            }
        }

        return ret;
    }

    private Tree buildArchitectureTree(Iterable<HuGMe.ArchDef.Component> a_components, String a_filter) {
        Tree tree = new Tree();
        for(HuGMe.ArchDef.Component c : a_components) {
            if (a_filter.length() == 0 || c.getName().startsWith(a_filter)) {
                tree.addNode(c.getName(), c);
            }
        }

        return tree;
    }

    public String getArchRootFilter() {
        return treeViewRoots[0];
    }
}
