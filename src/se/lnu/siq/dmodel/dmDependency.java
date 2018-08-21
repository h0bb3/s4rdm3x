package se.lnu.siq.dmodel;

//import sun.plugin2.liveconnect.ArgumentHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tohto on 2017-04-24.
 */
public class dmDependency {

    public dmClass getTarget() {
        return m_target;
    }

    public Iterable<Integer> lines() { return m_lines; }

    public Type getType() {
        return m_type;
    }

    public int getCount() {
        return m_lines.size();
    }

    public void inc(int a_line) {
        m_lines.add(a_line);
    }

    public enum Type {
        Extends,
        Implements,
        Field,
        Argument,
        Returns,
        LocalVar,   // def of local var other than self
        MethodCall, // call on other class than self
        OwnFieldUse,   // use of self fields
        FieldUse,   // use of field in some other class
        Throws,     // throws defined exceptions in method headers
        Unknown
    }

    private dmClass m_target;
    private Type m_type;
    private List<Integer> m_lines;

    public dmDependency(dmClass a_target, Type a_type, int a_line) {
        m_target = a_target;
        m_type = a_type;
        m_lines = new ArrayList<>();
        m_lines.add(a_line);
    }

    public dmDependency(dmClass a_target, Type a_type, Iterable<Integer> a_lines) {
        m_target = a_target;
        m_type = a_type;
        m_lines = new ArrayList<>();
        for(Integer i : a_lines) {
            m_lines.add(i);
        }
    }

}
