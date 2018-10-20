package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.NodeGenerator;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumberOfChildrenTest {

    @Test
    void test1() {
        NumberOfChildren sut = new NumberOfChildren();
        NodeGenerator ng = new NodeGenerator();
        Graph g = ng.generateGraph(dmDependency.Type.Extends, new String [] {"BA", "CA"});
        Node n1 = g.getNode("A");

        sut.assignMetric(g.getNodeSet());
        assertEquals(2.0, sut.getMetric(n1));
    }

    @Test
    void test2() {
        NumberOfChildren sut = new NumberOfChildren();
        NodeGenerator ng = new NodeGenerator();
        Graph g = ng.generateGraph(dmDependency.Type.Implements, new String [] {"BA", "CB"});
        Node n1 = g.getNode("A");

        sut.assignMetric(g.getNodeSet());
        assertEquals(2.0, sut.getMetric(n1));
    }

    @Test
    void test3() {
        NumberOfChildren sut = new NumberOfChildren();
        NodeGenerator ng = new NodeGenerator();
        Graph g = ng.generateGraph(dmDependency.Type.Implements, new String [] {"AB", "CB"});
        Node n1 = g.getNode("A");

        sut.assignMetric(g.getNodeSet());
        assertEquals(0.0, sut.getMetric(n1));
    }

    @Test
    void testCircular() {
        NumberOfChildren sut = new NumberOfChildren();
        NodeGenerator ng = new NodeGenerator();
        Graph g = ng.generateGraph(dmDependency.Type.Extends, new String [] {"BA", "CB", "AC"}); // circular inheritance is impossible
        Node n1 = g.getNode("A");

        sut.assignMetric(g.getNodeSet());
        assertEquals(2.0, sut.getMetric(n1));
    }

    @Test
    void testMixedRelations() {
        NumberOfChildren sut = new NumberOfChildren();
        NodeGenerator ng = new NodeGenerator();
        Graph g = ng.generateGraph(dmDependency.Type.Implements, new String [] {"AA", "BA", "CB"});
        ng.addToGraph(g, dmDependency.Type.MethodCall, new String [] {"BA", "CB", "CB", "AC", "AA"});
        Node n1 = g.getNode("A");

        sut.assignMetric(g.getNodeSet());
        assertEquals(2.0, sut.getMetric(n1));
    }

    @Test
    void testMultipleClassesinSameNode() {
        NumberOfChildren sut = new NumberOfChildren();
        NodeGenerator ng = new NodeGenerator();
        Graph g = ng.generateGraph(dmDependency.Type.Implements, new String [] {"BA", "CB"});
        ng.addToGraph(g, dmDependency.Type.Extends, new String [] {"BA", "CB"});
        Node n1 = g.getNode("A");

        sut.assignMetric(g.getNodeSet());
        assertEquals(2.0, sut.getMetric(n1));
    }
}
