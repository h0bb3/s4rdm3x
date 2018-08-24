package se.lnu.siq.s4rdm3x.experiments;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;
import se.lnu.siq.s4rdm3x.cmd.util.FanInCache;
import se.lnu.siq.s4rdm3x.experiments.metric.Metric;
import se.lnu.siq.s4rdm3x.experiments.system.System;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

public class ExperimentRunner {
    private final String m_metricTag = "metric";
        protected Random m_rand = new Random();
    private RunListener m_listener = null;
    private System m_sua;
    private Metric m_metric;

    public ExperimentRunner(System a_sua, Metric a_metric) {
        m_sua = a_sua;
        m_metric = a_metric;
    }

    public interface RunListener {
        BasicRunData OnRunInit(BasicRunData a_rd, Graph a_g, HuGMe.ArchDef a_arch);
        void OnRunCompleted(BasicRunData a_rd, Graph a_g, HuGMe.ArchDef a_arch);
    }

    public static class BasicRunData {
        public String m_metric;
        public long m_time;
        public int m_id;
        public double m_omega;
        public double m_phi;
        public double m_initialClusteringPercent;
        public int m_iterations;
        public int m_totalManuallyClustered;
        public int m_totalAutoClustered;
        public int m_totalAutoWrong;
        public int m_totalFailedClusterings;
    }

    public void setRunListener(RunListener a_listener) {
        m_listener = a_listener;
    }

    public void run(Graph a_g) {

        int i = 0;

        FanInCache fic = null;

        if (!m_sua.load(a_g)) {
            return;
        }
        HuGMe.ArchDef arch = m_sua.createAndMapArch(a_g);



        while(true) {

            m_metric.assignMetric(a_g, arch);

            BasicRunData rd = new BasicRunData();
            rd.m_metric = m_metric.getName();
            rd.m_initialClusteringPercent = m_rand.nextDouble();
            rd.m_phi = m_rand.nextDouble();
            rd.m_omega = m_rand.nextDouble();

            arch.cleanNodeClusters(a_g);
            assignInitialClusters(a_g, arch, rd.m_initialClusteringPercent);



            //rd.m_totalMapped = 0;
            rd.m_totalManuallyClustered = 0;
            rd.m_totalAutoClustered = 0;
            rd.m_totalAutoWrong = 0;
            rd.m_iterations = 0;
            rd.m_totalFailedClusterings = 0;
            rd.m_id = i;

            if (fic == null) {
                fic = new FanInCache(arch.getMappedNodes(a_g.getNodeSet()));
            }

            if (m_listener != null) {
                rd = m_listener.OnRunInit(rd, a_g, arch);
            }
            while(true) {
                HuGMe c = new HuGMe(rd.m_omega, rd.m_phi, true, arch, fic);
                long start = java.lang.System.nanoTime();
                c.run(a_g);
                rd.m_time = java.lang.System.nanoTime() - start;

                rd.m_totalManuallyClustered += c.m_manuallyMappedNodes;
                rd.m_totalAutoClustered += c.m_automaticallyMappedNodes;
                rd.m_totalAutoWrong  += c.m_autoWrong;
                rd.m_totalFailedClusterings  += c.m_failedMappings;

                if (c.m_automaticallyMappedNodes + c.m_manuallyMappedNodes == 0) {
                    break;
                }

                rd.m_iterations++;
            }

            if (m_listener != null) {
                m_listener.OnRunCompleted(rd, a_g, arch);
            }

            i++;
        }
    }





    private void assignInitialClusters(Graph a_g, HuGMe.ArchDef a_arch, double a_percentage) {
        ArrayList<Node> sortedNodes = new ArrayList<>();
        for (Node n : a_g.getEachNode()) {
            if (a_arch.getMappedComponent(n) != null) {
                sortedNodes.add(n);
            }
        }

        // this sorts to lowest first
        sortedNodes.sort(Comparator.comparingDouble(a_n -> {
            return m_metric.getMetric(a_n);
        }));

        int nodeCount = (int) ((double) sortedNodes.size() * a_percentage);
        if (nodeCount <= 0) {
            nodeCount = 1;
        }

        ArrayList<Node> workingSet = getWorkingSet(sortedNodes, nodeCount);

        // we may have added too many nodes (i.e. the last batch may be bigger)
        while (workingSet.size() > nodeCount) {
            int firstBatchSize = getFirstBatchSize(workingSet);
            workingSet.remove(Math.abs(m_rand.nextInt()) % firstBatchSize);
        }

        for (Node n : workingSet) {
            HuGMe.ArchDef.Component component = a_arch.getMappedComponent(n);
            component.clusterToNode(n);
        }
    }

    private void assignInitialClustersPerComponent(Graph a_g, HuGMe.ArchDef a_arch, double a_percentage) {
        // OBS this assigns a number of classes per component, this is not actually that realistic
        for (HuGMe.ArchDef.Component component : a_arch.getComponents()) {

            ArrayList<Node> sortedNodes = new ArrayList<>();
            for (Node n : a_g.getEachNode()) {
                if (component.isMappedTo(n)) {
                    sortedNodes.add(n);
                }
            }

            // this sorts to lowest first
            sortedNodes.sort(Comparator.comparingDouble(a_n -> {
                return m_metric.getMetric(a_n);
            }));

            int nodeCount = (int) ((double) sortedNodes.size() * a_percentage);
            if (nodeCount <= 0) {
                nodeCount = 1;
            }

            ArrayList<Node> workingSet = getWorkingSet(sortedNodes, nodeCount);

            // we may have added too many nodes (i.e. the last batch may be bigger)
            while (workingSet.size() > nodeCount) {
                int firstBatchSize = getFirstBatchSize(workingSet);
                workingSet.remove(Math.abs(m_rand.nextInt()) % firstBatchSize);
            }

            for (Node n : workingSet) {
                component.clusterToNode(n);
            }
        }
    }



    private int getFirstBatchSize(ArrayList<Node> a_set) {
        int firstBatchSize = 1;
        double firstBatchFan = m_metric.getMetric(a_set.get(0));
        while(firstBatchSize < a_set.size() && firstBatchFan == m_metric.getMetric(a_set.get(firstBatchSize))) {
            firstBatchSize++;
        }

        return firstBatchSize;
    }

    private ArrayList<Node> getWorkingSet(ArrayList<Node> a_sortedList, int nodesToAdd) {
        // things can have the same metric so we need to count this
        ArrayList<Node> workingSet = new ArrayList<>();
        double  currentMetric = m_metric.getMetric(a_sortedList.get(a_sortedList.size() - 1));
        int ix = a_sortedList.size() - 1;
        int count = 0;
        while(ix >= 0 && count < nodesToAdd) {

            if (currentMetric != m_metric.getMetric(a_sortedList.get(ix))) {
                currentMetric = m_metric.getMetric(a_sortedList.get(ix));
                count = a_sortedList.size() - ix - 1;  // we have completed the whole batch (at ix - 1) with the same metric
            }
            ix--;
        }
        if (ix >= 0) {
            ix += 2;   // we need to move one index up 2 positions as this is the last index at the valid count.
        } else {
            ix = 0; // we went to the end
        }

        for (; ix < a_sortedList.size(); ix++) {
            workingSet.add(a_sortedList.get(ix));
        }

        return workingSet;
    }


}
