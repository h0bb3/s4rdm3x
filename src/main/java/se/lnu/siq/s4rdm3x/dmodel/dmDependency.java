package se.lnu.siq.s4rdm3x.dmodel;

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

    public dmClass getSource() {
        return m_source;
    }

    public Iterable<Integer> lines() { return m_lines; }

    public Type getType() {
        return m_type;
    }

    public int getCount() {
        return m_lines.size();
    }

    public void addLine(int a_line) {
        if (m_type.isFileBased && m_lines.size() > 0) {
            throw new RuntimeException("There can only be one file dependency per type.");
        }
        m_lines.add(a_line);
    }

    public enum Type {
        Extends(false),
        Implements(false),
        Field(false),
        Argument(false),
        Returns(false),
        LocalVar(false),   // def of local var other than self
        MethodCall(false), // call on other class than self
        ConstructorCall(false),   // calls new on an object including self
        OwnFieldUse(false),   // use of self fields
        FieldUse(false),   // use of field in some other class
        Throws(false),     // throws defined exceptions in method headers
        File_Horizontal (true), // defined in the same directory
        File_Vertical(true), // defined as a one level parent - child directory relation
        Unknown(false);

        public final boolean isFileBased;
        Type(boolean a_isFileBased) {
            isFileBased = a_isFileBased;
        }
    }

    private dmClass m_target;
    private dmClass m_source;
    private Type m_type;
    private List<Integer> m_lines;

    public dmDependency(dmClass a_source, dmClass a_target, Type a_type, int a_line) {
        m_source = a_source;
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
