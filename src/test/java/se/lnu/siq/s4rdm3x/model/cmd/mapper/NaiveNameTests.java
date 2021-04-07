package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.NodeGenerator;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.dmodel.dmFile;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NaiveNameTests {

   @Test
   public void simpleTest() {
      NodeGenerator ng = new NodeGenerator();
      dmFile.dmDirectory root = new dmFile.dmDirectory("root", null);
      ArchDef arch = new ArchDef();
      ArchDef.Component c1, c2;

      c1 = arch.addComponent("c1");
      c2 = arch.addComponent("c2");
      CGraph g = new CGraph();

      CNode a = g.createNode("a");
      a.addClass(ng.createClass("c1.a", root));
      CNode b = g.createNode("b");
      b.addClass(ng.createClass("c2.b", root));

      c1.mapToNode(a);
      c2.mapToNode(b);

      NaiveNameMatcher sut = new NaiveNameMatcher(false, arch);

     sut.run(g);

     assertEquals(2, a.getAttractions()[0]);
     assertEquals(0, a.getAttractions()[1]);

      assertEquals(2, b.getAttractions()[1]);
      assertEquals(0, b.getAttractions()[0]);

      assertTrue(arch.getClusteredComponent(a) == c1);
      assertTrue(arch.getClusteredComponent(b) == c2);
   }


   @Test
   public void superComponentTest() {
      NodeGenerator ng = new NodeGenerator();
      dmFile.dmDirectory root = new dmFile.dmDirectory("root", null);
      ArchDef arch = new ArchDef();
      ArchDef.Component c1, c2, r;

      c1 = arch.addComponent("c1");
      c2 = arch.addComponent("c2");
      r = arch.addComponent("root");
      CGraph g = new CGraph();

      CNode a = g.createNode("a");
      a.addClass(ng.createClass("root.c1.a", root));
      CNode b = g.createNode("b");
      b.addClass(ng.createClass("root.c2.b", root));

      CNode c = g.createNode("c");
      c.addClass(ng.createClass("root.c", root));

      c1.mapToNode(a);
      c2.mapToNode(b);
      r.mapToNode(c);

      NaiveNameMatcher sut = new NaiveNameMatcher(false, arch);

      sut.run(g);

      assertEquals(3, a.getAttractions()[0]);
      assertEquals(0, a.getAttractions()[1]);
      assertEquals(3, a.getAttractions()[2]);

      assertEquals(0, b.getAttractions()[0]);
      assertEquals(3, b.getAttractions()[1]);
      assertEquals(3, b.getAttractions()[2]);

      assertEquals(0, c.getAttractions()[0]);
      assertEquals(0, c.getAttractions()[1]);
      assertEquals(3, c.getAttractions()[2]);

      //assertTrue(arch.getClusteredComponent(a) == c1);
      //assertTrue(arch.getClusteredComponent(b) == c2);
      //assertTrue(arch.getClusteredComponent(c) == r);

   }

   @Test
   public void complexNameTest() {
      NodeGenerator ng = new NodeGenerator();
      dmFile.dmDirectory root = new dmFile.dmDirectory("root", null);
      ArchDef arch = new ArchDef();
      ArchDef.Component c1, c2;

      c1 = arch.addComponent("c1.api");
      c2 = arch.addComponent("c2.api");
      CGraph g = new CGraph();

      CNode a = g.createNode("a");
      a.addClass(ng.createClass("c1.api.a", root));
      CNode b = g.createNode("b");
      b.addClass(ng.createClass("c2.api.b", root));

      c1.mapToNode(a);
      c2.mapToNode(b);

      NaiveNameMatcher sut = new NaiveNameMatcher(false, arch);

      sut.run(g);

      assertEquals(5, a.getAttractions()[0]);
      assertEquals(3, a.getAttractions()[1]);

      assertEquals(3, b.getAttractions()[0]);
      assertEquals(5, b.getAttractions()[1]);


      assertTrue(arch.getClusteredComponent(a) == c1);
      assertTrue(arch.getClusteredComponent(b) == c2);
   }
}
