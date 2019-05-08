package experimenting;

import archviz.HNode;
import glm_.vec2.Vec2;
import glm_.vec4.Vec4;
import gui.ImGuiWrapper;
import imgui.HoveredFlag;
import imgui.ImGui;
import imgui.WindowFlag;
import imgui.internal.Window;
import mapping.MappingView;
import se.lnu.siq.s4rdm3x.experiments.*;
import se.lnu.siq.s4rdm3x.model.CGraph;

import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Queue;


public class ExperimentView implements ExperimentViewThread.DataListener {

    ArrayList<ExperimentViewThread> m_experiments = new ArrayList<>();

    private String m_saveFile = "C:\\hObbE\\projects\\coding\\research\\test.csv";
    private String m_experimentSaveFile = "C:\\hObbE\\projects\\coding\\research\\experiment.xml";


    private ScatterPlot m_performanceVsInitialMapped = new ScatterPlot();
    private ScatterPlot m_precisionVsInitialMapped = new ScatterPlot();
    private ScatterPlot m_recallVsInitialMapped = new ScatterPlot();

    private ExperimentRunData.BasicRunData m_popupMenuData = null;

    public ArrayList<ExperimentRunData.BasicRunData> m_selectedDataPoints = new ArrayList<>();
    public ArrayList<ExperimentView.MappingViewWrapper> m_mappingViews = new ArrayList<>();

    public ArrayList<ExperimentRunData.BasicRunData> m_experimentData = new ArrayList<>();  // this one is accessed by threads so take care...

    public void doView(ImGui a_imgui, ArchDef a_arch, CGraph a_g, HNode.VisualsManager a_nvm) {

        ImGuiWrapper iw = new ImGuiWrapper(a_imgui);

        if (iw.button("Add Experiment", 0)) {
            m_experiments.add(new ExperimentViewThread(m_experiments.size()));
        }


        doPlots(iw);

        for (ExperimentViewThread experiment :  m_experiments) {
            experiment.doExperiment(iw, this);
        }
    }

    public synchronized void onNewData(ExperimentRunData.BasicRunData a_rd, int a_color) {
        m_performanceVsInitialMapped.addData(a_rd.m_initialClusteringPercent, a_rd.calcAutoPerformance(), m_experimentData.size(), a_color);
        m_precisionVsInitialMapped.addData(a_rd.m_initialClusteringPercent, a_rd.calcAutoPrecision(), m_experimentData.size(), a_color);
        m_recallVsInitialMapped.addData(a_rd.m_initialClusteringPercent, a_rd.calcAutoRecall(), m_experimentData.size(), a_color);

        m_experimentData.add(a_rd);
    }

    private void doPlots(ImGuiWrapper a_imgui) {
        m_experimentSaveFile = a_imgui.inputTextSingleLine("##SaveEperimentsAs", m_experimentSaveFile);
        a_imgui.imgui().sameLine(0);
        if (a_imgui.button("Save Experiments", 0)) {
            try {
                ArrayList<ExperimentRunner> experiments = new ArrayList<>();
                for (ExperimentViewThread e : m_experiments) {
                    experiments.add(e.createExperiment());
                }
                ExperimentXMLPersistence exmlp = new ExperimentXMLPersistence();
                exmlp.saveExperiments(experiments, m_experimentSaveFile);
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
        }

        a_imgui.imgui().sameLine(0);
        if (a_imgui.button("Load Experiment", 0)) {
            ExperimentXMLPersistence exmlp = new ExperimentXMLPersistence();
            try {
                ArrayList<ExperimentRunner> experiments = exmlp.loadExperiments(m_experimentSaveFile);
                if (experiments.size() != 0) {
                    ExperimentRunner exr = experiments.get(0);

                    ExperimentViewThread ex = new ExperimentViewThread(m_experiments.size(), exr);
                    m_experiments.add(ex);
                }
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
        }

        if (m_performanceVsInitialMapped.dataCount() > 0 ) {
            a_imgui.imgui().sameLine(0);
            if (a_imgui.button("Clear Data", 0)) {
                m_performanceVsInitialMapped.clearData();
                m_precisionVsInitialMapped.clearData();
                m_recallVsInitialMapped.clearData();
                m_selectedDataPoints.clear();
            }
            m_saveFile = a_imgui.inputTextSingleLine("##SaveAsExperimentDataAs", m_saveFile);
            a_imgui.imgui().sameLine(0);
            if (a_imgui.button("Save Data", 0)) {
                Path filePath = Paths.get(m_saveFile);
                RundDataCSVFileSaver saver = new RundDataCSVFileSaver();
                try {
                    if (!Files.exists(filePath)) {
                        Files.createFile(filePath);
                        try {
                            saver.writeHeader(filePath);
                        } catch (IOException e) {
                            System.out.println("Could not write to file");
                        }
                    }

                    try {
                        saver.writeData(filePath, m_experimentData);
                    } catch (IOException e) {
                        System.out.println("Could not write to file");
                    }

                } catch (IOException e) {
                    System.out.println("Could not create file");
                }
            }

        }



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
        //boolean mouseClicked = true;

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

        if(mouseClicked && selectedData.size() > 0 && m_selectedDataPoints.size() > 0) {
            mouseClicked = false;
            m_selectedDataPoints.clear();
        }


        for (ScatterPlot.Data pd : selectedData) {
            ExperimentRunData.BasicRunData exd = m_experimentData.get(pd.m_id);

            if (a_imgui.beginTooltip()) {
                a_imgui.text("System:\t" + exd.m_system.getName());
                a_imgui.text("Metric:\t" + exd.m_metric.getName());
                a_imgui.text("Size:\t" + exd.m_totalMapped);
                a_imgui.text("Initial:\t" + exd.getInitialClusteringNodeCount());
                a_imgui.text("A. Clustered:\t" + exd.getAutoClusteredNodeCount());
                a_imgui.text("A. Failed:\t\t" + exd.m_totalAutoWrong);
                a_imgui.text("M. Clustered:\t" + exd.m_totalManuallyClustered);
                a_imgui.text("M. Failed:\t\t" + exd.m_totalFailedClusterings);
                a_imgui.endTooltip();
            }





            if (mouseClicked) {
                // good luck with this one :D
             //   m_experimentData.stream().filter((d -> powerSelectionFilters[m_powerSelection].test(d, exd) && !m_selectedDataPoints.contains(d))).forEach(d->m_selectedDataPoints.add(d));
            }

            if (beginPopupContextItem(a_imgui, "SomePopupID", 1)) {
                m_popupMenuData = exd;
                doPopupMenu(a_imgui, m_popupMenuData);
                a_imgui.endPopup();
            } else {
                m_popupMenuData = null;
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

        if (selectedData.size() == 0 && m_popupMenuData != null) {
            if (beginPopupContextItem(a_imgui, "SomePopupID", 1)) {
                m_popupMenuData = m_popupMenuData;
                doPopupMenu(a_imgui, m_popupMenuData);
                a_imgui.endPopup();
            } else {
                m_popupMenuData = null;
            }
        }

        for (ExperimentView.MappingViewWrapper mv : m_mappingViews) {
            mv.doView(a_imgui);
        }
    }

    // this one is needed to create context menus over child windows after the child is ended.
    boolean beginPopupContextItem(ImGuiWrapper a_imgui, String a_id, int a_mouseButton) {
        Window window = a_imgui.imgui().getCurrentWindow();
        int id = a_id != null ? window.getId(a_id) : window.getDc().getLastItemId(); // If user hasn't passed an ID, we can use the LastItemID. Using LastItemID as a Popup ID won't conflict!

        if (a_imgui.imgui().isMouseReleased(a_mouseButton) && a_imgui.imgui().isWindowHovered(HoveredFlag.AllowWhenBlockedByPopup.getI() | HoveredFlag.RootAndChildWindows.getI())) {
            a_imgui.imgui().openPopupEx(id);
        }

        return a_imgui.imgui().beginPopupEx(id, WindowFlag.AlwaysAutoResize.or(WindowFlag.NoTitleBar.or(WindowFlag.NoSavedSettings)));
    }


    public void doPopupMenu(ImGuiWrapper a_imgui, ExperimentRunData.BasicRunData a_runData) {
        boolean showing = false;
        ExperimentView.MappingViewWrapper foundMV = null;

        for (ExperimentView.MappingViewWrapper mv : m_mappingViews) {
            if (mv.isViewFor(a_runData)) {
                showing = mv.isShowing();
                foundMV = mv;
                break;
            }
        }

        if (a_imgui.menuItem("Show Mapping", "", showing, true)) {
            showing = !showing;
            if (foundMV == null && showing) {
                CGraph graph = new CGraph();
                a_runData.m_system.load(graph);
                ArchDef arch = a_runData.m_system.createAndMapArch(graph);
                foundMV = new ExperimentView.MappingViewWrapper(graph, arch, a_runData);
                //foundMV.setInitialNBData(a_rundData, graph, arch);
                //foundMV.setInitialClustering(a_rundData.m_initialClustering);
                m_mappingViews.add(foundMV);
            }

            if (foundMV != null) {
                foundMV.show(showing);
            }
        }

        /*{
            if (m_workingColor == null) {
                m_workingColor = new Vec4(m_currentColor);
            }
            if (a_imgui.imgui().colorEdit3("Set Point Color", m_workingColor, 0)) {

                int intCol = a_imgui.toColor(m_workingColor);
                //m_workingColor = null;
                for (ExperimentRunData.BasicRunData brd : m_selectedDataPoints) {
                    int ix = m_experimentData.indexOf(brd);

                    m_performanceVsInitialMapped.setColor(ix, intCol);
                    m_recallVsInitialMapped.setColor(ix, intCol);
                    m_precisionVsInitialMapped.setColor(ix, intCol);
                }
            }
        }*/
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
