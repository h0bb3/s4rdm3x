package se.lnu.siq.s4rdm3x.model.cmd;

import se.lnu.siq.s4rdm3x.dmodel.JarProjectLoader;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmProject;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.io.IOException;

/**
 * Created by tohto on 2017-08-23.
 */
public class LoadJar {

    private String m_file;
    private String []m_rootPackages;
    private dmProject m_project;


    public LoadJar(String a_file, String[] a_rootPackages) {
        m_file = a_file;
        m_rootPackages = new String[a_rootPackages.length];
        for (int i = 0; i < m_rootPackages.length; i++) {
            m_rootPackages[i] = a_rootPackages[i].replace('/', '.').replace('\\', '.');
        }
        m_project = null;
    }

    public LoadJar(String a_file, String a_rootPackage) {
        m_file = a_file;
        m_rootPackages = a_rootPackage.split(",");
        m_project = null;
    }

    public void run(CGraph a_g) throws IOException {

        JarProjectLoader b = new JarProjectLoader();
        if (m_project == null) {
            m_project = b.buildProjectFromJAR(m_file, m_rootPackages);
        } else {
            m_project = b.buildProjectFromJAR(m_file, m_rootPackages, m_project);
        }

        for (dmClass c : m_project.getClasses()) {
            String cName = c.getFileName();
            CNode n = a_g.createNode(cName);
            n.addClass(c);
        }

    }

    public dmProject getProject() {
        return m_project;
    }

    public void setFile(String a_file) {
        m_file = a_file;
    }
}
