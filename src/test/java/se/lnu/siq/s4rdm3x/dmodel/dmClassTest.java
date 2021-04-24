package se.lnu.siq.s4rdm3x.dmodel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class dmClassTest {

   @Test
   public void toJavaSourceFileTest() {

      String [] parts = dmClass.toJavaSourceFile("a");
      assertEquals(1, parts.length);
      assertEquals("a", parts[0]);

      parts = dmClass.toJavaSourceFile("a.b");
      assertEquals(2, parts.length);
      assertEquals("a", parts[0]);
      assertEquals("b", parts[1]);

      parts = dmClass.toJavaSourceFile("a$b");
      assertEquals(1, parts.length);
      assertEquals("a", parts[0]);

      parts = dmClass.toJavaSourceFile("a$b$c");
      assertEquals(1, parts.length);
      assertEquals("a", parts[0]);

   }

   public static boolean createsDoubleFileDependencies() {
      dmClass c1 = new dmClass("c1", new dmFile("f1", new dmFile.dmDirectory("")));
      dmClass c2 = new dmClass("c2", new dmFile("f2", new dmFile.dmDirectory("")));

      c1.addVerticalFileDependency(c2);
      c1.addHorizontalFileDependency(c2);
      return (c1.getDependencyCount() + c2.getDependencyCount()) == 4;
   }

   @Test
   public void doubleFileDependenciesTest() {
      assertEquals(dmClass.createsDoubleFileDependencies(), createsDoubleFileDependencies());
   }
}
