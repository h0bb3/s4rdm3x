import archviz.HNode;
import gui.ImGuiWrapper;
import hiviz.Tree;
import imgui.ImGui;
import org.graphstream.graph.Graph;
import se.lnu.siq.s4rdm3x.model.cmd.hugme.HuGMe;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.ArrayList;

public class TreeView {
    private int[] m_treeViewSelection = {0};
    private String[] treeViewRoots = {"", "", ""};
    Tree.TNode m_selectedNode;
    CNode m_selectedClass;

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

        if (a_imgui.combo("", m_treeViewSelection, items, items.size())) {
        }

        a_imgui.beginColumns("TreeViewColumns", 2, 0);

        ImGuiWrapper iw = new ImGuiWrapper(a_imgui);
        treeViewRoots[m_treeViewSelection[0]] = iw.inputTextSingleLine("Root", treeViewRoots[m_treeViewSelection[0]]);

        switch (m_treeViewSelection[0]) {
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

        if (selected != null && selected.getObject() != null && iw.isMouseClicked(0, false) && m_treeViewSelection[0] == g_classesId) {
            m_selectedClass = (CNode) selected.getObject();
        }

        if (selected != null) {
            iw.text(selected.getName());
        }

        if (m_selectedNode != null) {
            switch (m_treeViewSelection[0]) {
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


        a_imgui.nextColumn();
            if (m_treeViewSelection[0] == g_classesId && m_selectedClass != null) {
                doClassView(iw, a_g, m_selectedClass);
            }

        a_imgui.endColumns();

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

    private void doClassView(ImGuiWrapper a_imgui, CGraph a_g, CNode a_selectedNode) {
        a_imgui.text(a_selectedNode.getLogicName());

        a_imgui.text("Fan in: ");
        for (CNode n : a_g.getNodes()) {
            if (n.hasDependency(a_selectedNode)) {
                a_imgui.text(n.getLogicName());
            }
        }

        a_imgui.text("Fan out: ");
        for (CNode n : a_g.getNodes()) {
            if (a_selectedNode.hasDependency(n)) {
                a_imgui.text(n.getLogicName());
            }
        }
    }

    public String getArchRootFilter() {
        return treeViewRoots[0];
    }
}
