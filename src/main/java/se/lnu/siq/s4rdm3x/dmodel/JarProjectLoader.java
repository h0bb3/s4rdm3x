package se.lnu.siq.s4rdm3x.dmodel;

import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by tohto on 2017-08-16.
 */
public class JarProjectLoader {

    public dmProject buildProjectFromJAR(String a_jarFileName, String[] a_rootPackages, dmProject a_project) throws IOException {
        JarFile jarFile = new JarFile(a_jarFileName);
        ASMdmProjectBuilder builder = new ASMdmProjectBuilder(a_project);

        //URL[] urls = { new URL("jar:file:" + a_jarFileName +"!/") };
        //URLClassLoader cl = URLClassLoader.newInstance(urls);

        Enumeration<JarEntry> entries = jarFile.entries();

        dmProject p = builder.getProject();

        p.addPackageToBlackList("java.lang");
        p.addPackageToBlackList("java.util");
        p.addClassToBlackList("int");
        p.addClassToBlackList("float");
        p.addClassToBlackList("boolean");
        p.addClassToBlackList("void");
        p.addClassToBlackList("double");

        p.doTrackConstantDeps(false);

        builder.dontPrint();

        while(entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();

            if (entry.getName().endsWith(".class")) {
                if (startsWithAny(entry.getName(), a_rootPackages)) {
                    //System.out.println(entry.getName());
                    InputStream in = jarFile.getInputStream(entry);
                    ClassReader classReader = new ClassReader(in);
                    classReader.accept(builder, 0);
                }
            }
        }

        return builder.getProject();
    }

    private boolean startsWithAny(String a_fullString, String[] a_starts    ) {
        if (a_starts.length == 0) {
            return true;
        }
        a_fullString = a_fullString.replace("\\", ".").replace("/", ".");
        for(String start : a_starts) {
            if (a_fullString.startsWith(start)) {
                return true;
            }
        }
        return false;
    }

    public dmProject buildProjectFromJAR(String a_jarFileName, String a_rootPackage) throws IOException {
        String[] roots = {a_rootPackage};
        return buildProjectFromJAR(a_jarFileName, roots);
    }

    public dmProject buildProjectFromJAR(String m_file, String[] m_rootPackages) throws IOException {
        return buildProjectFromJAR(m_file, m_rootPackages, new dmProject());
    }
}
