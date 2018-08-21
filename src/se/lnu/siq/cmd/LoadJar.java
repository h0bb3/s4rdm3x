package se.lnu.siq.asm_gs_test.cmd;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.dmodel.JarProjectLoader;
import se.lnu.siq.dmodel.dmClass;
import se.lnu.siq.dmodel.dmProject;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by tohto on 2017-08-23.
 */
public class LoadJar {

    private String m_file;
    private String m_rootPackage;
    private dmProject m_project;



    public LoadJar(String a_file, String a_rootPackage) {
        m_file = a_file;
        m_rootPackage = a_rootPackage;
        m_project = null;

    }

    public void run(Graph a_g) throws IOException {

        JarProjectLoader b = new JarProjectLoader();
        m_project = b.buildProjectFromJAR(m_file, m_rootPackage);
        AttributeUtil au = new AttributeUtil();
        NodeUtil nu = new NodeUtil(a_g);


        for (dmClass c : m_project.getClasses()) {
            String cName = c.getFileName();
            Node n = nu.createNode(cName);
            au.addClass(n, c);
        }

    }

    public dmProject getProject() {
        return m_project;
    }
}
