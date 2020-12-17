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

        Arrays.sort(m_individuals, (o1, o2) -> {return (int)(o1.getF1() - o2.getF1());});
    }

    public Generation(Generation a_prevGen) {
        m_graph = a_prevGen.m_graph;
        m_arch = a_prevGen.m_arch;
        m_r = a_prevGen.m_r;
        m_individuals = new Individual[a_prevGen.m_individuals.length];

        // create the individuals

        // elitism
        m_individuals[0] = new Individual(a_prevGen.m_individuals[0]);

        for (int i = 1; i < m_individuals.length; i++) {
            ArrayList<Individual> population = new ArrayList<>();

            int i1Ix = tournament(m_individuals.length / 3, a_prevGen.m_individuals);
            Individual i1 = a_prevGen.m_individuals[i1Ix];
            a_prevGen.m_individuals[i1Ix] = null;
            int i2Ix = tournament(m_individuals.length / 3, a_prevGen.m_individuals);
            Individual i2 = a_prevGen.m_individuals[i2Ix];
            a_prevGen.m_individuals[i1Ix] = i1;

            m_individuals[i] = new Individual(i1, i2, m_r.nextLong());
        }
    }

    private int tournament(int a_tournamentSize, Individual[] a_individuals) {
        Integer [] tournament = new Integer[a_tournamentSize];
        Arrays.setAll(tournament, operand -> -1);

        for (int i = 0; i < tournament.length; i++) {
            tournament[i] = selectRandomIndividual(tournament, a_individuals);
        }

        Arrays.sort(tournament, (o1, o2)->{return Double.compare(a_individuals[o2].getF1(), a_individuals[o1].getF1());});

        return tournament[0];
    }

    private int selectRandomIndividual(Integer[] a_tournament, Individual[] a_population) {

        int ret = 0;
        boolean taken = false;
        do {
            ret = m_r.nextInt(a_population.length);
            taken = a_population[ret] == null;
            if (!taken) {
                for (Integer i : a_tournament) {
                    if (ret == i) {
                        taken = true;
                        break;
                    }
                }
            }

        } while (taken);

        return ret;
    }


    public void eval() {

        ArrayList<Iterable<String>> initialSets = createInitialSets(10);

        for (int i = 0; i < m_individuals.length; i++) {
            java.lang.System.out.println("\tEvaluating individual: " + i);
            m_individuals[i].eval(initialSets);
            java.lang.System.out.println("\t\tF1: " + m_individuals[i].getF1());
        }
        Arrays.sort(m_individuals, (o1, o2)->{return Double.compare(o2.getF1(), o1.getF1());});
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
}
