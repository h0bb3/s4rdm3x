package experimenting;

import archviz.HNode;
import glm_.vec2.Vec2;
import glm_.vec4.Vec4;
import gui.ImGuiWrapper;
import imgui.ImGui;
import se.lnu.siq.s4rdm3x.experiments.*;
import se.lnu.siq.s4rdm3x.experiments.ExperimentRunner.RunListener;
import se.lnu.siq.s4rdm3x.experiments.metric.*;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import weka.core.Utils;

import java.io.IOException;
import java.util.ArrayList;

public class ExperimentView {

    ExThread m_exThread = new ExThread();

    public void doView(ImGui a_imgui, ArchDef a_arch, CGraph a_g, HNode.VisualsManager a_nvm) {

        ImGuiWrapper iw = new ImGuiWrapper(a_imgui);

        m_exThread.doExperiment(iw);


    }


    private static class ExThread extends Thread {
        ExperimentRunner m_experiment;
        public ArrayList<ExperimentRunData.BasicRunData> m_experimentData = new ArrayList<>();  // this one is accessed by threads so take care...
        private double m_avgPerformance = 0;
        private int m_avgCount = 0;
        private ScatterPlot m_performanceVsInitialMapped = new ScatterPlot();
        private ScatterPlot m_precisionVsInitialMapped = new ScatterPlot();
        private ScatterPlot m_recallVsInitialMapped = new ScatterPlot();

        public ArrayList<ExperimentRunData.BasicRunData> m_selectedDataPoints = new ArrayList<>();



        ExThread() {

        }

        public void doExperiment(ImGuiWrapper a_imgui) {
            Vec2 size = new Vec2(a_imgui.imgui().getContentRegionAvailWidth(), a_imgui.getTextLineHeightWithSpacing() * 2 + a_imgui.imgui().getContentRegionAvailWidth() / 3);

            a_imgui.imgui().beginChild("ExThread1", size, true, 0);

            if (m_experiment == null || m_experiment.getState() == ExperimentRunner.State.Idle) {
                if (a_imgui.button("Run Experiment", 0)) {
                    try {
                        if (m_experiment != null) {
                            m_experiment.stop();
                            halt();
                        }
                        se.lnu.siq.s4rdm3x.experiments.system.FileBased sys = new se.lnu.siq.s4rdm3x.experiments.system.FileBased("data/systems/lucene/lucene-system_model.txt");
                        m_experiment = new NBMapperExperimentRunner(sys, new Rand());

                        m_experiment.setRunListener(new ExperimentRunner.RunListener() {
                            public ExperimentRunData.BasicRunData OnRunInit(ExperimentRunData.BasicRunData a_rd, CGraph a_g, ArchDef a_arch) {

                                return a_rd;
                            }

                            public void OnRunCompleted(ExperimentRunData.BasicRunData a_rd, CGraph a_g, ArchDef a_arch) {

                                m_performanceVsInitialMapped.addData(a_rd.m_initialClusteringPercent, a_rd.calcAutoPerformance(), m_experimentData.size());
                                m_precisionVsInitialMapped.addData(a_rd.m_initialClusteringPercent, a_rd.calcAutoPrecision(), m_experimentData.size());
                                m_recallVsInitialMapped.addData(a_rd.m_initialClusteringPercent, a_rd.calcAutoRecall(), m_experimentData.size());

                                if (m_avgCount == 0) {
                                    m_avgCount = 1;
                                    m_avgPerformance = a_rd.calcAutoPerformance();
                                } else {
                                    m_avgPerformance = m_avgPerformance * (m_avgCount / (double) (m_avgCount + 1)) + a_rd.calcAutoPerformance() / (double) (m_avgCount + 1);
                                    m_avgCount++;
                                }
                                m_experimentData.add(a_rd);
                            }
                        });

                        m_avgPerformance = 0;
                        m_avgCount = 0;

                        Thread t = new Thread(this);
                        t.start();

                    } catch (IOException e) {
                        System.out.println(e);
                        System.out.println(e.getStackTrace());
                    }
                }
            } else {
                if (a_imgui.button("Stop Experiment", 0)) {
                    m_experiment.stop();
                    halt();
                }
            }

            a_imgui.imgui().sameLine(0);

            a_imgui.text(String.format("Average Performance: %.2f", getAvgPerformance() * 100));

            ArrayList<ScatterPlot.Data> selectedData = new ArrayList<>();
            a_imgui.imgui().beginColumns("plots", 3, 0);
            a_imgui.text("Auto Performance vs Initial Set Size");
            m_performanceVsInitialMapped.doPlot(a_imgui, selectedData);

            a_imgui.imgui().nextColumn();
            a_imgui.text("Auto Precision vs Initial Set Size");
            m_precisionVsInitialMapped.doPlot(a_imgui, selectedData);

            a_imgui.imgui().nextColumn();
            a_imgui.text("Auto Recall vs Initial Set Size");
            m_recallVsInitialMapped.doPlot(a_imgui, selectedData);
            a_imgui.imgui().endColumns();

            final int blue = a_imgui.toColor(new Vec4(0.25, 0.25, 1, 0.75));

            boolean mouseClicked = a_imgui.isMouseClicked(0, false);

            for (ExperimentRunData.BasicRunData exd : m_selectedDataPoints) {

                Vec2 screenPos = new Vec2(exd.m_initialClusteringPercent, exd.calcAutoPerformance());
                m_performanceVsInitialMapped.toScreenPos(screenPos, screenPos);
                a_imgui.addCircleFilled(screenPos, 4, blue, 6);

                screenPos = new Vec2(exd.m_initialClusteringPercent, exd.calcAutoPrecision());
                m_precisionVsInitialMapped.toScreenPos(screenPos, screenPos);
                a_imgui.addCircleFilled(screenPos, 4, blue, 6);

                screenPos = new Vec2(exd.m_initialClusteringPercent, exd.calcAutoRecall());
                m_recallVsInitialMapped.toScreenPos(screenPos, screenPos);
                a_imgui.addCircleFilled(screenPos, 4, blue, 6);
            }


            for (ScatterPlot.Data pd : selectedData) {
                ExperimentRunData.BasicRunData exd = m_experimentData.get(pd.m_id);

                if (mouseClicked) {
                    if (m_selectedDataPoints.contains(exd)) {
                        m_selectedDataPoints.remove(exd);
                    } else {
                        m_selectedDataPoints.add(exd);
                    }
                }

                Vec2 screenPos = new Vec2(pd.m_point.getX(), exd.calcAutoPerformance());
                m_performanceVsInitialMapped.toScreenPos(screenPos, screenPos);
                a_imgui.addCircleFilled(screenPos, 4, blue, 6);

                screenPos = new Vec2(pd.m_point.getX(), exd.calcAutoPrecision());
                m_precisionVsInitialMapped.toScreenPos(screenPos, screenPos);
                a_imgui.addCircleFilled(screenPos, 4, blue, 6);

                screenPos = new Vec2(pd.m_point.getX(), exd.calcAutoRecall());
                m_recallVsInitialMapped.toScreenPos(screenPos, screenPos);
                a_imgui.addCircleFilled(screenPos, 4, blue, 6);
            }

            for (ExperimentRunData.BasicRunData exd : m_selectedDataPoints) {
                a_imgui.text(exd.m_system);
            }


            a_imgui.imgui().endChild();
        }

        public void run() {
            //java.lang.System.out.print("" + m_ix + ", ");
            CGraph graph = new CGraph();
            //m_fs = new RunFileSaver(m_sua.getName(), m_metric.getName(), m_doSaveMappings);

            //m_exr.setRunListener(m_fs);
            m_experiment.run(graph);
        }

        public ExperimentRunner.State getExState() {
            if (m_experiment != null) {
                return m_experiment.getState();
            } else {
                return ExperimentRunner.State.Idle;
            }
        }

        public void halt() {
            if (m_experiment != null) {
                m_experiment.stop();
            }
        }

        public double getAvgPerformance() {
            return m_avgPerformance;
        }
    }
}
