package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.NodeGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BCCCTest {
    @Test
    void test1() {
        ByteCodeCyclomaticComplexity sut = new ByteCodeCyclomaticComplexity();
        NodeGenerator ng = new NodeGenerator();
        Graph g = ng.getGraph1();
        Node n1 = g.getNode("n1");

        sut.assignMetric(g.getNodeSet());
        assertEquals(17, sut.getMetric(n1));
    }

    @Test
    void test2() {
        ByteCodeCyclomaticComplexity sut = new ByteCodeCyclomaticComplexity();
        NodeGenerator ng = new NodeGenerator();
        Graph g = ng.getGraph2();
        Node n1 = g.getNode("n1");

        sut.assignMetric(g.getNodeSet());
        assertEquals(17, sut.getMetric(n1));
    }
}
