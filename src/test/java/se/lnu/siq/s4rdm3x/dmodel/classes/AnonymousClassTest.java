package se.lnu.siq.s4rdm3x.dmodel.classes;

public class AnonymousClassTest {

    public void aLocalVariableAnonClass() {

        Object t = new Object() {
            public String toString() {
                return "Anonymous Object";
            }
        };

        System.out.println(t.toString());
    }

    public Object aReturnAnonClass() {
        return new Object() {
            public String toString() {
                return "Anonymous Object as Return";
            }
        };
    }
}
