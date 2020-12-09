package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.MagicInvoker;
import se.lnu.siq.s4rdm3x.dmodel.NodeGenerator;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class NBMapperTests {

    private static class SUT extends NBMapper {
        public SUT(ArchDef a_arch) {
            super(a_arch, true, true, true, false, 0, 0.9);
        }



        private String getDependencyStringFromNode(CNode a_from, Iterable<ClusteredNode> a_tos) {
            MagicInvoker mi = new MagicInvoker(this);
            return (String)mi.invokeMethodMagic(a_from, a_tos);
        }

        private String getDependencyStringToNode(CNode a_to, Iterable<ClusteredNode> a_froms) {
            MagicInvoker mi = new MagicInvoker(this);
            return (String)mi.invokeMethodMagic(a_to, a_froms);
        }

        private ArrayList<Instance> findInstances(String a_className, Instances a_instances) {
            ArrayList<Instance> ret = new ArrayList<>();
            Attribute classAttribute = a_instances.classAttribute();
            for (Instance i : a_instances) {
                if (a_className.equals(classAttribute.value((int)i.value(classAttribute)))) {
                    ret.add(i);
                }
            }

            return ret;
        }

        void assertClassCount(int a_expected, Instances a_instances) {
            Set<String> classes = new HashSet<>();
            Attribute classAttribute = a_instances.classAttribute();
            for (Instance i : a_instances) {
                classes.add(classAttribute.value((int)i.value(classAttribute)));
            }

            assertEquals(a_expected, classes.size());
        }

        int sumAttributeCount(Iterable<Instance> a_instances, String a_attributeName) {
            int sum = 0;

            for (Instance i : a_instances) {

                Enumeration<Attribute> attribs = i.enumerateAttributes();
                while (attribs.hasMoreElements()) {
                    Attribute a = attribs.nextElement();
                    if (a.name().equalsIgnoreCase(a_attributeName)) {
                        sum += (int)i.value(a);
                    }
                }
            }
            return sum;
        }

        double getAttributeWeight(Iterable<Instance> a_instances, String a_attributeName) {

            for (Instance i : a_instances) {

                Enumeration<Attribute> attribs = i.enumerateAttributes();

                while (attribs.hasMoreElements()) {
                    Attribute a = attribs.nextElement();
                    if (a.name().equalsIgnoreCase(a_attributeName)) {
                        if ((int)i.value(a) > 0) {
                            return a.weight();
                        }
                    }
                }
            }
            return -1;
        }

        void assertAttributeTrue(String a_className, String a_attributeName, Instances a_instances, int a_expectedCount, double a_expectedWeight) {
            ArrayList<Instance> iForClass = findInstances(a_className, a_instances);

            assertTrue(iForClass.size() > 0 , "No Instances found for class: "  + a_className);

            assertEquals(a_expectedCount, sumAttributeCount(iForClass, a_attributeName), "Attribute count differs for: " + a_className + "." + a_attributeName);
            assertEquals(a_expectedWeight, getAttributeWeight(iForClass, a_attributeName), "Attribute weight differs for: " + a_className + "." + a_attributeName);
        }

        public void assertAttributeNotFound(String a_className, String a_attributeName, Instances a_instances) {
            assertAttributeTrue(a_className, a_attributeName, a_instances, 0, -1);
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
        printInstances(actual);

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


        // each clustered node (a, b, c) will create an 2 instances one for node name and one for cda
        assertEquals(6, actual.size());

        // check that all components have been found
        for (ArchDef.Component c : sd.arch.getComponents()) {
            assertTrue(sut.findInstances(c.getName(), actual).size() > 0, "Component Has No Instances Assigned: " + c.getName());
        }

        // now we can check the expected attributes
        // c1 contains node a and b with and gets the name as a feature
        sut.assertAttributeTrue(sd.c1.getName(), sd.a.getName().toLowerCase(), actual, 1, 1.0);
        sut.assertAttributeTrue(sd.c1.getName(), sd.b.getName().toLowerCase(), actual, 1, 1.0);
        sut.assertAttributeTrue(sd.c2.getName(), sd.c.getName().toLowerCase(), actual, 1, 1.0);

        sut.assertAttributeNotFound(sd.c1.getName(), sd.c.getName().toLowerCase(), actual);
        sut.assertAttributeNotFound(sd.c1.getName(), sd.d.getName().toLowerCase(), actual);

        sut.assertAttributeNotFound(sd.c2.getName(), sd.a.getName().toLowerCase(), actual);
        sut.assertAttributeNotFound(sd.c2.getName(), sd.b.getName().toLowerCase(), actual);
        sut.assertAttributeNotFound(sd.c2.getName(), sd.d.getName().toLowerCase(), actual);


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
        sut.assertAttributeTrue(sd.c1.getName(), "Component1ReturnsComponent1", actual, 2, 1.0);
        sut.assertAttributeTrue(sd.c2.getName(), "Component1ReturnsComponent1", actual, 0, -1.0);

        sut.assertAttributeTrue(sd.c1.getName(), "Component1ReturnsComponent2", actual, 2, 1.0);
        sut.assertAttributeTrue(sd.c2.getName(), "Component1ReturnsComponent2", actual, 2, 1.0);

        sut.assertAttributeTrue(sd.c1.getName(), "Component2ReturnsComponent1", actual, 1, 1.0);
        sut.assertAttributeTrue(sd.c2.getName(), "Component2ReturnsComponent1", actual, 1, 1.0);
    }

    private void printInstances(Instances a_insts) {
        Attribute classAttribute = a_insts.classAttribute();
        for (Instance inst : a_insts) {

            Enumeration<Attribute> attribs = a_insts.enumerateAttributes();

            System.out.print("class: " + classAttribute.value((int)inst.value(classAttribute)) + " ");
            while (attribs.hasMoreElements()) {
                Attribute attr = attribs.nextElement();
                System.out.print(attr.name() + ":#" + (int)inst.value(attr) + ":w" + attr.weight() + " - ");

                String cName = classAttribute.value((int)inst.value(classAttribute));
            }
            System.out.println("");
        }
    }

    @Test
    void getPredictionData() {


        SUT_data sd = new SUT_data();
        SUT sut = new SUT(sd.arch);
        StringToWordVector filter = new StringToWordVector();
        filter.setOutputWordCounts(true);


        // as if mapped to c1
        Instances actual = sut.getPredictionDataForNode(new MapperBase.OrphanNode(sd.d, sd.arch), sut.getInitiallyMappedNodes(sd.g), sd.arch.getComponentNames(), sd.c1, filter, null);

        printInstances(actual);
        // node will create 1 instance
        assertEquals(1, actual.size());


        // check that all components have been found

        assertEquals(1, actual.size(), "Too many Instances found");
        assertEquals(1, sut.findInstances(sd.c2.getName(), actual).size(), "Component Has No Instances assigned: " + sd.c1.getName());


        // now we can check the expected attributes
        // c1 contains node a and b with and gets the name as a feature
        sut.assertAttributeTrue(sd.c2.getName(), sd.d.getName().toLowerCase(), actual,1, 1);
        sut.assertAttributeNotFound(sd.c2.getName(), sd.a.getName().toLowerCase(), actual);
        sut.assertAttributeNotFound(sd.c2.getName(), sd.b.getName().toLowerCase(), actual);
        sut.assertAttributeNotFound(sd.c2.getName(), sd.c.getName().toLowerCase(), actual);

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
        sut.assertAttributeTrue(sd.c2.getName(), "Component1ReturnsComponent2", actual,1, 1);
        sut.assertAttributeNotFound(sd.c2.getName(), "Component1ReturnsComponent1", actual);
        sut.assertAttributeNotFound(sd.c2.getName(), "Component2ReturnsComponent2", actual);
        sut.assertAttributeNotFound(sd.c2.getName(), "Component2ReturnsComponent1", actual);
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
