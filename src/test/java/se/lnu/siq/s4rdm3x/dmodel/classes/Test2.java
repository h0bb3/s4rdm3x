package se.lnu.siq.s4rdm3x.dmodel.classes;

import java.util.List;

/**
 * Created by tohto on 2017-04-21.
 */
public class Test2 {    // line (default constructor, and call to super)

    public Test3 getTest3() {
        return new Test3(null); // line
    }

    public void a3Arguments(String a_arg1, Integer a_arg2, Float a_arg3) {
    }   // line

    public void aGenericArgument(List<String> a_listOStrings) {
    }   // line

    public List<Object> aGenericReturnType() {
        return null; // line
    }

    public List<Object> aMix(List<String> a_listOStrings, String a_str) {
        return null;    // line
    }
}
