package se.lnu.siq.s4rdm3x.experiments;

import se.lnu.siq.s4rdm3x.experiments.metric.Metric;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

public class InitialSetGenerator {
    public void assignInitialClusters(CGraph a_g, ArchDef a_arch, double a_percentage, Metric a_metric, Random a_rand) {
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
        ArrayList<CNode> workingSet = getWorkingSet(nodes, nodeCount, a_metric, a_rand);

        // we may have added too many nodes (i.e. the last batch may be bigger)
        while (workingSet.size() > nodeCount) {
            int firstBatchSize = getFirstBatchSize(workingSet, a_metric);
            workingSet.remove(Math.abs(a_rand.nextInt()) % firstBatchSize);
        }

        for (CNode n : workingSet) {
            ArchDef.Component component = a_arch.getMappedComponent(n);
            component.clusterToNode(n, ArchDef.Component.ClusteringType.Initial);
        }
    }

    private int getFirstBatchSize(ArrayList<CNode> a_set, Metric a_metric) {
        int firstBatchSize = 1;
        double firstBatchFan = a_metric.getMetric(a_set.get(0));
        while(firstBatchSize < a_set.size() && firstBatchFan == a_metric.getMetric(a_set.get(firstBatchSize))) {
            firstBatchSize++;
        }

        return firstBatchSize;
    }

    private ArrayList<CNode> getWorkingSet(Iterable<CNode> a_nodes, int a_nodesToAdd, Metric a_metric, Random a_rand) {
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
            workingSet.remove(Math.abs(a_rand.nextInt()) % firstBatchSize);
        }

        return workingSet;
    }

    ArrayList<CNode> getWorkingSetTestHelper(Iterable<CNode> a_nodes, int a_nodesToAdd, Metric a_metric) {
        return getWorkingSet(a_nodes, a_nodesToAdd, a_metric, new Random());
    }

}
