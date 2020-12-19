package se.lnu.siq.s4rdm3x.magicinvoker_tests;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.MagicInvoker;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MagicInvokerTest {

    @Test
    public void test_aPrivateMethod1() {

        class Inner extends ClassWithPrivateMethods {
            private void aPrivateMethod1() {
                MagicInvoker sut = new MagicInvoker(this);
                sut.invokeMethodMagic();
                assertTrue(a_aPrivateMethod1Called);
            }
        }

        Inner i = new Inner();
        i.aPrivateMethod1();
    }

    @Test
    public void test_aPrivateMethod2() {

        class Inner extends ClassWithPrivateMethods {
            private void aPrivateMethod2() {
                MagicInvoker sut = new MagicInvoker(this);
                int actual = (int)sut.invokeMethodMagic();
                assertEquals(17, actual);
            }
        }

        Inner i = new Inner();
        i.aPrivateMethod2();
    }

    @Test
    public void test_aPrivateMethod3() {

        class Inner extends ClassWithPrivateMethods {
            private void aPrivateMethod3() {
                MagicInvoker sut = new MagicInvoker(this);
                ClassWithPrivateMethods actual = (ClassWithPrivateMethods)sut.invokeMethodMagic();
                assertEquals(this, actual);
            }
        }

        Inner i = new Inner();
        i.aPrivateMethod3();
    }

    @Test
    public void test_aPrivateMethod4() {

        class Inner extends ClassWithPrivateMethods {
            private void aPrivateMethod4(String a_str) {
                MagicInvoker sut = new MagicInvoker(this);
                String actual = (String)sut.invokeMethodMagic(a_str);
                assertEquals("Hello World", actual);
            }
        }

        Inner i = new Inner();
        i.aPrivateMethod4("");
    }

    @Test
    public void test_aPrivateMethod4_Overload() {

        class Inner extends ClassWithPrivateMethods {
            private String aPrivateMethod4() {
                MagicInvoker sut = new MagicInvoker(this);
                String actual = (String)sut.invokeMethodMagic();
                assertEquals("Hello Other World", actual);
                return actual;
            }
        }

        Inner i = new Inner();
        i.aPrivateMethod4();
    }


    @Test
    public void test_aPrivateMethod5() {

        class Inner extends ClassWithPrivateMethods {
            private void aPrivateMethod5() {
                MagicInvoker sut = new MagicInvoker(this);
                String actual = (String)sut.invokeMethodMagic(new ArrayList<String>());
                assertEquals("Hello Iterable World", actual);
            }
        }

        Inner i = new Inner();
        i.aPrivateMethod5();
    }

    @Test
    public void test_aPrivateMethod6() {

        class Inner extends ClassWithPrivateMethods {
            private String aPrivateMethod6(String ... a_args) {
                MagicInvoker sut = new MagicInvoker(this);
                String actual = (String)sut.invokeMethodMagic((Object)a_args);
               return actual;
            }
        }

        Inner i = new Inner();
        String actual = i.aPrivateMethod6("1", "7");
        assertEquals("17", actual);
    }

    @Test
    public void test_aPrivateMethod6_Overload() {

        class Inner extends ClassWithPrivateMethods {
            private String aPrivateMethod6(String a_arg, String ... a_args) {
                MagicInvoker sut = new MagicInvoker(this);
                String actual = (String)sut.invokeMethodMagic(a_arg, (Object)a_args);
                return actual;
            }
        }

        Inner i = new Inner();
        String actual = i.aPrivateMethod6("1", "7", "9");
        assertEquals("179-heyho", actual);
    }

    @Test
    public void test_aPrivateMethod7_Int() {

        class Inner extends ClassWithPrivateMethods {
            private int aPrivateMethod7(int a_n1, int a_n2) {
                MagicInvoker sut = new MagicInvoker(this);
                int actual = (int)sut.invokeMethodMagic(a_n1, a_n2);
                return actual;
            }
        }

        Inner i = new Inner();
        int actual = i.aPrivateMethod7(1, 7);
        assertEquals(1+7, actual);
    }

    @Test
    public void test_aPrivateMethod7_Float() {

        class Inner extends ClassWithPrivateMethods {
            private float aPrivateMethod7(float a_n1, float a_n2) {
                MagicInvoker sut = new MagicInvoker(this);
                float actual = (float)sut.invokeMethodMagic(a_n1, a_n2);
                return actual;
            }
        }

        Inner i = new Inner();
        float actual = i.aPrivateMethod7(1, 7);
        assertEquals(1.0f+7.0f, actual);
    }

    @Test
    public void test_aPrivateMethod8() {

        class Inner extends ClassWithPrivateMethods {
            private double aPrivateMethod8(double a_n1, boolean a_bool, char a_c, byte a_b, long a_long, short a_s) {
                MagicInvoker sut = new MagicInvoker(this);
                double actual = (double)sut.invokeMethodMagic(a_n1, a_bool, a_c, a_b, a_long, a_s);
                return actual;
            }
        }

        Inner i = new Inner();
        double actual = i.aPrivateMethod8(1.0, true, 't', (byte) 8, 17L, (short)17);
        assertEquals(1.0, actual);
    }
}
