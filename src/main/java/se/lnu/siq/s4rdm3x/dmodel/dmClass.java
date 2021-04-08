package se.lnu.siq.s4rdm3x.dmodel;

//import com.sun.corba.main.java.se.spi.ior.iiop.GIOPVersion;

import java.util.*;

/**
 * Created by tohto on 2017-04-24.
 */
public class dmClass {

    public double get;

    public dmFile getFile() {
        return m_file;
    }



    public static class Method {

        static class DependencyLine {
            public dmDependency m_dependency;
            public int m_line;

            DependencyLine(dmDependency a_dep, int a_line) {
                m_dependency = a_dep;
                m_line = a_line;
            }
        }

        public Method(String a_name, boolean a_isAbstract, boolean a_isSynthetic) {
            m_name = a_name;
            m_isAbstract = a_isAbstract;
            m_isSynthetic = a_isSynthetic;
            m_dependencies = new ArrayList<>();
        }

        private dmFile m_file;
        private boolean m_isAbstract;
        private boolean m_isSynthetic;
        private String m_name;
        private ArrayList<DependencyLine> m_dependencies;

        private HashSet<String> m_usedFields = new HashSet<>();

        public void useField(String a_name) {
            m_usedFields.add(a_name);
        }
        public void addDependency(dmDependency a_dependency, int a_line) {
            m_dependencies.add(new DependencyLine(a_dependency, a_line));
        }

        public int getUsedFieldCount() {
            return m_usedFields.size();
        }


        private int m_branchStatementCount = 0;
        public int getBranchStatementCount() { return m_branchStatementCount; }
        public void incBranchStatementCount() {
            m_branchStatementCount++;
        }

        private int m_instructionCount = 0;
        public void incInstructionCount() {
            m_instructionCount++;
        }
        public int getInstructionCount() {
            return m_instructionCount;
        }

        public String getName() {
            return m_name;
        }
        public boolean isAbstract() {return m_isAbstract;}
        public boolean isSynthetic() {return m_isSynthetic;}

        public boolean isConcrete() {
            return !isAbstract() && !isSynthetic();
        }
    }


    dmFile m_file;
    private String m_name;
    private List<dmDependency> m_deps;          // dependencies I have to others
    private List<dmDependency> m_incomingDeps;  // dependencies others have to me
    private ArrayList<Method> m_methods;

    private ArrayList<String> m_texts;

    private int m_lineCount = 0;

    // we can not rely on dependencies to fields as these can be blacklisted
    // or of template type List<Object> etc.
    private int m_fieldCount = 0;
    void incFieldCount() {
        m_fieldCount++;
    }
    public int getFieldCount() {
        return m_fieldCount;
    }


    void incLineCount() {
        m_lineCount++;
    }
    public int getLineCount() {
        return m_lineCount;
    }

    public void addText(String a_text) {
        if (a_text.length() > 0) {
            m_texts.add(a_text);
        }
    }

    public Iterable<String> getTexts() {
        return m_texts;
    }

    Method addMethod(String a_name, boolean a_isAbstract, boolean a_isSynthetic) {
        Method ret = new Method(a_name, a_isAbstract, a_isSynthetic);
        m_methods.add(ret);
        m_texts.add(a_name);

        return ret;
    }
    public int getMethodCount() {return m_methods.size(); }

    public int getConcreteMethodCount() {
        final int [] c = new int[] {0};
        m_methods.forEach(m->{if (m.isConcrete()) {c[0]++;}});
        return c[0];
    }

    public Iterable<Method> getMethods() {
        return m_methods;
    }
    public ArrayList<Method> getMethods(String a_methodName) {
        ArrayList<Method> ret = new ArrayList<>();
        for (Method m : m_methods) {
            if (a_methodName.contentEquals(m.getName())) {
                ret.add(m);
            }
        }

        return ret;
    }


    public dmClass(String a_name, dmFile a_file) {
        m_name = a_name;
        m_deps = new ArrayList<>();
        m_incomingDeps = new ArrayList<>();
        m_methods = new ArrayList<>();
        m_texts = new ArrayList<>();
        m_file = a_file;
    }

    public Collection<dmDependency> getIncomingDependencies() {
        return Collections.unmodifiableCollection(m_incomingDeps);
    }

    public static String [] toJavaSourceFile(String a_logicalName) {
        int innerIndex = a_logicalName.lastIndexOf('$');
        return innerIndex < 0 ? a_logicalName.split("\\.") : a_logicalName.substring(0, innerIndex).split("\\.");
    }

    public String getFileName() {
        if (m_file != null) {
            return m_file.getFullName("/").toString() + ".java";
        } else {
            return "no file name set";
        }
        /*String fileName = m_name.replace('.', '/') + ".java";
        if (!isInner()) {
            return fileName;
        } else {
            return fileName.substring(0, m_name.indexOf('$')) + ".java";
        }*/
    }

    public String[] getFileNameParts() {
        return getFileName().split("/");
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

    public void addHorizontalFileDependency(dmClass a_target) {

        // horizontal dependencies do not have a direction so add to both classes as first class dependencies
        addDependency(a_target, dmDependency.Type.File_Horizontal, -1);
        a_target.addDependency(this, dmDependency.Type.File_Horizontal, -1);
    }

    public void addVerticalFileDependency(dmClass a_subDirClass) {

        // vertical point from higher level to lower level
        addDependency(a_subDirClass, dmDependency.Type.File_Vertical, -1);
    }

    public void addDependency(String a_className, dmDependency.Type a_type) {
        addDependency(a_className, a_type, -1);
    }

    public void addDependency(String a_className, dmDependency.Type a_type, int a_line) {
        if (a_type.isFileBased) {
            throw new IllegalArgumentException("dmDependency.Type a_type must not have isFileBased == true was: " + a_type);
        }
        for (dmDependency d : m_deps) {
            if (d.getTarget().getName().compareTo(a_className) == 0 && d.getType() == a_type) {
                d.addLine(a_line);
                return;
            }
        }

        dmClass target = new dmClass(a_className, m_file.getRoot().createFile(a_className.split("\\.")));
        dmDependency d = new dmDependency(this, target, a_type, a_line);
        target.m_incomingDeps.add(d);
        m_deps.add(d);
    }

    public dmDependency addDependency(dmClass a_target, dmDependency.Type a_type, int a_line) {
        for (dmDependency d : m_deps) {
            if (d.getTarget() == a_target && d.getType() == a_type) {
                d.addLine(a_line);
                return d;
            }
        }

        dmDependency d = new dmDependency(this, a_target, a_type, a_line);
        a_target.m_incomingDeps.add(d);
        m_deps.add(d);
        return d;
    }

    public void addDependency(dmClass a_target, dmDependency.Type a_type, int a_line, Method a_method) {
        dmDependency d = addDependency(a_target, a_type, a_line);
        a_method.addDependency(d, a_line);
    }

    public Iterable<dmDependency> getDependencies() {
        return m_deps;
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
