package se.lnu.siq.s4rdm3x.g3n3z;

import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.Selector;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;

import java.util.Random;

public class Environment {
    CGraph m_graph;
    ArchDef m_arch;
    long m_seed;

    public Environment(CGraph a_graph, ArchDef a_arch, long a_seed) {
        m_graph = a_graph;
        m_arch = a_arch;
        m_seed = a_seed;

        // remove all non mapped nodes
        a_graph.getNodes(new Selector.ISelector() {
            @Override
            public boolean isSelected(CNode a_node) {
                return m_arch.getMappedComponent(a_node) == null;
            }
        }).forEach(n -> m_graph.removeNode(n));
    }

    public Individual evolve(final int a_individualsPerGeneration, final int a_maxGenerations) {
        Random r = new Random(m_seed);
        Generation currentGeneration = new Generation(m_graph, m_arch, a_individualsPerGeneration, r);

        for (int g = 0; g < a_maxGenerations; g++) {
            java.lang.System.out.println("Evaluating generation: " + g);
            currentGeneration.eval();
            java.lang.System.out.println("Best Individual F1 score: " + currentGeneration.getBest().getF1());
            if (g + 1 < a_maxGenerations) {
                java.lang.System.out.println("Spawing new generation...");
                currentGeneration = new Generation(currentGeneration);
            }
        }

        return currentGeneration.getBest();
    }


}
