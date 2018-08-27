package se.lnu.siq.s4rdm3x.cmd.util;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;

import static org.junit.jupiter.api.Assertions.*;

class FanInCacheTest {

    Graph m_g;
    Node m_n1, m_n2, m_n3;
    dmClass m_c1_1, m_c1_2;;
    dmClass m_c2_1, m_c2_2;
    dmClass m_c3_1, m_c3_2;

    @BeforeEach
    void setUp() {

        m_g = new MultiGraph("test");
        NodeUtil nu = new NodeUtil(m_g);
        AttributeUtil au = new AttributeUtil();

        m_n1 = nu.createNode("tn1");
        m_n2 = nu.createNode("tn2");
        m_n3 = nu.createNode("tn3");

        dmClass c3_1, c3_2;

        m_c1_1 = new dmClass("test/class_1_1.java");
        m_c1_2 = new dmClass("test/class_1_2.java");
        au.addClass(m_n1, m_c1_1);
        au.addClass(m_n1, m_c1_2);

        m_c2_1 = new dmClass("test/class_2_1.java");
        m_c2_2 = new dmClass("test/class_2_2.java");
        au.addClass(m_n2, m_c2_1);
        au.addClass(m_n2, m_c2_2);

        m_c3_1 = new dmClass("test/class_3_1.java");
        m_c3_2 = new dmClass("test/class_3_2.java");
        au.addClass(m_n3, m_c3_1);
        au.addClass(m_n3, m_c3_2);

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getFanIn() {
        m_c1_1.addDependency(m_c2_1, dmDependency.Type.FieldUse, 0);
        m_c1_1.addDependency(m_c2_1, dmDependency.Type.Field, 1);
        m_c1_1.addDependency(m_c2_1, dmDependency.Type.Argument, 2);
        FanInCache fic = new FanInCache(m_g.getNodeSet());

        assertEquals(0, fic.getFanIn(m_n1));
        assertEquals(3, fic.getFanIn(m_n2));
        assertEquals(0, fic.getFanIn(m_n3));

        assertEquals(0, fic.getFanIn(m_n1, m_n2));
        assertEquals(0, fic.getFanIn(m_n1, m_n3));
        assertEquals(3, fic.getFanIn(m_n2, m_n1));
        assertEquals(0, fic.getFanIn(m_n2, m_n3));
        assertEquals(0, fic.getFanIn(m_n3, m_n1));
        assertEquals(0, fic.getFanIn(m_n3, m_n2));
    }

    @Test
    void getFanIn1() {
        m_c1_2.addDependency(m_c2_1, dmDependency.Type.FieldUse, 0);
        m_c1_2.addDependency(m_c2_1, dmDependency.Type.Field, 1);
        m_c1_2.addDependency(m_c2_1, dmDependency.Type.Argument, 2);
        FanInCache fic = new FanInCache(m_g.getNodeSet());

        assertEquals(0, fic.getFanIn(m_n1));
        assertEquals(3, fic.getFanIn(m_n2));
        assertEquals(0, fic.getFanIn(m_n3));

        assertEquals(0, fic.getFanIn(m_n1, m_n2));
        assertEquals(0, fic.getFanIn(m_n1, m_n3));
        assertEquals(3, fic.getFanIn(m_n2, m_n1));
        assertEquals(0, fic.getFanIn(m_n2, m_n3));
        assertEquals(0, fic.getFanIn(m_n3, m_n1));
        assertEquals(0, fic.getFanIn(m_n3, m_n2));
    }

    @Test
    void getFanIn2() {
        m_c1_1.addDependency(m_c2_1, dmDependency.Type.FieldUse, 0);
        m_c1_1.addDependency(m_c2_1, dmDependency.Type.Field, 1);
        m_c1_1.addDependency(m_c2_1, dmDependency.Type.Argument, 2);
        m_c1_2.addDependency(m_c2_1, dmDependency.Type.FieldUse, 0);
        m_c1_2.addDependency(m_c2_1, dmDependency.Type.Field, 1);
        m_c1_2.addDependency(m_c2_1, dmDependency.Type.Argument, 2);
        FanInCache fic = new FanInCache(m_g.getNodeSet());

        assertEquals(0, fic.getFanIn(m_n1));
        assertEquals(6, fic.getFanIn(m_n2));
        assertEquals(0, fic.getFanIn(m_n3));

        assertEquals(0, fic.getFanIn(m_n1, m_n2));
        assertEquals(0, fic.getFanIn(m_n1, m_n3));
        assertEquals(6, fic.getFanIn(m_n2, m_n1));
        assertEquals(0, fic.getFanIn(m_n2, m_n3));
        assertEquals(0, fic.getFanIn(m_n3, m_n1));
        assertEquals(0, fic.getFanIn(m_n3, m_n2));
    }
}