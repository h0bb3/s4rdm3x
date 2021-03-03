package se.lnu.siq.s4rdm3x.model;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.NodeGenerator;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;

import static org.junit.jupiter.api.Assertions.*;

class CNodeTest {

    @Test
    void getDependenciesTest1() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.MethodCall, new String [] {"AB"});
        CNode a = g.getNode("A");
        CNode b = g.getNode("B");

        assertEquals(1, count(a.getDependencies(b)));
        assertEquals(1, count(a.getDependencies(g.getNodes())));
        assertEquals(0, count(b.getDependencies(a)));
        assertEquals(0, count(b.getDependencies(g.getNodes())));
    }

    private int count(Iterable<?> a_items) {
        int count[] = new int[] {0};
        a_items.forEach(i -> {count[0]++;});
        return count[0];
    }

    @Test
    void getDependenciesTest2() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.MethodCall, new String [] {"AA"});
        CNode a = g.getNode("A");

        assertEquals(1, count(a.getDependencies(a)));
        assertEquals(1, count(a.getDependencies(g.getNodes())));
    }

    @Test
    void getDependenciesTest3() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.MethodCall, new String [] {"AB","AC","BC"});
        CNode a = g.getNode("A");
        CNode b = g.getNode("B");
        CNode c = g.getNode("C");

        assertEquals(0, count(a.getDependencies(a)));
        assertEquals(1, count(a.getDependencies(b)));
        assertEquals(1, count(a.getDependencies(c)));
        assertEquals(2, count(a.getDependencies(g.getNodes())));

        assertEquals(0, count(b.getDependencies(a)));
        assertEquals(0, count(b.getDependencies(b)));
        assertEquals(1, count(b.getDependencies(c)));
        assertEquals(1, count(b.getDependencies(g.getNodes())));

        assertEquals(0, count(c.getDependencies(a)));
        assertEquals(0, count(c.getDependencies(b)));
        assertEquals(0, count(c.getDependencies(c)));
        assertEquals(0, count(c.getDependencies(g.getNodes())));
    }

    @Test
    void getDependenciesTest4() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.MethodCall, new String [] {"AB","AC","BC"});
        CNode a = g.getNode("A");
        CNode b = g.getNode("B");
        CNode c = g.getNode("C");

        class NoBsFilter implements CNode.DependencyFilter {

            @Override
            public boolean filter(CNode a_from, dmDependency a_dep, CNode a_to) {
                return a_from != b && a_to != b;
            }
        }
        NoBsFilter f = new NoBsFilter();

        assertEquals(0, count(a.getDependencies(a, f)));
        assertEquals(0, count(a.getDependencies(b, f)));
        assertEquals(1, count(a.getDependencies(c, f)));
        assertEquals(1, count(a.getDependencies(g.getNodes(), f)));

        assertEquals(0, count(b.getDependencies(a, f)));
        assertEquals(0, count(b.getDependencies(b, f)));
        assertEquals(0, count(b.getDependencies(c, f)));
        assertEquals(0, count(b.getDependencies(g.getNodes(), f)));

        assertEquals(0, count(c.getDependencies(a, f)));
        assertEquals(0, count(c.getDependencies(b, f)));
        assertEquals(0, count(c.getDependencies(c, f)));
        assertEquals(0, count(c.getDependencies(g.getNodes(), f)));
    }
}