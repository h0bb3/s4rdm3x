package experimenting;

import archviz.HNode;
import gui.ImGuiWrapper;
import imgui.ImGui;
import mapping.MappingView;
import se.lnu.siq.s4rdm3x.experiments.*;
import se.lnu.siq.s4rdm3x.model.CGraph;

import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;

import java.util.ArrayList;


public class ExperimentView {

    ArrayList<ExperimentViewThread> m_experiments = new ArrayList<>();

    public void doView(ImGui a_imgui, ArchDef a_arch, CGraph a_g, HNode.VisualsManager a_nvm) {

        ImGuiWrapper iw = new ImGuiWrapper(a_imgui);

        if (iw.button("Add Experiment", 0)) {
            m_experiments.add(new ExperimentViewThread(m_experiments.size()));
        }

        for (ExperimentViewThread experiment :  m_experiments) {
            experiment.doExperiment(iw);
        }
    }

    static class MappingViewWrapper {

        private CGraph m_graph;
        private ArchDef m_arch;
        private ExperimentRunData.BasicRunData m_data;
        private MappingView m_mv;
        private HNode.VisualsManager m_nvm;
        private boolean [] m_showView = {true};


        public MappingViewWrapper(CGraph a_graph, ArchDef a_arch, ExperimentRunData.BasicRunData a_data) {
            m_graph = a_graph;
            m_arch = a_arch;
            m_data = a_data;
            m_mv = new MappingView();
            m_nvm = new HNode.VisualsManager();

            if (a_data instanceof ExperimentRunData.NBMapperData) {
                m_mv.setInitialNBData((ExperimentRunData.NBMapperData)a_data, m_graph, m_arch);
            }
        }

        public void doView(ImGuiWrapper a_imgui) {
            if (m_showView[0]) {
                if (a_imgui.imgui().begin(m_data.m_system.getName() + "_" + m_data.m_id, m_showView, 0)) {

                    m_mv.doView(a_imgui.imgui(), m_arch, m_graph, m_nvm);

                    a_imgui.imgui().end();
                }
            }
        }

        public boolean isViewFor(ExperimentRunData.BasicRunData a_rundData) {
            return m_data == a_rundData;
        }

        public boolean isShowing() {
            return m_showView[0];
        }

        public void show(boolean showing) {
            m_showView[0] = showing;
        }
    }
}
