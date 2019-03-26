package se.lnu.siq.s4rdm3x.dmodel.classes.CPPCompatibility.simple;

public class BarArgs {
    private float m_float;

    public void add(float a_float) {
        m_float = m_float + a_float;
    }

    public float get() {
        return m_float;
    }
}
