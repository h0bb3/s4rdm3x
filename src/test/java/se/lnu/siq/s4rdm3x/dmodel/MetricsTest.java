package se.lnu.siq.s4rdm3x.dmodel;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MetricsTest {

    static final String g_classesPkg = "se.lnu.siq.s4rdm3x.dmodel.classes.";
    static final String g_classesDir = "/se/lnu/siq/s4rdm3x/dmodel/classes/";

    @Test
    void numberOfMethods_Test1() {
        try {
            dmClass c = getTestClass("Test1");

            assertEquals(4, c.getMethodCount());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void numberOfMethods_Test2() {
        try {
            dmClass c = getTestClass("Test2");

            assertEquals(6, c.getMethodCount());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void numberOfMethods_Test3() {
        try {

            dmClass c = getTestClass("Test3");
            assertEquals(5, c.getMethodCount());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void numberOfMethods_EnumTest() {
        try {

            dmClass c = getTestClass("EnumTest");
            // enums generate 4 methods, values, valueOf, <init> and <clinit>
            // some version of the compiler also generates synthetic method $values
            int expected = c.getMethods("$values").size() == 0 ? 4 : 5;
            assertEquals(expected, c.getMethodCount());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void branches_BranchesTest() {
        try {
            dmClass c = getTestClass("BranchesTest");

            assertEquals(1, c.getMethods("if_").get(0).getBranchStatementCount());
            assertEquals(1, c.getMethods("if_else").get(0).getBranchStatementCount());
            assertEquals(2, c.getMethods("if_elseif_else").get(0).getBranchStatementCount());
            assertEquals(1, c.getMethods("question").get(0).getBranchStatementCount());

            assertEquals(2, c.getMethods("if_and").get(0).getBranchStatementCount());
            assertEquals(2, c.getMethods("if_or").get(0).getBranchStatementCount());

            assertEquals(1, c.getMethods("for_").get(0).getBranchStatementCount());
            assertEquals(1, c.getMethods("while_").get(0).getBranchStatementCount());
            assertEquals(1, c.getMethods("do_while").get(0).getBranchStatementCount());

            assertEquals(2, c.getMethods("catch_").get(0).getBranchStatementCount());

            assertEquals(5, c.getMethods("switch_").get(0).getBranchStatementCount());

            assertEquals(1, c.getMethods("forEach").get(0).getBranchStatementCount());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void branches_NCSS_Test72() {
        try {
            dmClass c = getTestClass("NCSS_Test72");
            assertEquals(3, c.getMethods("testPWS").get(0).getBranchStatementCount());
            assertEquals(3, c.getMethods("testPWU10").get(0).getBranchStatementCount());
            assertEquals(4, c.getMethods("intersect").get(0).getBranchStatementCount());
            assertEquals(7, c.getMethods("verboseIntersect").get(0).getBranchStatementCount());
            assertEquals(1, c.getMethods("testQuestionMark").get(0).getBranchStatementCount());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void instructionCount_NCSS_Test72() {
        try {
            dmClass c = getTestClass("NCSS_Test72");
            assertEquals(40, c.getMethods("testPWS").get(0).getInstructionCount());
            assertEquals(41, c.getMethods("testPWU10").get(0).getInstructionCount());
            assertEquals(24, c.getMethods("intersect").get(0).getInstructionCount());
            assertEquals(44, c.getMethods("verboseIntersect").get(0).getInstructionCount());
            assertEquals(12, c.getMethods("testQuestionMark").get(0).getInstructionCount());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void fieldCount_Test3() {
        dmClass c = getTestClass("Test3");
        assertEquals(4, c.getFieldCount());
    }

    @Test
    void fieldCount_EnumTest() {
        dmClass c = getTestClass("EnumTest");
        assertEquals(3, c.getFieldCount());
    }

    @Test
    void fieldCount_ArrayTest() {
        dmClass c = getTestClass("ArrayTest");
        assertEquals(2, c.getFieldCount());
    }

    @Test
    void fieldCount_InnerClassTest() {
        dmClass c = getTestClass("InnerClassTest");
        assertEquals(3, c.getFieldCount());
    }

    @Test
    void fieldCount_InnerClassTest_Inner() {
        dmClass c = getTestClass("InnerClassTest$Inner");
        assertEquals(1, c.getFieldCount());
    }

    @Test
    void getUsedFieldCount_NCSS_Test72() {
        dmClass c = getTestClass("NCSS_Test72");
        assertEquals(0, c.getMethods("testPWS").get(0).getUsedFieldCount());
        assertEquals(2, c.getMethods("testPWU10").get(0).getUsedFieldCount());
        assertEquals(4, c.getMethods("intersect").get(0).getUsedFieldCount());
        assertEquals(4, c.getMethods("verboseIntersect").get(0).getUsedFieldCount());
        assertEquals(2, c.getMethods("testQuestionMark").get(0).getUsedFieldCount());
    }

    @Test
    void getUsedFieldCount_ArrayTest() {
        dmClass c = getTestClass("ArrayTest");
        assertEquals(2, c.getMethods("<init>").get(0).getUsedFieldCount());
        assertEquals(0, c.getMethods("getIntegers").get(0).getUsedFieldCount());
        assertEquals(0, c.getMethods("setFloats").get(0).getUsedFieldCount());
        assertEquals(0, c.getMethods("localVar").get(0).getUsedFieldCount());
        assertEquals(1, c.getMethods("arrayVar").get(0).getUsedFieldCount());
    }

    private dmClass getTestClass(String a_className) {
        try {
            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + a_className + ".class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(g_classesPkg + a_className);
            assertTrue(c != null);
            return c;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            assertTrue(false);
            return null;
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
