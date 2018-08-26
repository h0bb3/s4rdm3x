package se.lnu.siq.s4rdm3x.dmodel;

//import com.sun.corba.main.java.se.spi.ior.iiop.GIOPVersion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by tohto on 2017-04-24.
 */
public class dmClass {
    private String m_name;
    private List<dmDependency> m_deps;

    public dmClass(String a_name) {
        m_name = a_name;
        m_deps = new ArrayList<dmDependency>();
    }

    public String getFileName() {
        String fileName = m_name.replace('.', '/') + ".java";
        if (!isInner()) {
            return fileName;
        } else {
            return fileName.substring(0, m_name.indexOf('$')) + ".java";
        }
    }

    public boolean isInner() {
        return m_name.contains("$");
    }

    public String getClassName() {
        return m_name.substring(m_name.lastIndexOf('.') + 1);
    }

    public boolean hasDirectDependency(String a_name) {
        for(dmDependency d : m_deps) {
            if (d.getTarget().getName().startsWith(a_name)) {
                return true;
            }
        }

        return false;
    }

    private boolean recurseDependencySearch(Iterable<dmClass> a_targets, HashSet<dmClass> a_searched) {
        if (hasDirectDependency(a_targets)) {

            return true;
        } else {
            for(dmDependency d : m_deps) {
                dmClass source = d.getTarget();
                if (!a_searched.contains(source)) {
                    a_searched.add(source);
                    if (source.recurseDependencySearch(a_targets, a_searched)) {
                        System.out.println("dependency path via: " + source);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean recurseDependencySearch(Iterable<dmClass> a_targets, HashSet<dmClass> a_searched, List<dmClass> a_result) {
        for(dmDependency d : m_deps) {
            dmClass source = d.getTarget();
            if (a_searched.contains(source)) {

            }
        }

        /*if (hasDirectDependency(a_targets)) {
            return true;
        } else {
            for(dmDependency d : m_deps) {
                dmClass source = d.getTarget();
                if (a_searched.contains(source)) {
                    a_searched.add(source);
                    if (source.recurseDependencySearch(a_targets, a_searched)) {
                        return true;
                    }
                }
            }
        }*/

        return false;
    }

    public List<dmClass> getIndirectDependencies(Iterable<dmClass> a_targets) {
        List<dmClass> ret = new ArrayList<>();
        HashSet<dmClass> searched = new HashSet<>();

        return ret;
    }

    public boolean hasIndirectDependency(Iterable<dmClass> a_targets) {

        HashSet<dmClass> searched = new HashSet<>();
        return recurseDependencySearch(a_targets, searched);
    }

    public boolean hasDirectDependency(Iterable<dmClass> a_targets) {
        for(dmClass target : a_targets) {
            if (hasDirectDependency(target)) {
                //Sys.out.println("dependency found: " + target.getName());
                return true;
            }
        }

        return false;
    }

    public boolean hasDirectDependency(dmClass a_target) {
        for(dmDependency d : m_deps) {
            if (d.getTarget() == a_target) {
                return true;
            }
        }

        return false;
    }

    public boolean isAnonymous() {
        if (isInner()) {
            String name = m_name.substring(m_name.lastIndexOf('$') + 1);
            //Sys.out.println(name);
            try {
                Integer.parseInt(name);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    public boolean equals(String a_className) {
        return m_name.compareTo(a_className) == 0;
    }

    public void addDependency(String a_className, dmDependency.Type a_type) {
        addDependency(a_className, a_type, -1);
    }

    public void addDependency(String a_className, dmDependency.Type a_type, int a_line) {
        for (dmDependency d : m_deps) {
            if (d.getTarget().getName().compareTo(a_className) == 0 && d.getType() == a_type) {
                d.inc(a_line);
                return;
            }
        }

        dmDependency d = new dmDependency(new dmClass(a_className), a_type, a_line);
        m_deps.add(d);
    }

    public void addDependency(dmClass a_target, dmDependency.Type a_type, int a_line) {
        for (dmDependency d : m_deps) {
            if (d.getTarget() == a_target && d.getType() == a_type) {
                d.inc(a_line);
                return;
            }
        }

        dmDependency d = new dmDependency(a_target, a_type, a_line);
        m_deps.add(d);
    }

    public Iterable<dmDependency> getDependencies() {
        return (Iterable<dmDependency>)m_deps;
    }
    public int getDependencyCount() {
        int ret = 0;
        for(dmDependency d : getDependencies()) {
            ret += d.getCount();
        }

        return ret;
    }

    public String getName() {
        return m_name;
    }
}
