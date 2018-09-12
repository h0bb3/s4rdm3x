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
    public static String A_NONFINAL_STRING = "dret";

    static {
        Double d = 3.1415;
        A_NONFINAL_STRING = d.toString();
    }

    public Test3(Integer anInt) {
        m_list.add(anInt);
    }

    public void doSomething() {
        System.out.println("Hello Test3 World");
    }

    public String doStuff(Integer anInt) {
        return anInt.toString();
    }

    public static String processString(String a_string) {
        return a_string;
    }
}
