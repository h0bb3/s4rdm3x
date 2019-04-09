package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.NodeGenerator;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.stats;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HuGMeTests {

    @Test
    void testCountAttractP() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Returns, new String [] {"AB", "AB", "AB", "AB", "AB", "AB", "AB", "AB"});
        CNode a = g.getNode("A");   // a is the orphan
        CNode b = g.getNode("B");   // b is the mapped node
        ArrayList<ArrayList<CNode>> clusters = new ArrayList<>();
        ArchDef arch = new ArchDef();
        ArchDef.Component cA = arch.addComponent("A");
        ArchDef.Component cB = arch.addComponent("B");
        ArchDef.Component cC = arch.addComponent("C");
        cB.addDependencyTo(cA);
        cA.addDependencyTo(cC);

        clusters.add(new ArrayList<>());
        clusters.add(new ArrayList<>());
        clusters.add(new ArrayList<>());

        clusters.get(0).add(b);

        HuGMe sut = new HuGMeManual(10, 0.75, arch);

        assertEquals(8.0, sut.CountAttractP(a, 0, clusters));
        assertEquals(2.0, sut.CountAttractP(a, 1, clusters));
        assertEquals(0.0, sut.CountAttractP(a, 2, clusters));
    }

    @Test
    void testCountAttractP2() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Returns, new String [] {"AB", "AB", "AB", "AB", "BA", "BA", "BA", "BA"});
        CNode a = g.getNode("A");   // a is the orphan
        CNode b = g.getNode("B");   // b is the mapped node
        ArrayList<ArrayList<CNode>> clusters = new ArrayList<>();
        ArchDef arch = new ArchDef();
        ArchDef.Component cA = arch.addComponent("A");
        ArchDef.Component cB = arch.addComponent("B");
        ArchDef.Component cC = arch.addComponent("C");
        cB.addDependencyTo(cA);
        cA.addDependencyTo(cC);

        clusters.add(new ArrayList<>());
        clusters.add(new ArrayList<>());
        clusters.add(new ArrayList<>());

        clusters.get(0).add(b);

        HuGMe sut = new HuGMeManual(10, 0.75, arch);

        assertEquals(8.0, sut.CountAttractP(a, 0, clusters));
        assertEquals(1.0, sut.CountAttractP(a, 1, clusters));
        assertEquals(1.0, sut.CountAttractP(a, 2, clusters));
    }

    @Test
    void testProbabilities() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Returns, new String [] {"AB", "CD", "OB"});
        CNode a1 = g.getNode("A");
        CNode a2 = g.getNode("B");
        CNode b1 = g.getNode("C");
        CNode b2 = g.getNode("D");
        CNode o = g.getNode("O");
        ArrayList<ArrayList<CNode>> clusters = new ArrayList<>();
        ArchDef arch = new ArchDef();
        ArchDef.Component cA = arch.addComponent("A");
        ArchDef.Component cB = arch.addComponent("B");
        cB.addDependencyTo(cA);

        clusters.add(new ArrayList<>());
        clusters.add(new ArrayList<>());

        clusters.get(0).add(a1);clusters.get(0).add(a2);
        clusters.get(1).add(b1);clusters.get(0).add(b2);

        HuGMe sut = new HuGMeManual(0, 0.75, arch);
        assertEquals(1.0, sut.CountAttractP(o, 0, clusters));
        assertEquals(0.25, sut.CountAttractP(o, 1, clusters));

        double [] oAttractions = {sut.CountAttractP(o, 0, clusters), sut.CountAttractP(o, 1, clusters)};
        clusters.get(0).remove(a1);
        double [] a1Attractions = {sut.CountAttractP(a1, 0, clusters), sut.CountAttractP(a1, 1, clusters)};
        clusters.get(0).add(a1);

        clusters.get(0).remove(a2);
        double [] a2Attractions = {sut.CountAttractP(a2, 0, clusters), sut.CountAttractP(a2, 1, clusters)};
        clusters.get(0).add(a2);

        clusters.get(0).remove(b1);
        double [] b1Attractions = {sut.CountAttractP(b1, 0, clusters), sut.CountAttractP(b1, 1, clusters)};
        clusters.get(0).add(b1);

        clusters.get(0).remove(b2);
        double [] b2Attractions = {sut.CountAttractP(b2, 0, clusters), sut.CountAttractP(b2, 1, clusters)};
        clusters.get(0).add(b2);


        double [] AattractionFractions = {a1Attractions[0] / stats.sum(a1Attractions), a2Attractions[0] / stats.sum(a2Attractions)};
        double AattractionsMean = stats.mean(AattractionFractions);
        double AatractionsSD = stats.stdDev(AattractionFractions, AattractionsMean);

        //double prob = stats.getNormalProbabilityDensity(1 / 1.25, AattractionsMean, AatractionsSD);
        //double prob = stats.getNormalProbability(0, 0, 1);
        double prob = stats.getNormalProbability(0, AattractionFractions[0], AattractionsMean, AatractionsSD);


        double [] attractionFractions = {a1Attractions[0] / stats.sum(a1Attractions), a2Attractions[0] / stats.sum(a2Attractions),
                                         a1Attractions[1] / stats.sum(a1Attractions), a2Attractions[1] / stats.sum(a2Attractions),
                                        b1Attractions[0] / stats.sum(b1Attractions), b2Attractions[0] / stats.sum(b2Attractions),
                                        b1Attractions[1] / stats.sum(b1Attractions), b2Attractions[1] / stats.sum(b2Attractions)};
        double attractionsMean = stats.mean(attractionFractions);
        double atractionsSD = stats.stdDev(attractionFractions, AattractionsMean);

        double probFraction = stats.getNormalProbability(AattractionFractions[0], 1.0, attractionsMean, atractionsSD);

        double probability = (prob * (1/2.0)) / probFraction;
    }
}
