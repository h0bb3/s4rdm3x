package se.lnu.siq.s4rdm3x.experiments.metric;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.NodeGenerator;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BCCCTest {

    Metric m_sut = new ByteCodeCyclomaticComplexity();

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
        assertEquals(17, m_sut.getMetric(n1));
    }

    @Test
    void test2() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.getGraph2();
        CNode n1 = g.getNode("n1");

        m_sut.assignMetric(g.getNodes());
        assertEquals(17, m_sut.getMetric(n1));
    }


    @Test
    void test_InterfaceTest() {
        assertEquals(0, getMetric("InterfaceTest"));
    }

    @Test
    void test_InterfaceTest2() {
        assertEquals(1, getMetric("InterfaceTest2"));
    }

    @Test
    void test_Test1() {
        assertEquals(5, getMetric("Test1"));
    }

    @Test
    void test_Test2() {
        assertEquals(6, getMetric("Test2"));
    }

    @Test
    void test_Test3() {
        assertEquals(5, getMetric("Test3"));
    }

    @Test
    void test_EnumTest() {
        final String className = "EnumTest";
        NodeGenerator ng = new NodeGenerator();

        CNode a = ng.loadNode(className);
        // enums seem to have changed the bytecode generation between java versions i.e. the synthetic method $values
        // has been added with some code in it.
        int expected = a.getClassByName(ng.getFullClassName(className)).getMethods("$values").size() == 0 ? 4 : 5;

        assertEquals(expected, getMetric("EnumTest"));
    }

    @Test
    void test_BranchesTest() {
        assertEquals(33, getMetric("BranchesTest"));
    }

    @Test
    void test_ExceptionTest() {
        assertEquals(5, getMetric("ExceptionTest"));
    }
}
