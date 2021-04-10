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
      assertEquals("test$test", c.getName());

      assertEquals(2, sut.getClassCount());

      c = sut.addJavaClass("test");

      assertEquals("root/test.java", c.getFileName());
      assertEquals("test", c.getFile().getName());

      assertEquals(2, sut.getClassCount());
   }

   @Test
   void fileDepsToOuterClassesOnly1() {
      dmProject sut = new dmProject();
      dmClass inner = sut.addJavaClass("test$test");
      dmClass outer = sut.addJavaClass("test");
      sut.addJavaClass("sibling");
      sut.addJavaClass("dir.child");

      sut.addFileDependencies();

      assertEquals(0, inner.getIncomingDependencies().size());
      assertEquals(0, inner.getDependencyCount());

      assertEquals(2, outer.getIncomingDependencies().size());
      assertEquals(2, outer.getDependencyCount());
   }

   @Test
   void innerAndOuterHaveSameFileObject() {
      dmProject sut = new dmProject();
      dmClass inner = sut.addJavaClass("dir.test$test");
      dmClass outer = sut.addJavaClass("dir.test");


      sut.addFileDependencies();

      assertTrue(inner != outer);
      assertTrue(inner.getFile() == outer.getFile());

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
      assertEquals(c1, c2.getIncomingDependencies().iterator().next().getSource());

      // correct type
      assertEquals(dmDependency.Type.File_LevelDown, c1.getDependencies().iterator().next().getType());
      assertEquals(dmDependency.Type.File_LevelUp, c2.getDependencies().iterator().next().getType());

      // correct number
      assertEquals(1, c1.getDependencies().iterator().next().getCount());
      assertEquals(1, c2.getIncomingDependencies().iterator().next().getCount());

   }

   @Test
   void vh4FilesTest() {
      dmProject sut = new dmProject();

      dmClass c1 = sut.addJavaClass("c1");
      dmClass c2 = sut.addJavaClass("c2");
      dmClass c3 = sut.addJavaClass("subdir.c3");
      dmClass c4 = sut.addJavaClass("subdir.c4");

      sut.addFileDependencies();

      assertTrue(hasDependencyTarget(c1.getDependencies(), dmDependency.Type.File_Horizontal, c2));
      assertTrue(hasDependencyTarget(c2.getDependencies(), dmDependency.Type.File_Horizontal, c1));
      assertTrue(hasDependencySource(c1.getIncomingDependencies(), dmDependency.Type.File_Horizontal, c2));
      assertTrue(hasDependencySource(c2.getIncomingDependencies(), dmDependency.Type.File_Horizontal, c1));


      assertTrue(hasDependencyTarget(c1.getDependencies(), dmDependency.Type.File_LevelDown, c3));
      assertTrue(hasDependencyTarget(c1.getDependencies(), dmDependency.Type.File_LevelDown, c4));
      assertTrue(!hasDependencyTarget(c1.getDependencies(), dmDependency.Type.File_Horizontal, c3));
      assertTrue(!hasDependencyTarget(c1.getDependencies(), dmDependency.Type.File_Horizontal, c4));

      assertTrue(hasDependencyTarget(c2.getDependencies(), dmDependency.Type.File_LevelDown, c3));
      assertTrue(hasDependencyTarget(c2.getDependencies(), dmDependency.Type.File_LevelDown, c4));
      assertTrue(!hasDependencyTarget(c2.getDependencies(), dmDependency.Type.File_Horizontal, c3));
      assertTrue(!hasDependencyTarget(c2.getDependencies(), dmDependency.Type.File_Horizontal, c4));

      assertTrue(hasDependencyTarget(c3.getDependencies(), dmDependency.Type.File_LevelUp, c1));
      assertTrue(hasDependencyTarget(c3.getDependencies(), dmDependency.Type.File_LevelUp, c2));
      assertTrue(!hasDependencyTarget(c3.getDependencies(), dmDependency.Type.File_Horizontal, c1));
      assertTrue(!hasDependencyTarget(c3.getDependencies(), dmDependency.Type.File_Horizontal, c2));

      assertTrue(hasDependencyTarget(c4.getDependencies(), dmDependency.Type.File_LevelUp, c1));
      assertTrue(hasDependencyTarget(c4.getDependencies(), dmDependency.Type.File_LevelUp, c2));
      assertTrue(!hasDependencyTarget(c4.getDependencies(), dmDependency.Type.File_Horizontal, c1));
      assertTrue(!hasDependencyTarget(c4.getDependencies(), dmDependency.Type.File_Horizontal, c2));

      assertTrue(hasDependencyTarget(c3.getDependencies(), dmDependency.Type.File_Horizontal, c4));
      assertTrue(hasDependencyTarget(c4.getDependencies(), dmDependency.Type.File_Horizontal, c3));
   }

   private boolean hasDependencySource(Iterable<dmDependency> a_deps, dmDependency.Type a_type, dmClass a_source) {

      for (dmDependency d : a_deps) {
         if (d.getSource() == a_source && d.getType() == a_type) {
            return true;
         }
      }

      return false;
   }

   private boolean hasDependencyTarget(Iterable<dmDependency> a_deps, dmDependency.Type a_type, dmClass a_target) {
      for (dmDependency d : a_deps) {
         if (d.getTarget() == a_target && d.getType() == a_type) {
            return true;
         }
      }

      return false;
   }


}
