package se.lnu.siq.s4rdm3x.dmodel.classes;

import java.util.Iterator;
import java.util.List;

public class Test4 {
    public void iterateStuff(Iterable<Test3> tests) {
        for (Test3 localVar : tests) {
            localVar.toString();
        }
    }

    public void testVarargs(Test3 ... args) {
        try {
            for (Test3 localVar : args)
                localVar.toString();
        } catch (Exception e) {

        }
    }
}
