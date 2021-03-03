package se.lnu.siq.s4rdm3x.magicinvoker_tests;

public class ClassWithPrivateMethods {

    protected boolean a_aPrivateMethod1Called = false;

    private void aPrivateMethod1() {
        a_aPrivateMethod1Called = true;
    }

    private int aPrivateMethod2() {
        return 17;
    }

    private ClassWithPrivateMethods aPrivateMethod3() {
        return this;
    }

    private String aPrivateMethod4(String a_arg) {
        return "Hello World";
    }

    private String aPrivateMethod4() {
        return "Hello Other World";
    }

    private String aPrivateMethod5(Iterable<String> a_strings) {
        return "Hello Iterable World";
    }

    // This does not work
    private String aPrivateMethod6(String ... a_args) {
        String ret = new String();

        for (int i = 0; i < a_args.length; i++) {
            ret += a_args[i];
        }

        return ret;
    }

    // this will probably be very hard
    private String aPrivateMethod6(String a_arg, String ... a_args) {
        String ret = a_arg;

        for (int i = 0; i < a_args.length; i++) {
            ret += a_args[i];
        }

        return ret+"-heyho";
    }

    private int aPrivateMethod7(int a_n1, float a_n2) {
        return a_n1 + (int)a_n2;
    }

    private float aPrivateMethod7(float a_n1, float a_n2) {
        return a_n1 + a_n2;
    }

    private double aPrivateMethod8(double a_n1, boolean a_bool, char a_c, byte a_b, long a_long, short a_s) {
        return a_n1;
    }
}
