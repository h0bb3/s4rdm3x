package experimenting;

import glm_.vec2.Vec2;
import gui.ImGuiWrapper;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;

import java.util.ArrayList;

public class FailedClusterings {
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

            int getFrequency() {
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

            m_results.sort( (a, b) -> b.getFrequency() - a.getFrequency());
        }

        String m_name;
        ArrayList<String> m_components = new ArrayList<>();
        ArrayList<Result> m_results = new ArrayList<>();
    }

    ArrayList<System> m_systems = new ArrayList<>();

    void doShow(ImGuiWrapper a_imgui) {
        for (System s : m_systems) {
            ArrayList<String> header = new ArrayList<>();
            header.add("Experiment");
            header.add("System");
            header.add("Node");
            header.add("Mapping");
            header.add("Freq");
            int componentCount = s.m_components.size();
            for (String c : s.m_components) {
                header.add(c);
            }

            float[] colWidths = new float[header.size()];
            a_imgui.imgui().beginColumns("failedmappingsheader", header.size(), 0);
            for (int cIx = 0; cIx < header.size(); cIx++) {
                colWidths[cIx] = a_imgui.imgui().getColumnWidth(cIx);
                a_imgui.text(a_imgui.getLongestSubString(header.get(cIx), colWidths[cIx] - 13, "\\."));
                a_imgui.imgui().nextColumn();
            }

            a_imgui.imgui().endColumns();


            float rowHeight = a_imgui.getTextLineHeightWithSpacing();
            String[] colStrings = new String[header.size()];
            float xPos = a_imgui.imgui().getWindowPos().getX() + a_imgui.imgui().getScrollX();
            float yPos = a_imgui.imgui().getWindowPos().getY() - a_imgui.imgui().getScrollY();
            float cursorPosY = a_imgui.imgui().getCursorPosY();
            for (int i = 0; i < s.m_results.size(); i++) {
                System.Result r = s.m_results.get(i);

                a_imgui.imgui().setCursorPosY(cursorPosY);
                cursorPosY += rowHeight;
                colStrings[0] = r.m_experiment;
                colStrings[1] = s.m_name;
                colStrings[2] = a_imgui.getLongestSubString(r.m_logicNodeName, colWidths[2] - 13, "\\.");
                colStrings[3] = a_imgui.getLongestSubString(r.m_mapping, colWidths[3] - 13, "\\.");
                colStrings[4] = "" + r.getFrequency();
                for (int cIx = 0; cIx < componentCount; cIx++) {
                    colStrings[5 + cIx] = "" + r.getFrequency(s.m_components.get(cIx));
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
                    a_imgui.addText(clipMin, ImGuiWrapper.toColor(255, 255, 255, 255), colStrings[cIx]);
                    a_imgui.imgui().popClipRect();
                    cursorPosX += colWidths[cIx];
                }
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
