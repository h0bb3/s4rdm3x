import archviz.HNode;
import gui.ImGuiWrapper;
import hiviz.Tree;
import imgui.ImGui;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.hugme.HuGMe;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.ArrayList;

public class TreeView {
    private int[] treeViewSelection = {0};
    private String[] treeViewRoots = {"", "", ""};



    void doTreeView(ImGui a_imgui, HuGMe.ArchDef a_arch, CGraph a_g, HNode.VisualsManager a_nvm) {

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
            case 0: {
                for(HuGMe.ArchDef.Component c : a_arch.getComponents()) {
                    if (c.getName().startsWith(treeViewRoots[0]))
                        tree.addNode(c.getName());
                }
            } break;
            case 1: {
                for (CNode n : a_g.getNodes()) {

                    HuGMe.ArchDef.Component component = a_arch.getMappedComponent(n);

                    for (dmClass c : n.getClasses()) {
                        if (!c.isInner()) {
                            if (c.getName().startsWith(treeViewRoots[1])) {

                                Tree.TNode tn = tree.addNode(c.getName());
                                if (component != null) {
                                    tn.setName(tn.getName());
                                    tn.setMapping(component.getName(), a_nvm.getBGColor(component.getName()));
                                }
                            }
                        } else {
                            // tree.addNode(c.getName().replace("$", ".") + " (inner class)");
                        }
                    }
                }
            } break;
            case 2: {
            } break;
        }


        //NodeUtil nu = new NodeUtil(a_g);

        tree.doTree(a_imgui);
    }

    public String getArchRootFilter() {
        return treeViewRoots[0];
    }
}
