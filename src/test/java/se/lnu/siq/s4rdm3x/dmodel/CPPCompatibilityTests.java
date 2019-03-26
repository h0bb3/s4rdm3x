package se.lnu.siq.s4rdm3x.dmodel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.lnu.siq.s4rdm3x.dmodel.dmDependency.Type.ConstructorCall;

public class CPPCompatibilityTests extends ASMdmProjectBuilderTest {

    @Test
    void barTest() {
        try {

            String className = "CPPCompatibility.simple.Bar";
            dmClass expected = new dmClass(g_classesPkg + className);
            expected.addDependency("java.lang.Object", dmDependency.Type.Extends);
            expected.addDependency("java.lang.Object", ConstructorCall);
            expected.addDependency("void", dmDependency.Type.Returns);  // constructor

            expected.addDependency("float", dmDependency.Type.Returns);
            expected.addDependency("float", dmDependency.Type.Field);
            expected.addDependency("float", dmDependency.Type.OwnFieldUse);
            expected.addDependency("float", dmDependency.Type.OwnFieldUse);

             ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "CPPCompatibility/simple/Bar" + ".class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(g_classesPkg + "CPPCompatibility.simple.Bar");
            assertTrue(c != null);

            assertTrue(compare(expected, c) == 0);

            //dumpDependencies(c);
            //assertEquals(3, c.getDependencyCount());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            assertTrue(false);
        }

    }

    @Test
    void barArgsTest() {
        try {

            String className = "CPPCompatibility.simple.BarArgs";
            dmClass expected = new dmClass(g_classesPkg + className);
            expected.addDependency("java.lang.Object", dmDependency.Type.Extends);
            expected.addDependency("java.lang.Object", ConstructorCall);
            expected.addDependency("void", dmDependency.Type.Returns);  // constructor

            expected.addDependency("void", dmDependency.Type.Returns);
            expected.addDependency("float", dmDependency.Type.Argument);
            expected.addDependency("float", dmDependency.Type.Returns);
            expected.addDependency("float", dmDependency.Type.Field);
            expected.addDependency("float", dmDependency.Type.OwnFieldUse);
            expected.addDependency("float", dmDependency.Type.OwnFieldUse);
            expected.addDependency("float", dmDependency.Type.OwnFieldUse);

            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "CPPCompatibility/simple/BarArgs" + ".class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(g_classesPkg + className);
            assertTrue(c != null);

            assertTrue(compare(expected, c) == 0);

            //dumpDependencies(c);
            //assertEquals(3, c.getDependencyCount());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            assertTrue(false);
        }
    }


    @Test
    void barArrayTest() {
        try {

            String className = "CPPCompatibility.simple.BarArray";
            dmClass expected = new dmClass(g_classesPkg + className);
            expected.addDependency("java.lang.Object", dmDependency.Type.Extends);
            expected.addDependency("java.lang.Object", ConstructorCall);
            expected.addDependency("void", dmDependency.Type.Returns);  // constructor

            expected.addDependency("int", dmDependency.Type.Returns);
            expected.addDependency("float", dmDependency.Type.Field);
            expected.addDependency("float", dmDependency.Type.Returns);
            expected.addDependency("float", dmDependency.Type.OwnFieldUse);

            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "CPPCompatibility/simple/BarArray" + ".class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(g_classesPkg + className);
            assertTrue(c != null);

            assertTrue(compare(expected, c) == 0);

            //dumpDependencies(c);
            //assertEquals(3, c.getDependencyCount());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void barTemplateTest() {
        try {

            String className = "CPPCompatibility.simple.BarTemplate";
            dmClass expected = new dmClass(g_classesPkg + className);
            expected.addDependency("java.lang.Object", dmDependency.Type.Extends);
            expected.addDependency("java.lang.Object", ConstructorCall);
            expected.addDependency("void", dmDependency.Type.Returns);  // constructor

            expected.addDependency("java.lang.Float", dmDependency.Type.Field);
            expected.addDependency("java.util.ArrayList", dmDependency.Type.Field);
            expected.addDependency("java.lang.Float", dmDependency.Type.Returns);
            expected.addDependency("java.util.ArrayList", dmDependency.Type.Returns);
            expected.addDependency("java.util.ArrayList", dmDependency.Type.OwnFieldUse);

            /*expected.addDependency("int", dmDependency.Type.Returns);
            expected.addDependency("float", dmDependency.Type.Field);
            expected.addDependency("float", dmDependency.Type.Returns);
            expected.addDependency("float", dmDependency.Type.OwnFieldUse);*/

            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "CPPCompatibility/simple/BarTemplate" + ".class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(g_classesPkg + className);
            assertTrue(c != null);

            assertTrue(compare(expected, c) == 0);

            //dumpDependencies(c);
            //assertEquals(3, c.getDependencyCount());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void barParentTest() {
        try {

            String className = "CPPCompatibility.simple.BarParent";
            dmClass expected = new dmClass(g_classesPkg + className);
            expected.addDependency("java.lang.Object", dmDependency.Type.Extends);
            expected.addDependency("java.lang.Object", ConstructorCall);
            expected.addDependency("void", dmDependency.Type.Returns);  // constructor

            expected.addDependency("java.lang.Float", dmDependency.Type.Field);
            expected.addDependency("java.util.ArrayList", dmDependency.Type.Field);
            expected.addDependency("java.lang.Float", dmDependency.Type.Returns);
            expected.addDependency("java.util.ArrayList", dmDependency.Type.Returns);
            expected.addDependency("java.util.ArrayList", dmDependency.Type.OwnFieldUse);

            /*expected.addDependency("int", dmDependency.Type.Returns);
            expected.addDependency("float", dmDependency.Type.Field);
            expected.addDependency("float", dmDependency.Type.Returns);
            expected.addDependency("float", dmDependency.Type.OwnFieldUse);*/

            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "CPPCompatibility/simple/BarParent" + ".class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(g_classesPkg + className);
            assertTrue(c != null);

            assertTrue(compare(expected, c) == 0);

            //dumpDependencies(c);
            //assertEquals(3, c.getDependencyCount());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void barChainedCompositeCallParentTest() {
        try {

            String classNameBar = "CPPCompatibility.simple.BarChainedCompositeCall";
            String classNameFoo = "CPPCompatibility.simple.Foo";
            String classNameFuu = "CPPCompatibility.simple.Fuu";
            dmClass expected = new dmClass(g_classesPkg + classNameBar);
            expected.addDependency("java.lang.Object", dmDependency.Type.Extends);
            expected.addDependency("java.lang.Object", ConstructorCall);
            expected.addDependency("void", dmDependency.Type.Returns);  // constructor

            expected.addDependency(g_classesPkg + classNameFoo, dmDependency.Type.Field);
            expected.addDependency(g_classesPkg + classNameFoo, dmDependency.Type.ConstructorCall);

            expected.addDependency("int", dmDependency.Type.Returns);
            expected.addDependency("int", dmDependency.Type.Returns);
            expected.addDependency("int", dmDependency.Type.Argument);

            expected.addDependency(g_classesPkg + classNameFoo, dmDependency.Type.MethodCall);
            expected.addDependency(g_classesPkg + classNameFuu, dmDependency.Type.MethodCall);

            expected.addDependency(g_classesPkg + classNameFoo, dmDependency.Type.OwnFieldUse);
            expected.addDependency(g_classesPkg + classNameFoo, dmDependency.Type.OwnFieldUse);


            /*expected.addDependency("int", dmDependency.Type.Returns);
            expected.addDependency("float", dmDependency.Type.Field);
            expected.addDependency("float", dmDependency.Type.Returns);
            expected.addDependency("float", dmDependency.Type.OwnFieldUse);*/

            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "CPPCompatibility/simple/BarChainedCompositeCall" + ".class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(g_classesPkg + classNameBar);
            assertTrue(c != null);

            assertTrue(compare(expected, c) == 0);

            //dumpDependencies(c);
            //assertEquals(3, c.getDependencyCount());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            assertTrue(false);
        }
    }

}
