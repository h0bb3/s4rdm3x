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

    private ASMdmProjectBuilder getAsMdmProjectBuilder(String name) throws IOException {
        InputStream in = ASMdmProjectBuilder.class.getResourceAsStream(name);
        ASMdmProjectBuilder pb = new ASMdmProjectBuilder();
        pb.getProject().doTrackConstantDeps(true);
        ClassReader classReader = new ClassReader(in);
        classReader.accept(pb, 0);
        return pb;
    }
}
