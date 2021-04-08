package se.lnu.siq.s4rdm3x.dmodel.classes;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.dmodel.dmFile;
import se.lnu.siq.s4rdm3x.dmodel.dmProject;

import static org.junit.jupiter.api.Assertions.*;

public class dmProjectTests {

   @Test
   void blackListClass() {
      dmProject sut = new dmProject();
      sut.addClassToBlackList("void");
      dmClass c = sut.addJavaClass("void");

      assertNull(c);
      assertEquals(0, sut.getRootDirectory().fileCount());
   }

   @Test
   void addInnerJavaClass() {
      dmProject sut = new dmProject();
      dmClass c = sut.addJavaClass("test$test");

      assertEquals("root/test.java", c.getFileName());
      assertEquals("test", c.getFile().getName());

      c = sut.addJavaClass("test");

      assertEquals("root/test.java", c.getFileName());
      assertEquals("test", c.getFile().getName());
   }

   @Test
   void horizontal2FilesTest() {
      dmProject sut = new dmProject();

      dmClass c1 = sut.addJavaClass("c1");
      dmClass c2 = sut.addJavaClass("c2");

      sut.addFileDependencies();

      // both classes point at each other
      assertEquals(c2, c1.getDependencies().iterator().next().getTarget());
      assertEquals(c1, c2.getDependencies().iterator().next().getTarget());

      // correct type
      assertEquals(dmDependency.Type.File_Horizontal, c1.getDependencies().iterator().next().getType());
      assertEquals(dmDependency.Type.File_Horizontal, c2.getDependencies().iterator().next().getType());

      // correct number
      assertEquals(1, c1.getDependencies().iterator().next().getCount());
      assertEquals(1, c2.getDependencies().iterator().next().getCount());

   }

   @Test
   void vertical2FilesTest() {
      dmProject sut = new dmProject();

      dmClass c1 = sut.addJavaClass("c1");
      dmClass c2 = sut.addJavaClass("subdir.c2");

      sut.addFileDependencies();

      // both classes point at each other
      assertEquals(c2, c1.getDependencies().iterator().next().getTarget());
      assertEquals(c1, c2.getDependencies().iterator().next().getTarget());

      // correct type
      assertEquals(dmDependency.Type.File_Vertical, c1.getDependencies().iterator().next().getType());
      assertEquals(dmDependency.Type.File_Vertical, c2.getDependencies().iterator().next().getType());

      // correct number
      assertEquals(1, c1.getDependencies().iterator().next().getCount());
      assertEquals(1, c2.getDependencies().iterator().next().getCount());

   }

   @Test
   void vh4FilesTest() {
      dmProject sut = new dmProject();

      dmClass c1 = sut.addJavaClass("c1");
      dmClass c2 = sut.addJavaClass("c2");
      dmClass c3 = sut.addJavaClass("subdir.c3");
      dmClass c4 = sut.addJavaClass("subdir.c4");

      sut.addFileDependencies();

      assertTrue(hasDirectDependency(c1, dmDependency.Type.File_Horizontal, c2));
      assertTrue(hasDirectDependency(c2, dmDependency.Type.File_Horizontal, c1));

      assertTrue(hasDirectDependency(c1, dmDependency.Type.File_Vertical, c3));
      assertTrue(hasDirectDependency(c1, dmDependency.Type.File_Vertical, c4));
      assertTrue(!hasDirectDependency(c1, dmDependency.Type.File_Horizontal, c3));
      assertTrue(!hasDirectDependency(c1, dmDependency.Type.File_Horizontal, c4));

      assertTrue(hasDirectDependency(c2, dmDependency.Type.File_Vertical, c3));
      assertTrue(hasDirectDependency(c2, dmDependency.Type.File_Vertical, c4));
      assertTrue(!hasDirectDependency(c2, dmDependency.Type.File_Horizontal, c3));
      assertTrue(!hasDirectDependency(c2, dmDependency.Type.File_Horizontal, c4));

      assertTrue(hasDirectDependency(c3, dmDependency.Type.File_Vertical, c1));
      assertTrue(hasDirectDependency(c3, dmDependency.Type.File_Vertical, c2));
      assertTrue(!hasDirectDependency(c3, dmDependency.Type.File_Horizontal, c1));
      assertTrue(!hasDirectDependency(c3, dmDependency.Type.File_Horizontal, c2));

      assertTrue(hasDirectDependency(c4, dmDependency.Type.File_Vertical, c1));
      assertTrue(hasDirectDependency(c4, dmDependency.Type.File_Vertical, c2));
      assertTrue(!hasDirectDependency(c4, dmDependency.Type.File_Horizontal, c1));
      assertTrue(!hasDirectDependency(c4, dmDependency.Type.File_Horizontal, c2));

      assertTrue(hasDirectDependency(c3, dmDependency.Type.File_Horizontal, c4));
      assertTrue(hasDirectDependency(c4, dmDependency.Type.File_Horizontal, c3));
   }

   private boolean hasDirectDependency(dmClass a_from, dmDependency.Type a_type, dmClass a_target) {
      for (dmDependency d : a_from.getDependencies()) {
         if (d.getTarget() == a_target && d.getType() == a_type) {
            return true;
         }
      }

      return false;
   }


}
