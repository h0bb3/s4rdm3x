package se.lnu.siq.s4rdm3x.dmodel.classes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tohto on 2017-04-21.
 */
public class Test1 {

    public void AMetod() {
        String name = "nisse";

        // ifblock
        if (name.compareToIgnoreCase("NISSE") == 0) {
            Integer i = null;
            System.out.println(i.toString());
        }

        // block 0
        {
            Float f = null;
            System.out.println(f.toString());
        }

        // block 1
        {
            List<Object> lo = new ArrayList<Object>();
            lo.add(name);
            //System.out.println(lo.toString());
        }

        // block 2
        {
            Test2 t2 = new Test2();
            t2.getTest3().doSomething();
        }
    }

    public String anotherMethod() {
        Test2 t2 = new Test2();
        return Test3.processString(new String(t2.getTest3().doStuff(new Integer(25))));
    }

    public void aThirdMethod() {
        String str = Test3.A_FINAL_STRING;
        String str2 = Test3.A_NONFINAL_STRING;
    }
}
