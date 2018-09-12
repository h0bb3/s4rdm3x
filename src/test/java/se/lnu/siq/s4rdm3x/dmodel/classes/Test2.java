package se.lnu.siq.s4rdm3x.dmodel.classes;

import java.util.List;

/**
 * Created by tohto on 2017-04-21.
 */
public class Test2 {

    public Test3 getTest3() { return new Test3(null); }

    public void a3Arguments(String a_arg1, Integer a_arg2, Float a_arg3) {}

    public void aGenericArgument(List<String> a_listOStrings) { }

    public List<Object> aGenericReturnType() {return null;}

    public List<Object> aMix(List<String> a_listOStrings, String a_str) {return null;}
}
