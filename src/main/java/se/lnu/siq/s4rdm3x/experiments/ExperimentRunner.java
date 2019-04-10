package se.lnu.siq.s4rdm3x.experiments;

import se.lnu.siq.s4rdm3x.experiments.metric.Rand;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.HuGMe;
import se.lnu.siq.s4rdm3x.model.cmd.util.FanInCache;
import se.lnu.siq.s4rdm3x.experiments.metric.Metric;
import se.lnu.siq.s4rdm3x.experiments.system.System;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;

public abstract class ExperimentRunner {
    protected Random m_rand = new Random();
    private RunListener m_listener = null;
    private System m_sua;
    private Metric m_metric;
    private State m_state;

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

        public RandomBoolVariable(boolean a_value) {
            m_value = a_value ? 1 : 0;
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

        public void setInterval(double a_min, double a_max) {
            m_scale = a_max - a_min / 2;
            m_base = a_min + m_scale;
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
    }

    public void stop() {
        if (m_state == State.Running) {
            m_state = State.Stoping;
        }
    }

    public State getState() {
        return m_state;
    }


    public ExperimentRunner(System a_sua, Metric a_metric) {
        m_sua = a_sua;
        m_metric = a_metric;
    }

    public interface RunListener {
        public ExperimentRunData.BasicRunData OnRunInit(ExperimentRunData.BasicRunData a_rd, CGraph a_g, ArchDef a_arch);
        public void OnRunCompleted(ExperimentRunData.BasicRunData a_rd, CGraph a_g, ArchDef a_arch);
    }



    public void setRunListener(RunListener a_listener) {
        m_listener = a_listener;
    }

    public void run(CGraph a_g) {

        int i = 0;

        FanInCache fic = null;

        if (!m_sua.load(a_g)) {
            return;
        }
        ArchDef arch = m_sua.createAndMapArch(a_g);


        m_metric.assignMetric(arch.getMappedNodes(a_g.getNodes()));

        m_state = State.Running;
        RandomDoubleVariable initialClustering = new RandomDoubleVariable(0.1, 0.1);
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        while(m_state == State.Running) {

            ExperimentRunData.BasicRunData rd = createNewRunData(m_rand);
            rd.m_metric = m_metric;
            rd.m_system = m_sua.getName();
            rd.m_initialClusteringPercent = initialClustering.generate(m_rand);

            rd.m_initialClustered = arch.getClusteredNodeCount(a_g.getNodes());
            rd.m_totalMapped = arch.getMappedNodeCount(a_g.getNodes());

            rd.m_date = sdfDate.format(new Date());

            arch.cleanNodeClusters(a_g.getNodes());
            assignInitialClusters(a_g, arch, rd.m_initialClusteringPercent);

            //assignInitialClustersPerComponent(a_g, arch, rd.m_initialClusteringPercent);

            rd.m_initialClustered = arch.getClusteredNodeCount(a_g.getNodes());
            rd.m_totalMapped = arch.getMappedNodeCount(a_g.getNodes());


            //rd.m_totalMapped = 0;
            rd.m_totalManuallyClustered = 0;
            rd.m_totalAutoClustered = 0;
            rd.m_totalAutoWrong = 0;
            rd.m_iterations = 0;
            rd.m_totalFailedClusterings = 0;
            rd.m_id = i;

            if (fic == null) {
                fic = new FanInCache(arch.getMappedNodes(a_g.getNodes()));
            }

            if (m_listener != null) {
                rd = m_listener.OnRunInit(rd, a_g, arch);
            }
            while(m_state == State.Running) {
                if (runClustering(a_g, fic, arch)) break;
            }

            if (m_listener != null) {
                m_listener.OnRunCompleted(rd, a_g, arch);
            }

            i++;
            m_metric.reassignMetric(arch.getMappedNodes(a_g.getNodes()));
        }

        m_state = State.Idle;
    }

    protected abstract ExperimentRunData.BasicRunData createNewRunData(Random m_rand);

    protected abstract boolean runClustering(CGraph a_g, FanInCache fic, ArchDef arch);


    private void assignInitialClusters(CGraph a_g, ArchDef a_arch, double a_percentage) {
        ArrayList<CNode> nodes = new ArrayList<>();
        a_arch.getMappedNodes(a_g.getNodes()).forEach(a_n -> nodes.add(a_n));

        int nodeCount = (int) ((double) nodes.size() * a_percentage);
        if (nodeCount <= 0) {
            nodeCount = 1;
        }
        ArrayList<CNode> workingSet = getWorkingSet(nodes, nodeCount);

        // we may have added too many nodes (i.e. the last batch may be bigger)
        while (workingSet.size() > nodeCount) {
            int firstBatchSize = getFirstBatchSize(workingSet);
            workingSet.remove(Math.abs(m_rand.nextInt()) % firstBatchSize);
        }

        for (CNode n : workingSet) {
            ArchDef.Component component = a_arch.getMappedComponent(n);
            component.clusterToNode(n, ArchDef.Component.ClusteringType.Initial);
        }
    }

    private void assignInitialClustersPerComponent(CGraph a_g, ArchDef a_arch, double a_percentage) {
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

            ArrayList<CNode> workingSet = getWorkingSet(nodes, nodeCount);

            for (CNode n : workingSet) {
                component.clusterToNode(n, ArchDef.Component.ClusteringType.Initial);
            }
        }
    }



    private int getFirstBatchSize(ArrayList<CNode> a_set) {
        int firstBatchSize = 1;
        double firstBatchFan = m_metric.getMetric(a_set.get(0));
        while(firstBatchSize < a_set.size() && firstBatchFan == m_metric.getMetric(a_set.get(firstBatchSize))) {
            firstBatchSize++;
        }

        return firstBatchSize;
    }

    private ArrayList<CNode> getWorkingSet(Iterable<CNode> a_nodes, int a_nodesToAdd) {
        // this sorts to lowest first
        ArrayList<CNode> sortedNodes = new ArrayList<>();
        a_nodes.forEach(a_n -> {sortedNodes.add(a_n);});
        sortedNodes.sort(Comparator.comparingDouble(a_n -> {
            return m_metric.getMetric(a_n);
        }));

        // things can have the same metric so we need to count this
        ArrayList<CNode> workingSet = new ArrayList<>();
        double  currentMetric = m_metric.getMetric(sortedNodes.get(sortedNodes.size() - 1));
        int ix = sortedNodes.size() - 1;
        int count = 0;
        while(ix >= 0 && count < a_nodesToAdd) {

            if (currentMetric != m_metric.getMetric(sortedNodes.get(ix))) {
                currentMetric = m_metric.getMetric(sortedNodes.get(ix));
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
            int firstBatchSize = getFirstBatchSize(workingSet);
            workingSet.remove(Math.abs(m_rand.nextInt()) % firstBatchSize);
        }

        return workingSet;
    }

    ArrayList<CNode> getWorkingSetTestHelper(Iterable<CNode> a_nodes, int a_nodesToAdd) {
        return getWorkingSet(a_nodes, a_nodesToAdd);
    }


}
