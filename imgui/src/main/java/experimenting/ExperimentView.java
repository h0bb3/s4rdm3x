package experimenting;

import archviz.HNode;
import glm_.vec2.Vec2;
import glm_.vec4.Vec4;
import gui.ImGuiWrapper;
import gui.JavaProperty;
import imgui.HoveredFlag;
import imgui.ImGui;
import imgui.WindowFlag;
import imgui.internal.Window;
import kotlin.reflect.*;
import mapping.MappingView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import se.lnu.siq.s4rdm3x.experiments.*;
import se.lnu.siq.s4rdm3x.experiments.ExperimentRunner.RunListener;
import se.lnu.siq.s4rdm3x.experiments.metric.*;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import weka.core.Utils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ExperimentView {

    ExThread m_exThread = new ExThread();

    public void doView(ImGui a_imgui, ArchDef a_arch, CGraph a_g, HNode.VisualsManager a_nvm) {

        ImGuiWrapper iw = new ImGuiWrapper(a_imgui);

        m_exThread.doExperiment(iw);


    }

    private static class MappingViewWrapper {

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


    private static class ExThread extends Thread {
        String m_id;
        ExperimentRunner m_experiment;
        public ArrayList<ExperimentRunData.BasicRunData> m_experimentData = new ArrayList<>();  // this one is accessed by threads so take care...
        private double m_avgPerformance = 0;
        private int m_avgCount = 0;
        private ScatterPlot m_performanceVsInitialMapped = new ScatterPlot();
        private ScatterPlot m_precisionVsInitialMapped = new ScatterPlot();
        private ScatterPlot m_recallVsInitialMapped = new ScatterPlot();
        private Vec4 m_currentColor = new Vec4(0.75, 0.75, 0.75, 1);

        public ArrayList<ExperimentRunData.BasicRunData> m_selectedDataPoints = new ArrayList<>();
        public ArrayList<MappingViewWrapper> m_mappingViews = new ArrayList<>();

        static final int g_nbmapper_ex = 0;
        static final int g_hugmemapper_ex = 1;
        int m_experimentIx = 0;

        // nbmapper experiment parameters
        ExperimentRunner.RandomBoolVariable m_doStemming = new ExperimentRunner.RandomBoolVariable();
        ExperimentRunner.RandomBoolVariable m_doWordCount = new ExperimentRunner.RandomBoolVariable();
        ExperimentRunner.RandomDoubleVariable m_threshold = new ExperimentRunner.RandomDoubleVariable(0.9, 0);

        // hugme experiment parameters
        ExperimentRunner.RandomDoubleVariable m_omega = new ExperimentRunner.RandomDoubleVariable(0.5, 0.5);
        ExperimentRunner.RandomDoubleVariable m_phi = new ExperimentRunner.RandomDoubleVariable(0.5, 0.5);

        // generic experiment parameters
        ExperimentRunner.RandomDoubleVariable m_initialSetSize = new ExperimentRunner.RandomDoubleVariable(0.1, 0.1);
        SystemSelection m_selectedSystem = new SystemSelection();
        boolean m_useManualmapping = false;


        private static Metric[] g_metrics = { new Rand(),  new ByteCodeCyclomaticComplexity(), new ByteCodeInstructions(), new CouplingIn(), new CouplingOut(), new FanIn(), new FanOut(), new LCOMHS(), new LineCount(),
                new NumberOfMethods(), new NumberOfChildren(), new NumberOfChildLevels(), new NumberOfChildrenLevel0(), new NumberOfFields(), new NumberOfParents(), new Rank(), new NumberOfClasses()};
        private ArrayList<Metric> m_selectedMetrics = new ArrayList<>();
        private String m_saveFile = "C:\\hObbE\\projects\\coding\\research\\test.csv";

        static class SystemNameFile {
            public String m_name;
            public String m_file;

            SystemNameFile(String a_name, String a_file) {
                m_name = a_name;
                m_file = a_file;
            }
        }
        static class SystemSelection {
            SystemNameFile[] m_systems = {
                    new SystemNameFile("Ant", "data/systems/ant/ant-system_model.txt"),
                    new SystemNameFile("Argouml", "data/systems/argouml/argouml-system_model.txt"),
                    new SystemNameFile("JabRef", "data/systems/JabRef/3.7/jabref-3_7-system_model_1.txt"),
                    new SystemNameFile("Lucene", "data/systems/lucene/lucene-system_model.txt"),
                    new SystemNameFile("Sweethome 3d", "data/systems/sweethome3d/sweethome3d-system_model.txt"),
                    new SystemNameFile("Teammates", "data/systems/teammates/teammates-system_model.txt")};

            ArrayList<SystemNameFile> m_selectedSystems = new ArrayList<>();

            ArrayList<String> getSystemNames() {
                ArrayList<String> ret = new ArrayList<>();

                for (SystemNameFile snf : m_systems) {
                    ret.add(snf.m_name);
                }
                return ret;
            }

            Iterable<SystemNameFile> getSystems() {
                return Arrays.asList(m_systems);
            }

            Iterable<SystemNameFile> getSelectedSystems() {
                return m_selectedSystems;
            }

            boolean isSelected(SystemNameFile a_snf) {
                return m_selectedSystems.contains(a_snf);
            }

            void toogleSelection(SystemNameFile a_snf) {
                if (isSelected(a_snf)) {
                    m_selectedSystems.remove(a_snf);
                } else {
                    m_selectedSystems.add(a_snf);
                }
            }





            public int getSystemCount() {
                return m_systems.length;
            }

            public boolean isLastSystem(SystemNameFile a_snf) {
                return m_systems[m_systems.length - 1] == a_snf;
            }
        }


        private ExperimentRunData.BasicRunData m_popupMenuData = null;

        ExThread() {
            m_id = "ExThread1";
        }

        public void doPopupMenu(ImGuiWrapper a_imgui, ExperimentRunData.BasicRunData a_runData) {
            boolean showing = false;
            MappingViewWrapper foundMV = null;

            for (MappingViewWrapper mv : m_mappingViews) {
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
                    foundMV = new MappingViewWrapper(graph, arch, a_runData);
                    //foundMV.setInitialNBData(a_rundData, graph, arch);
                    //foundMV.setInitialClustering(a_rundData.m_initialClustering);
                    m_mappingViews.add(foundMV);
                }

                if (foundMV != null) {
                    foundMV.show(showing);
                }
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

        private ExperimentRunner.RandomBoolVariable doRandomBoolVariable(ImGuiWrapper a_imgui, String a_label, ExperimentRunner.RandomBoolVariable a_var) {
            String [] randomBoolLabels = {"Yes", "No", "Random"};

            int[] currentItem = {a_var.isRandom() ? 2 : a_var.getValue() ? 0 : 1};
            if (a_imgui.imgui().combo(a_label + "##" + m_id, currentItem, Arrays.asList(randomBoolLabels), 3)) {
                switch (currentItem[0]) {
                    case 0:
                        a_var = new ExperimentRunner.RandomBoolVariable(true);
                        break;
                    case 1:
                        a_var = new ExperimentRunner.RandomBoolVariable(false);
                        break;
                    case 2:
                        a_var = new ExperimentRunner.RandomBoolVariable();
                        break;
                    default:
                        System.out.println("Unhandled Case in Switch: " + currentItem[0]);
                        assert (false);
                        break;
                }
            }

            return a_var;
        }

        public void doExperiment(ImGuiWrapper a_imgui) {
            Vec2 size = new Vec2(a_imgui.imgui().getContentRegionAvailWidth(), a_imgui.getTextLineHeightWithSpacing() * 2 + a_imgui.imgui().getContentRegionAvailWidth() / 3);

            a_imgui.imgui().beginChild(m_id, size, true, 0);

            {
                String [] experiments = {"Naive Bayes Mapping", "HuGMe"};
                int [] exIx = {m_experimentIx};
                if (a_imgui.imgui().combo("Experiment Type" + "##" + m_id, exIx, Arrays.asList(experiments), 2)) {
                    m_experimentIx = exIx[0];
                }
            }

            if (m_experimentIx == g_nbmapper_ex) {

                m_doStemming = doRandomBoolVariable(a_imgui, "Use Stemming", m_doStemming);
                m_doWordCount = doRandomBoolVariable(a_imgui, "Use Word Counts", m_doWordCount);
                m_threshold = doRandomDoubleVariable(a_imgui, "Threshold", m_threshold);
            } else if (m_experimentIx == g_hugmemapper_ex) {
                m_omega = doRandomDoubleVariable(a_imgui, "Omega Threshold", m_omega);
                m_phi = doRandomDoubleVariable(a_imgui, "Phi", m_phi);
            }

            a_imgui.imgui().separator();
            {
                /*int [] currentSystem = {m_selectedSystem.getCurrentSystemIx()};
                if (a_imgui.imgui().combo("System##" + m_id, currentSystem, m_selectedSystem.getSystemNames(), m_selectedSystem.getSystemCount())) {
                    m_selectedSystem.setCurrentSystem(currentSystem[0]);
                }*/
                for (SystemNameFile snf : m_selectedSystem.getSystems()) {
                    boolean isSelected[] = {m_selectedSystem.isSelected(snf)};
                    if (a_imgui.imgui().checkbox(snf.m_name + "##" + m_id, isSelected)) {
                        m_selectedSystem.toogleSelection(snf);
                    }
                    if (!m_selectedSystem.isLastSystem(snf)) {
                        a_imgui.imgui().sameLine(0, 10);
                    }

                }
            }
            m_initialSetSize = doRandomDoubleVariable(a_imgui, "Initial Set Size", m_initialSetSize);

            if (a_imgui.imgui().collapsingHeader("Metrics##" + m_id, 0)){

                //if (a_imgui.imgui().beginCombo("Metric##" + m_id, m_selectedMetric.getName(), 0)) {

                int count = 0;
                    for (Metric m : g_metrics) {
                        boolean[] isSelected = {m_selectedMetrics.contains(m)};
                        if (a_imgui.imgui().checkbox(m.getName() + "##"+m_id, isSelected)) {
                            if (m_selectedMetrics.contains(m)) {
                                m_selectedMetrics.remove(m);
                            } else {
                                m_selectedMetrics.add(m);
                            }
                        }
                        count++;
                        if (count % 4 != 0) {
                            a_imgui.imgui().sameLine((count % 4) * 250, 0);
                        }

                    }
                    if (count % 4 != 0) {
                        a_imgui.imgui().newLine();
                    }
                a_imgui.imgui().separator();
            }


            {
                boolean [] manualMappnig = {m_useManualmapping};
                if (a_imgui.imgui().checkbox("Use Manual Mapping##" + m_id, manualMappnig)) {
                    m_useManualmapping = manualMappnig[0];
                }
            }


            a_imgui.imgui().colorEdit3("Plot Color##" + m_id, m_currentColor, 0);

            if (m_experiment == null || m_experiment.getState() == ExperimentRunner.State.Idle) {


                if (a_imgui.button("Run Experiment", 0)) {
                    try {
                        if (m_experiment != null) {
                            m_experiment.stop();

                            //halt();
                        }

                        ArrayList<se.lnu.siq.s4rdm3x.experiments.system.System> systems = new ArrayList<>();
                        for (SystemNameFile snf : m_selectedSystem.getSelectedSystems()) {
                            if (snf.m_file != null) {
                                systems.add(new se.lnu.siq.s4rdm3x.experiments.system.FileBased(snf.m_file));
                            }
                        }

                        if (m_experimentIx == g_nbmapper_ex) {
                            m_experiment = new NBMapperExperimentRunner(systems, m_selectedMetrics, m_useManualmapping, m_initialSetSize, m_doStemming, m_doWordCount, m_threshold);
                        } else if (m_experimentIx == g_hugmemapper_ex) {
                            m_experiment = new HuGMeExperimentRunner(systems, m_selectedMetrics, m_useManualmapping, m_initialSetSize, m_omega, m_phi);
                        }

                        m_experiment.setRunListener(new ExperimentRunner.RunListener() {
                            public ExperimentRunData.BasicRunData OnRunInit(ExperimentRunData.BasicRunData a_rd, CGraph a_g, ArchDef a_arch) {

                                return a_rd;
                            }

                            public void OnRunCompleted(ExperimentRunData.BasicRunData a_rd, CGraph a_g, ArchDef a_arch) {

                                m_performanceVsInitialMapped.addData(a_rd.m_initialClusteringPercent, a_rd.calcAutoPerformance(), m_experimentData.size(), a_imgui.toColor(m_currentColor));
                                m_precisionVsInitialMapped.addData(a_rd.m_initialClusteringPercent, a_rd.calcAutoPrecision(), m_experimentData.size(), a_imgui.toColor(m_currentColor));
                                m_recallVsInitialMapped.addData(a_rd.m_initialClusteringPercent, a_rd.calcAutoRecall(), m_experimentData.size(), a_imgui.toColor(m_currentColor));

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



                        Thread t = new Thread(this);
                        t.start();

                    } catch (IOException e) {
                        System.out.println(e);
                        System.out.println(e.getStackTrace());
                    }
                }
                if (m_performanceVsInitialMapped.dataCount() > 0 ) {
                    a_imgui.imgui().sameLine(0);
                    if (a_imgui.button("Clear Data", 0)) {
                        m_performanceVsInitialMapped.clearData();
                        m_precisionVsInitialMapped.clearData();
                        m_recallVsInitialMapped.clearData();
                    }
                    m_saveFile = a_imgui.inputTextSingleLine("##SaveAs"+m_id, m_saveFile);
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
            } else {
                if (a_imgui.button("Stop Experiment", 0)) {
                    m_avgPerformance = 0;
                    m_avgCount = 0;
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

            //boolean mouseClicked = a_imgui.isMouseClicked(0, false);
            boolean mouseClicked = false;

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

                a_imgui.beginTooltip();
                    a_imgui.text("System:\t" + exd.m_system.getName());
                    a_imgui.text("Metric:\t" + exd.m_metric.getName());
                    a_imgui.text("Size:\t" + exd.m_totalMapped);
                    a_imgui.text("Initial:\t" + exd.getInitialClusteringNodeCount());
                    a_imgui.text("A. Clustered:\t" + exd.getAutoClusteredNodeCount());
                    a_imgui.text("A. Failed:\t\t" + exd.m_totalAutoWrong);
                    a_imgui.text("M. Clustered:\t" + exd.m_totalManuallyClustered);
                    a_imgui.text("M. Failed:\t\t" + exd.m_totalFailedClusterings);
                a_imgui.endTooltip();

                if (mouseClicked) {
                    if (m_selectedDataPoints.contains(exd)) {
                        m_selectedDataPoints.remove(exd);
                    } else {
                        m_selectedDataPoints.add(exd);
                    }
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

            for (ExperimentRunData.BasicRunData exd : m_selectedDataPoints) {
                a_imgui.text(exd.m_system.getName());
            }

            /*if (beginPopupContextItem(a_imgui,"test_popup", 1)) {
                a_imgui.menuItem("test_item", "", false, true);
                a_imgui.endPopup();
            }*/


            a_imgui.imgui().endChild();

            for (MappingViewWrapper mv : m_mappingViews) {
                mv.doView(a_imgui);
            }
        }

        private ExperimentRunner.RandomDoubleVariable doRandomDoubleVariable(ImGuiWrapper a_imgui, String a_label, ExperimentRunner.RandomDoubleVariable a_threshold) {
            Float[] minArray = new Float[1]; minArray[0] = (float)a_threshold.getMin();
            Float[] maxArray = new Float[1]; maxArray[0] = (float)a_threshold.getMax();

            if (a_imgui.imgui().dragFloatRange2(a_label+"##"+m_id, new JavaProperty<>(minArray), new JavaProperty<>(maxArray), 0.01f, 0f, 1f, "%.2f", "%.2f", 1)) {
                double scale = (maxArray[0] - minArray[0]) / 2.0;
               a_threshold = new ExperimentRunner.RandomDoubleVariable(minArray[0] + scale, scale);
            }
            return a_threshold;
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
