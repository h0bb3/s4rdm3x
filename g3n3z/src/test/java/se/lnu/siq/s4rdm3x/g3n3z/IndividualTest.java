package se.lnu.siq.s4rdm3x.g3n3z;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.MagicInvoker;
import se.lnu.siq.s4rdm3x.experiments.InitialSetGenerator;
import se.lnu.siq.s4rdm3x.experiments.metric.Rand;
import se.lnu.siq.s4rdm3x.experiments.system.FileBased;
import se.lnu.siq.s4rdm3x.experiments.system.System;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


class IndividualTest {

    @Test
    public void testF1() {
        try {
            FileBased system = new FileBased("C:/hObbE/projects/coding/github/s4rdm3x/data/systems/ProM6.9/ProM_6_9.sysmdl");
            CGraph g = new CGraph();
            system.load(g);
            ArchDef a = system.createAndMapArch(g);
            Random r = new Random(171717);
            Rand rm = new Rand();
            rm.assignMetric(a.getMappedNodes(g.getNodes()));

            InitialSetGenerator isg = new InitialSetGenerator();
            isg.assignInitialClusters(g, a, 0.75, new Rand(), r);
            int mappedNodeCount = a.getMappedNodeCount(g.getNodes());
            int initialSetSize = a.getClusteredNodeCount(g.getNodes());

            class SUTIndividual extends Individual {

                SUTIndividual() {
                    super(g, a, 17);
                }

                private double runExperimentGetF1Score(int a_totalPossibleOrphans) {
                    MagicInvoker mi = new MagicInvoker(this);
                    return (double)mi.invokeMethodMagic(a_totalPossibleOrphans);
                }
            }

            SUTIndividual sut = new SUTIndividual();
            double sutF1 = sut.runExperimentGetF1Score(mappedNodeCount - initialSetSize);

            java.lang.System.out.println("" + sutF1);
        } catch (IOException | System.NoMappedNodesException e) {
            e.printStackTrace();
            assertTrue(false);
        }

    }

}