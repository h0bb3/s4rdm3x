package experimenting;

import archviz.HNode;
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

    ExThread m_exThread;

    public void doView(ImGui a_imgui, ArchDef a_arch, CGraph a_g, HNode.VisualsManager a_nvm) {

        ImGuiWrapper iw = new ImGuiWrapper(a_imgui);

        if (m_exThread == null) {
            iw.text("No Experiment Running");
            if (iw.button("Run Experiment", 0)) {
                try {
                    se.lnu.siq.s4rdm3x.experiments.system.FileBased sys = new se.lnu.siq.s4rdm3x.experiments.system.FileBased("data/systems/lucene/lucene-system_model.txt");


                    if (m_exThread != null) {
                        m_exThread.halt();
                    }

                    ExperimentRunner exr = new NBMapperExperimentRunner(sys, new Rand());


                    m_exThread = new ExThread(exr);
                    Thread t = new Thread(m_exThread);
                    t.start();

                } catch (IOException e) {
                    System.out.println(e);
                    System.out.println(e.getStackTrace());
                }
            }
        } else {
            iw.text("No Experiment Running: " + m_exThread.getExState());
            if (iw.button("Stop Experiment", 0)) {
                m_exThread.halt();
                m_exThread = null;
            }
        }

        if (m_exThread != null) {
            iw.text("" + Utils.doubleToString(m_exThread.getAvgPerformance() * 100, 2));
            /*for (int i = 0; i < m_exThread.m_experimentData.size(); i++) {
                ExperimentRunData.BasicRunData exd = m_exThread.m_experimentData.get(i);
                iw.text("Performance: " + exd.m_initialClustered + "\t" + Utils.doubleToString((exd.m_totalAutoWrong / (double) exd.m_totalAutoClustered), 2) + "\t" +
                        Utils.doubleToString((double) (exd.m_totalAutoClustered - exd.m_totalAutoWrong) / (double) exd.m_totalMapped * 100.0, 2));
            }*/
        }


    }


    private static class ExThread extends Thread {
        ExperimentRunner m_experiment;
        public ArrayList<ExperimentRunData.BasicRunData> m_experimentData = new ArrayList<>();  // this one is accessed by threads so take care...
        private double m_avgPerformance = 0;
        private int m_avgCount = 0;



        ExThread(ExperimentRunner a_expr) {
            m_experiment = a_expr;

            m_experiment.setRunListener(new ExperimentRunner.RunListener() {
                public ExperimentRunData.BasicRunData OnRunInit(ExperimentRunData.BasicRunData a_rd, CGraph a_g, ArchDef a_arch) {

                    return a_rd;
                }
                public void OnRunCompleted(ExperimentRunData.BasicRunData a_rd, CGraph a_g, ArchDef a_arch) {
                    if (m_avgCount == 0) {
                        m_avgCount = 1;
                        m_avgPerformance = a_rd.calcAutoPerformance();
                    } else {
                        m_avgPerformance = m_avgPerformance * (m_avgCount/(double)(m_avgCount + 1)) + a_rd.calcAutoPerformance() / (double)(m_avgCount + 1);
                        m_avgCount++;
                    }
                    m_experimentData.add(a_rd);
                }
            });
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
