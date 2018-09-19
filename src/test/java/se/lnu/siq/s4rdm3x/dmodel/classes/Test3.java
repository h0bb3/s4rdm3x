package se.lnu.siq.s4rdm3x.dmodel.classes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tohto on 2017-04-21.
 */
public class Test3 implements InterfaceTest {
    private String m_str;
    private List<Object> m_list;

    public final static String A_FINAL_STRING = "dret";
    public static String A_NONFINAL_STRING = "dret"; // line

    static {
        Double d = 3.1415;  // line
        A_NONFINAL_STRING = d.toString();   // line
    }   // line

    public Test3(Integer anInt) {  // line (call to super?)
        m_list.add(anInt);  // line count
        m_list.add(new Float(17));
    }   // line count (return created object)

    public void doSomething() {
        System.out.println("Hello Test3 World"); // line count
    } // line count

    public String doStuff(Integer anInt) {
        return anInt.toString(); // line count
    }

    public static String processString(String a_string) {
        return a_string;    // line count
    }
}
