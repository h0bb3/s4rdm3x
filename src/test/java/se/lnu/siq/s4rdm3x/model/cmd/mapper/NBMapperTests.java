package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.NodeGenerator;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import weka.classifiers.functions.SGDText;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class NBMapperTests {

    private static class SUT extends NBMapper {
        public SUT(ArchDef a_arch) {
            super(a_arch, true, true, true, false, 0, 0.9);
        }

        private String getDependencyStringFromNode(CNode a_from, Iterable<ClusteredNode> a_tos) {
            try {

                Method sutMethod = IRMapperBase.class.getDeclaredMethod("getDependencyStringFromNode", CNode.class, Iterable.class);
                sutMethod.setAccessible(true);
                return (String)sutMethod.invoke(this, a_from, a_tos);
            } catch (Exception e) {
                assertEquals(true, false);
            }
            return null;
        }

        private String getDependencyStringToNode(CNode a_to, Iterable<ClusteredNode> a_froms) {
            try {

                Method sutMethod = IRMapperBase.class.getDeclaredMethod("getDependencyStringToNode", CNode.class, Iterable.class);
                sutMethod.setAccessible(true);
                return (String)sutMethod.invoke(this, a_to, a_froms);
            } catch (Exception e) {
                assertEquals(true, false);
            }
            return null;
        }
    }

    private static class SUT_data {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Returns, new String [] {"AB", "BC", "CA", "DC", "AC", "AB"});
        CNode a = g.getNode("A");
        CNode b = g.getNode("B");
        CNode c = g.getNode("C");
        CNode d = g.getNode("D");   // this is the orphan
        ArchDef arch = new ArchDef();
        ArchDef.Component c1 = arch.addComponent("Component1");
        ArchDef.Component c2 = arch.addComponent("Component2");

        {
            c1.clusterToNode(a, ArchDef.Component.ClusteringType.Initial);
            c1.mapToNode(a);
            c1.clusterToNode(b, ArchDef.Component.ClusteringType.Initial);
            c1.mapToNode(b);

            c2.clusterToNode(c, ArchDef.Component.ClusteringType.Initial);
            c2.mapToNode(c);

            c2.mapToNode(d);    // this is the orphan
        }

    }

    @Test
    public void getDependencyStringFromNode_test() {
        NodeGenerator ng = new NodeGenerator();
        ArchDef a = new ArchDef();
        ArchDef.Component c1, c2;

        c1 = a.addComponent("Component1");
        c2 = a.addComponent("Component2");
        CGraph g = ng.generateGraph(dmDependency.Type.Implements, new String [] {"AB", "BC", "CA", "DC", "AC"});
        c1.mapToNode(g.getNode("A"));
        c1.clusterToNode(g.getNode("A"), ArchDef.Component.ClusteringType.Initial);

        c1.mapToNode(g.getNode("B"));
        c1.clusterToNode(g.getNode("B"), ArchDef.Component.ClusteringType.Initial);

        c1.mapToNode(g.getNode("C"));   // this should be component 1 for the mapping and component 2 for the clustering
        c2.clusterToNode(g.getNode("C"), ArchDef.Component.ClusteringType.Automatic);

        SUT sut = new SUT(null);

        ArrayList<MapperBase.ClusteredNode> nodes = new ArrayList<>();

        nodes.add(new MapperBase.ClusteredNode(g.getNode("A"), a));
        nodes.add(new MapperBase.ClusteredNode(g.getNode("B"), a));
        nodes.add(new MapperBase.ClusteredNode(g.getNode("C"), a));

        String expected = "Component1ImplementsComponent1 Component1ImplementsComponent2";
        String actual = sut.getDependencyStringFromNode(g.getNode("A"), nodes);
        assertEquals(expected, actual);

        expected = "Component1ImplementsComponent2";
        actual = sut.getDependencyStringFromNode(g.getNode("B"), nodes);
        assertEquals(expected, actual);

        expected = "Component2ImplementsComponent1";
        actual = sut.getDependencyStringFromNode(g.getNode("C"), nodes);
        assertEquals(expected, actual);
    }

    @Test
    public void getDependencyStringToNode_test() {
        NodeGenerator ng = new NodeGenerator();
        ArchDef a = new ArchDef();
        ArchDef.Component c1, c2;

        c1 = a.addComponent("Component1");
        c2 = a.addComponent("Component2");
        CGraph g = ng.generateGraph(dmDependency.Type.LocalVar, new String [] {"AB", "BC", "CA", "DC", "AC"});
        c1.mapToNode(g.getNode("A"));
        c1.clusterToNode(g.getNode("A"), ArchDef.Component.ClusteringType.Initial);

        c1.mapToNode(g.getNode("B"));
        c1.clusterToNode(g.getNode("B"), ArchDef.Component.ClusteringType.Initial);

        c1.mapToNode(g.getNode("C"));   // this should be component 1 for the mapping and component 2 for the clustering
        c2.clusterToNode(g.getNode("C"), ArchDef.Component.ClusteringType.Automatic);


        SUT sut = new SUT(null);

        ArrayList<MapperBase.ClusteredNode> nodes = new ArrayList<>();

        nodes.add(new MapperBase.ClusteredNode(g.getNode("A"), a));
        nodes.add(new MapperBase.ClusteredNode(g.getNode("B"), a));
        nodes.add(new MapperBase.ClusteredNode(g.getNode("C"), a));

        String expected = "Component2LocalVarComponent1";

        String actual = sut.getDependencyStringToNode(g.getNode("A"), nodes);
        assertEquals(expected, actual);

        expected = ""; // actually should be "Component1LocalVarComponent1" but as both components are the same no relations should be generated
        actual = sut.getDependencyStringToNode(g.getNode("B"), nodes);
        assertEquals(expected, actual);

        expected = "Component1LocalVarComponent2 Component1LocalVarComponent2";
        actual = sut.getDependencyStringToNode(g.getNode("C"), nodes);
        assertEquals(expected, actual);
    }

    @Test
    void getTrainingData() {

        SUT_data sd = new SUT_data();
        SUT sut = new SUT(sd.arch);
        StringToWordVector filter = new StringToWordVector();
        filter.setOutputWordCounts(true);


        Instances actual = sut.getTrainingData(sut.getInitiallyMappedNodes(sd.g), sd.arch, filter, null);

        NBMapper.Classifier nbc = new NBMapper.Classifier();

        try {
            nbc.buildClassifier(actual);
            sut.adjustClassProbabilities(sut.getInitiallyMappedNodes(sd.g), nbc);
            double [] classProbs = nbc.getProbabilityOfClass();

            assertEquals(classProbs[0], 2.0/3);
            assertEquals(classProbs[1], 1.0/3);

        } catch (Exception e) {
            assertFalse(true);
            e.printStackTrace();
        }


        System.out.println(actual);
        Attribute classAttribute = actual.classAttribute();

        // each clustered node (a, b, c) will create an 2 instances one for node name and one for cda
        assertEquals(6, actual.size());

        // the instance is a word matrix representation lets grab it.
        // and create a hashmap

        class CountWeight {
            int m_count;
            double m_weight;
        }
        class AttribMap extends HashMap<String, CountWeight> {
            boolean m_componentFound = false;

            void assertTrue(String a_name, int a_expectedCount, double a_expectedWeight) {
                CountWeight cw = get(a_name);
                assertNotNull(cw, "No Attribute found for name: "  + a_name);

                assertEquals(cw.m_count, a_expectedCount, "Attribute count differs for: " + a_name);
                assertEquals(cw.m_weight, a_expectedWeight, "Attribute weight differs for: " + a_name);

            }

            public void assertNotFound(String a_name) {
                CountWeight cw = get(a_name);
                assertNotNull(cw, "No Attribute found for name: "  + a_name);
                assertEquals(cw.m_count, 0, "Attribute found for " + a_name);
            }
        }

        Map<String, AttribMap> componentMap = new HashMap<>();
        componentMap.put(sd.c1.getName(), new AttribMap());
        componentMap.put(sd.c2.getName(), new AttribMap());


        for (Instance inst : actual) {

            Enumeration<Attribute> attribs = actual.enumerateAttributes();

            System.out.print("class: " + classAttribute.value((int)inst.value(classAttribute)) + " ");
            while (attribs.hasMoreElements()) {
                Attribute attr = attribs.nextElement();
                System.out.print(attr.name() + ":#" + (int)inst.value(attr) + ":w" + attr.weight() + " - ");

                String cName = classAttribute.value((int)inst.value(classAttribute));
                AttribMap attribMap = componentMap.get(cName);
                assertNotNull(attribMap, "could not find component: " + cName);
                attribMap.m_componentFound = true;
                if (!attribMap.containsKey(attr.name() )) {
                    attribMap.put(attr.name() , new CountWeight());
                }
                CountWeight cw = attribMap.get(attr.name());
                cw.m_count += (int)inst.value(attr);
                cw.m_weight = attr.weight();    // what happens if the same attribute has different weights... currently this should not happen but....
            }
            System.out.println("");
        }

        // check that all components have been found
        for (String name : componentMap.keySet()) {
            AttribMap am = componentMap.get(name);
            assertTrue(am.m_componentFound, "Component Has No Attributes assigned: " + name);
        }

        // now we can check the expected attributes
        // c1 contains node a and b with and gets the name as a feature
        componentMap.get(sd.c1.getName()).assertTrue(sd.a.getName().toLowerCase(), 1, 1.0);
        componentMap.get(sd.c1.getName()).assertTrue(sd.b.getName().toLowerCase(), 1, 1.0);
        componentMap.get(sd.c2.getName()).assertTrue(sd.c.getName().toLowerCase(), 1, 1.0);

        componentMap.get(sd.c1.getName()).assertNotFound(sd.c.getName().toLowerCase());
        componentMap.get(sd.c2.getName()).assertNotFound(sd.a.getName().toLowerCase());
        componentMap.get(sd.c2.getName()).assertNotFound(sd.b.getName().toLowerCase());

        // check the CDA relations
        // {"AB", "BC", "CA", "DC", "AC", "AB"}
        // A -> Component1
        // B -> Component1
        // C -> Component2
        // Returns
        // -> Component1ReturnsComponent1, Component1ReturnsComponent2, Component2ReturnsComponent1, Not in Clustered Nodes, Component1ReturnsComponent2, Component1ReturnsComponent1
        //
        // CDA words should be added for both components involved
        // c1                               c2
        // Component1ReturnsComponent1
        // Component1ReturnsComponent2      Component1ReturnsComponent2
        // Component2ReturnsComponent1      Component2ReturnsComponent1
        // Component1ReturnsComponent2      Component1ReturnsComponent2
        // Component1ReturnsComponent1
        //
        // Component1ReturnsComponent1: 2   0
        // Component1ReturnsComponent2: 2   2
        // Component2ReturnsComponent1: 1   1
        componentMap.get(sd.c1.getName()).assertTrue("Component1ReturnsComponent1", 2, 1.0);
        componentMap.get(sd.c2.getName()).assertTrue("Component1ReturnsComponent1", 0, 1.0);

        componentMap.get(sd.c1.getName()).assertTrue("Component1ReturnsComponent2", 2, 1.0);
        componentMap.get(sd.c2.getName()).assertTrue("Component1ReturnsComponent2", 2, 1.0);

        componentMap.get(sd.c1.getName()).assertTrue("Component2ReturnsComponent1", 1, 1.0);
        componentMap.get(sd.c2.getName()).assertTrue("Component2ReturnsComponent1", 1, 1.0);
    }

    @Test
    void getPredictionData() {


        SUT_data sd = new SUT_data();
        SUT sut = new SUT(sd.arch);
        StringToWordVector filter = new StringToWordVector();
        filter.setOutputWordCounts(true);


        // as if mapped to c1
        Instances actual = sut.getPredictionDataForNode(new MapperBase.OrphanNode(sd.d, sd.arch), sut.getInitiallyMappedNodes(sd.g), sd.arch.getComponentNames(), sd.c1, filter, null);

        System.out.println(actual);
        Attribute classAttribute = actual.classAttribute();

        // node will create 1 instance
        assertEquals(1, actual.size());

        // the instance is a word matrix representation lets grab it.
        // and create a hashmap

        class CountWeight {
            int m_count;
            double m_weight;
        }
        class AttribMap extends HashMap<String, CountWeight> {
            boolean m_componentFound = false;

            void assertTrue(String a_name, int a_expectedCount, double a_expectedWeight) {
                CountWeight cw = get(a_name);
                if (a_expectedCount > 1) {
                    assertNotNull(cw, "No Attribute found for name: " + a_name);
                } else if (cw == null) {
                    return;
                }
                assertEquals(a_expectedCount, cw.m_count, "Attribute count differs for: " + a_name);
                assertEquals(a_expectedWeight, cw.m_weight, "Attribute weight differs for: " + a_name);
            }

            public void assertNotFound(String a_name) {
                CountWeight cw = get(a_name);
                assertNotNull(cw, "No Attribute found for name: "  + a_name);
                assertEquals(0, cw.m_count, "Attribute found for " + a_name);
            }

            public void assertIsNull(String a_name) {
                CountWeight cw = get(a_name);
                assertNull(cw, "Attribute found for name: "  + a_name);
            }
        }

        Map<String, AttribMap> componentMap = new HashMap<>();
        componentMap.put(sd.c2.getName(), new AttribMap());


        for (Instance inst : actual) {

            Enumeration<Attribute> attribs = actual.enumerateAttributes();

            System.out.print("class: " + classAttribute.value((int)inst.value(classAttribute)) + " ");
            while (attribs.hasMoreElements()) {
                Attribute attr = attribs.nextElement();
                System.out.print(attr.name() + ":#" + (int)inst.value(attr) + ":w" + attr.weight() + " - ");

                String cName = classAttribute.value((int)inst.value(classAttribute));
                AttribMap attribMap = componentMap.get(cName);
                assertNotNull(attribMap, "could not find component: " + cName);
                attribMap.m_componentFound = true;
                if (!attribMap.containsKey(attr.name() )) {
                    attribMap.put(attr.name() , new CountWeight());
                }
                CountWeight cw = attribMap.get(attr.name());
                cw.m_count += (int)inst.value(attr);
                cw.m_weight = attr.weight();    // what happens if the same attribute has different weights... currently this should not happen but....
            }
            System.out.println("");
        }

        // check that all components have been found

        AttribMap am = componentMap.get(sd.c2.getName());
        assertTrue(am.m_componentFound, "Component Has No Attributes assigned: " + sd.c1.getName());


        // now we can check the expected attributes
        // c1 contains node a and b with and gets the name as a feature
        componentMap.get(sd.c2.getName()).assertTrue(sd.d.getName().toLowerCase(), 1, 1.0);
        componentMap.get(sd.c2.getName()).assertIsNull(sd.a.getName().toLowerCase());
        componentMap.get(sd.c2.getName()).assertIsNull(sd.b.getName().toLowerCase());
        componentMap.get(sd.c2.getName()).assertIsNull(sd.c.getName().toLowerCase());

        // check the CDA relations
        // {"AB", "BC", "CA", "DC", "AC", "AB"}
        // A -> Component1
        // B -> Component1
        // C -> Component2
        // D -> Component1
        // Returns
        // -> -, -, -, Component1ReturnsComponent2, -, -
        //
        // Component1ReturnsComponent2: 1
        componentMap.get(sd.c2.getName()).assertTrue("Component1ReturnsComponent2", 1, 1.0);
        componentMap.get(sd.c2.getName()).assertTrue("Component1ReturnsComponent1", 0, 1.0);
        componentMap.get(sd.c2.getName()).assertTrue("Component2ReturnsComponent2", 0, 1.0);
    }

    @Test
    void runTest() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Returns, new String [] {"AB", "BC", "CA", "DC", "AC"});
        CNode a = g.getNode("A");
        CNode b = g.getNode("B");
        CNode c = g.getNode("C");
        CNode d = g.getNode("D");   // this is the orphan

        ArchDef arch = new ArchDef();
        ArchDef.Component c1 = arch.addComponent("Component1");
        ArchDef.Component c2 = arch.addComponent("Component2");
        c1.clusterToNode(a, ArchDef.Component.ClusteringType.Initial);
        c1.mapToNode(a);
        c1.clusterToNode(b, ArchDef.Component.ClusteringType.Initial);
        c1.mapToNode(b);
        c2.clusterToNode(c, ArchDef.Component.ClusteringType.Initial);
        c2.mapToNode(c);


        c1.mapToNode(d);


        NBMapper sut = new NBMapper(arch, true, true, true, false, 0, 0.9);

        sut.run(g);

    }

    @Test
    void deCamelCaseTest() {
        NBMapper sut = new NBMapper(null, true, true, true, false, 0, 0.9);

        assertEquals("test", sut.deCamelCase("test", 3, null));
        assertEquals("test test", sut.deCamelCase("testTest", 3, null));
        assertEquals("test", sut.deCamelCase("Test", 3, null));
        assertEquals("test test", sut.deCamelCase("TestTest", 3, null));
        assertEquals("test test", sut.deCamelCase("TestTEST", 3, null));
        assertEquals("test test", sut.deCamelCase("Test_TEST", 3, null));
        assertEquals("test test", sut.deCamelCase("test_test", 3, null));
        assertEquals("test test", sut.deCamelCase("test-test", 3, null));
        assertEquals("test test test", sut.deCamelCase("test-testTest", 3, null));
        assertEquals("test", sut.deCamelCase("test-te", 3, null));
        assertEquals("", sut.deCamelCase("teTe", 3, null));
        assertEquals("test test test testing", sut.deCamelCase("testTest testTesting", 3, null));

        assertEquals("test test test test", sut.deCamelCase("testTest testTesting", 3, new weka.core.stemmers.SnowballStemmer()));
    }

    @Test
    void getMaxIndicesTest() {
        NBMapper sut = new NBMapper(null, true, true, true, false, 0, 0.9);

        assertEquals(0, sut.getMaxIndices(new double[]{1, 0})[0]);
        assertEquals(1, sut.getMaxIndices(new double[]{1, 0})[1]);
        assertEquals(1, sut.getMaxIndices(new double[]{0, 1})[0]);
        assertEquals(0, sut.getMaxIndices(new double[]{0, 1})[1]);
        assertEquals(3, sut.getMaxIndices(new double[]{0, 1, 2, 4, 3})[0]);
        assertEquals(4, sut.getMaxIndices(new double[]{0, 1, 2, 4, 3})[1]);
        assertEquals(3, sut.getMaxIndices(new double[]{3, 1, 2, 4, 4})[0]);
        assertEquals(0, sut.getMaxIndices(new double[]{3, 1, 2, 4})[1]);
        assertEquals(4, sut.getMaxIndices(new double[]{3, 1, 2, 4, 4})[1]);
        assertEquals(1, sut.getMaxIndices(new double[]{3, 5, 2, 4, 4})[0]);
        assertEquals(3, sut.getMaxIndices(new double[]{3, 5, 2, 4, 4})[1]);
    }
}
