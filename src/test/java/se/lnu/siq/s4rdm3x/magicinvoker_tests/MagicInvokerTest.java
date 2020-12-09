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
            private void aPrivateMethod4() {
                MagicInvoker sut = new MagicInvoker(this);
                String actual = (String)sut.invokeMethodMagic();
                assertEquals("Hello Other World", actual);
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
}
