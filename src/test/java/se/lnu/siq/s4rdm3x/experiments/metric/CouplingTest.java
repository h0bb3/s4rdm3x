package se.lnu.siq.s4rdm3x.experiments.metric;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.NodeGenerator;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CouplingTest {

    @Test
    void test1() {
        CouplingIn sutIn = new CouplingIn();
        CouplingOut sutOut = new CouplingOut();
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Extends, new String [] {"AB", "AC"});
        CNode a = g.getNode("A");
        CNode b = g.getNode("B");
        CNode c = g.getNode("C");

        sutIn.assignMetric(g.getNodes());
        assertEquals(0, sutIn.getMetric(a));
        assertEquals(1, sutIn.getMetric(b));
        assertEquals(1, sutIn.getMetric(c));

        sutOut.assignMetric(g.getNodes());
        assertEquals(2, sutOut.getMetric(a));
        assertEquals(0, sutOut.getMetric(b));
        assertEquals(0, sutOut.getMetric(c));
    }

    @Test
    void test2() {
        CouplingIn sutIn = new CouplingIn();
        CouplingOut sutOut = new CouplingOut();
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Extends, new String [] {"AB", "AC"});
        ng.addToGraph(g, dmDependency.Type.MethodCall, new String [] {"AB", "AC"});
        CNode a = g.getNode("A");
        CNode b = g.getNode("B");
        CNode c = g.getNode("C");

        sutIn.assignMetric(g.getNodes());
        assertEquals(0, sutIn.getMetric(a));
        assertEquals(1, sutIn.getMetric(b));
        assertEquals(1, sutIn.getMetric(c));

        sutOut.assignMetric(g.getNodes());
        assertEquals(2, sutOut.getMetric(a));
        assertEquals(0, sutOut.getMetric(b));
        assertEquals(0, sutOut.getMetric(c));
    }

    @Test
    void testCircular() {
        CouplingIn sutIn = new CouplingIn();
        CouplingOut sutOut = new CouplingOut();
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Extends, new String [] {"AB", "AC", "AA", "BB", "CC"});
        CNode a = g.getNode("A");
        CNode b = g.getNode("B");
        CNode c = g.getNode("C");

        sutIn.assignMetric(g.getNodes());
        assertEquals(0, sutIn.getMetric(a));
        assertEquals(1, sutIn.getMetric(b));
        assertEquals(1, sutIn.getMetric(c));

        sutOut.assignMetric(g.getNodes());
        assertEquals(2, sutOut.getMetric(a));
        assertEquals(0, sutOut.getMetric(b));
        assertEquals(0, sutOut.getMetric(c));
    }
}
