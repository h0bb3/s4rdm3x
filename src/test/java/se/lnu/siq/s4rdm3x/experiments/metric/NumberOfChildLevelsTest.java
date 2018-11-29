package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.NodeGenerator;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumberOfChildLevelsTest {

    @Test
    void test1() {
        NumberOfChildLevels sut = new NumberOfChildLevels();
        NodeGenerator ng = new NodeGenerator();
        Graph g = ng.generateGraph(dmDependency.Type.Extends, new String [] {"BA", "CA"});
        Node n1 = g.getNode("A");

        sut.assignMetric(g.getNodeSet());
        assertEquals(1.0, sut.getMetric(n1));
    }


    @Test
    void test2() {
        NumberOfChildLevels sut = new NumberOfChildLevels();
        NodeGenerator ng = new NodeGenerator();
        Graph g = ng.generateGraph(dmDependency.Type.Extends, new String [] {"BA", "CB"});
        Node n1 = g.getNode("A");

        sut.assignMetric(g.getNodeSet());
        assertEquals(2.0, sut.getMetric(n1));
    }

    @Test
    void test3() {
        NumberOfChildLevels sut = new NumberOfChildLevels();
        NodeGenerator ng = new NodeGenerator();
        Graph g = ng.generateGraph(dmDependency.Type.Extends, new String [] {"BA", "CB", "DA"});
        Node n1 = g.getNode("A");

        sut.assignMetric(g.getNodeSet());
        assertEquals(2.0, sut.getMetric(n1));
    }

    @Test
    void test4() {
        NumberOfChildLevels sut = new NumberOfChildLevels();
        NodeGenerator ng = new NodeGenerator();
        Graph g = ng.generateGraph(dmDependency.Type.Extends, new String [] {"DA", "BA", "CB"});
        Node n1 = g.getNode("A");

        sut.assignMetric(g.getNodeSet());
        assertEquals(2.0, sut.getMetric(n1));
    }

    @Test
    void test5() {
        NumberOfChildLevels sut = new NumberOfChildLevels();
        NodeGenerator ng = new NodeGenerator();
        Graph g = ng.generateGraph(dmDependency.Type.Extends, new String [] {"AD"});
        Node n1 = g.getNode("A");

        sut.assignMetric(g.getNodeSet());
        assertEquals(0.0, sut.getMetric(n1));
    }

    @Test
    void test6() {
        NumberOfChildLevels sut = new NumberOfChildLevels();
        NodeGenerator ng = new NodeGenerator();
        Graph g = ng.generateGraph(dmDependency.Type.Implements, new String [] {"BA", "DA", "CB", "EB", "GE", "FE"});
        Node n1 = g.getNode("A");

        sut.assignMetric(g.getNodeSet());
        assertEquals(3.0, sut.getMetric(n1));
    }

}
