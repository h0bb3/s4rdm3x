package se.lnu.siq.s4rdm3x.dmodel.classes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tohto on 2017-04-21.
 */
public class Test1 {    // line count default constructor returns Test1 object?

    public void AMetod() {
        String name = "nisse";  // line count

        // ifblock
        if (name.compareToIgnoreCase("NISSE") == 0) {   // line count
            Integer i = null;                               // line count
            System.out.println(i.toString());               // line count
        }

        // block 0
        {
            Float f = null;                     // line count
            System.out.println(f.toString());   // line count
        }

        // block 1
        {
            List<Object> lo = new ArrayList<Object>();  // line count
            lo.add(name);                          // line count
            //System.out.println(lo.toString());
        }

        // block 2
        {
            Test2 t2 = new Test2();         // line count
            t2.getTest3().doSomething();    // line count
        }
    }   // line count return void

    public String anotherMethod() {
        Test2 t2 = new Test2();     // line count
        return Test3.processString(new String(t2.getTest3().doStuff(new Integer(25)))); // line count return
    }

    public void aThirdMethod() {
        String str = Test3.A_FINAL_STRING;  // line count
        String str2 = Test3.A_NONFINAL_STRING;  // line count
    }   // line count return void
}
