package se.lnu.siq.s4rdm3x.dmodel;

import java.util.*;

/**
 * Created by tohto on 2017-04-24.
 */
public class dmProject {
    private Map<String, dmClass> m_classes;
    private List<BlackListItem> m_blackList;
    private Map<Object, dmClass> m_constants;
    private Map<Object, List<dmClassLinePair>> m_constantDependencies;
    private dmFile.dmDirectory m_rootDir;

    public boolean trackConstantDeps() {
        return m_trackConstantDeps;
    }

    public void doTrackConstantDeps(boolean a_trackConstantDeps) {
        m_trackConstantDeps = a_trackConstantDeps;
    }

    private boolean m_trackConstantDeps;

    private static class dmClassLinePair {
        public dmClass m_class;
        int m_line;
    }

    static private class BlackListItem {
        private String m_name;
        private boolean m_isPackage;

        public BlackListItem(String a_name, boolean a_isPackage) {
            m_name = a_name;
            m_isPackage = a_isPackage;
        }

        boolean isBlackListed(String a_name) {
            if (m_isPackage) {
               return a_name.startsWith(m_name);
            }
            return a_name.compareTo(m_name) == 0;
        }
    }

    public dmProject() {
        m_blackList = new ArrayList<>();
        m_classes = new HashMap<>();
        m_constants = new HashMap<>();
        m_constantDependencies = new HashMap<>();
        m_trackConstantDeps = false;
        m_rootDir = new dmFile.dmDirectory("root", null);

    }

    public void clear() {
        m_blackList.clear();
        m_classes.clear();
        m_constants.clear();
        m_constantDependencies.clear();
    }

    public void addClassToBlackList(String a_name) {
        m_blackList.add(new BlackListItem(a_name, false));
    }

    public void addPackageToBlackList(String a_name) {
        m_blackList.add(new BlackListItem(a_name, true));
    }

    public void addConstant(Object a_constant, dmClass a_containingClass) {
        if (m_trackConstantDeps) {
            m_constants.put(a_constant, a_containingClass);

            // resolve all previously added dependencies
            List<dmClassLinePair> l = m_constantDependencies.get(a_constant);
            if (l != null) {
                for (dmClassLinePair from : l) {
                    if (from.m_class.getName().compareTo(a_containingClass.getName()) != 0) {
                        from.m_class.addDependency(a_containingClass, dmDependency.Type.FieldUse, from.m_line);
                        //Sys.out.println("adding dependency to " + a_constant.toString());
                    }
                }

                m_constantDependencies.remove(a_constant);
            }
        }
    }

    public void addConstantDependency(Object a_constant, dmClass a_dependencyFrom, int a_line) {
        if (m_trackConstantDeps) {
            dmClass containingClass = m_constants.get(a_constant);
            if (containingClass != null) {
                if (a_dependencyFrom.getName().compareTo(containingClass.getName()) != 0) {
                    a_dependencyFrom.addDependency(containingClass, dmDependency.Type.FieldUse, a_line);
                    //Sys.out.println("adding dependency from: " + a_dependencyFrom.getName() + ", to: " + containingClass.getName() + ", because: " + a_constant.toString());
                }
            } else {
                List<dmClassLinePair> l = m_constantDependencies.get(a_constant);
                dmClassLinePair p = new dmClassLinePair();
                p.m_class = a_dependencyFrom;
                p.m_line = a_line;
                if (l == null) {
                    l = new ArrayList<>();
                    m_constantDependencies.put(a_constant, l);

                }
                l.add(p);
            }
        }
    }

    public boolean isBlackListed(String a_className) {
        for (BlackListItem bli : m_blackList) {
            if (bli.isBlackListed(a_className)) {
                return true;
            }
        }

        return false;
    }

    public dmClass addClass(dmClass a_c) {
        dmClass ret = a_c;

        if (isBlackListed(a_c.getName())) {
            return null;
        }

        ret = findClass(ret.getName());
        if (ret == null) {
            m_classes.put(a_c.getName(), a_c);
            ret = a_c;
        }

        return ret;
    }

    public dmClass addJavaClass(String a_logicalName) {
        // create directory structure and add the file / dirs etc
        dmFile f = addFile(dmClass.toJavaSourceFile(a_logicalName));
        return addClass(new dmClass(a_logicalName, f));
    }

    private dmFile addFile(String [] a_parts) {
        dmFile f = m_rootDir.createFile(a_parts);
        return f;
    }

    public Iterable<dmClass> getClasses() {
        return (Iterable<dmClass>)m_classes.values();
    }

    public dmClass findClass(String a_name) {

        return m_classes.get(a_name);
    }
}
