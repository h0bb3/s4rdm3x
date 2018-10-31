package se.lnu.siq.s4rdm3x.dmodel;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static se.lnu.siq.s4rdm3x.dmodel.dmDependency.Type.ConstructorCall;

/**
 * Created by tohto on 2017-04-27.
 */
class ASMdmProjectBuilderTest {

    static final String g_classesPkg = "se.lnu.siq.s4rdm3x.dmodel.classes.";
    static final String g_classesDir = "/se/lnu/siq/s4rdm3x/dmodel/classes/";

    void dumpDependencies(dmClass a_c) {
        System.out.println("\t" + a_c.getName());
        for (dmDependency d : a_c.getDependencies()) {
            System.out.println("\t-> " + d.getTarget().getName() + " : " + d.getType().toString() + " x " + d.getCount());
        }
    }

    private List<dmDependency> deepCopyDependencies(Iterable<dmDependency> a_source) {
        List<dmDependency> ret = new ArrayList<dmDependency>();

        for(dmDependency src : a_source) {
            Iterator<Integer> lines = src.lines().iterator();
            dmDependency cpy = new dmDependency(src.getSource(), src.getTarget(), src.getType(), lines.next());
            while (lines.hasNext()) {
                cpy.addLine(lines.next());
            }

            ret.add(cpy);
        }

        return ret;
    }

    private int compare(dmClass a_expected, dmClass a_actual) {

        if (a_expected.getName().compareTo(a_actual.getName()) != 0) {
            System.out.println("Class names do not match");
            System.out.println("\tExpected: " + a_expected.getName());
            System.out.println("\tActual: " + a_actual.getName());
            return a_expected.getName().compareTo(a_actual.getName());
        }

        if (a_expected.getDependencyCount() != a_actual.getDependencyCount()) {
            System.out.println("Dependency count does not match");
            System.out.println("\tExpected: " + a_expected.getDependencyCount());
            dumpDependencies(a_expected);
            System.out.println("\tActual: " + a_actual.getDependencyCount());
            dumpDependencies(a_actual);
            return a_expected.getDependencyCount() - a_actual.getDependencyCount();
        }

        List<dmDependency> eDeps = deepCopyDependencies(a_expected.getDependencies());
        List<dmDependency> aDeps = deepCopyDependencies(a_actual.getDependencies());

        // remove all equal dependencies
        Iterator<dmDependency> eIt = eDeps.iterator();

        while (eIt.hasNext()) {
            dmDependency eDep = eIt.next();

            Iterator<dmDependency> aIt = aDeps.iterator();
            boolean matched = false;
            while (aIt.hasNext()) {
                dmDependency aDep = aIt.next();

                if (eDep.getTarget().getName().compareTo(aDep.getTarget().getName()) == 0 && eDep.getType().compareTo(aDep.getType()) == 0) {

                    if (eDep.getCount() == aDep.getCount()) {
                        matched = true;
                        eIt.remove();
                        aIt.remove();
                        break;
                    } else {
                        // counts did not match
                        System.out.println("Dependency count does not match for type: " + eDep.getType() + ", with target:" + eDep.getTarget().getName());
                        System.out.println("\tExpected: " + eDep.getCount());
                        dumpDependencies(a_expected);
                        System.out.println("\tActual: " + aDep.getCount());
                        dumpDependencies(a_actual);
                        return eDep.getCount() - aDep.getCount();
                    }
                }
            }

            if (!matched) {
                System.out.println("Could not find dependency in actual of type " + eDep.getType() + ", with target:" + eDep.getTarget().getName());
                System.out.println("\tExpected:");
                dumpDependencies(a_expected);

                System.out.println("\tActual:");
                dumpDependencies(a_actual);

                return 1;
            }
        }

        return 0;
    }

    @Test
    void localSelfVars1() {
        try {

            dmClass expected = new dmClass(g_classesPkg + "SelfCall$SelfCall1");
            expected.addDependency("java.lang.Object", dmDependency.Type.Extends);
            expected.addDependency("java.lang.Object", ConstructorCall);
            expected.addDependency("void", dmDependency.Type.Returns);
            expected.addDependency("void", dmDependency.Type.Returns);
            expected.addDependency(g_classesPkg + "SelfCall$SelfCall1", dmDependency.Type.Argument);
            //expected.addDependency("se.lnu.siq.dmodel.classes.SelfCall$SelfCall1", dmDependency.Type.ArgumentUse);


            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "SelfCall$SelfCall1.class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(g_classesPkg + "SelfCall$SelfCall1");
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
    void localSelfVars2() {
        try {

            String className = g_classesPkg + "SelfCall$SelfCall2";
            dmClass expected = new dmClass(className);
            expected.addDependency("java.lang.Object", dmDependency.Type.Extends);
            expected.addDependency("java.lang.Object", ConstructorCall);
            expected.addDependency("void", dmDependency.Type.Returns);
            expected.addDependency("void", dmDependency.Type.Returns);
            expected.addDependency(className, dmDependency.Type.Field);
            expected.addDependency(className, dmDependency.Type.OwnFieldUse);


            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "SelfCall$SelfCall2.class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(className);
            assertTrue(c != null);

            assertTrue(compare(expected, c) == 0);


        } catch (Exception e) {
            assertTrue(false);
        }

    }

    @Test
    void localSelfVars3() {
        try {

            String className = g_classesPkg + "SelfCall$SelfCall3";
            dmClass expected = new dmClass(className);
            expected.addDependency("java.lang.Object", dmDependency.Type.Extends);
            expected.addDependency("java.lang.Object", ConstructorCall);
            expected.addDependency("void", dmDependency.Type.Returns);
            expected.addDependency("void", dmDependency.Type.Returns);
            expected.addDependency(className, dmDependency.Type.LocalVar);
            expected.addDependency(className, ConstructorCall);

            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "SelfCall$SelfCall3.class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(className);
            assertTrue(c != null);

            assertTrue(compare(expected, c) == 0);


        } catch (Exception e) {
            assertTrue(false);
        }
    }

    @Test
    void test3() {
        try {
            String className = g_classesPkg + "Test3";
            dmClass expected = new dmClass(className);
            expected.addDependency("java.lang.Object", dmDependency.Type.Extends);
            expected.addDependency(g_classesPkg + "InterfaceTest", dmDependency.Type.Implements);
            expected.addDependency("java.lang.Integer", dmDependency.Type.Argument); // argument in constructor
            expected.addDependency("java.lang.Object", ConstructorCall);
            expected.addDependency("java.util.List", dmDependency.Type.MethodCall); // add
            expected.addDependency("java.util.List", dmDependency.Type.OwnFieldUse); // add
            expected.addDependency("java.util.List", dmDependency.Type.MethodCall); // add
            expected.addDependency("java.util.List", dmDependency.Type.OwnFieldUse); // add
            expected.addDependency("java.lang.Float", ConstructorCall); // new Float
            expected.addDependency("void", dmDependency.Type.Returns);
            expected.addDependency("void", dmDependency.Type.Returns);
            expected.addDependency("java.lang.String", dmDependency.Type.Field);
            expected.addDependency("java.lang.Object", dmDependency.Type.Field);
            expected.addDependency("java.util.List", dmDependency.Type.Field);
            expected.addDependency("java.lang.System", dmDependency.Type.FieldUse);
            expected.addDependency("java.io.PrintStream", dmDependency.Type.MethodCall);

            // static constant
            expected.addDependency("java.lang.String", dmDependency.Type.Field);
            expected.addDependency("java.lang.String", dmDependency.Type.Field);

            // doStuff
            expected.addDependency("java.lang.String", dmDependency.Type.Returns);
            expected.addDependency("java.lang.Integer", dmDependency.Type.Argument);
            expected.addDependency("java.lang.Integer", dmDependency.Type.MethodCall);

            // processString
            expected.addDependency("java.lang.String", dmDependency.Type.Returns);
            expected.addDependency("java.lang.String", dmDependency.Type.Argument);

            // static initializer
            expected.addDependency("void", dmDependency.Type.Returns);

            expected.addDependency("java.lang.Double", dmDependency.Type.LocalVar);
            expected.addDependency("java.lang.Double", dmDependency.Type.MethodCall);   // static call java.lang.Double.valueOf (assignment)
            expected.addDependency("java.lang.Double", dmDependency.Type.MethodCall);   // toString call
            expected.addDependency("java.lang.String", dmDependency.Type.OwnFieldUse);  // setting a_NONFINAL_STRING to dret at the var declaration
            expected.addDependency("java.lang.String", dmDependency.Type.OwnFieldUse);  // setting a_NONFINAL_STRING



            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "Test3.class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(className);
            assertTrue(c != null);

            assertTrue(compare(expected, c) == 0);

            assertEquals(12, c.getLineCount());
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void test2() {
        try {
            String className = g_classesPkg + "Test2";
            dmClass expected = new dmClass(className);
            expected.addDependency("java.lang.Object", dmDependency.Type.Extends);
            expected.addDependency("java.lang.Object", ConstructorCall);
            expected.addDependency("void", dmDependency.Type.Returns);

            // getTest3 method
            expected.addDependency(g_classesPkg + "Test3", dmDependency.Type.Returns);
            expected.addDependency(g_classesPkg + "Test3", ConstructorCall);

            // a3Arguments method
            expected.addDependency("void", dmDependency.Type.Returns);
            expected.addDependency("java.lang.String", dmDependency.Type.Argument);
            expected.addDependency("java.lang.Integer", dmDependency.Type.Argument);
            expected.addDependency("java.lang.Float", dmDependency.Type.Argument);

            // aGenericArgument method
            expected.addDependency("void", dmDependency.Type.Returns);
            expected.addDependency("java.lang.String", dmDependency.Type.Argument);
            expected.addDependency("java.util.List", dmDependency.Type.Argument);

            // aGenericReturnType
            expected.addDependency("java.lang.Object", dmDependency.Type.Returns);
            expected.addDependency("java.util.List", dmDependency.Type.Returns);

            // aMix
            expected.addDependency("java.lang.Object", dmDependency.Type.Returns);
            expected.addDependency("java.util.List", dmDependency.Type.Returns);
            expected.addDependency("java.lang.String", dmDependency.Type.Argument);
            expected.addDependency("java.util.List", dmDependency.Type.Argument);
            expected.addDependency("java.lang.String", dmDependency.Type.Argument);



            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "Test2.class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(className);
            assertTrue(c != null);

            assertTrue(compare(expected, c) == 0);

            assertEquals(6, c.getLineCount());
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void test1() {
        try {
            String className = g_classesPkg + "Test1";
            dmClass expected = new dmClass(className);
            expected.addDependency("java.lang.Object", dmDependency.Type.Extends);
            expected.addDependency("java.lang.Object", ConstructorCall);
            expected.addDependency("void", dmDependency.Type.Returns);


            // AMethod
            expected.addDependency("void", dmDependency.Type.Returns);
            expected.addDependency("java.lang.String", dmDependency.Type.LocalVar);

            // ifblock
            expected.addDependency("java.lang.String", dmDependency.Type.MethodCall);
            expected.addDependency("java.lang.Integer", dmDependency.Type.LocalVar);
            expected.addDependency("java.lang.Integer", dmDependency.Type.MethodCall);
            expected.addDependency("java.lang.System", dmDependency.Type.FieldUse);
            expected.addDependency("java.io.PrintStream", dmDependency.Type.MethodCall);

            // block 0
            expected.addDependency("java.lang.Float", dmDependency.Type.LocalVar);
            expected.addDependency("java.lang.Float", dmDependency.Type.MethodCall);
            expected.addDependency("java.lang.System", dmDependency.Type.FieldUse);
            expected.addDependency("java.io.PrintStream", dmDependency.Type.MethodCall);

            // block 1
            expected.addDependency("java.util.List", dmDependency.Type.LocalVar);
            expected.addDependency("java.util.ArrayList", ConstructorCall);
            expected.addDependency("java.lang.Object", dmDependency.Type.LocalVar);
            expected.addDependency("java.util.List", dmDependency.Type.MethodCall);

            // block 2
            expected.addDependency(g_classesPkg + "Test2", dmDependency.Type.LocalVar);
            expected.addDependency(g_classesPkg + "Test2", ConstructorCall);
            expected.addDependency(g_classesPkg + "Test2", dmDependency.Type.MethodCall);
            expected.addDependency(g_classesPkg + "Test3", dmDependency.Type.MethodCall);

            // anotherMethod
            expected.addDependency("java.lang.String", dmDependency.Type.Returns);
            expected.addDependency(g_classesPkg + "Test2", dmDependency.Type.LocalVar);
            expected.addDependency(g_classesPkg + "Test2", dmDependency.Type.ConstructorCall);
            expected.addDependency(g_classesPkg + "Test2", dmDependency.Type.MethodCall);
            expected.addDependency(g_classesPkg + "Test3", dmDependency.Type.MethodCall);
            expected.addDependency(g_classesPkg + "Test3", dmDependency.Type.MethodCall);
            expected.addDependency("java.lang.String", dmDependency.Type.ConstructorCall);
            expected.addDependency("java.lang.Integer", dmDependency.Type.ConstructorCall);


            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "Test1.class");
            if (pb.getProject().trackConstantDeps()) {   // parse Test3 that contains the public static final constant
                ClassReader classReader = new ClassReader(ASMdmProjectBuilder.class.getResourceAsStream(g_classesDir + "Test3.class"));
                classReader.accept(pb, 0);
            }

            // a third method
            expected.addDependency("void", dmDependency.Type.Returns);

            expected.addDependency("java.lang.String", dmDependency.Type.LocalVar); // str
            expected.addDependency("java.lang.String", dmDependency.Type.LocalVar); // str2

            if (pb.getProject().trackConstantDeps()) {
                expected.addDependency(g_classesPkg + "Test3", dmDependency.Type.FieldUse);   // A_FINAL_STRING
            }
            expected.addDependency(g_classesPkg + "Test3", dmDependency.Type.FieldUse);   // A_NONFINAL_STRING



            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(className);
            assertTrue(c != null);

            assertTrue(compare(expected, c) == 0);

            assertEquals(17, c.getLineCount());


            dmClass test2 = pb.getProject().findClass(g_classesPkg + "Test2");
            assertNotNull(test2);
            assertEquals(3, test2.getIncomingDependencies().size());
            for (dmDependency d : test2.getIncomingDependencies()) {
                assertEquals(c, d.getSource());
                switch (d.getType()) {
                    case ConstructorCall:
                        assertEquals(2, d.getCount());
                    break;
                    case LocalVar:
                        assertEquals(2, d.getCount());
                        break;
                    case MethodCall:
                        assertEquals(2, d.getCount());
                    break;
                    default:
                        assertTrue(false);
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void arrayTest2() {
        try {
            dmClass expected = new dmClass(g_classesPkg + "ArrayTest2");

            // constructor
            expected.addDependency("java.lang.Object", dmDependency.Type.Extends);
            expected.addDependency("java.lang.Object", ConstructorCall);
            expected.addDependency("void", dmDependency.Type.Returns);

            // the arrays
            expected.addDependency("java.lang.String", dmDependency.Type.LocalVar);
            expected.addDependency("java.lang.String", ConstructorCall);
            expected.addDependency("void", dmDependency.Type.Returns);

            expected.addDependency("java.lang.Float", dmDependency.Type.LocalVar);
            expected.addDependency("java.lang.Float", ConstructorCall);
            expected.addDependency("void", dmDependency.Type.Returns);

            expected.addDependency("java.lang.Integer", dmDependency.Type.LocalVar);
            expected.addDependency("java.lang.Integer", ConstructorCall);
            expected.addDependency("void", dmDependency.Type.Returns);

            expected.addDependency("java.lang.Double", dmDependency.Type.LocalVar);
            expected.addDependency("java.lang.Double", ConstructorCall);
            expected.addDependency("void", dmDependency.Type.Returns);

            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "ArrayTest2.class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(expected.getName());
            assertTrue(c != null);

            assertTrue(compare(expected, c) == 0);
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    @Test
    void arrayTest() {
        try {

            dmClass expected = new dmClass(g_classesPkg + "ArrayTest");

            expected.addDependency("java.lang.Object", dmDependency.Type.Extends);
            expected.addDependency("java.lang.String", dmDependency.Type.Field);
            expected.addDependency("java.lang.String", dmDependency.Type.Field);

            // constructor
            expected.addDependency("java.lang.Object", ConstructorCall);
            expected.addDependency("void", dmDependency.Type.Returns);
            expected.addDependency("java.lang.String", ConstructorCall);
            expected.addDependency("java.lang.String", dmDependency.Type.OwnFieldUse);
            expected.addDependency("java.lang.String", dmDependency.Type.OwnFieldUse);
            expected.addDependency("java.lang.String", dmDependency.Type.OwnFieldUse);
            expected.addDependency("java.lang.String", dmDependency.Type.OwnFieldUse);

            // getIntegers
            expected.addDependency("int", dmDependency.Type.Returns);

            // setFloats
            expected.addDependency("void", dmDependency.Type.Returns);
            expected.addDependency("float", dmDependency.Type.Argument);

            // localVar
            expected.addDependency("void", dmDependency.Type.Returns);
            expected.addDependency("java.lang.Object", dmDependency.Type.LocalVar);
            expected.addDependency("java.lang.Object", ConstructorCall);

            // arrayVar
            expected.addDependency("void", dmDependency.Type.Returns);
            expected.addDependency("java.lang.String", dmDependency.Type.OwnFieldUse);

            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "ArrayTest.class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(expected.getName());
            assertTrue(c != null);

            assertTrue(compare(expected, c) == 0);

            //dumpDependencies(c);
            //assertEquals(3, c.getDependencyCount());

        } catch (Exception e) {
            assertTrue(false);
        }
    }

    @Test
    void exceptionTest() {
        try {

            dmClass expected = new dmClass(g_classesPkg + "ExceptionTest");
            expected.addDependency("java.lang.Object", dmDependency.Type.Extends);
            expected.addDependency("java.lang.Object", ConstructorCall);
            expected.addDependency("void", dmDependency.Type.Returns);

            // Throws Exception
            expected.addDependency("void", dmDependency.Type.Returns);
            expected.addDependency("java.lang.Exception", dmDependency.Type.Throws);
            expected.addDependency("java.lang.NumberFormatException", dmDependency.Type.ConstructorCall);


            // catchesException
            expected.addDependency("void", dmDependency.Type.Returns);
            //expected.addDependency("double", dmDependency.Type.LocalVar);
            expected.addDependency("java.lang.Exception", dmDependency.Type.LocalVar);
            expected.addDependency("java.lang.Exception", dmDependency.Type.MethodCall);
            expected.addDependency("java.lang.NumberFormatException", dmDependency.Type.LocalVar);
            expected.addDependency("java.lang.NumberFormatException", dmDependency.Type.MethodCall);

            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "ExceptionTest.class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(expected.getName());
            assertTrue(c != null);

            assertTrue(compare(expected, c) == 0);

            //dumpDependencies(c);
            //assertEquals(3, c.getDependencyCount());

        } catch (Exception e) {
            assertTrue(false);
        }
    }

    @Test
    void innerClassTest1() {
        try {

            dmClass expected = new dmClass(g_classesPkg + "InnerClassTest");
            expected.addDependency("java.lang.Object", dmDependency.Type.Extends);
            expected.addDependency("java.lang.Object", ConstructorCall);
            expected.addDependency("void", dmDependency.Type.Returns);

            expected.addDependency("java.lang.String", dmDependency.Type.Field);
            expected.addDependency("java.lang.String", dmDependency.Type.Field);
            expected.addDependency(g_classesPkg + "InnerClassTest$Inner2", dmDependency.Type.Field);

            // asReturnType1
            expected.addDependency("java.lang.Object", dmDependency.Type.Returns);
            expected.addDependency(g_classesPkg + "InnerClassTest$1", ConstructorCall);

            // asReturnType2
            expected.addDependency("java.lang.Object", dmDependency.Type.Returns);
            expected.addDependency(g_classesPkg + "InnerClassTest$2", ConstructorCall);

            // asDeepNesting
            expected.addDependency("java.lang.Object", dmDependency.Type.Returns);
            expected.addDependency(g_classesPkg + "InnerClassTest$3", ConstructorCall);

            // synthetic accessors generated as we access m_str1 and m_str2
            expected.addDependency(g_classesPkg + "InnerClassTest", dmDependency.Type.Argument);
            expected.addDependency("java.lang.String", dmDependency.Type.OwnFieldUse);
            expected.addDependency("java.lang.String", dmDependency.Type.Returns);
            expected.addDependency(g_classesPkg + "InnerClassTest", dmDependency.Type.Argument);
            expected.addDependency("java.lang.String", dmDependency.Type.OwnFieldUse);
            expected.addDependency("java.lang.String", dmDependency.Type.Returns);


            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir+ "InnerClassTest.class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(expected.getName());
            assertTrue(c != null);

            assertTrue(compare(expected, c) == 0);

            //dumpDependencies(c);
            //assertEquals(3, c.getDependencyCount());

        } catch (Exception e) {
            assertTrue(false);
        }
    }

    @Test
    void innerClassTest2() {
        try {

            dmClass expected = new dmClass(g_classesPkg + "InnerClassTest$2");
            expected.addDependency("java.lang.Object", dmDependency.Type.Extends);
            expected.addDependency("java.lang.Object", ConstructorCall);
            expected.addDependency("void", dmDependency.Type.Returns);

            // toString
            expected.addDependency("java.lang.String", dmDependency.Type.Returns);
            expected.addDependency(g_classesPkg + "InnerClassTest", dmDependency.Type.FieldUse);


            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "InnerClassTest$2.class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(expected.getName());
            assertTrue(c != null);

            assertTrue(compare(expected, c) == 0);

            //dumpDependencies(c);
            //assertEquals(3, c.getDependencyCount());

        } catch (Exception e) {
            assertTrue(false);
        }
    }

    @Test
    void innerClassTest3_1() {
        try {

            dmClass expected = new dmClass(g_classesPkg + "InnerClassTest$3$1");
            expected.addDependency("java.lang.Object", dmDependency.Type.Extends);
            expected.addDependency("java.lang.Object", ConstructorCall);
            expected.addDependency("void", dmDependency.Type.Returns);

            // toString
            expected.addDependency("java.lang.String", dmDependency.Type.Returns);
            expected.addDependency(g_classesPkg + "InnerClassTest", dmDependency.Type.FieldUse);


            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "InnerClassTest$3$1.class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(expected.getName());
            assertTrue(c != null);

            assertTrue(compare(expected, c) == 0);

            //dumpDependencies(c);
            //assertEquals(3, c.getDependencyCount());

        } catch (Exception e) {
            assertTrue(false);
        }
    }

    @Test
    void innerClassTest_Inner() {
        try {

            dmClass expected = new dmClass(g_classesPkg + "InnerClassTest$Inner");
            expected.addDependency("java.lang.Object", dmDependency.Type.Extends);
            expected.addDependency("java.lang.Object", ConstructorCall);
            expected.addDependency("void", dmDependency.Type.Returns);

            expected.addDependency("java.lang.Integer", dmDependency.Type.Field);


            // constructor
            expected.addDependency("java.lang.Integer", dmDependency.Type.OwnFieldUse);
            expected.addDependency("java.lang.Integer", dmDependency.Type.Argument);



            // toString
            expected.addDependency("java.lang.String", dmDependency.Type.Returns);


            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "InnerClassTest$Inner.class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(expected.getName());
            assertTrue(c != null);

            assertTrue(compare(expected, c) == 0);

            //dumpDependencies(c);
            //assertEquals(3, c.getDependencyCount());

        } catch (Exception e) {
            assertTrue(false);
        }
    }

    @Test
    void innerClassTest_Inner2() {
        try {

            dmClass expected = new dmClass(g_classesPkg + "InnerClassTest$Inner2");
            expected.addDependency("java.lang.Object", dmDependency.Type.Extends);
            expected.addDependency("java.lang.Object", ConstructorCall);
            expected.addDependency("void", dmDependency.Type.Returns);

            // toString
            expected.addDependency("java.lang.String", dmDependency.Type.Returns);
            expected.addDependency(g_classesPkg + "InnerClassTest", dmDependency.Type.FieldUse);


            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "InnerClassTest$Inner2.class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(expected.getName());
            assertTrue(c != null);

            assertTrue(compare(expected, c) == 0);

            //dumpDependencies(c);
            //assertEquals(3, c.getDependencyCount());

        } catch (Exception e) {
            assertTrue(false);
        }
    }


    @Test
    void EnumTest() {
        try {

            dmClass expected = new dmClass(g_classesPkg + "EnumTest");
            expected.addDependency("java.lang.Enum", dmDependency.Type.Extends);
            expected.addDependency("java.lang.Enum", ConstructorCall);
            expected.addDependency("void", dmDependency.Type.Returns);

            expected.addDependency(g_classesPkg + "EnumTest", dmDependency.Type.Field);
            expected.addDependency(g_classesPkg + "EnumTest", dmDependency.Type.Field);
            expected.addDependency(g_classesPkg + "EnumTest", dmDependency.Type.Field);

            // enum keyword encapsulates a lot of syntax

            // fields are created used in <clinit>
            // this is automatically generated and adds a lot of own field uses
            expected.addDependency(g_classesPkg + "EnumTest", ConstructorCall);
            expected.addDependency(g_classesPkg + "EnumTest", ConstructorCall);
            expected.addDependency(g_classesPkg + "EnumTest", ConstructorCall);
            expected.addDependency(g_classesPkg + "EnumTest", ConstructorCall);   // an array is also created
            expected.addDependency(g_classesPkg + "EnumTest", dmDependency.Type.OwnFieldUse);
            expected.addDependency(g_classesPkg + "EnumTest", dmDependency.Type.OwnFieldUse);
            expected.addDependency(g_classesPkg + "EnumTest", dmDependency.Type.OwnFieldUse);
            expected.addDependency(g_classesPkg + "EnumTest", dmDependency.Type.OwnFieldUse);
            expected.addDependency(g_classesPkg + "EnumTest", dmDependency.Type.OwnFieldUse);
            expected.addDependency(g_classesPkg + "EnumTest", dmDependency.Type.OwnFieldUse);
            expected.addDependency(g_classesPkg + "EnumTest", dmDependency.Type.OwnFieldUse); // values array
            expected.addDependency("void", dmDependency.Type.Returns);


            // static values() method
            expected.addDependency(g_classesPkg + "EnumTest", dmDependency.Type.OwnFieldUse);
            expected.addDependency(g_classesPkg + "EnumTest", dmDependency.Type.Returns);

            // static valueOf() method
            expected.addDependency("java.lang.String", dmDependency.Type.Argument);
            expected.addDependency(g_classesPkg + "EnumTest", dmDependency.Type.Returns);
            expected.addDependency("java.lang.Enum", dmDependency.Type.MethodCall);

            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "EnumTest.class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(expected.getName());
            assertTrue(c != null);

            assertTrue(compare(expected, c) == 0);

            //dumpDependencies(c);
            //assertEquals(3, c.getDependencyCount());

        } catch (Exception e) {
            assertTrue(false);
        }
    }

    @Test
    void interfaceTest() {
        try {

            dmClass expected = new dmClass(g_classesPkg + "InterfaceTest");
            expected.addDependency("java.lang.Object", dmDependency.Type.Extends);
            //expected.addDependency("java.lang.Object", dmDependency.Type.MethodCall);
            //expected.addDependency("void", dmDependency.Type.Returns);

            // doStuff
            expected.addDependency("java.lang.String", dmDependency.Type.Returns);
            expected.addDependency("java.lang.Integer", dmDependency.Type.Argument);


            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "InterfaceTest.class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(expected.getName());
            assertTrue(c != null);

            assertTrue(compare(expected, c) == 0);

            dmClass cStr = pb.getProject().findClass("java.lang.String");
            assertNotNull(cStr);
            assertEquals(1, cStr.getIncomingDependencies().size());
            assertEquals(c, cStr.getIncomingDependencies().iterator().next().getSource());
            assertEquals(dmDependency.Type.Returns, cStr.getIncomingDependencies().iterator().next().getType());
            assertEquals(1, cStr.getIncomingDependencies().iterator().next().getCount());


            //dumpDependencies(c);
            //assertEquals(3, c.getDependencyCount());

        } catch (Exception e) {
            assertTrue(false);
        }
    }

    private ASMdmProjectBuilder getAsMdmProjectBuilder(String name) throws IOException {
        InputStream in = ASMdmProjectBuilder.class.getResourceAsStream(name);
        ASMdmProjectBuilder pb = new ASMdmProjectBuilder();
        pb.getProject().doTrackConstantDeps(true);
        ClassReader classReader = new ClassReader(in);
        classReader.accept(pb, 0);
        return pb;
    }

}