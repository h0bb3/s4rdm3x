package se.lnu.siq.s4rdm3x.experiments.metric;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.NodeGenerator;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumberOfChildLevelsTest {

    @Test
    void test1() {
        NumberOfChildLevels sut = new NumberOfChildLevels();
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Extends, new String [] {"BA", "CA"});
        CNode n1 = g.getNode("A");

        sut.assignMetric(g.getNodes());
        assertEquals(1.0, sut.getMetric(n1));
    }


    @Test
    void test2() {
        NumberOfChildLevels sut = new NumberOfChildLevels();
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Extends, new String [] {"BA", "CB"});
        CNode n1 = g.getNode("A");

        sut.assignMetric(g.getNodes());
        assertEquals(2.0, sut.getMetric(n1));
    }

    @Test
    void test3() {
        NumberOfChildLevels sut = new NumberOfChildLevels();
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Extends, new String [] {"BA", "CB", "DA"});
        CNode n1 = g.getNode("A");

        sut.assignMetric(g.getNodes());
        assertEquals(2.0, sut.getMetric(n1));
    }

    @Test
    void test4() {
        NumberOfChildLevels sut = new NumberOfChildLevels();
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Extends, new String [] {"DA", "BA", "CB"});
        CNode n1 = g.getNode("A");

        sut.assignMetric(g.getNodes());
        assertEquals(2.0, sut.getMetric(n1));
    }

    @Test
    void test5() {
        NumberOfChildLevels sut = new NumberOfChildLevels();
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Extends, new String [] {"AD"});
        CNode n1 = g.getNode("A");

        sut.assignMetric(g.getNodes());
        assertEquals(0.0, sut.getMetric(n1));
    }

    @Test
    void test6() {
        NumberOfChildLevels sut = new NumberOfChildLevels();
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Implements, new String [] {"BA", "DA", "CB", "EB", "GE", "FE"});
        CNode n1 = g.getNode("A");

        sut.assignMetric(g.getNodes());
        assertEquals(3.0, sut.getMetric(n1));
    }

}
