package se.lnu.siq.s4rdm3x.g3n3z;

import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.HuGMe;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.MapperBase;

import java.util.Random;

public class Individual {

    private final CGraph m_graph;
    private final ArchDef m_arch;
    private Random m_rand = null;
    private final long m_seed;
    private double m_f1 = -1;
    private int m_eliteGeneration;

    private MapperBase.DependencyWeights m_weights;

    public Individual(CGraph a_toCopy, ArchDef a_arch, long a_randomSeed) {
        m_graph = copy(a_toCopy, a_arch);
        m_arch = a_arch;
        m_weights = new MapperBase.DependencyWeights(1.0);
        m_seed = a_randomSeed;
        m_rand = new Random(m_seed);

        for (dmDependency.Type t : dmDependency.Type.values()) {
            m_weights.setWeight(t, m_rand.nextDouble());
        }
        m_eliteGeneration = 0;
    }

    public Individual(Individual a_p1, Individual a_p2) {
        m_graph = copy(a_p1.m_graph, a_p1.m_arch);
        m_arch = a_p1.m_arch;
        m_weights = new MapperBase.DependencyWeights(1.0);
        m_seed = (a_p1.m_seed + a_p2.m_seed) / 2;
        m_rand = new Random(m_seed);
        m_eliteGeneration = 0;

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

    public int getEliteGenerations() {
        return m_eliteGeneration;
    }

    public double eval(Iterable<Iterable<String>> a_initialSets) {
        double f1 = 0;
        m_f1 = -Double.MAX_VALUE;
        int setCount = 0;
        for (Iterable<String> initialSet : a_initialSets) {
            //System.out.println("\t\t\tEvaluating initial set: " + setCount);
            m_arch.cleanNodeClusters(m_graph.getNodes(), false);

            int initialSetSize = copyInitialSetToGraph(initialSet);

            //System.out.println("\t\t\tRunning experiment... ");
            f1 += runExperimentGetF1Score(m_graph.getNodeCount() - initialSetSize);
            setCount++;
        }


        m_f1 = f1 / setCount;

        /*
        // maximize the first weight only
        m_f1 = m_weights.getWeight(dmDependency.Type.values()[4]);
        m_f1 += m_weights.getWeight(dmDependency.Type.values()[1]);
        for (dmDependency.Type t : dmDependency.Type.values()) {
            if (t != dmDependency.Type.values()[4] && t != dmDependency.Type.values()[1]) {
                m_f1 -= m_weights.getWeight(t);
            }
        }*/

        return m_f1;
    }

    public double getF1() {
        return m_f1;
    }

    private double runExperimentGetF1Score(int a_totalPossibleOrphans) {
        double ret = 0;
        int clusterFails = 0;
        int actualOrphans = 0;

        HuGMe exp;
        do {
            exp = createExperiment();
            exp.run(m_graph);
            clusterFails += exp.m_autoWrong;
             actualOrphans += exp.getAutoClusteredOrphanCount();
        } while (exp.getAutoClusteredOrphanCount() > 0);

        double precision = (double)(actualOrphans - clusterFails) / (double)actualOrphans;
        double recall = (double)(actualOrphans - clusterFails) / (double)a_totalPossibleOrphans;

        return (2.0 * precision * recall) / (precision + recall);

    }

    private HuGMe createExperiment() {
        HuGMe ret = new HuGMe(0, 1, false, m_arch, m_weights);
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
}
