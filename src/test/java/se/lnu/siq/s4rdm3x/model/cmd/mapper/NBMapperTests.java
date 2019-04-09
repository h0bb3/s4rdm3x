package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.NodeGenerator;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.Enumeration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NBMapperTests {

    @Test
    public void getDependencyStringFromNode() {
        NodeGenerator ng = new NodeGenerator();

        CGraph g = ng.generateGraph(dmDependency.Type.Implements, new String [] {"AB", "BC", "CA", "DC", "AC"});
        g.getNode("A").setMapping("Component1");
        g.getNode("B").setMapping("Component1");
        g.getNode("C").setMapping("Component2");

        NBMapper sut = new NBMapper(null);

        String expected = "Component1ImplementsComponent1 Component1ImplementsComponent2";
        String actual = sut.getDependencyStringFromNode(g.getNode("A"), g.getNodes());

        assertEquals(expected, actual);

        expected = "Component1ImplementsComponent2";
        actual = sut.getDependencyStringFromNode(g.getNode("B"), g.getNodes());
        assertEquals(expected, actual);

        expected = "Component2ImplementsComponent1";
        actual = sut.getDependencyStringFromNode(g.getNode("C"), g.getNodes());
        assertEquals(expected, actual);
    }

    @Test
    public void getDependencyStringToNode() {
        NodeGenerator ng = new NodeGenerator();

        CGraph g = ng.generateGraph(dmDependency.Type.LocalVar, new String [] {"AB", "BC", "CA", "AC"});
        g.getNode("A").setMapping("Component1");
        g.getNode("B").setMapping("Component1");
        g.getNode("C").setMapping("Component2");

        NBMapper sut = new NBMapper(null);

        String expected = "Component2LocalVarComponent1";
        String actual = sut.getDependencyStringToNode(g.getNode("A"), g.getNodes());

        assertEquals(expected, actual);

        expected = "Component1LocalVarComponent1";
        actual = sut.getDependencyStringToNode(g.getNode("B"), g.getNodes());
        assertEquals(expected, actual);

        expected = "Component1LocalVarComponent2 Component1LocalVarComponent2";
        actual = sut.getDependencyStringToNode(g.getNode("C"), g.getNodes());
        assertEquals(expected, actual);
    }

    @Test
    void getTrainingData() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.Returns, new String [] {"AB", "BC", "CA", "DC", "AC", "AB"});
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

        c2.mapToNode(d);

        NBMapper sut = new NBMapper(arch);
        StringToWordVector filter = new StringToWordVector();
        filter.setOutputWordCounts(true);

        Instances actual = sut.getTrainingData(sut.getInitiallyMappedNodes(g), arch, filter);

        System.out.println(actual);
        Attribute classAttribute = actual.classAttribute();

        for (Instance inst : actual) {

            Enumeration<Attribute> attribs = actual.enumerateAttributes();

            System.out.print("class: " + classAttribute.value((int)inst.value(classAttribute)) + " ");
            while (attribs.hasMoreElements()) {
                Attribute attr = attribs.nextElement();
                //if (attr.isNumeric()) {
                    System.out.print(attr.name() + ":" + inst.value(attr));
                //} else {
                    //System.out.print(attr.name() + ":" + inst.stringValue(attr));
                //}
            }
            System.out.println("");
            //inst.stringValue()


        }
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


        NBMapper sut = new NBMapper(arch);

        sut.run(g);

    }

    @Test
    void deCamelCaseTest() {
        NBMapper sut = new NBMapper(null);

        assertEquals("test", sut.deCamelCase("test", 3, false));
        assertEquals("test test", sut.deCamelCase("testTest", 3, false));
        assertEquals("test", sut.deCamelCase("Test", 3, false));
        assertEquals("test test", sut.deCamelCase("TestTest", 3, false));
        assertEquals("test test", sut.deCamelCase("TestTEST", 3, false));
        assertEquals("test test", sut.deCamelCase("Test_TEST", 3, false));
        assertEquals("test test", sut.deCamelCase("test_test", 3, false));
        assertEquals("test test", sut.deCamelCase("test-test", 3, false));
        assertEquals("test test test", sut.deCamelCase("test-testTest", 3, false));
        assertEquals("test", sut.deCamelCase("test-te", 3, false));
        assertEquals("", sut.deCamelCase("teTe", 3, false));
        assertEquals("test test test testing", sut.deCamelCase("testTest testTesting", 3, false));
        assertEquals("test test test test", sut.deCamelCase("testTest testTesting", 3, true));
    }
}
