package se.lnu.siq.s4rdm3x.dmodel.classes;

/**
 * Created by tohto on 2017-05-01.
 */
public class ArrayTest {
    private String[] m_strings;
    private String m_string;

    public ArrayTest() {
        m_strings = new String[17];
        m_string = "Hello World";
        m_strings[0] = m_string;
    }


    public int[] getIntegers() { return null; }

    public void setFloats(float[] a_floats) {
        a_floats[0] = 3.141592f;
    }

    public void localVar() {
        Object[] arr = new Object[17];
    }
}
