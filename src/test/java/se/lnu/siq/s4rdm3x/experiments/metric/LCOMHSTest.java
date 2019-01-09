package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Node;
import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.NodeGenerator;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LCOMHSTest {

    static final String g_classesPkg = "se.lnu.siq.s4rdm3x.dmodel.classes.";
    static final String g_classesDir = "se/lnu/siq/s4rdm3x/dmodel/classes/";

    @Test
    void test1() {
        LCOMHS sut = new LCOMHS();
        NodeGenerator ng = new NodeGenerator();
        CNode a = ng.loadNode("LCOMTest1");

        sut.assignMetric(Arrays.asList(a));

        assertEquals(1, sut.getMetric(a));
    }

    @Test
    void test2() {
        LCOMHS sut = new LCOMHS();
        NodeGenerator ng = new NodeGenerator();
        CNode a = ng.loadNode("ArrayTest");

        sut.assignMetric(Arrays.asList(a));

        assertEquals(0.875, sut.getMetric(a));
    }


    @Test
    void test3() {
        LCOMHS sut = new LCOMHS();
        NodeGenerator ng = new NodeGenerator();

        CNode a = ng.loadNode("InnerClassTest");
        sut.assignMetric(Arrays.asList(a));

        assertEquals(1.333, sut.getMetric(a), 0.001);
    }


    @Test
    void test4() {
        LCOMHS sut = new LCOMHS();
        NodeGenerator ng = new NodeGenerator();

        CNode a = ng.loadNode("NCSS_Test72");
        sut.assignMetric(Arrays.asList(a));

        assertEquals(0.733, sut.getMetric(a), 0.001);
    }


    @Test
    void test5() {
        LCOMHS sut = new LCOMHS();
        NodeGenerator ng = new NodeGenerator();


        CNode a = ng.loadNode("SelfCall");
        sut.assignMetric(Arrays.asList(a));
        assertEquals(0, sut.getMetric(a), 0.001);
    }
}
