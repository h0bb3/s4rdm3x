package se.lnu.siq.s4rdm3x.dmodel.classes;

/**
 * Created by tohto on 2017-04-26.
 */
public class SelfCall {

    // -> java.lang.Object : Extends x 1
    // -> void : Returns x 2
    // -> java.lang.Object : MethodCall x 1
    // -> se.lnu.siq.asm_test1.classes.SelfCall$SelfCall1 : Argument x 1
    // -> se.lnu.siq.asm_test1.classes.SelfCall$SelfCall1 : ArgumentUse x 1
    public static class SelfCall1 {

        public void someMethod(SelfCall1 a_someArg) {

            a_someArg.someMethod(null); // this is a call on another object than this, should be counted as a argument use
            someMethod(null);  // this is a call on this should not be counted
        }
    }

    // -> java.lang.Object : Extends x 1
    // -> void : Returns x 2
    // -> java.lang.Object : MethodCall x 1
    // -> se.lnu.siq.asm_test1.classes.SelfCall$SelfCall2 : Field x 1
    // -> se.lnu.siq.asm_test1.classes.SelfCall$SelfCall2 : OwnFieldUse x 1
    public static class SelfCall2 {
        private SelfCall2 a_field;
        public void someMethod() {
            a_field.someMethod();
        }
    }

    // -> java.lang.Object : Extends x 1
    // -> void : Returns x 2
    // -> java.lang.Object : MethodCall x 1
    // -> se.lnu.siq.asm_test1.classes.SelfCall$SelfCall3 : LocalVar x 1
    public static class SelfCall3 {

        public void someMethod() {
            SelfCall3 sc = new SelfCall3();
            sc.someMethod();
        }
    }
}
