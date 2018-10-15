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
            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "Test1.class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(g_classesPkg + "Test1");
            assertTrue(c != null);

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
            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "Test2.class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(g_classesPkg + "Test2");
            assertTrue(c != null);

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
            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "Test3.class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(g_classesPkg + "Test3");
            assertTrue(c != null);

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
            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "EnumTest.class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(g_classesPkg + "EnumTest");
            assertTrue(c != null);

            // enums generate 4 methods, values, valueOf, <init> and <clinit>
            assertEquals(4, c.getMethodCount());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void branches_BranchesTest() {
        try {
            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "BranchesTest.class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(g_classesPkg + "BranchesTest");
            assertTrue(c != null);

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

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void branches_NCSS_Test72() {
        try {
            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "NCSS_Test72.class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(g_classesPkg + "NCSS_Test72");
            assertTrue(c != null);

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
            ASMdmProjectBuilder pb = getAsMdmProjectBuilder(g_classesDir + "NCSS_Test72.class");

            assertTrue(pb.getProject() != null);

            dmClass c = pb.getProject().findClass(g_classesPkg + "NCSS_Test72");
            assertTrue(c != null);

            assertEquals(40, c.getMethods("testPWS").get(0).getInstructionCount()); // -1
            assertEquals(41, c.getMethods("testPWU10").get(0).getInstructionCount()); // -1
            assertEquals(24, c.getMethods("intersect").get(0).getInstructionCount()); // -3
            assertEquals(44, c.getMethods("verboseIntersect").get(0).getInstructionCount()); // -9
            assertEquals(12, c.getMethods("testQuestionMark").get(0).getInstructionCount()); //-1

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
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
