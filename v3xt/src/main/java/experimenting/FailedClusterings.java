package experimenting;

import glm_.vec2.Vec2;
import gui.ImGuiWrapper;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.util.CSVFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class FailedClusterings {
    private String m_saveDir = Paths.get("").toAbsolutePath().toString();

    public static class System {
        public class Result {
            public class Clustering {
                String m_name;
                int m_frequency;
            }

            String m_logicNodeName;
            String m_mapping;
            String m_experiment;

            ArrayList<Clustering> m_clustrings = new ArrayList<>();

            void addClustering(String a_clustering) {
                Clustering clust = null;
                for (Clustering c : m_clustrings) {
                    if (c.m_name.equals(a_clustering)) {
                        clust = c;
                        break;
                    }
                }

                if (clust == null) {
                    clust = new Clustering();
                    clust.m_name = a_clustering;
                    clust.m_frequency = 0;
                    m_clustrings.add(clust);
                }

                clust.m_frequency++;
            }

            double getErrorRatio() {
                return (double)getErrorFrequency() / (double)getTotal();
            }

            int getErrorFrequency() {
                final int[] sum = {0};
                m_clustrings.forEach(c -> {if (!c.m_name.equals(m_mapping)) sum[0] += c.m_frequency;});
                return sum[0];
            }

            public int getTotal() {
                final int[] sum = {0};
                m_clustrings.forEach(c -> sum[0] += c.m_frequency);
                return sum[0];
            }

            public int getFrequency(String a_clusteringName) {
                for (Clustering c : m_clustrings) {
                    if (c.m_name.equals(a_clusteringName)) {
                        return c.m_frequency;
                    }
                }

                return 0;
            }
        }

        private Result findResult(String a_logicName, String a_experiment) {

            for(Result r : m_results) {
                if (r.m_logicNodeName.equals(a_logicName) && r.m_experiment.equals(a_experiment)) {
                    return r;
                }
            }

            return null;
        }

        private String findComponent(String a_component) {
            for (String c : m_components) {
                if (c.equals(a_component)) {
                    return c;
                }
            }

            return null;
        }

        void addResult(CNode a_node, String a_experiment) {
            Result r = findResult(a_node.getLogicName(), a_experiment);

            if (r == null) {
                r = new Result();

                r.m_logicNodeName = a_node.getLogicName();
                r.m_mapping = a_node.getMapping();
                r.m_experiment = a_experiment;

                m_results.add(r);
            }
            r.addClustering(a_node.getClusteringComponentName());

            if (findComponent(a_node.getClusteringComponentName()) == null) {
                m_components.add(a_node.getClusteringComponentName());
            }

            m_results.sort( (a, b) -> { return Double.compare(b.getErrorRatio(), a.getErrorRatio());});

        }

        String m_name;
        ArrayList<String> m_components = new ArrayList<>();
        ArrayList<Result> m_results = new ArrayList<>();
    }

    ArrayList<System> m_systems = new ArrayList<>();
    String m_selectedNodeLogicName;


    private ArrayList<String> getHeaderStrings(System a_s) {
        ArrayList<String> header = new ArrayList<>();
        header.add("Experiment");
        header.add("Node");
        header.add("Mapping");
        header.add("Error Ratio");
        for (String c : a_s.m_components) {
            header.add(c);
        }

        return header;
    }

    private boolean createFile(Path a_fp) {
        try {
            Files.createFile(a_fp);
            return true;
        } catch (Exception e) {
            java.lang.System.out.println("Could not create File: " + a_fp);
            return false;
        }
    }

    void doShow(ImGuiWrapper a_imgui) {

        m_saveDir = a_imgui.inputTextSingleLine("###SaveAsFailDataDir", m_saveDir);

        a_imgui.sameLine(0);
        if (m_systems.size() == 0) {
            a_imgui.pushDisableWidgets();
        }
        if (a_imgui.button("Save Data", 0)) {
            for (System s :m_systems) {
                String f = m_saveDir + File.separator + s.m_name + ".csv";

                Path fp = Paths.get(f);
                if (createFile(fp)) {
                    CSVFile csv = new CSVFile(fp);
                    ArrayList<Iterable<String>> rows = new ArrayList<>();

                    for (int i = 0; i < s.m_results.size(); i++) {
                        System.Result r = s.m_results.get(i);
                        ArrayList<String> row = new ArrayList<>();

                        row.add(r.m_experiment);
                        row.add(r.m_logicNodeName);
                        row.add(r.m_mapping);
                        row.add(String.format("%.4f", r.getErrorRatio()));
                        for (int cIx = 0; cIx < s.m_components.size(); cIx++) {
                            row.add("" + r.getFrequency(s.m_components.get(cIx)));
                        }

                        //csv.writeRow(row);
                        rows.add(row);
                    }
                    try {
                        csv.writeHeader(getHeaderStrings(s));
                        csv.writeRows(rows);
                    } catch (Exception e) {
                        java.lang.System.out.println("Could not write row to File: " + f);
                        e.printStackTrace();
                    }
                }
            }
        }
        a_imgui.sameLine(0);
        if (a_imgui.button("Clear Data###ClearFailData", 0)) {
            m_systems.clear();
        }
        if (m_systems.size() == 0) {
            a_imgui.popDisableWidgets();
        }

        for (System s : m_systems) {
            if (a_imgui.imgui().collapsingHeader(s.m_name, 0)) {



                ArrayList<String> header = getHeaderStrings(s);

                float[] colWidths = new float[header.size()];
                a_imgui.imgui().beginColumns("failedmappingsheader", header.size(), 0);
                for (int cIx = 0; cIx < header.size(); cIx++) {
                    colWidths[cIx] = a_imgui.imgui().getColumnWidth(cIx);
                    //a_imgui.imgui().selectable(a_imgui.getLongestSubString(header.get(cIx), colWidths[cIx] - 13, "\\.") + "##" + header.get(cIx), false, 0, new Vec2(0, 0));
                    a_imgui.text(a_imgui.getLongestSubString(header.get(cIx), colWidths[cIx] - 13, "\\."));
                    if (a_imgui.imgui().isItemHovered(0)) {
                        a_imgui.beginTooltip();
                        a_imgui.text(header.get(cIx));
                        a_imgui.endTooltip();
                    }
                    a_imgui.imgui().nextColumn();
                }

                a_imgui.imgui().endColumns();


                float rowHeight = a_imgui.getTextLineHeightWithSpacing();
                final int white = ImGuiWrapper.toColor(255, 255, 255, 255);
                final int red = ImGuiWrapper.toColor(255, 52, 52, 255);
                final int green = ImGuiWrapper.toColor(155, 255, 155, 255);
                class ColStrings {
                    String m_colString = null;
                    String m_toolTip = null;
                    int m_color = white;
                }
                ColStrings[] colStrings = new ColStrings[header.size()];
                Arrays.setAll(colStrings, i -> new ColStrings());

                float xPos = a_imgui.imgui().getWindowPos().getX() + a_imgui.imgui().getScrollX();
                float yPos = a_imgui.imgui().getWindowPos().getY() - a_imgui.imgui().getScrollY();
                float cursorPosY = a_imgui.imgui().getCursorPosY();
                int componentCount = s.m_components.size();

                for (int i = 0; i < s.m_results.size(); i++) {
                    System.Result r = s.m_results.get(i);

                    a_imgui.imgui().setCursorPosY(cursorPosY);
                    cursorPosY += rowHeight;
                    colStrings[0].m_colString = r.m_experiment;
                    colStrings[1].m_colString = a_imgui.getLongestSubString(r.m_logicNodeName, colWidths[1] - 13, "\\.");
                    colStrings[1].m_toolTip = r.m_logicNodeName;
                    colStrings[2].m_colString = a_imgui.getLongestSubString(r.m_mapping, colWidths[2] - 13, "\\.");
                    colStrings[2].m_toolTip = r.m_mapping;
                    colStrings[3].m_colString = String.format("%.2f", r.getErrorRatio() * 100);
                    for (int cIx = 0; cIx < componentCount; cIx++) {
                        colStrings[4 + cIx].m_colString = "" + r.getFrequency(s.m_components.get(cIx));
                        colStrings[4 + cIx].m_color = r.m_mapping.equals(s.m_components.get(cIx)) ? green : red;
                    }

                    float cursorXOffset = 13;
                    float cursorPosX = cursorXOffset;
                    Vec2 clipMin = new Vec2();
                    Vec2 clipMax = new Vec2();
                    for (int cIx = 0; cIx < colStrings.length; cIx++) {
                        a_imgui.imgui().setCursorPosX(cursorPosX);
                        clipMin.setX(xPos + cursorPosX);
                        clipMin.setY(yPos + cursorPosY - rowHeight);
                        clipMax.setX(xPos + cursorPosX + colWidths[cIx] - cursorXOffset);
                        clipMax.setY(yPos + cursorPosY);
                        //a_imgui.addRect(clipMin, clipMax, ImGuiWrapper.toColor(255, 255, 255, 255), 0, 0, 1);
                        a_imgui.imgui().pushClipRect(clipMin, clipMax, true);
                        if (colStrings[cIx].m_toolTip != null) {
                            if (a_imgui.isInsideClipRect(a_imgui.getMousePos())) {
                                a_imgui.beginTooltip();
                                a_imgui.text(colStrings[cIx].m_toolTip);
                                a_imgui.endTooltip();
                            }
                        }
                        if (cIx == 1 && a_imgui.isInsideClipRect(a_imgui.getMousePos()) && a_imgui.isMouseClicked(0, false)) {
                            m_selectedNodeLogicName = r.m_logicNodeName;
                        }
                        a_imgui.addText(clipMin, colStrings[cIx].m_color, colStrings[cIx].m_colString);
                        a_imgui.imgui().popClipRect();
                        cursorPosX += colWidths[cIx];
                    }
                }
                a_imgui.imgui().setCursorPosX(0);
                a_imgui.imgui().setCursorPosY(a_imgui.imgui().getCursorPosY() + a_imgui.imgui().getFrameHeightWithSpacing());
            }
        }
    }

    public void add(CNode a_node, String a_system, String a_experiment) {

        System sys = null;
        for (System s : m_systems) {
            if (a_system.equals(s.m_name)) {
                sys = s;
            }
        }

        if (sys == null) {
            sys = new System();
            sys.m_name = a_system;
            m_systems.add(sys);
        }

        sys.addResult(a_node, a_experiment);
    }




}
