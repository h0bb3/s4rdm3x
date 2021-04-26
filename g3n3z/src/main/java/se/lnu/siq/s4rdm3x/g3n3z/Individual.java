package se.lnu.siq.s4rdm3x.g3n3z;

import org.graalvm.compiler.lir.CompositeValue;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.HuGMe;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.MapperBase;
import se.lnu.siq.s4rdm3x.stats;

import java.util.Arrays;
import java.util.Random;

public class Individual implements Comparable<Individual> {

    private final CGraph m_graph;
    private final ArchDef m_arch;
    private Random m_rand = null;
    private final long m_seed;
    private double [] m_f1Scores;
    private double m_medianF1Score; // this is an optimization
    private int m_eliteGeneration;

    //private Individual m_p1;
    //private Individual m_p2;

    private MapperBase.DependencyWeights m_weights;

    public Individual(CGraph a_toCopy, ArchDef a_arch, long a_randomSeed) {
        m_graph = copy(a_toCopy, a_arch);
        m_arch = a_arch;
        m_weights = new MapperBase.DependencyWeights(1.0);
        m_seed = a_randomSeed;
        m_rand = new Random(m_seed);
        final double [] weights = new double[] {0, 0.25, 0.5, 0.75, 1.0};

        for (dmDependency.Type t : dmDependency.Type.values()) {
            //m_weights.setWeight(t, m_rand.nextDouble());
            m_weights.setWeight(t, weights[m_rand.nextInt(weights.length)]);
        }
        m_eliteGeneration = 0;
    }

    public Individual(Individual a_p1, Individual a_p2) {
        m_graph = copy(a_p1.m_graph, a_p1.m_arch);
        m_arch = a_p1.m_arch;
        m_weights = new MapperBase.DependencyWeights(1.0);
        m_seed = (a_p1.m_rand.nextInt() + a_p2.m_rand.nextInt()) / 2;
        m_rand = new Random(m_seed);
        m_eliteGeneration = 0;

        //m_p1 = a_p1;
        //m_p2 = a_p2;

        for (dmDependency.Type t : dmDependency.Type.values()) {

            if (m_rand.nextDouble() < 0.5) {
                m_weights.setWeight(t, a_p1.m_weights.getWeight(t));
            } else {
                m_weights.setWeight(t, a_p2.m_weights.getWeight(t));
            }
            /*double p1 = a_p1.m_weights.getWeight(t);
            double p2 = a_p2.m_weights.getWeight(t);
            m_weights.setWeight(t, (p1 + p2) / 2.0);*/
        }
    }

    public boolean equals(Individual a_i) {
        for (dmDependency.Type t : dmDependency.Type.values()) {
            double v1 = m_weights.getWeight(t);
            double v2 = a_i.m_weights.getWeight(t);
            if (Math.abs(v1-v2) > 0.0001) {
                return false;
            }
        }
        return true;
    }

    public Individual(Individual a_eliteIndividual) {
        m_graph = copy(a_eliteIndividual.m_graph, a_eliteIndividual.m_arch);
        m_arch = a_eliteIndividual.m_arch;
        m_weights = new MapperBase.DependencyWeights(a_eliteIndividual.m_weights);
        m_seed = a_eliteIndividual.m_seed;
        m_rand = new Random(m_seed);
        m_eliteGeneration = a_eliteIndividual.m_eliteGeneration + 1;
    }

    public void setWeights(MapperBase.DependencyWeights a_weights) {
        for (dmDependency.Type t : dmDependency.Type.values()) {
            m_weights.setWeight(t, a_weights.getWeight(t));
        }
    }

    public int getEliteGenerations() {
        return m_eliteGeneration;
    }

    public double eval(Iterable<Iterable<String>> a_initialSets) {
        double f1 = 0;


        int setCount = 0;
        for (Iterable<String> initialSet : a_initialSets) {setCount++;}
        m_f1Scores = new double[setCount];
        Arrays.fill(m_f1Scores, -Double.MAX_VALUE);
        m_medianF1Score = -Double.MAX_VALUE;
        int setIx = 0;

        final double filterThresholdBase = 0.1;
        final double filterThresholdMax = 0.75;
        final double filterThresholdDelta = (filterThresholdMax - filterThresholdBase) / (setCount - 1);
        double filterThreshold = filterThresholdBase;

        for (Iterable<String> initialSet : a_initialSets) {
            //System.out.println("\t\t\tEvaluating initial set: " + setCount);
            m_arch.cleanNodeClusters(m_graph.getNodes(), false);

            int initialSetSize = copyInitialSetToGraph(initialSet);

            //System.out.println("\t\t\tRunning experiment... ");
            f1 = runExperimentGetF1Score(filterThreshold, m_graph.getNodeCount() - initialSetSize);
            m_f1Scores[setIx] = f1;
            setIx++;
            filterThreshold += filterThresholdDelta;
        }



        // maximize the first weight only
        /*for (setIx = 0; setIx < setCount; setIx++) {
            m_f1Scores[setIx] = m_weights.getWeight(dmDependency.Type.values()[4]);
            m_f1Scores[setIx] += m_weights.getWeight(dmDependency.Type.values()[1]);
            m_f1Scores[setIx] -= m_rand.nextDouble() * 0.5;  // add some random noise
            for (dmDependency.Type t : dmDependency.Type.values()) {
                if (t != dmDependency.Type.values()[4] && t != dmDependency.Type.values()[1]) {
                    m_f1Scores[setIx] -= m_weights.getWeight(t);
                }
            }
        }*/

        f1 = stats.medianUnsorted(m_f1Scores);
        m_medianF1Score = f1;

        return f1;
    }

    public double getMedianF1() {
        return m_medianF1Score;
    }
    public double getMeanF1() {
        return stats.mean(m_f1Scores);
    }

    public double getF1Score(int a_initialSetIx) {
        return m_f1Scores[a_initialSetIx];
    }

    private double runExperimentGetF1Score(double a_filterThreshold, int a_totalPossibleOrphans) {
        double ret = 0;
        int clusterFails = 0;
        int autoClusteredOrphans = 0;


        HuGMe exp;
        do {
            exp = createExperiment(a_filterThreshold);
            exp.run(m_graph);
            clusterFails += exp.m_autoWrong;
            autoClusteredOrphans += exp.getAutoClusteredOrphanCount();
        } while (exp.getAutoClusteredOrphanCount() > 0);

        double precision = 0;
        double truePositive = autoClusteredOrphans - clusterFails;
        double falseNegatives = a_totalPossibleOrphans - autoClusteredOrphans;
        if (autoClusteredOrphans > 0) {
            precision = (double) truePositive / (double) autoClusteredOrphans;
        }
        double recall = 0;
        if (a_totalPossibleOrphans > 0) {
            recall = truePositive / (truePositive + falseNegatives);
        }

        if (precision > 0 || recall > 0) {
            return (2.0 * precision * recall) / (precision + recall);
        } else {
            return 0.0;
        }
    }

    private HuGMe createExperiment(double a_filterThreshold) {
        HuGMe ret = new HuGMe(a_filterThreshold, 1, false, m_arch, m_weights);
        return ret;
    }

    private int copyInitialSetToGraph(Iterable<String> a_initialSet) {
        int ret = 0;
        for (String nodeName : a_initialSet) {
            CNode n = m_graph.getNode(nodeName);
            ArchDef.Component c = m_arch.getMappedComponent(n);
            c.clusterToNode(n, ArchDef.Component.ClusteringType.Initial);
            ret++;
        }
        return ret;
    }

    private CGraph copy(CGraph a_g, ArchDef a_arch) {
        CGraph ret = new CGraph();

        // we need a copy of the mapped nodes as we will be manipulating the mapping
        for(CNode n : a_g.getNodes()) {
            CNode copy = ret.createNode(n.getName());
            copy.shallowCopy(n);
        }

        return ret;
    }

    public double getDW(dmDependency.Type t) {
        return m_weights.getWeight(t);
    }

    public void mutate(double a_amplitude) {
        dmDependency.Type t = dmDependency.Type.values()[m_rand.nextInt(dmDependency.Type.values().length)];

        double w = m_weights.getWeight(t);
        w += 2.0 * (m_rand.nextDouble() - 0.5) * a_amplitude; // +/- amplitude
        if (w < 0) {
            w = 0;
        } else if (w > 1) {
            w = 1;
        }

        m_weights.setWeight(t, w);
    }

    @Override
    public int compareTo(Individual a_i) {
        // swap the objects so we get the correct order
        //return Double.compare(a_i.getMeanF1(), getMeanF1());
        int sumScoreThis = 0;
        int sumScoreI = 0;
        for (int i = 0; i < m_f1Scores.length; i++) {
            if (a_i.m_f1Scores[i] > m_f1Scores[i]) {
                sumScoreI++;
            } else if (a_i.m_f1Scores[i] < m_f1Scores[i]) {
                sumScoreThis++;
            }
        }

        return Integer.compare(sumScoreI, sumScoreThis);
    }
}
