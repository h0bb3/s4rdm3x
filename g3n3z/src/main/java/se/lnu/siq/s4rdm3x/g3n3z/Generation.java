package se.lnu.siq.s4rdm3x.g3n3z;

import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.Selector;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Generation {
    CGraph m_graph;
    ArchDef m_arch;
    Individual[] m_individuals;
    Random m_r;

    public Generation(CGraph a_graph, ArchDef a_arch, int a_individuals, Random a_r) {
        m_graph = a_graph;
        m_arch = a_arch;
        m_r = a_r;
        m_individuals = new Individual[a_individuals];
        for (int i = 0; i < m_individuals.length; i++) {
            m_individuals[i] = new Individual(a_graph, a_arch, m_r.nextInt());
        }

    }

    public Generation(Generation a_prevGen) {
        m_graph = a_prevGen.m_graph;
        m_arch = a_prevGen.m_arch;
        m_r = a_prevGen.m_r;
        m_individuals = new Individual[a_prevGen.m_individuals.length];

        // create the individuals

        // elitism - but kill the elite individual if it has lived a long life
        int elitismOffset = a_prevGen.m_individuals[0].getEliteGenerations() < 1 ? 1: 0;
        if (elitismOffset > 0) {
            m_individuals[0] = new Individual(a_prevGen.m_individuals[0]);
        }

        for (int i = elitismOffset; i < m_individuals.length; i++) {
            Integer[] pop = tournament(5, a_prevGen.m_individuals);
            Individual i1 = a_prevGen.m_individuals[pop[0]];
            Individual i2 = a_prevGen.m_individuals[pop[1]];

            m_individuals[i] = new Individual(i1, i2);
            if (m_r.nextDouble() < 0.2) {
                m_individuals[i].mutate();
            }

        }
        // if things are too equal to the previous generation we need to mix it up
        /*if (countEquals(a_prevGen.m_individuals, m_individuals) > m_individuals.length / 3) {
            for (int i = 1 + m_individuals.length / 2 - 1; i < m_individuals.length; i++) {
                m_individuals[i] = new Individual(m_graph, m_arch, m_r.nextLong());
            }
        }*/
    }

    private int countEquals(Individual[] a_individuals1, Individual[] a_individuals2, int a_elitismOffset) {
        int count = 0;
        for (int i = a_elitismOffset; i < a_individuals1.length; i++) {
            for (int j = 0; j < a_individuals2.length; j++) {
                if (a_individuals1[i].equals(a_individuals2[j])) {
                    count++;
                    break;
                }
            }
        }

        return count;
    }

    private Integer[] tournament(int a_tournamentSize, Individual[] a_population) {
        Integer [] tournament = new Integer[a_tournamentSize];
        Arrays.setAll(tournament, operand -> -1);

        for (int i = 0; i < tournament.length; i++) {
            tournament[i] = selectRandomIndividual(tournament, a_population);
        }

        Arrays.sort(tournament, (o1, o2)->{return Double.compare(a_population[o2].getF1(), a_population[o1].getF1());});

        return tournament;
    }

    private int selectRandomIndividual(Integer[] a_takenIndices, Individual[] a_population) {

        int ret = 0;
        boolean taken = false;
        do {
            ret = m_r.nextInt(a_population.length);
            taken = false;
            for (Integer i : a_takenIndices) {
                if (ret == i) {
                    taken = true;
                    break;
                }
            }

        } while (taken);

        return ret;
    }


    public void eval() {

        ArrayList<Iterable<String>> initialSets = createInitialSets(10);
        final int[] doneCount = {0};

        for (int i = 0; i < m_individuals.length; i++) {

            Individual indiv = m_individuals[i];
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    indiv.eval(initialSets);
                    System.out.println("\t\tIndividual F1: " + indiv.getF1());
                    doneCount[0]++;
                }
            };
            Thread t = new Thread(r);
            t.start();

            //indiv.eval(initialSets);
            //System.out.println("\t\tIndividual F1: " + indiv.getF1());
            //doneCount[0]++;
        }

        while(doneCount[0] != m_individuals.length) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {

            }
        }

        Arrays.sort(m_individuals, (o1, o2)->{return Double.compare(o2.getF1(), o1.getF1());});
    }

    private boolean allDone() {
        for (Individual i : m_individuals) {
            if (i.getF1() <= -Double.MAX_VALUE) {
                return false;
            }
        }

        return true;
    }

    private ArrayList<Iterable<String>> createInitialSets(int a_setCount) {
        final double start = 0.01;
        final double delta = (1.0 - 2 * start) / a_setCount;    // top and bottom size bounds
        ArrayList<Iterable<String>> sets = new ArrayList<>();

        for (int i = 0; i < a_setCount; i++) {
            sets.add(createInitialSet(start + delta * i));
        }

        return sets;
    }

    private ArrayList<String> createInitialSet(double a_size) {
        int totalMappedNodes = 0;
        int [] perComponent = new int[m_arch.getComponentCount()];
        ArrayList<CNode> set = new ArrayList<>();

        m_graph.getNodes().forEach(n -> {set.add(n);});

        for (int i = 0; i < m_arch.getComponentCount(); i++) {
            ArchDef.Component c = m_arch.getComponent(i);
            class Counter implements Selector.ISelector {
                int m_count = 0;
                @Override
                public boolean isSelected(CNode a_node) {
                    if (c.isMappedTo(a_node)) {
                        m_count++;
                    }
                    return false;
                }
            }
            Counter counter = new Counter();
            m_graph.getNodes(counter);
            perComponent[i] = counter.m_count;
            totalMappedNodes += perComponent[i];
        }

        int nodesToRemove = totalMappedNodes - (int)(totalMappedNodes * a_size + 1.0);

        while (nodesToRemove > 0) {
            // we need at least one node per component
            if (set.size() - 1 < m_arch.getComponentCount()) {
                break;
            }

            // find a candidate that is valid
            CNode candidate = null;
            int candidateIx = 0;
            int componentIx = 0;
            do {

                candidateIx = m_r.nextInt(set.size());
                candidate = set.get(candidateIx);
                componentIx = m_arch.getComponentIx(m_arch.getMappedComponent(candidate));
            } while (perComponent[componentIx] - 1 < 0);

            set.remove(candidateIx);
            perComponent[componentIx]--;
            nodesToRemove--;
        }

        ArrayList<String> ret = new ArrayList<>(set.size());
        set.forEach(n -> { ret.add(n.getName());});

        return ret;
    }

    public Individual getBest() {
        return m_individuals[0];
    }

    public double getAverageScore() {
        double ret = 0;
        for(Individual i : m_individuals) {
            ret += i.getF1();
        }

        return ret / m_individuals.length;
    }
}
