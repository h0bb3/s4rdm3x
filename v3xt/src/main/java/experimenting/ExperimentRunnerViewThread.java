package experimenting;

import gui.ImGuiWrapper;
import gui.JavaProperty;
import se.lnu.siq.s4rdm3x.experiments.*;
import se.lnu.siq.s4rdm3x.experiments.metric.*;
import se.lnu.siq.s4rdm3x.experiments.metric.aggregated.RelativeLineCount;
import se.lnu.siq.s4rdm3x.experiments.system.FileBased;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.util.SystemModelReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Encapsulates one mapping experiment, ie. a specification of what systems to use and how the initial set is generated. It then contains the actual mappers to run. It also manages the visualization of the results of running experiments.
 */
class ExperimentRunnerViewThread extends Thread {
    String m_id;
    ExperimentRunner m_experiment;

    private double m_avgPerformance = 0;
    private int m_avgCount = 0;

    private String m_name = "";

    static final int g_nbmapper_ex = 0;
    static final int g_hugmemapper_ex = 1;
    static final int g_irattract_ex = 2;
    static final int g_lsiattract_ex = 3;
    int m_experimentIx = 0;



    // generic experiment parameters
    ExperimentRunner.RandomDoubleVariable m_initialSetSize = new ExperimentRunner.RandomDoubleVariable(0.1, 0.1);
    private boolean m_initialSetPerComponent = false;
    SystemSelection m_selectedSystem = new SystemSelection();
    boolean m_useIntialMapping = false;

    static int g_id = 0;
    private ArrayList<MapperView> m_experiments = new ArrayList<>();


    public ExperimentRunnerViewThread(ExperimentRunnerViewThread a_toBeCopied) {
        m_id = "ExThread_" + g_id; g_id++;
        m_name = a_toBeCopied.m_name;
        m_experimentIx = a_toBeCopied.m_experimentIx;
        m_initialSetSize = new ExperimentRunner.RandomDoubleVariable(a_toBeCopied.m_initialSetSize);
        m_selectedSystem = new SystemSelection(a_toBeCopied.m_selectedSystem);
        for (Metric exrMetric : a_toBeCopied.m_selectedMetrics.getSelected()) {
            m_selectedMetrics.select(exrMetric);
        }
        for (MapperView exv : a_toBeCopied.m_experiments) {
            m_experiments.add(new MapperView(exv));
        }
    }

    ExperimentRunnerViewThread() {
        m_id = "ExThread_" + g_id; g_id++;
        m_selectedMetrics.select(new Rand());
    }

    ExperimentRunnerViewThread(ExperimentRunner a_runner) {
        m_id = "ExThread_" + g_id; g_id++;
        setExperiment(a_runner);
    }


    public void runExperiment(DataListener a_newDataListener) {
        try {
            if (m_experiment != null) {
                m_experiment.stop();
            }

            m_experiment = createExperiment();

            m_experiment.setRunListener(new ExperimentRunner.RunListener() {
                public ExperimentRunData.BasicRunData OnRunInit(ExperimentRunData.BasicRunData a_rd, CGraph a_g, ArchDef a_arch) {

                    return a_rd;
                }

                public void OnRunCompleted(ExperimentRunData.BasicRunData a_rd, CGraph a_g, ArchDef a_arch, ExperimentRun a_source) {

                    MapperView source = null;

                    for (MapperView exv : m_experiments) {
                        if (exv.getExperimentRun() == a_source) {
                            source = exv;
                            break;
                        }
                    }

                    a_newDataListener.onNewData(a_rd, source);

                    if (m_avgCount == 0) {
                        m_avgCount = 1;
                        m_avgPerformance = a_rd.calcAutoPerformance();
                    } else {
                        m_avgPerformance = m_avgPerformance * (m_avgCount / (double) (m_avgCount + 1)) + a_rd.calcAutoPerformance() / (double) (m_avgCount + 1);
                        m_avgCount++;
                    }

                }
            });


            Thread t = new Thread(this);
            t.start();

        } catch (IOException e) {
            System.out.println(e);
            System.out.println(e.getStackTrace());
        }
    }

    public void stopExperiment() {
        if (m_experiment != null) {
            m_experiment.stop();
            m_experiment = null;
        }
    }

    public Iterable<MapperView> getMappers() {
        return m_experiments;
    }

    public void addMapper(MapperView a_mv) {
        m_experiments.add(a_mv);
    }

    public MapperView findMapper(ExperimentRun a_er) {
        for (MapperView mv : m_experiments) {
            if (mv.getExperimentRun() == a_er) {
                return mv;
            }
        }

        return null;
    }

    static class MetricPair {

        MetricPair(String a_name, Metric a_abs) {
            m_name = a_name;
            m_absMetric = a_abs;
            m_relMetric = new RelativeLineCount(a_abs);
        }

        MetricPair(String a_name, Metric a_abs, Metric a_rel) {
            m_name = a_name;
            m_absMetric = a_abs;
            m_relMetric = a_rel;
        }

        String m_name;
        Metric m_absMetric;
        Metric m_relMetric;

        public String getName() {
            return m_name;
        }
    }

    static class MetricSelection {

        private MetricPair[] m_metrics = { new MetricPair("Random", new Rand(), null), new MetricPair("BC. Cyclo. Cpl.", new ByteCodeCyclomaticComplexity()), new MetricPair("BC. Instr. Count", new ByteCodeInstructions()),
                new MetricPair("Coupling In", new CouplingIn()), new MetricPair("Coupling Out", new CouplingOut()), new MetricPair("Fan In", new FanIn()), new MetricPair("Fan Out", new FanOut()),
                new MetricPair("LCOM HS", new LCOMHS()), new MetricPair("Line Count", new LineCount(), null), new MetricPair("Method Count", new NumberOfMethods()), new MetricPair("Child Count", new NumberOfChildren()),
                new MetricPair("Child Level Count", new NumberOfChildLevels()), new MetricPair("Child Count Lvl 0", new NumberOfChildrenLevel0()), new MetricPair("Field Count", new NumberOfFields()), new MetricPair("Parent Count", new NumberOfParents()),
                new MetricPair("Rank", new Rank()), new MetricPair("Class Count", new NumberOfClasses())
        };

        /*private static Metric[] g_metrics = {new NumberOfMethods(), new NumberOfChildren(), new NumberOfChildLevels(), new NumberOfChildrenLevel0(), new NumberOfFields(), new NumberOfParents(), new Rank(), new NumberOfClasses()};*/
        private ArrayList<Metric> m_selectedMetrics = new ArrayList<>();

        void select(Metric a_metric) {
            if (!isSelected(a_metric)) {
                m_selectedMetrics.add(a_metric);
            }
        }


        boolean isSelected(Metric a_metric) {
            return getSelected(a_metric) != null;
        }

        Metric getSelected(Metric a_metric) {
            for (Metric m : m_selectedMetrics) {
                if (m.getName().equals(a_metric.getName())) {
                    return m;
                }
            }

            return null;
        }

        Iterable<MetricPair> getMetricPairs() {
            return Arrays.asList(m_metrics);
        }

        public void toogle(Metric a_m) {
            Metric m = getSelected(a_m);
            if (m != null) {
                m_selectedMetrics.remove(m);
            } else {
                m_selectedMetrics.add(a_m);
            }
        }

        public void clear() {
            m_selectedMetrics.clear();
        }

        public Iterable<Metric> getSelected() {
            return m_selectedMetrics;
        }
    }

    MetricSelection m_selectedMetrics = new MetricSelection();


    static class SystemNameFile {
        public String m_name;
        public String m_file;

        SystemNameFile(String a_name, String a_file) {
            m_name = a_name;
            m_file = a_file;
        }

        private SystemNameFile() {
            if (g_systems == null) {
                g_systems = scan();
            }
        }

        static private ArrayList<SystemNameFile> g_systems = null;
        static SystemNameFile g_dummy = new SystemNameFile();

        public static Iterable<SystemNameFile> globalSystems() {
            return g_systems;
        }

        public int getSystemCount() {
            return g_systems.size();
        }

        public boolean isLastSystem() {

            return g_systems.get(g_systems.size() - 1) == this;
        }

        private ArrayList<SystemNameFile> scan() {
            ArrayList<SystemNameFile> ret = new ArrayList<>();
            try {
                Files.find(Paths.get("data/"), Integer.MAX_VALUE, (filePath, fileAttr) -> fileAttr.isRegularFile()).filter(f -> f.toFile().getName().endsWith(".sysmdl")).forEach(f -> {

                    SystemModelReader smr = new SystemModelReader();
                    smr.readFile(f.toString());
                    boolean jarExists[] = new boolean[1];
                    jarExists[0] = false;
                    if (smr.m_jars.size() > 0) {
                        jarExists[0] = true;
                        smr.m_jars.forEach(j -> {
                            String jar = f.getParent().toString() + File.separator + j;
                            if (!Files.exists(Paths.get(jar))) {
                                jarExists[0] = false;
                            }
                        });

                    }

                    if (jarExists[0]) {
                        ret.add(new SystemNameFile(smr.m_name, f.toString()));
                    } else {
                        System.out.println("Could not add system model: " + f.toString() + " - referenced jars do not exist");
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

            return ret;
        }
    }

    static class SystemSelection {

        ArrayList<SystemNameFile> m_selectedSystems = new ArrayList<>();

        public SystemSelection(SystemSelection a_cpy) {
            a_cpy.m_selectedSystems.forEach(sf -> m_selectedSystems.add(sf));
        }

        public SystemSelection() {
        }



        ArrayList<String> getSystemNames() {
            ArrayList<String> ret = new ArrayList<>();

            for (SystemNameFile snf : SystemNameFile.globalSystems()) {
                ret.add(snf.m_name);
            }
            return ret;
        }

        Iterable<SystemNameFile> getSystems() {
            return SystemNameFile.globalSystems();
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




        public void selectFileName(String a_fileName) {
            for (SystemNameFile snf : SystemNameFile.globalSystems()) {
                if (snf.m_file.equals(a_fileName)) {
                    if (!isSelected(snf)) {
                        m_selectedSystems.add(snf);
                    }
                }
            }
        }

        public void clearSelection() {
            m_selectedSystems.clear();
        }
    }






    interface DataListener {
        void onNewData(ExperimentRunData.BasicRunData a_rd, MapperView a_source);
    }

    public enum DoExperimentAction {
        None,
        Delete,
        Copy
    }

    public DoExperimentAction doExperiment(ImGuiWrapper a_imgui, DataListener a_newDataListener) {
        DoExperimentAction ret = DoExperimentAction.None;
        if (a_imgui.imgui().collapsingHeader("Experiment: " + m_name + "###Header" + m_id, 0)) {

            if (isRunningExperiment()) {
                a_imgui.pushDisableWidgets();
            }

            m_name = a_imgui.inputTextSingleLine("Name###Name" + m_id, m_name);

            {
                for (SystemNameFile snf : m_selectedSystem.getSystems()) {
                    boolean isSelected[] = {m_selectedSystem.isSelected(snf)};
                    if (a_imgui.imgui().checkbox(snf.m_name + "##" + m_id, isSelected)) {
                        m_selectedSystem.toogleSelection(snf);
                    }
                    if (!snf.isLastSystem()) {
                        a_imgui.imgui().sameLine(0, 10);
                    }
                }
            }

            {
                boolean[] initialMappnig = {m_useIntialMapping};
                if (a_imgui.imgui().checkbox("Use Initial Mapping##" + m_id, initialMappnig)) {
                    m_useIntialMapping = initialMappnig[0];
                }
            }

            {
                a_imgui.sameLine(0);
                boolean[] initialSetPerComponent = {m_initialSetPerComponent};
                if (a_imgui.imgui().checkbox("Initial Set Per Component##" + m_id, initialSetPerComponent)) {
                    m_initialSetPerComponent = initialSetPerComponent[0];
                }
            }

            m_initialSetSize = doRandomDoubleVariable(a_imgui, "Initial Set Size", m_initialSetSize);

            a_imgui.imgui().indent(3);
            if (a_imgui.collapsingHeader("Metrics##" + m_id, 0)) {

                final float boxWidth = a_imgui.imgui().getTextLineHeightWithSpacing() + 5;
                final float colWidth = a_imgui.calcTextSize("Child Count Lvl 0", false).getX() + 13 + 2 * boxWidth;

                int count = 0;
                for (MetricPair m : m_selectedMetrics.getMetricPairs()) {

                    if (m.m_relMetric != null) {
                        boolean[] isSelected = {m_selectedMetrics.isSelected(m.m_relMetric)};
                        if (a_imgui.imgui().checkbox("##" + m.getName() + m_id, isSelected)) {
                            m_selectedMetrics.toogle(m.m_relMetric);
                        }
                        if (a_imgui.imgui().isItemHovered(0) && a_imgui.beginTooltip()) {
                            a_imgui.text("Relative Linecount");
                            a_imgui.endTooltip();
                        }
                        // for some reason the first column gets a wierd offset
                        a_imgui.imgui().sameLine((count % 4) * colWidth + boxWidth, (count % 4) == 0 ? 11 : 0);
                    }

                    boolean[] isSelected = {m_selectedMetrics.isSelected(m.m_absMetric)};
                    if (a_imgui.imgui().checkbox(m.getName() + "##" + m_id, isSelected)) {
                        m_selectedMetrics.toogle(m.m_absMetric);
                    }
                    if (a_imgui.imgui().isItemHovered(0) && a_imgui.beginTooltip()) {
                        a_imgui.text("Absolute");
                        a_imgui.endTooltip();
                    }
                    count++;
                    if (count % 4 != 0) {
                        a_imgui.imgui().sameLine((count % 4) * colWidth, 0);
                    }

                }
                if (count % 4 != 0) {
                    a_imgui.imgui().newLine();
                }
            }
            a_imgui.imgui().indent(-3);


            if (a_imgui.button("Add Mapper##" + m_id, 0)) {
                m_experiments.add(new MapperView());
            }
            a_imgui.sameLine(0);
            if (a_imgui.button("Delete Mappers##" + m_id, 0)) {
                m_experiments.add(new MapperView());
            }

            if (isRunningExperiment()) {
                a_imgui.popDisableWidgets();
            }

            class ActionPair {
                ActionPair(DoExperimentAction a_action, MapperView a_mapper) {
                    m_action = a_action;
                    m_mapper = a_mapper;
                }
                DoExperimentAction m_action;
                MapperView m_mapper;
            }
            ArrayList<ActionPair> actions = new ArrayList<>();
            for (MapperView exv : m_experiments) {
                a_imgui.imgui().indent(3);
                actions.add(new ActionPair(exv.doExperiment(a_imgui, isRunningExperiment()), exv));
                a_imgui.imgui().indent(-3);
            }
            for (ActionPair ap : actions) {
                if (ap.m_action == DoExperimentAction.Delete) {
                    m_experiments.remove(ap.m_mapper);
                } else if (ap.m_action == DoExperimentAction.Copy) {
                    m_experiments.add(new MapperView(ap.m_mapper));
                }
            }


            if (!isRunningExperiment()) {


                if (a_imgui.button("Run Experiment##" + m_id, 0)) {
                    runExperiment(a_newDataListener);
                }

            } else {
                if (a_imgui.button("Stop Experiment##" + m_id, 0)) {
                    m_avgPerformance = 0;
                    m_avgCount = 0;
                    m_experiment.stop();
                    halt();
                }
            }

            a_imgui.sameLine(0);
            if (a_imgui.button("Copy Experiment##" + m_id, 0)) {
                ret = DoExperimentAction.Copy;
            }
            a_imgui.sameLine(0);
            if (a_imgui.button("Delete Experiment##" + m_id, 0)) {
                ret = DoExperimentAction.Delete;
            }
        }

        return ret;
    }

    private boolean isRunningExperiment() {
        return !(m_experiment == null || m_experiment.getState() == ExperimentRunner.State.Idle);
    }

    private void setExperiment(ExperimentRunner a_exr) {

        if (m_experiment != null && m_experiment.getState() == ExperimentRunner.State.Running) {
            m_experiment.stop();
            m_experiment = null;
        }

        m_selectedMetrics.clear();
        for (Metric exrMetric : a_exr.getMetrics()) {
            m_selectedMetrics.select(exrMetric);
        }
        m_selectedSystem.clearSelection();
        a_exr.getSystems().forEach(s -> m_selectedSystem.selectFileName(((FileBased)s).getFile()));

        m_useIntialMapping = a_exr.doUseInitialMapping();
        m_initialSetSize = a_exr.getInitialSetSize();
        m_initialSetPerComponent = a_exr.initialSetPerComponent();


        for (ExperimentRun ex : a_exr.getExperiments()) {
            m_experiments.add(new MapperView(ex));
        }



        m_name = a_exr.getName();
        if (m_name == null) {
            m_name = "";
        }
    }

    ExperimentRunner createExperiment() throws IOException {
        ExperimentRunner ret = null;
        ArrayList<se.lnu.siq.s4rdm3x.experiments.system.System> systems = new ArrayList<>();
        for (SystemNameFile snf : m_selectedSystem.getSelectedSystems()) {
            if (snf.m_file != null) {
                systems.add(new se.lnu.siq.s4rdm3x.experiments.system.FileBased(snf.m_file));
            }
        }

        ret = new ExperimentRunner(systems, m_selectedMetrics.getSelected(), getExperiments(), m_useIntialMapping, m_initialSetSize, m_initialSetPerComponent);
        ret.setName(m_name);

        return ret;
    }

    private Iterable<ExperimentRun> getExperiments() {
        ArrayList<ExperimentRun> ret = new ArrayList<>();
        for (MapperView exv : m_experiments) {
            ret.add(exv.createExperiment());
        }
        return ret;
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
