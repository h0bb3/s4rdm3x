package mapping;

import glm_.vec2.Vec2;
import gui.ImGuiWrapper;
import imgui.ComboFlag;
import imgui.SelectableFlag;
import imgui.TreeNodeFlag;
import imgui.WindowFlag;
import imgui.internal.ColumnsFlag;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.stats;

import java.util.ArrayList;
import java.util.Arrays;

public class ResultView {
    private CNode m_selectedNode;

    static class Mapping {
        static class Node {
            String m_name;
            String m_mapping;
            String m_clustering;
            double[] m_attractions;
        }

        String m_name;

        ArrayList<Node> m_okNodes = new ArrayList();
        ArrayList<Node> m_nokNodes = new ArrayList();
        ArrayList<Node> m_unclusteredNodes = new ArrayList();
    }

    private Mapping.Node createNode(CNode a_n) {
        Mapping.Node mn = new Mapping.Node();

        mn.m_name = a_n.getLogicName();
        if (a_n.getAttractions() != null) {
            mn.m_attractions = Arrays.copyOf(a_n.getAttractions(), a_n.getAttractions().length);
        } else {
            mn.m_attractions = null;
        }
        mn.m_mapping = a_n.getMapping();
        mn.m_clustering = a_n.getClusteringComponentName();

        return mn;
    }

    public CNode getByLogicName(Iterable<CNode> a_in, String a_name) {
        for (CNode n : a_in) {
            if (n.getLogicName().equals(a_name)) {
                return n;
            }
        }
        return null;
    }

    void addResult(String a_name, CGraph a_graph) {
        Mapping m =  new Mapping();
        m.m_name = a_name;
        for (CNode n : a_graph.getNodes()) {
            if (n.getClusteringComponentName() != null && n.getClusteringComponentName().length() > 0) {
                if (!n.getClusteringType().equals("Initial")) {
                    Mapping.Node mn = createNode(n);

                    if (mn.m_mapping.equals(mn.m_clustering)) {
                        m.m_okNodes.add(mn);
                    } else {
                        m.m_nokNodes.add(mn);
                    }
                }
            } else {
                Mapping.Node mn = createNode(n);
                mn.m_clustering = "";
                m.m_unclusteredNodes.add(mn);
            }
        }


        m_mappings.add(m);
    }

    ArrayList<Mapping> m_mappings = new ArrayList<>();

    void doShow(ImGuiWrapper a_imgui, CGraph a_g, ArchDef a_arch, float a_width) {

        Mapping toBeDeleted = null;

        for (Mapping m : m_mappings) {
            if (a_imgui.imgui().collapsingHeader(m.m_name + " " + m.m_okNodes.size() + " " + m.m_nokNodes.size() + " " + m.m_unclusteredNodes.size() + "##" + m.hashCode(),0)) {
                //a_imgui.imgui().beginChild("" + m.hashCode(), new Vec2(0, 0), true, WindowFlag.NoScrollbar.getI());
                a_imgui.imgui().beginGroup();
                if (a_imgui.button("Delete Result##" + m.hashCode(), 0)) {
                    toBeDeleted = m;
                }
                a_imgui.imgui().indent(17);
                if (a_imgui.imgui().collapsingHeader("Ok##" + m.hashCode(), 0)) {
                    doClusteringTable(m.m_okNodes, m.m_okNodes.size(), a_imgui, a_g, a_arch, "okresult" + m.hashCode(), a_width);
                }
                if (a_imgui.imgui().collapsingHeader("Failed##" + m.hashCode(), 0)) {
                    doClusteringTable(m.m_nokNodes, m.m_nokNodes.size(), a_imgui, a_g, a_arch, "nokresult" + m.hashCode(), a_width);
                }
                if (a_imgui.imgui().collapsingHeader("Unclustered##" + m.hashCode(), 0)) {
                    doClusteringTable(m.m_unclusteredNodes, m.m_unclusteredNodes.size(), a_imgui, a_g, a_arch, "unclusteredresult" + m.hashCode(), a_width);
                }
                a_imgui.imgui().endGroup();
            }
        }

        if (toBeDeleted != null) {
            m_mappings.remove(toBeDeleted);
        }

    }

    private void doClusteringTable(Iterable<Mapping.Node> a_nodes, int a_nodeCount, ImGuiWrapper a_imgui, CGraph a_g, ArchDef a_arch, String a_tableId, float a_width) {
        // table of attraction values
        final int columnCount = a_arch.getComponentCount() + 5;
        Vec2 columnSize = new Vec2( a_width - 20, (a_nodeCount) * a_imgui.imgui().getFrameHeightWithSpacing());

        //final float maxHeight = columnSize.getY() + 2 * a_imgui.imgui().getFrameHeightWithSpacing() < a_imgui.imgui().getContentRegionMax().getY() - a_imgui.imgui().getCursorPosY() ? columnSize.getY() + 2 * a_imgui.imgui().getFrameHeightWithSpacing() : a_imgui.imgui().getContentRegionMax().getY() - a_imgui.imgui().getCursorPosY();
        final float maxHeight = columnSize.getY() + 2 * a_imgui.imgui().getFrameHeightWithSpacing();

        // we need this to be able to handle columns in columns problems.
        a_imgui.imgui().beginChild(a_tableId + "OuterChildWindow", new Vec2(a_width - 10, maxHeight), false, 0);

        // header
        a_imgui.imgui().beginColumns(a_tableId+ "TableColumns", columnCount, ColumnsFlag.NoPreserveWidths.getI());
        a_imgui.text("Entity");
        a_imgui.imgui().nextColumn();

        a_imgui.text("Mapping");
        a_imgui.imgui().nextColumn();

        a_imgui.text("Clustering");
        a_imgui.imgui().nextColumn();

        int cIx  = 0;
        for (; cIx < a_arch.getComponentCount(); cIx++) {

            String text = a_imgui.getLongestSubString(a_arch.getComponent(cIx).getName(), a_imgui.imgui().getColumnWidth(a_imgui.imgui().getColumnIndex()), "\\.");

            a_imgui.text(text);
            a_imgui.imgui().nextColumn();
        }

        a_imgui.text("Average");
        a_imgui.imgui().nextColumn();
        a_imgui.text("StdDev");


        float [] columnWidths = new float[columnCount];
        for (cIx = 0; cIx < columnCount; cIx++) {
            columnWidths[cIx] = a_imgui.imgui().getColumnWidth(cIx);
        }


        a_imgui.imgui().endColumns();

        a_imgui.imgui().separator();

        a_imgui.imgui().beginChild(a_tableId+"ScrollTable", new Vec2(a_width - 10, 0), false, 0);
        //a_imgui.imgui().beginChild(a_tableId+"RowTable", new Vec2(a_width - 55, (float)columnSize.getY()), true, 0);

        // rows
        for (Mapping.Node n : a_nodes) {
            a_imgui.imgui().beginColumns(a_tableId + "TableColumns", columnCount, ColumnsFlag.NoResize.getI());

            for (cIx = 0; cIx < columnCount; cIx++) {
                a_imgui.imgui().setColumnWidth(cIx, columnWidths[cIx]);
            }

            double [] attractions = n.m_attractions;
            if (attractions == null) {
                attractions = new double[a_arch.getComponentCount()];
                Arrays.fill(attractions, -1);
            }

            String logicName = a_imgui.getLongestSubString(n.m_name, columnWidths[a_imgui.imgui().getColumnIndex()], "\\.");
            //a_imgui.text(logicName);
            if (a_imgui.imgui().selectable(logicName, false, 0, new Vec2(a_imgui.imgui().getColumnWidth(a_imgui.imgui().getColumnIndex()), a_imgui.imgui().getFrameHeightWithSpacing()))) {
                m_selectedNode = a_g.getNode(n.m_name);
            }
            a_imgui.imgui().nextColumn();


            /*ArchDef.Component mappedComponent = a_arch.getMappedComponent(n);
            if (mappedComponent != null) {
                a_imgui.text(a_imgui.getLongestSubString(mappedComponent.getName(), columnWidths[a_imgui.imgui().getColumnIndex()] - (a_imgui.imgui().getCursorPos().getX() - a_imgui.imgui().getColumnOffset(a_imgui.imgui().getColumnIndex())), "\\."));
            } else {
                a_imgui.text("");
            }
            a_imgui.imgui().nextColumn();*/
            a_imgui.text(a_imgui.getLongestSubString(n.m_mapping, columnWidths[a_imgui.imgui().getColumnIndex()] - (a_imgui.imgui().getCursorPos().getX() - a_imgui.imgui().getColumnOffset(a_imgui.imgui().getColumnIndex())), "\\."));
            a_imgui.imgui().nextColumn();


            a_imgui.text(a_imgui.getLongestSubString(n.m_clustering, columnWidths[a_imgui.imgui().getColumnIndex()] - (a_imgui.imgui().getCursorPos().getX() - a_imgui.imgui().getColumnOffset(a_imgui.imgui().getColumnIndex())), "\\."));

            /*{   // comboboxes for the clustering
                int [] selectedComponent = {0};

                ArchDef.Component clusteredTo = a_arch.getClusteredComponent(n);


                Vec2 zeroSize = new Vec2(0, 0);
                a_imgui.imgui().pushItemWidth(-1);
                if (a_imgui.imgui().beginCombo("##"+n.m_name, a_imgui.getLongestSubString(clusteredTo.getName(), columnWidths[a_imgui.imgui().getColumnIndex()] - a_imgui.imgui().getFrameHeightWithSpacing() - (a_imgui.imgui().getCursorPos().getX() - a_imgui.imgui().getColumnOffset(a_imgui.imgui().getColumnIndex())), "\\."), ComboFlag.None.getI())) {

                    for (int i = 0; i < a_arch.getComponentCount(); i++) {
                        ArchDef.Component c = a_arch.getComponent(i);
                        if (a_imgui.imgui().selectable(c.getName(), c == clusteredTo, SelectableFlag.None.getI(), zeroSize)) {
                            selectedComponent[0] = i;
                            c.clusterToNode(n, ArchDef.Component.ClusteringType.Manual);
                        }
                    }

                    a_imgui.imgui().endCombo();
                }
                a_imgui.imgui().popItemWidth();

            }*/
            a_imgui.imgui().nextColumn();

            double mean = stats.mean(attractions);
            double stddev = stats.stdDev(attractions, mean);

            cIx = 0;
            for (; cIx < a_arch.getComponentCount(); cIx++) {
                String text = "-";
                if (n.m_attractions != null) {
                    text = "" + Math.round(attractions[cIx] * 100D) / 100D;
                    if (attractions[cIx] >= mean) {
                        text += " (>=m)";
                    }
                    if (attractions[cIx] - mean >= stddev) {
                        text += " (>=sd)";
                    }
                }
                 a_imgui.text(text);
                 a_imgui.imgui().nextColumn();

            }


            if (n.m_attractions != null) {
                a_imgui.text("" + mean);
                a_imgui.imgui().nextColumn();

                a_imgui.text("" + stddev);
                a_imgui.imgui().nextColumn();
            } else {
                a_imgui.text("-");
                a_imgui.imgui().nextColumn();

                a_imgui.text("-");
                a_imgui.imgui().nextColumn();
            }

            a_imgui.imgui().endColumns();
        }
        //a_imgui.imgui().endChild();
        a_imgui.imgui().endChild();
        a_imgui.imgui().endChild();
    }
}
