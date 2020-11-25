package experimenting;

import glm_.vec2.Vec2;
import glm_.vec4.Vec4;
import gui.ImGuiWrapper;
import imgui.HoveredFlag;
import imgui.WindowFlag;
import imgui.internal.classes.Window;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import se.lnu.siq.s4rdm3x.experiments.*;


import se.lnu.siq.s4rdm3x.model.CNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiPredicate;


public class ExperimentsView implements ExperimentRunnerViewThread.DataListener {

    ArrayList<ExperimentRunnerViewThread> m_experiments = new ArrayList<>();

    private String m_saveFile = Paths.get("output.csv").toAbsolutePath().toString();
    private String m_experimentSaveFile = Paths.get("experiment.xml").toAbsolutePath().toString();


    private ScatterPlot m_performanceVsInitialMapped = new ScatterPlot();
    private ScatterPlot m_precisionVsInitialMapped = new ScatterPlot();
    private ScatterPlot m_recallVsInitialMapped = new ScatterPlot();

    private BoxPlot m_performanceBP = new BoxPlot();
    private BoxPlot m_precisionBP = new BoxPlot();
    private BoxPlot m_recallBP = new BoxPlot();

    FailedClusterings m_fails = new FailedClusterings();

    private RunData m_popupMenuData = null;

    public ArrayList<RunData> m_selectedDataPoints = new ArrayList<>();

    public String getSelectedNodeLogicName() {
        String node = m_fails.m_selectedNodeLogicName;
        m_fails.m_selectedNodeLogicName = null;
        return node;
    }

    private static class RunData {
        MapperView m_source;
        ExperimentRunData.BasicRunData m_data;

        public RunData(ExperimentRunData.BasicRunData a_data, MapperView a_source) {
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

    public void doView(ImGuiWrapper a_imgui) {

        if (a_imgui.button("Add Experiment", 0)) {
            m_experiments.add(new ExperimentRunnerViewThread());
        }
        a_imgui.sameLine(0);
        if (a_imgui.button("Delete Experiments", 0)) {
            m_experiments.clear();
        }
        a_imgui.sameLine(0);
        if (a_imgui.button("Run Experiments", 0)) {
            m_experiments.forEach(e -> e.runExperiment(this));
        }
        a_imgui.sameLine(0);
        if (a_imgui.button("Stop Experiments", 0)) {
            m_experiments.forEach(e -> e.stopExperiment());
        }



        doSaveButtons(a_imgui);

        a_imgui.imgui().checkbox("Show Scatter Plots", m_showScatterPlots);
        if (m_showScatterPlots[0]) {
            if (a_imgui.imgui().begin("Experiment Scatter Plots", m_showScatterPlots, 0)) {

                doScatterPlots(a_imgui);

                a_imgui.imgui().end();
            }
        }
        a_imgui.sameLine(0);
        a_imgui.imgui().checkbox("Show Box Plots", m_showBoxPlots);
        if (m_showBoxPlots[0]) {
            if (a_imgui.imgui().begin("Experiment Box Plots", m_showBoxPlots, 0)) {

                doBoxPlots(a_imgui);
                a_imgui.imgui().end();
            }
        }

        a_imgui.sameLine(0);
        a_imgui.imgui().checkbox("Show Fails", m_showFails);
        if (m_showFails[0]) {
            if (a_imgui.imgui().begin("Failed Clusterings", m_showFails, 0)) {

                m_fails.doShow(a_imgui);

                a_imgui.imgui().end();
            }
        }


        ExperimentRunnerViewThread toBeDeleted = null;
        ExperimentRunnerViewThread toBeCopied = null;
        for (ExperimentRunnerViewThread experiment :  m_experiments) {
            ExperimentRunnerViewThread.DoExperimentAction action = experiment.doExperiment(a_imgui, this);
            if (action == ExperimentRunnerViewThread.DoExperimentAction.Delete) {
                toBeDeleted = experiment;
            }
            if (action == ExperimentRunnerViewThread.DoExperimentAction.Copy) {
                toBeCopied = experiment;
            }
        }

        if (toBeDeleted != null) {
            toBeDeleted.stopExperiment();
            m_experiments.remove(toBeDeleted);
        }
        if (toBeCopied != null) {
            m_experiments.add(new ExperimentRunnerViewThread(toBeCopied));
        }
    }

    private void doBoxPlots(ImGuiWrapper a_iw) {

        a_iw.imgui().beginColumns("boxplots", 3, 0);
        a_iw.text("F1-score");
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
        a_imgui.sameLine(0);
        if (a_imgui.button("Save Experiments", 0)) {
            try {
                HashMap<ExperimentRun, MapperView> experiments = new HashMap<>();
                ArrayList<ExperimentRunner> runners = new ArrayList<>();
                for (ExperimentRunnerViewThread e : m_experiments) {
                    runners.add(e.createExperiment());
                    for (MapperView mv : e.getMappers()) {
                        experiments.put(mv.getExperimentRun(), mv);
                    }
                }
                ExperimentXMLPersistence exmlp = new ExperimentXMLPersistence();
                exmlp.saveExperiments(runners, m_experimentSaveFile, new ExperimentXMLPersistence.ListenerB() {
                    @Override
                    public void onLoadedExperiment(Element a_experimentElement, ExperimentRunner a_runner, ExperimentRun a_loadedExperiment) {
                    }

                    @Override
                    public void onSavedExperiment(Document a_doc, Element a_experimentElement, ExperimentRun a_savedExperiment) {
                        MapperView evt = experiments.get(a_savedExperiment);
                        float[] col = new float[4];
                        a_experimentElement.appendChild(exmlp.vec4ToElement(a_doc, evt.getColor().to(col), "plot_color"));
                    }
                });
            } catch (Exception e) {
                java.lang.System.out.println(e);
                e.printStackTrace();
            }
        }

        a_imgui.sameLine(0);
        if (a_imgui.button("Load Experiment", 0)) {
            ExperimentXMLPersistence exmlp = new ExperimentXMLPersistence();
            try {
                HashMap<ExperimentRunner, ExperimentRunnerViewThread> experiments = new HashMap<>();
                ArrayList<ExperimentRunner> loadedRunners = exmlp.loadExperimentRunners(m_experimentSaveFile, new ExperimentXMLPersistence.ListenerB() {
                    @Override
                    public void onLoadedExperiment(Element a_experimentElement, ExperimentRunner a_runner, ExperimentRun a_loadedExperiment) {

                        ExperimentRunnerViewThread ex;
                        if (experiments.containsKey(a_runner)) {
                            ex = experiments.get(a_runner);
                        } else {
                            ex = new ExperimentRunnerViewThread(a_runner);
                            experiments.put(a_runner, ex);
                            m_experiments.add(ex);
                        }
                        MapperView mv = ex.findMapper(a_loadedExperiment);
                        mv.getColor().put(exmlp.elementToVec4(a_experimentElement, "plot_color"));
                    }

                    @Override
                    public void onSavedExperiment(Document a_doc, Element a_experimentElement, ExperimentRun a_savedExperiment) {
                    }
                });

                // there may now be runners without any mappers and these are not created by the above code
                // so lets check that and create them
                for (ExperimentRunner er : loadedRunners) {
                    if (!experiments.containsKey(er)) {
                        m_experiments.add(new ExperimentRunnerViewThread(er));
                    }
                }


            } catch (Exception e) {
                System.out.println("Exception when attempting to load/save:" + m_experimentSaveFile);
                java.lang.System.out.println(e);
                e.printStackTrace();
            }
        }

        if (m_performanceVsInitialMapped.dataCount() > 0 ) {
            m_saveFile = a_imgui.inputTextSingleLine("###SaveAsExperimentDataAs", m_saveFile);
            a_imgui.sameLine(0);
            if (a_imgui.button("Save Data", 0)) {
                Path filePath = Paths.get(m_saveFile);
                RundDataCSVFileSaver saver = new RundDataCSVFileSaver(filePath);
                try {
                    if (!Files.exists(filePath)) {
                        Files.createFile(filePath);
                        try {
                            saver.writeHeader();
                        } catch (IOException e) {
                            java.lang.System.err.println("Could not write to file: " + e.getMessage());
                        }
                    } else {
                        saver.setHeaderCount();
                    }

                    try {
                        saver.writeData(getExperimentRunData());
                    } catch (IOException e) {
                        java.lang.System.err.println("Could not write to file: " + e.getMessage());
                    } catch (IllegalArgumentException e) {
                        java.lang.System.err.println("Could not write to file: " + e.getMessage());
                    }

                } catch (IOException e) {
                    java.lang.System.err.println("Could not create file: " + e.getMessage());
                }
            }
            a_imgui.sameLine(0);
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


    public synchronized void onNewData(ExperimentRunData.BasicRunData a_rd, MapperView a_src) {

        int intColor = ImGuiWrapper.toColor(a_src.getColor());
        //m_performanceVsInitialMapped.addData(a_rd.m_initialClusteringPercent, a_rd.calcAutoPerformance(), m_experimentData.size(), intColor);
        m_performanceVsInitialMapped.addData(a_rd.m_initialClusteringPercent, a_rd.calcF1Score(), m_experimentData.size(), intColor);
        m_precisionVsInitialMapped.addData(a_rd.m_initialClusteringPercent, a_rd.calcAutoPrecision(), m_experimentData.size(), intColor);
        m_recallVsInitialMapped.addData(a_rd.m_initialClusteringPercent, a_rd.calcAutoRecall(), m_experimentData.size(), intColor);

        //m_performanceBP.addData(a_rd.calcAutoPerformance(), m_experimentData.size(), intColor);
        m_performanceBP.addData(a_rd.calcF1Score(), m_experimentData.size(), intColor);
        m_precisionBP.addData(a_rd.calcAutoPrecision(), m_experimentData.size(), intColor);
        m_recallBP.addData(a_rd.calcAutoRecall(), m_experimentData.size(), intColor);


        m_experimentData.add(new RunData(a_rd, a_src));

        for (CNode n : a_rd.getAutoClusteredNodes()) {
            if (!n.getClusteringType().equals("Initial")) {
                //m_fails.add(n, a_rd.m_system.getName(), a_src.m_experiment.getName());
                m_fails.add(n, a_rd.m_system.getName(), a_src.getName());
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
        a_imgui.text("F1-Score");
        m_performanceVsInitialMapped.doPlot(a_imgui, selectedData);

        a_imgui.imgui().nextColumn();
        a_imgui.text("Precision");
        m_precisionVsInitialMapped.doPlot(a_imgui, selectedData);

        a_imgui.imgui().nextColumn();
        a_imgui.text("Recall");
        m_recallVsInitialMapped.doPlot(a_imgui, selectedData);
        a_imgui.imgui().endColumns();

        if (m_powerSelection == paintIx) {
            selectedData.forEach(dp -> {
                if (!m_selectedDataPoints.contains(m_experimentData.get(dp.m_id)))
                    m_selectedDataPoints.add(m_experimentData.get(dp.m_id));
            });
        }

        final int blue = ImGuiWrapper.toColor(new Vec4(0.25, 0.25, 1, 0.75));

        boolean mouseClicked = a_imgui.isMouseClicked(0, false);
        //boolean mouseClicked = true;

        for (RunData exd : m_selectedDataPoints) {

            Vec2 screenPos = new Vec2(exd.m_data.m_initialClusteringPercent, exd.m_data.calcF1Score());
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
                //a_imgui.text("Mapper:\t" + exd.m_source.getMapper());
                //a_imgui.text("Experiment:\t" + "farre");
                a_imgui.text("System:\t" + exd.m_data.m_system.getName());
                a_imgui.text("Metric:\t" + exd.m_data.m_metric.getName());
                a_imgui.text("Size:\t" + exd.m_data.m_totalMapped);
                a_imgui.text("Initial:\t" + exd.m_data.getInitialClusteringNodeCount());
                a_imgui.text("A. Clustered:\t" + exd.m_data.getAutoClusteredNodeCount());
                a_imgui.text("A. Failed:\t\t" + exd.m_data.m_totalAutoWrong);
                a_imgui.text("Iterations:\t\t" + exd.m_data.m_iterations);
//                a_imgui.text("M. Clustered:\t" + exd.m_data.m_totalManuallyClustered);
//                a_imgui.text("M. Failed:\t\t" + exd.m_data.m_totalFailedClusterings);
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


            Vec2 screenPos = new Vec2(pd.m_point.getX(), exd.m_data.calcF1Score());
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

        /*for (ExperimentsView.MappingViewWrapper mv : m_mappingViews) {
            mv.doView(a_imgui);
        }*/
    }

    // this one is needed to create context menus over child windows after the child is ended.
    boolean beginPopupContextItem(ImGuiWrapper a_imgui, String a_id, int a_mouseButton) {
        Window window = a_imgui.imgui().getCurrentWindow();

        Object oId = new Object();

        int id = a_id != null ? window.getID(oId) : window.getDc().getLastItemId(); // If user hasn't passed an ID, we can use the LastItemID. Using LastItemID as a Popup ID won't conflict!

        if (a_imgui.imgui().isMouseReleased(a_imgui.int2MB(a_mouseButton)) && a_imgui.imgui().isWindowHovered(HoveredFlag.AllowWhenBlockedByPopup.i | HoveredFlag.RootAndChildWindows.i)) {
            a_imgui.imgui().openPopupEx(id);
        }

        return a_imgui.imgui().beginPopupEx(id, WindowFlag.AlwaysAutoResize.or(WindowFlag.NoTitleBar.or(WindowFlag.NoSavedSettings)));
    }


    public void doPopupMenu(ImGuiWrapper a_imgui, RunData a_selected, int a_color) {
        boolean showing = false;
        /*ExperimentsView.MappingViewWrapper foundMV = null;

        for (ExperimentsView.MappingViewWrapper mv : m_mappingViews) {
            if (mv.isViewFor(a_selected.m_data)) {
                showing = mv.isShowing();
                foundMV = mv;
                break;
            }
        }*/

        /*if (a_imgui.menuItem("Show Mapping", "", showing, true)) {
            showing = !showing;
            if (foundMV == null && showing) {
                CGraph graph = new CGraph();
                a_selected.m_data.m_system.load(graph);

                ArchDef arch = null;
                try {
                    arch = a_selected.m_data.m_system.createAndMapArch(graph);
                } catch (System.NoMappedNodesException e) {
                    // do nothing...
                    arch = e.m_arch;
                }

                m_mappingViews.add(foundMV);
            }

            if (foundMV != null) {
                foundMV.show(showing);
            }
        }*/

        {
            if (m_workingColor == null) {
                m_workingColor = ImGuiWrapper.fromColor(a_color);
            }
            a_imgui.imgui().colorEdit3("Set Point Color", m_workingColor, 0);

            int intCol = ImGuiWrapper.toColor(m_workingColor);
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
}
