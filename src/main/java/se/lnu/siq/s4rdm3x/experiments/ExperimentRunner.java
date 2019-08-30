package se.lnu.siq.s4rdm3x.experiments;

import javafx.scene.shape.Arc;
import se.lnu.siq.s4rdm3x.model.Selector;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.util.FanInCache;
import se.lnu.siq.s4rdm3x.experiments.metric.Metric;
import se.lnu.siq.s4rdm3x.experiments.system.System;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.util.SystemModelReader;

import java.text.SimpleDateFormat;
import java.util.*;

public abstract class ExperimentRunner {
    protected boolean m_doUseManualmapping;
    protected Random m_rand = new Random();
    private RunListener m_listener = null;
    private ArrayList<System> m_suas = new ArrayList<>();
    private ArrayList<Metric> m_metrics = new ArrayList<>();
    private State m_state;  // this is the desired state
    private State m_currentState;   // this is the actual state
    private RandomDoubleVariable m_initialSetSize;
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

    public boolean doUseManualmapping() {
        return m_doUseManualmapping;
    }

    public RandomDoubleVariable getInitialSetSize() {
        return m_initialSetSize;
    }

    public boolean useInitialMapping() {return m_useInitialMapping;}


    private static class GraphArchitecturePair {
        public CGraph m_g;
        public ArchDef m_a;
        ArrayList<SystemModelReader.Mapping> m_initialMapping;
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

    public ExperimentRunner(Iterable<System> a_suas, Iterable<Metric> a_metrics, boolean a_doUseManualmapping, boolean a_doUseInitialMapping, RandomDoubleVariable a_initialSetSize) {
        a_suas.forEach(s -> m_suas.add(s));
        a_metrics.forEach(m -> m_metrics.add(m));
        m_initialSetSize = a_initialSetSize;
        m_doUseManualmapping = a_doUseManualmapping;
        m_useInitialMapping = a_doUseInitialMapping;
    }

    public ExperimentRunner(System a_sua, Metric a_metric, boolean a_doUseManualmapping, boolean a_doUseInitialMapping, RandomDoubleVariable a_initialSetSize) {
        m_suas .add(a_sua);
        m_metrics.add(a_metric);
        m_initialSetSize = a_initialSetSize;
        m_doUseManualmapping = a_doUseManualmapping;
        m_useInitialMapping = a_doUseInitialMapping;
    }

    public interface RunListener {
        public ExperimentRunData.BasicRunData OnRunInit(ExperimentRunData.BasicRunData a_rd, CGraph a_g, ArchDef a_arch);
        public void OnRunCompleted(ExperimentRunData.BasicRunData a_rd, CGraph a_g, ArchDef a_arch);
    }

    public abstract ExperimentRunner clone();

    public void setRunListener(RunListener a_listener) {
        m_listener = a_listener;
    }

    public void run(CGraph a_g) {

        HashMap<System, GraphArchitecturePair> loadedSystems = new HashMap<>();
        int i = 0;

        FanInCache fic = null;


        m_currentState = State.Running;
        m_state = State.Running;
        RandomDoubleVariable initialClustering = m_initialSetSize;
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
                            gap.m_a = sua.createAndMapArch(gap.m_g);
                            loadedSystems.put(sua, gap);
                            a_g = gap.m_g;
                            arch = gap.m_a;

                        } else {
                            GraphArchitecturePair gap = loadedSystems.get(sua);
                            a_g = gap.m_g;
                            arch = gap.m_a;
                        }

                        // this is an optimization if we only have one metric we do not need to reassign it
                        if (prevMetric != metric) {
                            metric.assignMetric(arch.getMappedNodes(a_g.getNodes()));
                        }

                        final ExperimentRunData.BasicRunData rd = createNewRunData(m_rand);
                        rd.m_metric = metric;
                        rd.m_system = sua;
                        rd.m_initialClusteringPercent = initialClustering.generate(m_rand);
                        rd.m_totalMapped = arch.getMappedNodeCount(a_g.getNodes());

                        rd.m_date = sdfDate.format(new Date());

                        arch.cleanNodeClusters(a_g.getNodes());


                        if (m_useInitialMapping) {
                            // Set the initial set an initial set from architecture
                            sua.setInitialMapping(a_g, arch);
                        }
                        assignInitialClusters(a_g, arch, rd.m_initialClusteringPercent, metric);

                        //assignInitialClustersPerComponent(a_g, arch, rd.m_initialClusteringPercent);

                        arch.getClusteredNodes(a_g.getNodes()).forEach(n -> rd.addInitialClusteredNode(n));
                        rd.m_totalMapped = arch.getMappedNodeCount(a_g.getNodes());


                        //rd.m_totalMapped = 0;
                        rd.m_totalManuallyClustered = 0;
                        rd.m_totalAutoWrong = 0;
                        rd.m_iterations = 0;
                        rd.m_totalFailedClusterings = 0;
                        rd.m_id = i;

                        if (fic == null) {
                            fic = new FanInCache(arch.getMappedNodes(a_g.getNodes()));
                        }

                        if (m_listener != null) {
                            m_listener.OnRunInit(rd, a_g, arch);
                        }
                        long start = java.lang.System.nanoTime();
                        while (!runClustering(a_g, fic, arch));  // we always run until we are finished even if we are stopped to avoid partial data sets.
                        rd.m_time = java.lang.System.nanoTime() - start;

                        arch.getClusteredNodes(a_g.getNodes(), ArchDef.Component.ClusteringType.Automatic).forEach(n -> rd.addAutoClusteredNode(n));

                        if (m_listener != null) {
                            m_listener.OnRunCompleted(rd, a_g, arch);
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

    protected abstract ExperimentRunData.BasicRunData createNewRunData(Random m_rand);

    protected abstract boolean runClustering(CGraph a_g, FanInCache fic, ArchDef arch);


    private void assignInitialClusters(CGraph a_g, ArchDef a_arch, double a_percentage, Metric a_metric) {
        ArrayList<CNode> nodes = new ArrayList<>();
        final int[] initialMappingCount = {0};
        a_arch.getMappedNodes(a_g.getNodes()).forEach(a_n -> {
            ArchDef.Component c = a_arch.getClusteredComponent(a_n);
            // there may be initial clusterings already set here so don't use them.
            if (c == null || c.getClusteringType(a_n) != ArchDef.Component.ClusteringType.Initial) {
                nodes.add(a_n);
            } else {
                initialMappingCount[0]++;
            }
        });

        int nodeCount = (int) ((double) nodes.size() * a_percentage);
        if (nodeCount <= 0 && initialMappingCount[0] == 0) {
            nodeCount = 1;
        }
        ArrayList<CNode> workingSet = getWorkingSet(nodes, nodeCount, a_metric);

        // we may have added too many nodes (i.e. the last batch may be bigger)
        while (workingSet.size() > nodeCount) {
            int firstBatchSize = getFirstBatchSize(workingSet, a_metric);
            workingSet.remove(Math.abs(m_rand.nextInt()) % firstBatchSize);
        }

        for (CNode n : workingSet) {
            ArchDef.Component component = a_arch.getMappedComponent(n);
            component.clusterToNode(n, ArchDef.Component.ClusteringType.Initial);
        }
    }

    /*private void assignInitialClustersPerComponent(CGraph a_g, ArchDef a_arch, double a_percentage, Metric a_metric) {
        // OBS this assigns a number of classes per component, this is not actually that realistic
        for (ArchDef.Component component : a_arch.getComponents()) {

            ArrayList<CNode> nodes = new ArrayList<>();
            for (CNode n : a_g.getNodes()) {
                if (component.isMappedTo(n)) {
                    nodes.add(n);
                }
            }

            int nodeCount = (int) ((double) nodes.size() * a_percentage);
            if (nodeCount <= 0) {
                nodeCount = 1;
            }

            ArrayList<CNode> workingSet = getWorkingSet(nodes, nodeCount, a_metric);

            for (CNode n : workingSet) {
                component.clusterToNode(n, ArchDef.Component.ClusteringType.Initial);
            }
        }
    }*/



    private int getFirstBatchSize(ArrayList<CNode> a_set, Metric a_metric) {
        int firstBatchSize = 1;
        double firstBatchFan = a_metric.getMetric(a_set.get(0));
        while(firstBatchSize < a_set.size() && firstBatchFan == a_metric.getMetric(a_set.get(firstBatchSize))) {
            firstBatchSize++;
        }

        return firstBatchSize;
    }

    private ArrayList<CNode> getWorkingSet(Iterable<CNode> a_nodes, int a_nodesToAdd, Metric a_metric) {
        // this sorts to lowest first
        ArrayList<CNode> sortedNodes = new ArrayList<>();
        a_nodes.forEach(a_n -> {sortedNodes.add(a_n);});
        sortedNodes.sort(Comparator.comparingDouble(a_n -> {
            return a_metric.getMetric(a_n);
        }));

        // things can have the same metric so we need to count this
        ArrayList<CNode> workingSet = new ArrayList<>();
        double  currentMetric = a_metric.getMetric(sortedNodes.get(sortedNodes.size() - 1));
        int ix = sortedNodes.size() - 1;
        int count = 0;
        while(ix >= 0 && count < a_nodesToAdd) {

            if (currentMetric != a_metric.getMetric(sortedNodes.get(ix))) {
                currentMetric = a_metric.getMetric(sortedNodes.get(ix));
                count = sortedNodes.size() - ix - 1;  // we have completed the whole batch (at ix - 1) with the same metric
            }
            ix--;
        }
        if (ix >= 0) {
            ix += 2;   // we need to move one index up 2 positions as this is the last index at the valid count.
        } else {
            ix = 0; // we went to the end
        }

        for (; ix < sortedNodes.size(); ix++) {
            workingSet.add(sortedNodes.get(ix));
        }

        // we may have added too many nodes (i.e. the last batch may be bigger)
        while (workingSet.size() > a_nodesToAdd) {
            int firstBatchSize = getFirstBatchSize(workingSet, a_metric);
            workingSet.remove(Math.abs(m_rand.nextInt()) % firstBatchSize);
        }

        return workingSet;
    }

    ArrayList<CNode> getWorkingSetTestHelper(Iterable<CNode> a_nodes, int a_nodesToAdd, Metric a_metric) {
        return getWorkingSet(a_nodes, a_nodesToAdd, a_metric);
    }


}
