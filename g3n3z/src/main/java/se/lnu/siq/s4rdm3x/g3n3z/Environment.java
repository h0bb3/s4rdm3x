package se.lnu.siq.s4rdm3x.g3n3z;

import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.Selector;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.MapperBase;

import java.util.ArrayList;
import java.util.Random;

public class Environment {
    CGraph m_graph;
    ArchDef m_arch;
    long m_seed;
    private String m_systemName;

    public Environment(CGraph a_graph, ArchDef a_arch, long a_seed, String a_systemName) {
        m_graph = a_graph;
        m_arch = a_arch;
        m_seed = a_seed;
        m_systemName = a_systemName;

        // remove all non mapped nodes
        a_graph.getNodes(new Selector.ISelector() {
            @Override
            public boolean isSelected(CNode a_node) {
                return m_arch.getMappedComponent(a_node) == null;
            }
        }).forEach(n -> m_graph.removeNode(n));
    }

    public void printIndividual(Individual a_i) {
        java.lang.System.out.println("\tF1:\t"+ a_i.getMedianF1());
        for (dmDependency.Type t: dmDependency.Type.values()) {
            java.lang.System.out.println("\t"+ t + ":\t" + a_i.getDW(t));
        }
    }

    public Individual evolve(final int a_individualsPerGeneration, final int a_maxGenerations, final int a_maxThreads, final double a_chanceOfMutation, final double a_mutationAmplitude, final int a_eliteIndividualMaxAge, int a_tournamentSize) {
        Random r = new Random(m_seed);
        Generation currentGeneration = new Generation(m_graph, m_arch, a_individualsPerGeneration, r);

        printParameters(a_individualsPerGeneration, a_maxGenerations, a_chanceOfMutation, a_mutationAmplitude, a_eliteIndividualMaxAge, a_tournamentSize);

        // inject the optimal solution in the first generation
        /*Individual specimen = currentGeneration.getIndividual(a_individualsPerGeneration - 1);
        MapperBase.DependencyWeights dw = new MapperBase.DependencyWeights(0.0);
        dw.setWeight(dmDependency.Type.values()[4], 1.0);
        dw.setWeight(dmDependency.Type.values()[1], 1.0);
        specimen.setWeights(dw);*/

        ArrayList<Double> popAverageScore = new ArrayList<>();
        ArrayList<Double> bestIndScore = new ArrayList<>();
        Individual overallBest = currentGeneration.getBest();

        for (int g = 0; g < a_maxGenerations; g++) {
            java.lang.System.out.println("Evaluating generation: " + g);
            currentGeneration.eval(a_maxThreads, 10);
            java.lang.System.out.println("\tAverage F1 score:\t" + currentGeneration.getAverageScore());
            java.lang.System.out.println("\tBest Individual F1 score:\t" + currentGeneration.getBest().getMedianF1());
            popAverageScore.add(currentGeneration.getAverageScore());
            bestIndScore.add(currentGeneration.getBest().getMedianF1());
            printIndividual(currentGeneration.getBest());
            if (overallBest.getMedianF1() < currentGeneration.getBest().getMedianF1()) {
                overallBest = currentGeneration.getBest();
            }

            //if (currentGeneration.isStale()) {
            //    currentGeneration.setExtremes(a_individualsPerGeneration / 2);
            //}

            if (g + 1 < a_maxGenerations) {
                currentGeneration = new Generation(currentGeneration, a_chanceOfMutation, a_mutationAmplitude, a_eliteIndividualMaxAge, a_tournamentSize);
            }
        }
        java.lang.System.out.println("best\taverage");
        for (int i = 0; i < popAverageScore.size(); i++) {
            java.lang.System.out.println("" + bestIndScore.get(i) + "\t" + popAverageScore.get(i));
        }

        java.lang.System.out.println(java.lang.System.lineSeparator() + "Evaluation done." + java.lang.System.lineSeparator() + "Weights for last generation best individual (this is probably the best solution):");
        printIndividual(currentGeneration.getBest());

        java.lang.System.out.println(java.lang.System.lineSeparator() + "Weights for all generations best individual:");
        printIndividual(overallBest);

        printParameters(a_individualsPerGeneration, a_maxGenerations, a_chanceOfMutation, a_mutationAmplitude, a_eliteIndividualMaxAge, a_tournamentSize);


        return currentGeneration.getBest();
    }

    private void printParameters(int a_individualsPerGeneration, int a_maxGenerations, double a_chanceOfMutation, double a_mutationAmplitude, int a_eliteIndividualMaxAge, int a_tournamentSize) {
        System.out.println(System.lineSeparator() + "Algorithm Parameters:");
        System.out.println("\tsystem:\t" + m_systemName);
        System.out.println("\tindividuals:\t" + a_individualsPerGeneration);
        System.out.println("\tgenerations:\t" + a_maxGenerations);
        System.out.println("\tmutation chance:\t" + a_chanceOfMutation);
        System.out.println("\tmutation amplitude:\t" + a_mutationAmplitude);
        System.out.println("\tmax age:\t" + a_eliteIndividualMaxAge);
        System.out.println("\ttournament size:\t" + a_tournamentSize);
        System.out.println("\tseed:\t" + m_seed);
    }


}
