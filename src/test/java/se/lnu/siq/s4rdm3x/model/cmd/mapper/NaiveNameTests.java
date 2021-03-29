package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.NodeGenerator;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NaiveNameTests {

   @Test
   public void simpleTest() {
      NodeGenerator ng = new NodeGenerator();
      ArchDef arch = new ArchDef();
      ArchDef.Component c1, c2;

      c1 = arch.addComponent("c1");
      c2 = arch.addComponent("c2");
      CGraph g = new CGraph();

      CNode a = g.createNode("a");
      a.addClass(new dmClass("c1.a"));
      CNode b = g.createNode("b");
      b.addClass(new dmClass("c2.b"));

      c1.mapToNode(a);
      c2.mapToNode(b);

      NaiveNameMatcher sut = new NaiveNameMatcher(false, arch);

     sut.run(g);

     assertEquals(1, a.getAttractions()[0]);
     assertEquals(0, a.getAttractions()[1]);

      assertEquals(1, b.getAttractions()[1]);
      assertEquals(0, b.getAttractions()[0]);

      assertTrue(arch.getClusteredComponent(a) == c1);
      assertTrue(arch.getClusteredComponent(b) == c2);
   }


   @Test
   public void superComponentTest() {
      NodeGenerator ng = new NodeGenerator();
      ArchDef arch = new ArchDef();
      ArchDef.Component c1, c2, root;

      c1 = arch.addComponent("c1");
      c2 = arch.addComponent("c2");
      root = arch.addComponent("root");
      CGraph g = new CGraph();

      CNode a = g.createNode("a");
      a.addClass(new dmClass("root.c1.a"));
      CNode b = g.createNode("b");
      b.addClass(new dmClass("root.c2.b"));

      CNode c = g.createNode("c");
      c.addClass(new dmClass("root.c"));

      c1.mapToNode(a);
      c2.mapToNode(b);
      root.mapToNode(c);

      NaiveNameMatcher sut = new NaiveNameMatcher(false, arch);

      sut.run(g);

      assertEquals(2, a.getAttractions()[0]);
      assertEquals(0, a.getAttractions()[1]);
      assertEquals(1, a.getAttractions()[2]);

      assertEquals(0, b.getAttractions()[0]);
      assertEquals(2, b.getAttractions()[1]);
      assertEquals(1, b.getAttractions()[2]);

      assertEquals(0, c.getAttractions()[0]);
      assertEquals(0, c.getAttractions()[1]);
      assertEquals(1, c.getAttractions()[2]);

      assertTrue(arch.getClusteredComponent(a) == c1);
      assertTrue(arch.getClusteredComponent(b) == c2);
      assertTrue(arch.getClusteredComponent(c) == root);

   }

   @Test
   public void complexNameTest() {
      NodeGenerator ng = new NodeGenerator();
      ArchDef arch = new ArchDef();
      ArchDef.Component c1, c2, root;

      c1 = arch.addComponent("c1.api");
      c2 = arch.addComponent("c2.api");
      CGraph g = new CGraph();

      CNode a = g.createNode("a");
      a.addClass(new dmClass("c1.api.a"));
      CNode b = g.createNode("b");
      b.addClass(new dmClass("c2.api.b"));

      c1.mapToNode(a);
      c2.mapToNode(b);

      NaiveNameMatcher sut = new NaiveNameMatcher(false, arch);

      sut.run(g);

      assertEquals(3, a.getAttractions()[0]);
      assertEquals(2, a.getAttractions()[1]);

      assertEquals(2, b.getAttractions()[0]);
      assertEquals(3, b.getAttractions()[1]);


      assertTrue(arch.getClusteredComponent(a) == c1);
      assertTrue(arch.getClusteredComponent(b) == c2);
   }
}
