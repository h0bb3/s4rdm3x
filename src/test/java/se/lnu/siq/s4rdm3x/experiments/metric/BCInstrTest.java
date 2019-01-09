package se.lnu.siq.s4rdm3x.experiments.metric;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.NodeGenerator;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BCInstrTest {

    Metric m_sut = new ByteCodeInstructions();

    double getMetric(String a_javaClass) {
        NodeGenerator ng = new NodeGenerator();

        CNode a = ng.loadNode(a_javaClass);
        m_sut.assignMetric(Arrays.asList(a));

        return  m_sut.getMetric(a);
    }

    @Test
    void test1() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.getGraph1();
        CNode n1 = g.getNode("n1");

        m_sut.assignMetric(g.getNodes());
        assertEquals(2*17, m_sut.getMetric(n1));
    }

    @Test
    void test2() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.getGraph2();
        CNode n1 = g.getNode("n1");

        m_sut.assignMetric(g.getNodes());
        assertEquals(2*17, m_sut.getMetric(n1));
    }

    @Test
    void test_InterfaceTest() {
        assertEquals(0, getMetric("InterfaceTest"));
    }

    @Test
    void test_InterfaceTest2() {
        assertEquals(4, getMetric("InterfaceTest2"));
    }

    @Test
    void test_Test1() {
        assertEquals(58, getMetric("Test1"));
    }

    @Test
    void test_Test2() {
        assertEquals(14, getMetric("Test2"));
    }

    @Test
    void test_Test3() {
        assertEquals(34, getMetric("Test3"));
    }

    @Test
    void test_EnumTest() {
        assertEquals(47,  getMetric("EnumTest"));
    }

    @Test
    void test_NCSS_Test72() {
        assertEquals(170, getMetric("NCSS_Test72"));
    }

    @Test
    void test_BranchesTest() {
        assertEquals(142, getMetric("BranchesTest"));
    }

    @Test
    void test_ExceptionTest() {
        assertEquals(19, getMetric("ExceptionTest"));
    }

    @Test
    void test_StaticTest() {
        assertEquals(41, getMetric("StaticTest"));
    }

    @Test
    void test_AbstractClassTest() {
        assertEquals(18, getMetric("AbstractClassTest"));
    }

    @Test
    void test_InnerClassTest() {
        assertEquals(24, getMetric("InnerClassTest"));
    }

    @Test
    void test_AnonymousClassTest() {
        assertEquals(18, getMetric("AnonymousClassTest"));
    }

    @Test
    void test_AnonymousClassTest$1() {
        assertEquals(8, getMetric("AnonymousClassTest$1"));
    }

    @Test
    void test_JabRef_BibDatabaseContextChangedEvent() {
        assertEquals(3, getMetric("BibDatabaseContextChangedEvent"));
    }

    @Test
    void test_JabRef_WebFetcher() {
        assertEquals(2, getMetric("WebFetcher"));
    }

    @Test
    void test_JabRef_DragDropPopupPane() {
        assertEquals(36, getMetric("DragDropPopupPane"));
    }

    @Test
    void test_JabRef_DragDropPopupPane$1() {
        assertEquals(11, getMetric("DragDropPopupPane$1"));
    }

}
