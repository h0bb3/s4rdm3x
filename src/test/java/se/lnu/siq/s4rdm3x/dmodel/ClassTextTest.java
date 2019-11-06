package se.lnu.siq.s4rdm3x.dmodel;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClassTextTest {

    static final String g_classesPkg = "se.lnu.siq.s4rdm3x.dmodel.classes.";
    static final String g_classesDir = "/se/lnu/siq/s4rdm3x/dmodel/classes/";

    @Test
    void text_Test1() {
        try {
            dmClass c = getTestClass("Test1");
            String[] expected = new String[] {"name", "AMetod", "i", "f", "t2", "anotherMethod", "aThirdMethod", "str", "str2", "lo", "nisse", "NISSE", "25"};
            String[] notExpected = new String[] {"Test1","String", "compareToIgnoreCase", "System.out.println", "System", "out", "println", "toString", "Float", "List<Object>", "List", "Object", "add", "Test2", "getTest3()", "doSomething()", "processString", "getTest3()", "doStuff", "Test3.A_FINAL_STRING", "A_FINAL_STRING", "Test3.A_NONFINAL_STRING", "Test3", "A_NONFINAL_STRING"};

            assertExists(c.getTexts(), expected);
            assertNotExists(c.getTexts(), notExpected);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void text_Test2() {
        try {
            dmClass c = getTestClass("Test2");
            String[] expected = new String[] {"getTest3", "a3Arguments", "aGenericArgument", "aGenericReturnType", "aMix", "a_arg1", "a_arg2", "a_arg3", "a_listOStrings", "a_listOStrings", "a_str"};
            String[] notExpected = new String[] {"Test2", "Test3", "List<Object>", "List", "Object", "String", "return", "null"};

            assertExists(c.getTexts(), expected);
            assertNotExists(c.getTexts(), notExpected);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    void text_Test3() {
        try {
            dmClass c = getTestClass("Test3");
            String[] expected = new String[] {"m_str", "m_list", "A_FINAL_STRING", "A_NONFINAL_STRING", "d", "anInt", "a_string", "dret", "dreta", "Hello Test3 World", "3.1415", "17.0"};
            String[] notExpected = new String[] {"Test3", "InterfaceTest", "List<Object>", "List", "Object", "String", "return", "null", "final", "static"};

            assertExists(c.getTexts(), expected);
            assertNotExists(c.getTexts(), notExpected);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            assertTrue(false);
        }
    }
    private void assertExists(Iterable<String> texts, String[] expected) {
        for (int i = 0; i < expected.length; i++) {
            boolean found = false;

            for (String s : texts) {
                if (s.equals(expected[i])) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                System.out.println("Could not find: " + expected[i]);
            }
            assertTrue(found);
        }
    }

    private void assertNotExists(Iterable<String> texts, String[] expected) {
        for (int i = 0; i < expected.length; i++) {
            boolean found = false;

            for (String s : texts) {
                if (s.equals(expected[i])) {
                    found = true;
                    break;
                }
            }

            if (found) {
                System.out.println("Could find: " + expected[i]);
            }
            assertTrue(!found);
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
}
