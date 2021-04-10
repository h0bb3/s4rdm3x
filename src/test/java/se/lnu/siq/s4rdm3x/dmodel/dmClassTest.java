package se.lnu.siq.s4rdm3x.dmodel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
