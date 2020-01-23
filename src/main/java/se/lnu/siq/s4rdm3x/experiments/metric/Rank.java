package se.lnu.siq.s4rdm3x.experiments.metric;


import jdk.jshell.spi.ExecutionControl;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Rank extends Metric {

    public String getName() {
        return "Rank";
    }

    public void assignMetric(Iterable<CNode> a_nodes) {
        final double precision = 0.0000001;

        int[] nodeCount = new int[1];
        HashMap<CNode, Double> ranks = new HashMap<>();
        HashMap<CNode, Integer> outDegrees = new HashMap<>();
        HashMap<CNode, ArrayList<CNode>> fanIn = new HashMap<CNode, ArrayList<CNode>>();

        a_nodes.forEach(n -> nodeCount[0]++);

        a_nodes.forEach(n -> ranks.put(n, 1.0 / nodeCount[0]));

        a_nodes.forEach(n -> outDegrees.put(n, calcOutDegree(n, a_nodes)));
        a_nodes.forEach(n -> fanIn.put(n, getFanIn(n, a_nodes)));




        while (compute(ranks, outDegrees, fanIn) > precision);

        for (CNode n : ranks.keySet()) {
            n.setMetric(getName(), ranks.get(n));
        }
    }

    private ArrayList<CNode> getFanIn(CNode a_n, Iterable<CNode> a_nodes) {
        ArrayList<CNode> ret = new ArrayList<>();
        for (CNode other : a_nodes) {
            if (a_nodes != a_n) {
                for (int i = 0; i < other.getDependencyCount(a_n); i++) {
                    ret.add(other);
                }
            }
        }

        return ret;
    }

    private int calcOutDegree(CNode a_n, Iterable<CNode> a_nodes) {
        int outDegree = 0;
        for (CNode other : a_nodes) {
            if (other != a_n) {
                outDegree += a_n.getDependencyCount(other);
            }
        }

        return outDegree;
    }

    private double compute(HashMap<CNode, Double> a_ranks, Map<CNode, Integer> a_outDegrees, Map<CNode, ArrayList<CNode>> a_fanIn) {
        final double dampingFactor = 0.85;
        final int nodeCount = a_ranks.size();
        double dampingTerm = (1 - dampingFactor) / nodeCount;
        double [] newRanks = new double[nodeCount];
        double danglingRank = 0;

        int ix = 0;
        for (CNode n : a_ranks.keySet()) {

            double sum = 0;
            for (CNode other : a_fanIn.get(n)) {
                sum += a_ranks.get(other) / a_outDegrees.get(other);
            }
            newRanks[ix] = dampingTerm + dampingFactor * sum;

            if (a_outDegrees.get(n) == 0) {
                danglingRank += a_ranks.get(n);
            }
            ix++;
        }

        danglingRank *= dampingFactor / nodeCount;

        double normDiff = 0;
        ix = 0;
        for (CNode n : a_ranks.keySet()) {

            double currentRank = a_ranks.get(n);
            double newRank = newRanks[ix] + danglingRank;
            normDiff += Math.abs(newRank - currentRank);
            a_ranks.replace(n, newRank);
            ix++;
        }

        return normDiff;
    }

    public void reassignMetric(Iterable<CNode> a_nodes) {

    }
}
