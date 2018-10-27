package se.lnu.siq.s4rdm3x.dmodel.classes;

public class LCOMTest1 {
    private String arg1;
    private String arg2;

    public void aMethod1() {
        System.out.println(arg1);
        System.out.println(arg2);
        PrivateInner p = new PrivateInner();
        System.out.println(p.arg3);
    }

    private class PrivateInner {
        private String arg3;

        public void aMethod() {
            System.out.println(arg1);
            System.out.println(arg2);
            System.out.println(arg3);
        }
    }

    public static class PublicStaticInner {
        String arg3;

        public void aMethod() {
            //System.out.println(arg1); - does not work
            //System.out.println(arg2);  - does not work
            System.out.println(arg3);
        }
    }
}
