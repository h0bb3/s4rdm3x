package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.NodeGenerator;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ThesisTests {
   @Test
   public void NBTest_1() {

      ArchDef arch = new ArchDef();
      CGraph g = generateCase1(arch);
      NBMapper sut = new NBMapper(arch, true, false, false, false, 0, 0.9);

      sut.run(g);

      NBMapper.Classifier sutClassifier = sut.getClassifier();

      String sutStr = sutClassifier.toString();

      CNode o = g.getNodeByName("o");

      assertEquals(1.0/3, sutClassifier.getProbabilityOfClass()[0]);
      assertEquals(1.0/3, sutClassifier.getProbabilityOfClass()[1]);
      assertEquals(1.0/3, sutClassifier.getProbabilityOfClass()[2]);

      // AmethodcallB|A
      assertEquals(2.0/3, sutClassifier.getProbabilityOfWord(1, 0));
      // AmethodcallC|A
      assertEquals(1.0/3, sutClassifier.getProbabilityOfWord(2, 0));

      // AmethodcallB|B
      assertEquals(2.0/4, sutClassifier.getProbabilityOfWord(1, 1));
      // AmethodcallC|B
      assertEquals(2.0/4, sutClassifier.getProbabilityOfWord(2, 1));

      // AmethodcallB|C
      assertEquals(1.0/3, sutClassifier.getProbabilityOfWord(1, 2));
      // AmethodcallC|C
      assertEquals(2.0/3, sutClassifier.getProbabilityOfWord(2, 2));

      // A|AmethodcallB
      assertEquals(4.0/9, o.getAttractions()[0], 0.00001);

      // B|BmethodcallB
      assertEquals(3.0/9, o.getAttractions()[1], 0.00001);

      // C|CmetodcallC
      assertEquals(3.0/9, o.getAttractions()[2], 0.00001);

   }

   @Test
   public void CATest() {
      ArchDef arch = new ArchDef();
      CGraph g = generateCase1(arch);

      double phi = 1.0;

      HuGMe sut = new HuGMe(0, phi, false, arch);

      sut.run(g);

      CNode o = g.getNodeByName("o");

      assertEquals(0, o.getAttractions()[0]);
      assertEquals(1, o.getAttractions()[1]);
      assertEquals(0, o.getAttractions()[2]);
   }

   @Test
   public void IRTest() {
      ArchDef arch = new ArchDef();
      CGraph g = generateCase2(arch);

      IRAttractMapper sut = new IRAttractMapper(arch, false, true, false, false, false, 0);

      sut.run(g);

      Vector<IRAttractMapper.WordVector> sutTD = sut.getTrainingData();
      CNode o = g.getNodeByName("o");

      //assertEquals(2.0/3 / (Math.sqrt(2) * Math.sqrt(11.0/9)), o.getAttractions()[0], 0.000001);
      //assertEquals(3.0/3 / (Math.sqrt(2) * Math.sqrt(2)), o.getAttractions()[1], 0.000001);
      //assertEquals(0, o.getAttractions()[2], 0.000001);
      //assertEquals(0, o.getAttractions()[3], 0.000001);

   }

   @Test
   public void LSITest() {
      ArchDef arch = new ArchDef();
      CGraph g = generateCase2(arch);

      LSIAttractMapper sut = new LSIAttractMapper(arch, false, true, false, false, false, 0);

      sut.run(g);

      CNode o = g.getNodeByName("o");

   }


   @Test
   public void NBTest_2() {
      ArchDef arch = new ArchDef();
      CGraph g = generateCase2(arch);

      NBMapper sut = new NBMapper(arch, true, false, false, false, 0, 0.9);

      sut.run(g);

      NBMapper.Classifier sutClassifier = sut.getClassifier();

      String sutStr = sutClassifier.toString();

      CNode o = g.getNodeByName("o");

      assertEquals(3.0/12.0, sutClassifier.getProbabilityOfClass()[0]);
      assertEquals(3.0/12.0, sutClassifier.getProbabilityOfClass()[1]);
      assertEquals(3.0/12.0, sutClassifier.getProbabilityOfClass()[2]);
      assertEquals(3.0/12.0, sutClassifier.getProbabilityOfClass()[2]);

      assertTrue(o.getAttractions()[0] > o.getAttractions()[1]);
      assertTrue(o.getAttractions()[1] > o.getAttractions()[2]);
      assertEquals(o.getAttractions()[2], o.getAttractions()[3]);

   }

   private CGraph generateCase2(ArchDef a_arch) {
      NodeGenerator ng = new NodeGenerator();
      CGraph g = ng.generateGraph(dmDependency.Type.MethodCall, new String [] {"ab", "ac", "bc", "ob", "oc", "af", "cf", "ag", "ah"}); // nodes c & e represents nodes all mapped to B with relations to c
      CNode a = g.getNode("a");
      CNode a2 = g.createNode("a2");
      CNode a3 = g.createNode("a3");
      CNode d1 = g.getNode("f");
      CNode d2 = g.getNode("g");
      CNode d3 = g.getNode("h");

      CNode b = g.getNode("b");
      CNode b2 = g.createNode("b2");
      CNode b3 = g.createNode("b3");

      //CNode b2 = g.getNode("d");
      //CNode b3 = g.getNode("e");
      CNode c = g.getNode("c");
      CNode c2 = g.createNode("c2");
      CNode c3 = g.createNode("c3");
      CNode o = g.getNode("o");

      ArchDef.Component A = a_arch.addComponent("A");
      ArchDef.Component B = a_arch.addComponent("B");
      ArchDef.Component C = a_arch.addComponent("C");
      ArchDef.Component D = a_arch.addComponent("D");

      A.clusterToNode(a, ArchDef.Component.ClusteringType.Initial);
      A.mapToNode(a);
      A.clusterToNode(a2, ArchDef.Component.ClusteringType.Initial);
      A.mapToNode(a2);
      A.clusterToNode(a3, ArchDef.Component.ClusteringType.Initial);
      A.mapToNode(a3);

      B.clusterToNode(b, ArchDef.Component.ClusteringType.Initial);
      B.mapToNode(b);
      B.clusterToNode(b2, ArchDef.Component.ClusteringType.Initial);
      B.mapToNode(b2);
      B.clusterToNode(b3, ArchDef.Component.ClusteringType.Initial);
      B.mapToNode(b3);

      C.clusterToNode(c, ArchDef.Component.ClusteringType.Initial);
      C.mapToNode(c);
      C.clusterToNode(c2, ArchDef.Component.ClusteringType.Initial);
      C.mapToNode(c2);
      C.clusterToNode(c3, ArchDef.Component.ClusteringType.Initial);
      C.mapToNode(c3);

      D.clusterToNode(d1, ArchDef.Component.ClusteringType.Initial);
      D.mapToNode(d1);
      D.clusterToNode(d2, ArchDef.Component.ClusteringType.Initial);
      D.mapToNode(d2);
      D.clusterToNode(d3, ArchDef.Component.ClusteringType.Initial);
      D.mapToNode(d3);

      A.mapToNode(o);

      return g;
   }

   private CGraph generateCase1(ArchDef a_arch) {
      a_arch.clear();

      NodeGenerator ng = new NodeGenerator();
      CGraph g = ng.generateGraph(dmDependency.Type.MethodCall, new String [] {"ab", "ob", "bc"});
      CNode a = g.getNode("a");
      CNode b = g.getNode("b");
      CNode c = g.getNode("c");
      CNode o = g.getNode("o");
      ArchDef.Component A = a_arch.addComponent("A");
      ArchDef.Component B = a_arch.addComponent("B");
      ArchDef.Component C = a_arch.addComponent("C");

      A.clusterToNode(a, ArchDef.Component.ClusteringType.Initial);
      A.mapToNode(a);

      B.clusterToNode(b, ArchDef.Component.ClusteringType.Initial);
      B.mapToNode(b);

      C.clusterToNode(c, ArchDef.Component.ClusteringType.Initial);
      C.mapToNode(c);

      A.mapToNode(o);

      return g;
   }
}
