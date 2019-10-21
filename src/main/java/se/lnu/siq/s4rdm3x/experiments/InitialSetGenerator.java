package se.lnu.siq.s4rdm3x.experiments;

import javafx.scene.shape.Arc;
import se.lnu.siq.s4rdm3x.experiments.metric.Metric;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.Selector;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;

import java.util.*;

public class InitialSetGenerator {

    public void assignInitialClusters(CGraph a_g, ArchDef a_arch, double a_percentage, Metric a_metric, Random a_rand) {
        assignInitialClusters(a_g, a_arch, a_percentage, a_metric, a_rand, 1);
    }

    private void assignInitialClusters(CGraph a_g, ArchDef a_arch, double a_percentage, Metric a_metric, Random a_rand, int a_minNodesToCluster) {
        ArrayList<CNode> nodes = new ArrayList<>();
        final int[] initialMappingCount = {0};
        a_arch.getMappedNodes(a_g.getNodes()).forEach(a_n -> {
            if (isValidForInitialSet(a_n, a_arch.getMappedComponent(a_n))) {
                nodes.add(a_n);
            } else {
                initialMappingCount[0]++;
            }
        });

        int nodeCount = getNodesToAdd(nodes.size(), a_percentage, initialMappingCount[0], a_minNodesToCluster);

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

    private int getNodesToAdd(int a_availableNodes, double a_percentage, int a_initiallyMapped, int a_minNodesToCluster) {
        if (a_availableNodes == 0) {
            return 0;
        }

        int nodeCount = (int) ((double) a_availableNodes * a_percentage);
        if (nodeCount < a_minNodesToCluster && a_initiallyMapped == 0) {
            // we should at least have one initially mapped
            nodeCount = a_minNodesToCluster;
        } else if (nodeCount >= a_availableNodes) {
            // we should have at least one node to cluster
            nodeCount = a_availableNodes - 1;
        }

        return nodeCount;
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

    private boolean isValidForInitialSet(CNode a_node, ArchDef.Component a_mapping) {
        return a_mapping.isMappedTo(a_node) && a_mapping.getClusteringType(a_node) != ArchDef.Component.ClusteringType.Initial;
    }


    public void assignInitialClustersPerComponent(CGraph a_g, ArchDef a_arch, double a_percentage, Metric a_metric, Random a_rand) {

        // TODO: a better approach is to make he initial assignment as normal and then make sure that all components have at least one clustering
        // this would achieve a better distribution of the initial set and also handle higher percentage cases better.

        assignInitialClusters(a_g, a_arch, a_percentage, a_metric, a_rand, a_arch.getComponentCount());

        class NodesPerComponent {
            NodesPerComponent(ArchDef.Component a_component, ArrayList<CNode> a_nodes) {
                m_component = a_component;
                m_nodes = a_nodes;
            }
            ArchDef.Component m_component;
            ArrayList<CNode> m_nodes;
        }
        ArrayList<NodesPerComponent> nodesPerComponent = new ArrayList<>();

        for (ArchDef.Component component : a_arch.getComponents()) {
            ArrayList<CNode> nodes = new ArrayList<>();

            // all all nodes that are clustered to the component to the list
            a_g.getNodes(a_node -> a_arch.getClusteredComponent(a_node) == component).forEach(n -> nodes.add(n));

            nodesPerComponent.add(new NodesPerComponent(component, nodes));
        }

        // redistribute from the large sets to the 0 sets
        nodesPerComponent.sort((a, b) -> {return a.m_nodes.size() - b.m_nodes.size();});
        while (nodesPerComponent.get(0).m_nodes.size() == 0) {
            NodesPerComponent toNpc = nodesPerComponent.get(0);
            NodesPerComponent fromNpc = nodesPerComponent.get(nodesPerComponent.size() - 1);
            // remove one random node from the largest set
            int randIx = a_rand.nextInt(fromNpc.m_nodes.size());
            fromNpc.m_component.removeClustering(fromNpc.m_nodes.get(randIx));
            fromNpc.m_nodes.remove(randIx);

            // get one random node from the mapped nodes and cluster it
            ArrayList<CNode> workingSet = getWorkingSet(a_g.getNodes(n -> a_arch.getMappedComponent(n) == toNpc.m_component), 1, a_metric, a_rand);
            toNpc.m_component.clusterToNode(workingSet.get(0), ArchDef.Component.ClusteringType.Initial);
            toNpc.m_nodes.add(workingSet.get(0));

            nodesPerComponent.sort((a, b) -> {return a.m_nodes.size() - b.m_nodes.size();});
        }




        /*for (ArchDef.Component component : a_arch.getComponents()) {
            int initialMappingCount = 0;

            ArrayList<CNode> nodes = new ArrayList<>();
            for (CNode n : a_g.getNodes()) {

                if (isValidForInitialSet(n, component)) {
                    nodes.add(n);
                } else if (a_arch.getMappedComponent(n) == component) {
                    initialMappingCount++;
                }
            }

            int nodeCount = getNodesToAdd(nodes.size(), a_percentage, initialMappingCount, 1);

            ArrayList<CNode> workingSet = getWorkingSet(nodes, nodeCount, a_metric, a_rand);

            for (CNode n : workingSet) {
                component.clusterToNode(n, ArchDef.Component.ClusteringType.Initial);
            }
        }*/
    }

}
