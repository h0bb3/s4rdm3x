package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.NodeGenerator;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.stats;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.lnu.siq.s4rdm3x.dmodel.dmClassTest.createsDoubleFileDependencies;

public class HuGMeTests {

    @Test
    void testCountAttractP() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Returns, new String [] {"AB", "AB", "AB", "AB", "AB", "BA", "BA", "BA"});
        ArrayList<ArrayList<MapperBase.ClusteredNode>> clusters = new ArrayList<>();
        ArchDef arch = new ArchDef();
        ArchDef.Component cA = arch.addComponent("A");
        ArchDef.Component cB = arch.addComponent("B");
        ArchDef.Component cC = arch.addComponent("C");
        cB.addDependencyTo(cA);
        cA.addDependencyTo(cC);

        cA.mapToNode(g.getNode("A"));

        cB.mapToNode(g.getNode("B"));
        cB.clusterToNode(g.getNode("B"), ArchDef.Component.ClusteringType.Initial);

        MapperBase.OrphanNode a = new MapperBase.OrphanNode(g.getNode("A"), arch);   // a is the orphan
        MapperBase.ClusteredNode b = new MapperBase.ClusteredNode(g.getNode("B"), arch);   // b is the mapped node

        clusters.add(new ArrayList<>());
        clusters.add(new ArrayList<>());
        clusters.add(new ArrayList<>());

        clusters.get(1).add(b);

        final double othersWeight = 0.75;
        HuGMe sut = new HuGMeManual(10, othersWeight, arch);

        assertEquals(8.0 - 5 * 1.0 - 3 * othersWeight, sut.CountAttractP(a, 0, clusters));   // B -> A this means 5 deps are in violation and 3 are just against cohesion principle
        assertEquals(8.0, sut.CountAttractP(a, 1, clusters));   // a has all relations to b so attraction should be maximal
        assertEquals(8.0 - 8 * 1.0, sut.CountAttractP(a, 2, clusters));
    }


    @Test
    void testCountAttractPHorizontalFileDeps() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.File_Horizontal, new String [] {"AB"});
        ArrayList<ArrayList<MapperBase.ClusteredNode>> clusters = new ArrayList<>();
        ArchDef arch = new ArchDef();
        ArchDef.Component cA = arch.addComponent("A");
        ArchDef.Component cB = arch.addComponent("B");
        cA.addDependencyTo(cB);

        cA.mapToNode(g.getNode("A"));

        cB.mapToNode(g.getNode("B"));
        cB.clusterToNode(g.getNode("B"), ArchDef.Component.ClusteringType.Initial);

        MapperBase.OrphanNode a = new MapperBase.OrphanNode(g.getNode("A"), arch);   // a is the orphan
        MapperBase.ClusteredNode b = new MapperBase.ClusteredNode(g.getNode("B"), arch);   // b is the mapped node

        clusters.add(new ArrayList<>());
        clusters.add(new ArrayList<>());

        clusters.get(0).add(b);

        HuGMe sut = new HuGMeManual(10, 0.0, arch);

        // File dependencies should not incur violations so the violation count will always be 0 hence the same attraction
        assertEquals(1.0, sut.CountAttractP(a, 0, clusters));
        assertEquals(1.0, sut.CountAttractP(a, 1, clusters));
    }

    @Test
    void testProbabilities() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Returns, new String [] {"AB", "CD", "OB"});

        ArrayList<ArrayList<MapperBase.ClusteredNode>> clusters = new ArrayList<>();
        ArchDef arch = new ArchDef();
        ArchDef.Component cA = arch.addComponent("A");
        ArchDef.Component cB = arch.addComponent("B");
        cB.addDependencyTo(cA);

        cA.mapToNode(g.getNode("A"));
        cA.clusterToNode(g.getNode("A"), ArchDef.Component.ClusteringType.Initial);

        cA.mapToNode(g.getNode("B"));
        cA.clusterToNode(g.getNode("B"), ArchDef.Component.ClusteringType.Initial);

        cB.mapToNode(g.getNode("C"));
        cB.clusterToNode(g.getNode("C"), ArchDef.Component.ClusteringType.Initial);

        cB.mapToNode(g.getNode("D"));
        cB.clusterToNode(g.getNode("D"), ArchDef.Component.ClusteringType.Initial);

        cB.mapToNode(g.getNode("O"));

        MapperBase.ClusteredNode a1 = new MapperBase.ClusteredNode(g.getNode("A"), arch);
        MapperBase.ClusteredNode a2 = new MapperBase.ClusteredNode(g.getNode("B"), arch);
        MapperBase.ClusteredNode b1 = new MapperBase.ClusteredNode(g.getNode("C"), arch);
        MapperBase.ClusteredNode b2 = new MapperBase.ClusteredNode(g.getNode("D"), arch);
        MapperBase.OrphanNode o = new MapperBase.OrphanNode(g.getNode("O"), arch);


        clusters.add(new ArrayList<>());
        clusters.add(new ArrayList<>());

        clusters.get(0).add(a1);clusters.get(0).add(a2);
        clusters.get(1).add(b1);clusters.get(0).add(b2);

        HuGMe sut = new HuGMe(0, 0.75, false, arch);
        assertEquals(1.0, sut.CountAttractP(o, 0, clusters));
        assertEquals(0.25, sut.CountAttractP(o, 1, clusters));
    }

    @Test
    void testDependencyWeightHalving() {
        double initial = 0.5;
        MapperBase.DependencyWeights dw = new MapperBase.DependencyWeights(initial);
        HuGMe sut = new HuGMe(0, 0, false, null, dw);

        final double factor = dmClass.createsDoubleFileDependencies() ? 0.5 : 1.0;

        dw = sut.getDependencyWeights();
        for (dmDependency.Type t : dmDependency.Type.values()) {
            if (t.isFileBased) {
                assertEquals(initial * factor, dw.getWeight(t));
            } else {
                assertEquals(initial, dw.getWeight(t));
            }
        }
    }

    @Test
    void testDependencyWeightHalving2() {
        double initial = 1.0;
        MapperBase.DependencyWeights dw = new MapperBase.DependencyWeights(initial);
        HuGMe sut = new HuGMe(0, 0, false, null);

        final double factor = dmClass.createsDoubleFileDependencies() ? 0.5 : 1.0;

        dw = sut.getDependencyWeights();
        for (dmDependency.Type t : dmDependency.Type.values()) {
            if (t.isFileBased) {
                assertEquals(initial * factor, dw.getWeight(t));
            } else {
                assertEquals(initial, dw.getWeight(t));
            }
        }
    }

    @Test
    void testWeights() {
        NodeGenerator ng = new NodeGenerator();
        dmDependency.Type dType = dmDependency.Type.Returns;
        MapperBase.DependencyWeights dw = new MapperBase.DependencyWeights(1);
        dw.setWeight(dType, 0.5);
        CGraph g = ng.generateGraph(dType, new String [] {"AB", "CD", "OB"});

        ArrayList<ArrayList<MapperBase.ClusteredNode>> clusters = new ArrayList<>();
        ArchDef arch = new ArchDef();
        ArchDef.Component cA = arch.addComponent("A");
        ArchDef.Component cB = arch.addComponent("B");
        cB.addDependencyTo(cA);

        cA.mapToNode(g.getNode("A"));
        cA.clusterToNode(g.getNode("A"), ArchDef.Component.ClusteringType.Initial);

        cA.mapToNode(g.getNode("B"));
        cA.clusterToNode(g.getNode("B"), ArchDef.Component.ClusteringType.Initial);

        cB.mapToNode(g.getNode("C"));
        cB.clusterToNode(g.getNode("C"), ArchDef.Component.ClusteringType.Initial);

        cB.mapToNode(g.getNode("D"));
        cB.clusterToNode(g.getNode("D"), ArchDef.Component.ClusteringType.Initial);

        cB.mapToNode(g.getNode("O"));

        MapperBase.ClusteredNode a1 = new MapperBase.ClusteredNode(g.getNode("A"), arch);
        MapperBase.ClusteredNode a2 = new MapperBase.ClusteredNode(g.getNode("B"), arch);
        MapperBase.ClusteredNode b1 = new MapperBase.ClusteredNode(g.getNode("C"), arch);
        MapperBase.ClusteredNode b2 = new MapperBase.ClusteredNode(g.getNode("D"), arch);
        MapperBase.OrphanNode o = new MapperBase.OrphanNode(g.getNode("O"), arch);


        clusters.add(new ArrayList<>());
        clusters.add(new ArrayList<>());

        clusters.get(0).add(a1);clusters.get(0).add(a2);
        clusters.get(1).add(b1);clusters.get(0).add(b2);

        HuGMe sut = new HuGMe(0, 0.75, false, arch, dw);
        assertEquals(1.0 * dw.getWeight(dType), sut.CountAttractP(o, 0, clusters));
        assertEquals(0.25 * dw.getWeight(dType), sut.CountAttractP(o, 1, clusters));
    }
}
