package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.NodeGenerator;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ThesisTests {
   @Test
   public void NBTest_1() {
      NodeGenerator ng = new NodeGenerator();
      CGraph g = ng.generateGraph(dmDependency.Type.MethodCall, new String [] {"ab", "ob"});
      CNode a = g.getNode("a");
      CNode b = g.getNode("b");
      CNode o = g.getNode("o");
      ArchDef arch = new ArchDef();
      ArchDef.Component A = arch.addComponent("A");
      ArchDef.Component B = arch.addComponent("B");
      A.clusterToNode(a, ArchDef.Component.ClusteringType.Initial);
      A.mapToNode(a);
      B.clusterToNode(b, ArchDef.Component.ClusteringType.Initial);
      B.mapToNode(b);

      NBMapper sut = new NBMapper(arch, true, false, false, false, 0, 0.9);

      sut.run(g);

      NBMapper.Classifier sutClassifier = sut.getClassifier();

      String sutStr = sutClassifier.toString();

      assertEquals(0.5, sutClassifier.getProbabilityOfClass()[0]);
      assertEquals(0.5, sutClassifier.getProbabilityOfClass()[1]);
      assertEquals((1.0+1.0)/(1.0+1.0), sutClassifier.getProbabilityOfWord(1, 0));
      assertEquals((1.0+1.0)/(1.0+1.0), sutClassifier.getProbabilityOfWord(1, 1));

   }

   @Test
   public void NBTest_2() {
      NodeGenerator ng = new NodeGenerator();
      CGraph g = ng.generateGraph(dmDependency.Type.MethodCall, new String [] {"ab", "ac", "bc", "dc", "ec"}); // nodes c & e represents nodes all mapped to B with relations to c
      CNode a = g.getNode("a");
      CNode b1 = g.getNode("b");
      CNode b2 = g.getNode("d");
      CNode b3 = g.getNode("e");
      CNode c = g.getNode("c");

      CNode o = g.getNode("o");
      ArchDef arch = new ArchDef();
      ArchDef.Component A = arch.addComponent("A");
      ArchDef.Component B = arch.addComponent("B");
      ArchDef.Component C = arch.addComponent("C");
      A.clusterToNode(a, ArchDef.Component.ClusteringType.Initial);
      A.mapToNode(a);
      B.clusterToNode(b1, ArchDef.Component.ClusteringType.Initial);
      B.mapToNode(b1);
      B.clusterToNode(b2, ArchDef.Component.ClusteringType.Initial);
      B.mapToNode(b2);
      B.clusterToNode(b3, ArchDef.Component.ClusteringType.Initial);
      B.mapToNode(b3);
      C.clusterToNode(c, ArchDef.Component.ClusteringType.Initial);
      C.mapToNode(c);

      NBMapper sut = new NBMapper(arch, true, false, false, false, 0, 0.9);

      sut.run(g);

      NBMapper.Classifier sutClassifier = sut.getClassifier();

      String sutStr = sutClassifier.toString();

      assertEquals(1.0/5.0, sutClassifier.getProbabilityOfClass()[0]);
      assertEquals(3.0/5.0, sutClassifier.getProbabilityOfClass()[1]);
      assertEquals(1.0/5.0, sutClassifier.getProbabilityOfClass()[2]);

      assertEquals((1.0+1.0)/(2.0+3.0), sutClassifier.getProbabilityOfWord(1, 0));  // AMethodCallB
      assertEquals((1.0+1.0)/(2.0+3.0), sutClassifier.getProbabilityOfWord(2, 0));  // AMethodCallC
      assertEquals((1.0)/(2.0+3.0), sutClassifier.getProbabilityOfWord(3, 0));  // BMethodCallC

      assertEquals((1.0+1.0)/(4.0+3.0), sutClassifier.getProbabilityOfWord(1, 1));  // AMethodCallB
      assertEquals((1.0)/(4.0+3.0), sutClassifier.getProbabilityOfWord(2, 1), 0.000001);  // AMethodCallC
      assertEquals((1.0+3.0)/(4.0+3.0), sutClassifier.getProbabilityOfWord(3, 1));  // BMethodCallC

      assertEquals((1.0)/(2.0+3.0), sutClassifier.getProbabilityOfWord(1, 2));  // AMethodCallB
      assertEquals((1.0+1.0)/(2.0+3.0), sutClassifier.getProbabilityOfWord(2, 2));  // AMethodCallC
      assertEquals((1.0+1.0)/(2.0+3.0), sutClassifier.getProbabilityOfWord(3, 2));  // BMethodCallC

   }
}
