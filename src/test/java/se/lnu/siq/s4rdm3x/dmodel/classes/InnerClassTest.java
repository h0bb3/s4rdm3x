package se.lnu.siq.s4rdm3x.dmodel.classes;

/**
 * Created by tohto on 2017-05-03.
 */
public class InnerClassTest {

    private class Inner {

        Integer m_int;
        public Inner(Integer anInt) {
            m_int = anInt;
        }

        public String toString() {
            return "Hello Inner World";
        }
    }

    private class Inner2 {
        public String toString() {
            return m_str1;  // this is a field use in the containing class
        }
    }

    private String m_str1;
    private String m_str2;
    private Inner2 m_inner2;

    public Object asReturnType1() {
        return new Object() {
            public String toString() { return "Hello World"; }
        };
    }

    public Object asReturnType2() {
        return new Object() {
            public String toString() { return m_str2; }  // this is a field use in the containing class
        };
    }

    public Object asDeepNesting() {
        return new Object() {
            public String toString() {
                Object o = new Object() { public String toString(){return m_str1;}};    // this is a field use in the containing containing class
                return o.toString();
            }
        };
    }
}
