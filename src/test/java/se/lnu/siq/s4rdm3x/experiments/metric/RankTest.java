package se.lnu.siq.s4rdm3x.experiments.metric;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.NodeGenerator;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RankTest {

    final double m_delta = 1.0e-3;

    @Test
    void test1() {
        Rank sut = new Rank();
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.getGraph1();
        CNode n1 = g.getNode("n1");

        sut.assignMetric(g.getNodes());
        assertEquals(1.0, sut.getMetric(n1));
    }

    @Test
    void test2() {
        Rank sut = new Rank();
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(new String [] {"AB"});

        sut.assignMetric(g.getNodes());
        assertEquals(.3509, sut.getMetric(g.getNode("A")), m_delta);
        assertEquals(.6491, sut.getMetric(g.getNode("B")), m_delta);
    }

    @Test
    void test3() {
        // https://davidpynes.github.io/Tutorials/Graphs/Graph_04/
        Rank sut = new Rank();
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(new String [] {"AB", "BC", "CA", "DC", "AC"});

        sut.assignMetric(g.getNodes());
        assertEquals(0.3725, sut.getMetric(g.getNode("A")), m_delta);
        assertEquals(0.1958, sut.getMetric(g.getNode("B")), m_delta);
        assertEquals(0.3941, sut.getMetric(g.getNode("C")), m_delta);
        assertEquals(0.0375, sut.getMetric(g.getNode("D")), m_delta);
    }

    @Test
    void test4() {
        Rank sut = new Rank();
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(new String [] {"AB", "AC"});

        sut.assignMetric(g.getNodes());
        assertEquals(.260, sut.getMetric(g.getNode("A")), m_delta);
        assertEquals(.370, sut.getMetric(g.getNode("B")), m_delta);
        assertEquals(.370, sut.getMetric(g.getNode("C")), m_delta);
    }

    @Test
    void test5() {
        Rank sut = new Rank();
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(new String [] {"AB", "AB", "AC"});

        sut.assignMetric(g.getNodes());
        assertEquals(.260, sut.getMetric(g.getNode("A")), m_delta);
        assertEquals(.4069, sut.getMetric(g.getNode("B")), m_delta);
        assertEquals(.3333, sut.getMetric(g.getNode("C")), m_delta);
    }

    @Test
    void testWikipediaExample() {
        Rank sut = new Rank();
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(new String [] {"BC", "CB", "DA", "DB", "ED", "EB", "EF", "FB", "FE", "GB", "GE", "HB", "HE", "IB", "IE", "JE", "KE"});

        sut.assignMetric(g.getNodes());
        assertEquals(3.3, 100 * sut.getMetric(g.getNode("A")), 1.0e-1);
        assertEquals(38.4, 100 * sut.getMetric(g.getNode("B")), 1.0e-1);
        assertEquals(34.3, 100 * sut.getMetric(g.getNode("C")), 1.0e-1);
        assertEquals(3.9, 100 * sut.getMetric(g.getNode("D")), 1.0e-1);
        assertEquals(8.1, 100 * sut.getMetric(g.getNode("E")), 1.0e-1);
        assertEquals(3.9, 100 * sut.getMetric(g.getNode("F")), 1.0e-1);
        assertEquals(1.6, 100 * sut.getMetric(g.getNode("G")), 1.0e-1);
        assertEquals(1.6, 100 * sut.getMetric(g.getNode("H")), 1.0e-1);
        assertEquals(1.6, 100 * sut.getMetric(g.getNode("I")), 1.0e-1);
        assertEquals(1.6, 100 * sut.getMetric(g.getNode("J")), 1.0e-1);
        assertEquals(1.6, 100 * sut.getMetric(g.getNode("K")), 1.0e-1);
    }

    /*public static Graph toyGraph() {
        Graph g = new SingleGraph("test", false, true);
        String[] edgeIds = {"BC", "CB", "DA", "DB", "ED", "EB", "EF", "FB", "FE", "GB", "GE", "HB", "HE", "IB", "IE", "JE", "KE"};
        for (String id : edgeIds)
            g.addEdge(id, id.substring(0, 1), id.substring(1,2), true);
        return g;
    }

    @Test
    public void testRank() {
        Graph g = toyGraph();
        PageRank pr = new PageRank();
        pr.init(g);
        pr.compute();

        assertEquals(3.3, 100 * pr.getRank(g.getNode("A")), 1.0e-1);
        assertEquals(38.4, 100 * pr.getRank(g.getNode("B")), 1.0e-1);
        assertEquals(34.3, 100 * pr.getRank(g.getNode("C")), 1.0e-1);
        assertEquals(3.9, 100 * pr.getRank(g.getNode("D")), 1.0e-1);
        assertEquals(8.1, 100 * pr.getRank(g.getNode("E")), 1.0e-1);
        assertEquals(3.9, 100 * pr.getRank(g.getNode("F")), 1.0e-1);
        assertEquals(1.6, 100 * pr.getRank(g.getNode("G")), 1.0e-1);
        assertEquals(1.6, 100 * pr.getRank(g.getNode("H")), 1.0e-1);
        assertEquals(1.6, 100 * pr.getRank(g.getNode("I")), 1.0e-1);
        assertEquals(1.6, 100 * pr.getRank(g.getNode("J")), 1.0e-1);
        assertEquals(1.6, 100 * pr.getRank(g.getNode("K")), 1.0e-1);

    }*/
}
