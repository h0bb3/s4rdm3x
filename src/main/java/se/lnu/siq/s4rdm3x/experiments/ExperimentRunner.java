package se.lnu.siq.s4rdm3x.experiments;

import javafx.scene.shape.Arc;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.experiments.metric.Metric;
import se.lnu.siq.s4rdm3x.experiments.system.System;
import se.lnu.siq.s4rdm3x.model.CGraph;

import java.text.SimpleDateFormat;
import java.util.*;

public class ExperimentRunner {
    protected Random m_rand = new Random();
    private RunListener m_listener = null;
    private ArrayList<ExperimentRun> m_experiments = new ArrayList<>();
    private ArrayList<System> m_suas = new ArrayList<>();
    private ArrayList<Metric> m_metrics = new ArrayList<>();
    private State m_state;  // this is the desired state
    private State m_currentState;   // this is the actual state

    private RandomDoubleVariable m_initialSetSize;
    private boolean m_initialSetPerComponent = false;

    private String m_name;
    private boolean m_useInitialMapping;


    public String getName() {
        return m_name;
    }

    public void setName(String a_name) {
        m_name = a_name;
    }

    public Iterable<System> getSystems() {
        return m_suas;
    }

    public Iterable<Metric> getMetrics() {
        return m_metrics;
    }

    public RandomDoubleVariable getInitialSetSize() {
        return m_initialSetSize;
    }

    public boolean doUseInitialMapping() {return m_useInitialMapping;}

    public Iterable<? extends ExperimentRun> getExperiments() {
        return m_experiments;
    }

    public boolean initialSetPerComponent() {
        return m_initialSetPerComponent;
    }

    public int getSystemCount() {
        return m_suas.size();
    }


    public enum State {
      Running,
      Stoping,
      Idle
    };

    public static class RandomBoolVariable {
        int m_value;
        boolean m_doGenerate;

        public RandomBoolVariable() {
            m_doGenerate = true;
            m_value = -1;
        }


        public RandomBoolVariable(boolean a_setValue) {
            m_doGenerate = false;
            m_value = a_setValue ? 1 : 0;
        }

        public RandomBoolVariable(RandomBoolVariable a_cpy) {
            m_value = a_cpy.m_value;
            m_doGenerate = a_cpy.m_doGenerate;
        }

        public boolean isRandom() {
            return m_doGenerate;
        }
        public boolean getValue() {
            return m_value == 1;
        }

        public boolean generate(Random a_rand) {
            if (m_doGenerate) {
                m_value = a_rand.nextBoolean() ? 1 : 0;
            }
            return getValue();
        }
    }

    public static class RandomIntVariable {
        private int m_value;
        private int m_min;
        private int m_max;

        public RandomIntVariable(int a_min, int a_max) {
            m_min = a_min;
            m_max = a_max;
            if (a_min > a_max) {
                throw new IllegalArgumentException("Min value larger than max value");
            }
        }

        public RandomIntVariable(RandomIntVariable a_cpy) {
            m_value = a_cpy.m_value;
            m_min = a_cpy.m_min;
            m_max = a_cpy.m_max;
        }

        public RandomIntVariable(int a_base) {
            m_min = m_max = a_base;
        }

        public int generate(Random a_rand) {
            m_value = m_min + a_rand.nextInt(m_max - m_min + 1);
            return m_value;
        }

        public int getMin() {
            return m_min;
        }

        public int getMax() {
            return m_max;
        }
    }

    public static class RandomDoubleVariable {
        double m_value;
        double m_base;
        double m_scale;


        public RandomDoubleVariable() {
            set(0.5, 0.5);
        }

        public RandomDoubleVariable(double a_base) {
            set(a_base, 0);
        }

        public RandomDoubleVariable(double a_base, double a_scale) {
            set(a_base, a_scale);
        }

        public RandomDoubleVariable(RandomDoubleVariable a_cpy) {
            m_value = a_cpy.m_value;
            m_base = a_cpy.m_base;
            m_scale = a_cpy.m_scale;
        }

        public void set(double a_base, double a_scale) {
            m_base = a_base;
            m_scale = a_scale;

            m_value = a_base - a_scale * 2; // generate something illegal
        }

        public double generate(Random a_rand) {
            m_value = m_base + ((a_rand.nextDouble() - 0.5) * 2) * m_scale;
            return m_value;
        }

        public double getValue() {
            return m_value;
        }

        public double getMin() {
            return m_base - m_scale;
        }

        public double getMax() {
            return m_base + m_scale;
        }

        public double getBase() {
            return m_base;
        }

        public double getScale() {
            return m_scale;
        }

        public void setMinMax(double a_min, double a_max) {
            m_scale = (a_max - a_min)  / 2.0;
            m_base = a_min + m_scale;
        }
    }

    public void stop() {
        m_state = State.Stoping;
        while(getState() == ExperimentRunner.State.Running) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
        }
    }

    public State getState() {
        return m_currentState;
    }

    public ExperimentRunner(Iterable<System> a_suas, Iterable<Metric> a_metrics, Iterable<ExperimentRun> a_experiments, boolean a_doUseInitialMapping, RandomDoubleVariable a_initialSetSize, boolean a_initialSetPerComponent) {
        a_suas.forEach(s -> m_suas.add(s));
        a_metrics.forEach(m -> m_metrics.add(m));
        a_experiments.forEach(e -> m_experiments.add(e));
        m_initialSetSize = a_initialSetSize;
        m_useInitialMapping = a_doUseInitialMapping;
        m_initialSetPerComponent = a_initialSetPerComponent;
    }

    public ExperimentRunner(ExperimentRunner a_toCopy, System a_singleSystem) {
        m_suas.add(a_singleSystem);
        a_toCopy.m_metrics.forEach(m -> m_metrics.add(m));
        a_toCopy.m_experiments.forEach(e -> m_experiments.add(e.clone()));
        m_initialSetSize = new RandomDoubleVariable(a_toCopy.m_initialSetSize);
        m_useInitialMapping = a_toCopy.m_useInitialMapping;
        m_initialSetPerComponent = a_toCopy.m_initialSetPerComponent;
        m_name = a_toCopy.getName();
    }

    public ExperimentRunner(ExperimentRunner a_toCopy) {
        a_toCopy.m_suas.forEach(s -> m_suas.add(s));
        a_toCopy.m_metrics.forEach(m -> m_metrics.add(m));
        a_toCopy.m_experiments.forEach(e -> m_experiments.add(e.clone()));
        m_initialSetSize = new RandomDoubleVariable(a_toCopy.m_initialSetSize);
        m_useInitialMapping = a_toCopy.m_useInitialMapping;
        m_initialSetPerComponent = a_toCopy.m_initialSetPerComponent;
        m_name = a_toCopy.getName();
    }

    public interface RunListener {
        public ExperimentRunData.BasicRunData OnRunInit(ExperimentRunData.BasicRunData a_rd, CGraph a_g, ArchDef a_arch);
        public void OnRunCompleted(ExperimentRunData.BasicRunData a_rd, CGraph a_g, ArchDef a_arch, ExperimentRun a_source);
    }

    public void setRunListener(RunListener a_listener) {
        m_listener = a_listener;
    }

    public void run(CGraph a_g) {

        class GraphArchitecturePair {
            public CGraph m_g;
            public ArchDef m_a;

            RandomDoubleVariable m_initialSetRatio;
        }

        HashMap<System, GraphArchitecturePair> loadedSystems = new HashMap<>();
        int i = 0;

        m_currentState = State.Running;
        m_state = State.Running;
        RandomDoubleVariable initialClustering = m_initialSetSize;
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        InitialSetGenerator setGenerator = new InitialSetGenerator();
        RandomDoubleVariable initialSetRatio;
        while(m_state == State.Running) {

            for (System sua : m_suas) {
                Metric prevMetric = null;
                for (Metric metric : m_metrics) {
                    for (int iterations = 0; iterations < 10 && m_state == State.Running; iterations++) {
                        ArchDef arch;

                        if (!loadedSystems.containsKey(sua)) {

                            GraphArchitecturePair gap = new GraphArchitecturePair();
                            gap.m_g = new CGraph();
                            sua.load(gap.m_g);
                            try {
                                gap.m_a = sua.createAndMapArch(gap.m_g);
                            } catch (System.NoMappedNodesException ex) {
                                // we just print some warnings and remove the offending components
                                for (ArchDef.Component c : ex.m_components) {
                                    java.lang.System.out.println("Warning: No nodes mapped to component: " + c.getName() + " in system: " + sua.getName() + " - removing component from analysis.");
                                    ex.m_arch.removeComponent(c);
                                }
                                gap.m_a = ex.m_arch;
                            }

                            loadedSystems.put(sua, gap);
                            a_g = gap.m_g;
                            arch = gap.m_a;

                            // we need to adjust the initial set generation so that we do not get a skewed distribution at the extremes
                            // e.g. if we have many arch components possibly many initial set ratios will generate this amount of nodes
                            gap.m_initialSetRatio = new RandomDoubleVariable(m_initialSetSize);
                            final int minInitialSet = m_initialSetPerComponent ? arch.getComponentCount() : 1;

                            int mappedNodeCount = arch.getMappedNodeCount(a_g.getNodes());
                            double min = (double)minInitialSet / (double)mappedNodeCount;
                            double max = (double)(mappedNodeCount) / (double)mappedNodeCount - 0.000000001;

                            if (gap.m_initialSetRatio.getMin() > min) {
                                min = gap.m_initialSetRatio.getMin();
                            }

                            if (gap.m_initialSetRatio.getMax() < max ) {
                                max = gap.m_initialSetRatio.getMax();
                            }

                            gap.m_initialSetRatio.setMinMax(min, max);

                            initialSetRatio = gap.m_initialSetRatio;

                        } else {
                            GraphArchitecturePair gap = loadedSystems.get(sua);
                            a_g = gap.m_g;
                            arch = gap.m_a;
                            initialSetRatio = gap.m_initialSetRatio;
                        }

                        // this is an optimization if we only have one metric we do not need to reassign it
                        if (prevMetric != metric) {
                            metric.assignMetric(arch.getMappedNodes(a_g.getNodes()));
                        }


                        if (m_useInitialMapping) {
                            // Set the initial set an initial set from architecture
                            sua.setInitialMapping(a_g, arch);
                        }
                        arch.cleanNodeClusters(a_g.getNodes(), false);
                        if (m_initialSetPerComponent) {
                            setGenerator.assignInitialClustersPerComponent(a_g, arch, initialSetRatio.generate(m_rand), metric, m_rand);
                        } else {
                            setGenerator.assignInitialClusters(a_g, arch, initialSetRatio.generate(m_rand), metric, m_rand);
                        }


                        for (ExperimentRun experiment : m_experiments) {

                            final ExperimentRunData.BasicRunData rd = experiment.createNewRunData(m_rand);
                            rd.m_metric = metric;
                            rd.m_system = sua;
                            rd.m_totalMapped = arch.getMappedNodeCount(a_g.getNodes());


                            rd.m_date = sdfDate.format(new Date());
                            rd.m_mapperName = experiment.getName();

                            arch.getClusteredNodes(a_g.getNodes(), ArchDef.Component.ClusteringType.Initial).forEach(n -> rd.addInitialClusteredNode(n));

                            rd.m_initialClusteringPercent = (double)rd.getInitialClusteringNodeCount() / (double)rd.m_totalMapped;

                            //rd.m_totalMapped = 0;
                            rd.m_totalManuallyClustered = 0;
                            rd.m_totalAutoWrong = 0;
                            rd.m_iterations = 0;
                            rd.m_totalFailedClusterings = 0;
                            rd.m_id = i;

                            if (m_listener != null) {
                                m_listener.OnRunInit(rd, a_g, arch);
                            }
                            long start = java.lang.System.nanoTime();

                            // we always run until we are finished even if we are stopped to avoid partial data sets.
                            while (!experiment.runClustering(a_g, arch)) {

                                // we now move the clustered nodes from autom/manual to initial
                                // this reflects an iterative mapping approach.
                                /*for (CNode a_n : arch.getMappedNodes(a_g.getNodes())) {
                                    ArchDef.Component c = arch.getClusteredComponent(a_n);
                                    if (c != null && c.getClusteringType(a_n) != ArchDef.Component.ClusteringType.Initial) {
                                        // TODO: this component could be wrong and should maye be corrected
                                        c.clusterToNode(a_n, ArchDef.Component.ClusteringType.Initial);
                                    }
                                }*/
                            }


                            rd.m_time = java.lang.System.nanoTime() - start;

                            if (m_listener != null) {
                                m_listener.OnRunCompleted(rd, a_g, arch, experiment);
                            }

                            arch.cleanNodeClusters(a_g.getNodes(), true);
                        }

                        i++;
                        prevMetric = metric;
                        // Needed?
                        metric.reassignMetric(arch.getMappedNodes(a_g.getNodes()));
                    }
                    if (m_state != State.Running) {
                        break;
                    }
                }

                if (m_state != State.Running) {
                    break;
                }
            }
        }

        m_currentState = State.Idle;
    }


}
