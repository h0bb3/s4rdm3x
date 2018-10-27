package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.cmd.util.NodeUtil;
import se.lnu.siq.s4rdm3x.dmodel.NodeGenerator;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LCOMHSTest {

    static final String g_classesPkg = "se.lnu.siq.s4rdm3x.dmodel.classes.";
    static final String g_classesDir = "se/lnu/siq/s4rdm3x/dmodel/classes/";

    @Test
    void test1() {
        LCOMHS sut = new LCOMHS();
        NodeGenerator ng = new NodeGenerator();
        Graph g = ng.loadGraph("/" + g_classesDir + "LCOMTest1.class");
        NodeUtil nu = new NodeUtil(g);
        Node a = nu.findNode(g_classesDir + "LCOMTest1.java");

        sut.assignMetric(g.getNodeSet());

        sut.assignMetric(g.getNodeSet());
        assertEquals(1, sut.getMetric(a));
    }

    @Test
    void test2() {
        LCOMHS sut = new LCOMHS();
        NodeGenerator ng = new NodeGenerator();
        Graph g = ng.loadGraph("/" + g_classesDir + "ArrayTest.class");
        NodeUtil nu = new NodeUtil(g);
        Node a = nu.findNode(g_classesDir + "ArrayTest.java");

        sut.assignMetric(g.getNodeSet());

        sut.assignMetric(g.getNodeSet());
        assertEquals(0.875, sut.getMetric(a));
    }


    @Test
    void test3() {
        LCOMHS sut = new LCOMHS();
        NodeGenerator ng = new NodeGenerator();
        Graph g = ng.loadGraph("/" + g_classesDir + "InnerClassTest.class");
        NodeUtil nu = new NodeUtil(g);
        Node a = nu.findNode(g_classesDir + "InnerClassTest.java");

        sut.assignMetric(g.getNodeSet());

        sut.assignMetric(g.getNodeSet());
        assertEquals(1.333, sut.getMetric(a), 0.001);
    }


    @Test
    void test4() {
        LCOMHS sut = new LCOMHS();
        NodeGenerator ng = new NodeGenerator();
        Graph g = ng.loadGraph("/" + g_classesDir + "NCSS_Test72.class");
        NodeUtil nu = new NodeUtil(g);
        Node a = nu.findNode(g_classesDir + "NCSS_Test72.java");

        sut.assignMetric(g.getNodeSet());

        sut.assignMetric(g.getNodeSet());
        assertEquals(0.733, sut.getMetric(a), 0.001);
    }


    @Test
    void test5() {
        LCOMHS sut = new LCOMHS();
        NodeGenerator ng = new NodeGenerator();
        Graph g = ng.loadGraph("/" + g_classesDir + "SelfCall.class");
        NodeUtil nu = new NodeUtil(g);
        Node a = nu.findNode(g_classesDir + "SelfCall.java");

        sut.assignMetric(g.getNodeSet());

        sut.assignMetric(g.getNodeSet());
        assertEquals(0, sut.getMetric(a), 0.001);
    }
}
