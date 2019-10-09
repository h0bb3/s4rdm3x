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
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import se.lnu.siq.s4rdm3x.experiments.*;
import se.lnu.siq.s4rdm3x.model.CGraph;

import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiPredicate;


public class ExperimentView implements ExperimentViewThread.DataListener {

    ArrayList<ExperimentViewThread> m_experiments = new ArrayList<>();

    private String m_saveFile = "C:\\hObbE\\projects\\coding\\research\\test.csv";
    private String m_experimentSaveFile = "C:\\hObbE\\projects\\coding\\research\\experiment.xml";


    private ScatterPlot m_performanceVsInitialMapped = new ScatterPlot();
    private ScatterPlot m_precisionVsInitialMapped = new ScatterPlot();
    private ScatterPlot m_recallVsInitialMapped = new ScatterPlot();

    private BoxPlot m_performanceBP = new BoxPlot();
    private BoxPlot m_precisionBP = new BoxPlot();
    private BoxPlot m_recallBP = new BoxPlot();

    FailedClusterings m_fails = new FailedClusterings();

    private RunData m_popupMenuData = null;

    public ArrayList<RunData> m_selectedDataPoints = new ArrayList<>();
    public ArrayList<ExperimentView.MappingViewWrapper> m_mappingViews = new ArrayList<>();

    public String getSelectedNodeLogicName() {
        String node = m_fails.m_selectedNodeLogicName;
        m_fails.m_selectedNodeLogicName = null;
        return node;
    }

    private static class RunData {
        ExperimentViewThread m_source;
        ExperimentRunData.BasicRunData m_data;

        public RunData(ExperimentRunData.BasicRunData a_data, ExperimentViewThread a_source) {
            m_source = a_source;
            m_data = a_data;
        }
    }

    public ArrayList<RunData> m_experimentData = new ArrayList<>();  // this one is accessed by threads so take care...
    private boolean[] m_showScatterPlots = {true};
    private boolean[] m_showBoxPlots = {true};
    private boolean[] m_showFails = {true};
    private Vec4 m_workingColor = null;

    Iterable<? extends ExperimentRunData.BasicRunData> getExperimentRunData() {

        class BrdIterable implements Iterable<ExperimentRunData.BasicRunData> {

            @NotNull
            @Override
            public Iterator<ExperimentRunData.BasicRunData> iterator() {
                return new Iterator<ExperimentRunData.BasicRunData>() {

                    Iterator<RunData> m_it = m_experimentData.iterator();

                    @Override
                    public boolean hasNext() {
                        return m_it.hasNext();
                    }

                    @Override
                    public ExperimentRunData.BasicRunData next() {
                        return m_it.next().m_data;
                    }
                };
            }
        }

        return new BrdIterable();
    }

    public void doView(ImGui a_imgui, ArchDef a_arch, CGraph a_g, HNode.VisualsManager a_nvm) {

        ImGuiWrapper iw = new ImGuiWrapper(a_imgui);

        if (iw.button("Add Experiment", 0)) {
            m_experiments.add(new ExperimentViewThread());
        }
        a_imgui.sameLine(0);
        if (iw.button("Delete Experiments", 0)) {
            m_experiments.clear();
        }
        a_imgui.sameLine(0);
        if (iw.button("Run Experiments", 0)) {
            m_experiments.forEach(e -> e.runExperiment(this));
        }
        a_imgui.sameLine(0);
        if (iw.button("Stop Experiments", 0)) {
            m_experiments.forEach(e -> e.stopExperiment());
        }



        doSaveButtons(iw);

        a_imgui.checkbox("Show Scatter Plots", m_showScatterPlots);
        if (m_showScatterPlots[0]) {
            if (a_imgui.begin("Experiment Scatter Plots", m_showScatterPlots, 0)) {

                doScatterPlots(iw);

                a_imgui.end();
            }
        }
        a_imgui.sameLine(0);
        a_imgui.checkbox("Show Box Plots", m_showBoxPlots);
        if (m_showBoxPlots[0]) {
            if (a_imgui.begin("Experiment Box Plots", m_showBoxPlots, 0)) {

                doBoxPlots(iw);
                a_imgui.end();
            }
        }

        a_imgui.sameLine(0);
        a_imgui.checkbox("Show Fails", m_showFails);
        if (m_showFails[0]) {
            if (a_imgui.begin("Failed Clusterings", m_showFails, 0)) {

                m_fails.doShow(iw);

                a_imgui.end();
            }
        }


        ExperimentViewThread toBeDeleted = null;
        ExperimentViewThread toBeCopied = null;
        for (ExperimentViewThread experiment :  m_experiments) {
            ExperimentViewThread.DoExperimentAction action = experiment.doExperiment(iw, this);
            if (action == ExperimentViewThread.DoExperimentAction.Delete) {
                toBeDeleted = experiment;
            }
            if (action == ExperimentViewThread.DoExperimentAction.Copy) {
                toBeCopied = experiment;
            }
        }

        if (toBeDeleted != null) {
            toBeDeleted.stopExperiment();
            m_experiments.remove(toBeDeleted);
        }
        if (toBeCopied != null) {
            m_experiments.add(new ExperimentViewThread(toBeCopied));
        }
    }

    private void doBoxPlots(ImGuiWrapper a_iw) {

        a_iw.imgui().beginColumns("boxplots", 3, 0);
        a_iw.text("Performance");
        m_performanceBP.doPlot(a_iw);

        a_iw.imgui().nextColumn();
        a_iw.text("Precision");
        m_precisionBP.doPlot(a_iw);

        a_iw.imgui().nextColumn();
        a_iw.text("Recall");
        m_recallBP.doPlot(a_iw);
        a_iw.imgui().endColumns();


    }

    private void doSaveButtons(ImGuiWrapper a_imgui) {
        m_experimentSaveFile = a_imgui.inputTextSingleLine("##SaveEperimentsAs", m_experimentSaveFile);
        a_imgui.imgui().sameLine(0);
        if (a_imgui.button("Save Experiments", 0)) {
            try {
                HashMap<ExperimentRunner, ExperimentViewThread> experiments = new HashMap<>();
                for (ExperimentViewThread e : m_experiments) {
                    experiments.put(e.createExperiment(), e);
                }
                ExperimentXMLPersistence exmlp = new ExperimentXMLPersistence();
                exmlp.saveExperiments(experiments.keySet(), m_experimentSaveFile, new ExperimentXMLPersistence.Listener() {
                    @Override
                    public void onLoadedExperiment(Element a_experimentElement, ExperimentRunner a_loadedExperiment) {
                    }

                    @Override
                    public void onSavedExperiment(Document a_doc, Element a_experimentElement, ExperimentRunner a_savedExperiment) {
                        ExperimentViewThread evt = experiments.get(a_savedExperiment);
                        a_experimentElement.appendChild(exmlp.vec4ToElement(a_doc, evt.getColor().toFloatArray(), "plot_color"));
                    }
                });
            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
        }

        a_imgui.imgui().sameLine(0);
        if (a_imgui.button("Load Experiment", 0)) {
            ExperimentXMLPersistence exmlp = new ExperimentXMLPersistence();
            try {
                ArrayList<ExperimentRunner> experiments = exmlp.loadExperiments(m_experimentSaveFile, new ExperimentXMLPersistence.Listener() {
                    @Override
                    public void onLoadedExperiment(Element a_experimentElement, ExperimentRunner a_loadedExperiment) {
                        ExperimentViewThread ex = new ExperimentViewThread(a_loadedExperiment);
                        ex.getColor().setArray(exmlp.elementToVec4(a_experimentElement, "plot_color"));
                        m_experiments.add(ex);
                    }

                    @Override
                    public void onSavedExperiment(Document a_doc, Element a_experimentElement, ExperimentRunner a_loadedExperiment) {
                    }
                });

            } catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }
        }

        if (m_performanceVsInitialMapped.dataCount() > 0 ) {
            m_saveFile = a_imgui.inputTextSingleLine("###SaveAsExperimentDataAs", m_saveFile);
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
                        saver.writeData(filePath, getExperimentRunData());
                    } catch (IOException e) {
                        System.out.println("Could not write to file");
                    }

                } catch (IOException e) {
                    System.out.println("Could not create file");
                }
            }
            a_imgui.imgui().sameLine(0);
            if (a_imgui.button("Clear Data", 0)) {
                m_performanceVsInitialMapped.clearData();
                m_precisionVsInitialMapped.clearData();
                m_recallVsInitialMapped.clearData();
                m_performanceBP.clearData();
                m_precisionBP.clearData();
                m_recallBP.clearData();
                m_selectedDataPoints.clear();
            }
        }
    }


    public synchronized void onNewData(ExperimentRunData.BasicRunData a_rd, ExperimentViewThread a_src) {

        int intColor = ImGuiWrapper.toColor(a_src.getColor());
        m_performanceVsInitialMapped.addData(a_rd.m_initialClusteringPercent, a_rd.calcAutoPerformance(), m_experimentData.size(), intColor);
        m_precisionVsInitialMapped.addData(a_rd.m_initialClusteringPercent, a_rd.calcAutoPrecision(), m_experimentData.size(), intColor);
        m_recallVsInitialMapped.addData(a_rd.m_initialClusteringPercent, a_rd.calcAutoRecall(), m_experimentData.size(), intColor);

        m_performanceBP.addData(a_rd.calcAutoPerformance(), m_experimentData.size(), intColor);
        m_precisionBP.addData(a_rd.calcAutoPrecision(), m_experimentData.size(), intColor);
        m_recallBP.addData(a_rd.calcAutoRecall(), m_experimentData.size(), intColor);


        m_experimentData.add(new RunData(a_rd, a_src));

        for (CNode n : a_rd.getAutoClusteredNodes()) {
            if (!n.getClusteringType().equals("Initial")) {
                m_fails.add(n, a_rd.m_system.getName(), a_src.m_experiment.getName());
            }
        }
    }

    private int m_powerSelection = 0;
    interface PowerSelectionFilter extends BiPredicate<RunData, RunData> {}

    private void doScatterPlots(ImGuiWrapper a_imgui) {

        final String[] powerSelections = {"None", "System", "Metric", "Algorithm", "Paint"};
        final int paintIx = 4;

        final PowerSelectionFilter[] powerSelectionFilters = new PowerSelectionFilter[]{
                (target, source) -> false,
                (target, source) -> target.m_data.m_system.getName().equals(source.m_data.m_system.getName()),
                (target, source) -> target.m_data.m_metric.getName().equals(source.m_data.m_metric.getName()),
                (target, source) -> target.m_data.getClass() == source.m_data.getClass()
        };


        if (m_experimentData.size() > 0) {
            int [] selectedItem = {m_powerSelection};
            if (a_imgui.imgui().combo("Power Selection", selectedItem, Arrays.asList(powerSelections), powerSelections.length)) {
                m_powerSelection = selectedItem[0];
            }
        }


        ArrayList<ScatterPlot.Data> selectedData = new ArrayList<>();
        a_imgui.imgui().beginColumns("scatterplots", 3, 0);
        a_imgui.text("Auto Performance vs Initial Set Size");
        m_performanceVsInitialMapped.doPlot(a_imgui, selectedData);

        a_imgui.imgui().nextColumn();
        a_imgui.text("Auto Precision vs Initial Set Size");
        m_precisionVsInitialMapped.doPlot(a_imgui, selectedData);

        a_imgui.imgui().nextColumn();
        a_imgui.text("Auto Recall vs Initial Set Size");
        m_recallVsInitialMapped.doPlot(a_imgui, selectedData);
        a_imgui.imgui().endColumns();

        if (m_powerSelection == paintIx) {
            selectedData.forEach(dp -> {
                if (!m_selectedDataPoints.contains(m_experimentData.get(dp.m_id)))
                    m_selectedDataPoints.add(m_experimentData.get(dp.m_id));
            });
        }

        final int blue = a_imgui.toColor(new Vec4(0.25, 0.25, 1, 0.75));

        boolean mouseClicked = a_imgui.isMouseClicked(0, false);
        //boolean mouseClicked = true;

        for (RunData exd : m_selectedDataPoints) {

            Vec2 screenPos = new Vec2(exd.m_data.m_initialClusteringPercent, exd.m_data.calcAutoPerformance());
            m_performanceVsInitialMapped.toScreenPos(screenPos, screenPos);
            a_imgui.addCircleFilled(screenPos, 4, blue, 6);

            screenPos = new Vec2(exd.m_data.m_initialClusteringPercent, exd.m_data.calcAutoPrecision());
            m_precisionVsInitialMapped.toScreenPos(screenPos, screenPos);
            a_imgui.addCircleFilled(screenPos, 4, blue, 6);

            screenPos = new Vec2(exd.m_data.m_initialClusteringPercent, exd.m_data.calcAutoRecall());
            m_recallVsInitialMapped.toScreenPos(screenPos, screenPos);
            a_imgui.addCircleFilled(screenPos, 4, blue, 6);
        }

        if(mouseClicked && selectedData.size() > 0 && m_selectedDataPoints.size() > 0) {
            mouseClicked = false;
            m_selectedDataPoints.clear();
        }


        for (ScatterPlot.Data pd : selectedData) {
            RunData exd = m_experimentData.get(pd.m_id);

            if (a_imgui.beginTooltip()) {
                a_imgui.text("Experiment:\t" + exd.m_source.getName());
                a_imgui.text("System:\t" + exd.m_data.m_system.getName());
                a_imgui.text("Metric:\t" + exd.m_data.m_metric.getName());
                a_imgui.text("Size:\t" + exd.m_data.m_totalMapped);
                a_imgui.text("Initial:\t" + exd.m_data.getInitialClusteringNodeCount());
                a_imgui.text("A. Clustered:\t" + exd.m_data.getAutoClusteredNodeCount());
                a_imgui.text("A. Failed:\t\t" + exd.m_data.m_totalAutoWrong);
                a_imgui.text("M. Clustered:\t" + exd.m_data.m_totalManuallyClustered);
                a_imgui.text("M. Failed:\t\t" + exd.m_data.m_totalFailedClusterings);
                a_imgui.endTooltip();
            }

            if (mouseClicked) {
                // good luck with this one :D
                m_experimentData.stream().filter((d -> powerSelectionFilters[m_powerSelection].test(d, exd) && !m_selectedDataPoints.contains(d))).forEach(d->m_selectedDataPoints.add(d));
            }

            if (beginPopupContextItem(a_imgui, "SomePopupID", 1)) {
                m_popupMenuData = exd;
                doPopupMenu(a_imgui, m_popupMenuData, pd.m_color);
                a_imgui.endPopup();
            } else {
                m_popupMenuData = null;
                m_workingColor = null;
            }


            Vec2 screenPos = new Vec2(pd.m_point.getX(), exd.m_data.calcAutoPerformance());
            m_performanceVsInitialMapped.toScreenPos(screenPos, screenPos);
            a_imgui.addCircleFilled(screenPos, 4, blue, 6);

            screenPos = new Vec2(pd.m_point.getX(), exd.m_data.calcAutoPrecision());
            m_precisionVsInitialMapped.toScreenPos(screenPos, screenPos);
            a_imgui.addCircleFilled(screenPos, 4, blue, 6);

            screenPos = new Vec2(pd.m_point.getX(), exd.m_data.calcAutoRecall());
            m_recallVsInitialMapped.toScreenPos(screenPos, screenPos);
            a_imgui.addCircleFilled(screenPos, 4, blue, 6);
        }

        if (selectedData.size() == 0 && m_popupMenuData != null) {
            if (beginPopupContextItem(a_imgui, "SomePopupID", 1)) {
                doPopupMenu(a_imgui, m_popupMenuData, 0);
                a_imgui.endPopup();
            } else {
                m_popupMenuData = null;
                m_workingColor = null;
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


    public void doPopupMenu(ImGuiWrapper a_imgui, RunData a_selected, int a_color) {
        boolean showing = false;
        ExperimentView.MappingViewWrapper foundMV = null;

        for (ExperimentView.MappingViewWrapper mv : m_mappingViews) {
            if (mv.isViewFor(a_selected.m_data)) {
                showing = mv.isShowing();
                foundMV = mv;
                break;
            }
        }

        if (a_imgui.menuItem("Show Mapping", "", showing, true)) {
            showing = !showing;
            if (foundMV == null && showing) {
                CGraph graph = new CGraph();
                a_selected.m_data.m_system.load(graph);
                ArchDef arch = a_selected.m_data.m_system.createAndMapArch(graph);
                foundMV = new ExperimentView.MappingViewWrapper(graph, arch, a_selected.m_data);
                //foundMV.setInitialNBData(a_rundData, graph, arch);
                //foundMV.setInitialClustering(a_rundData.m_initialClustering);
                m_mappingViews.add(foundMV);
            }

            if (foundMV != null) {
                foundMV.show(showing);
            }
        }

        {
            if (m_workingColor == null) {
                m_workingColor = a_imgui.fromColor(a_color);
            }
            a_imgui.imgui().colorEdit3("Set Point Color", m_workingColor, 0);

            int intCol = a_imgui.toColor(m_workingColor);
            setPlotsPointColor(a_selected, intCol);
            for (RunData brd : m_selectedDataPoints) {
                setPlotsPointColor(brd, intCol);
            }
        }
    }

    private void setPlotsPointColor(RunData a_rd, int a_color) {
        int ix = m_experimentData.indexOf(a_rd);

        m_performanceVsInitialMapped.setColor(ix, a_color);
        m_recallVsInitialMapped.setColor(ix, a_color);
        m_precisionVsInitialMapped.setColor(ix, a_color);
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
