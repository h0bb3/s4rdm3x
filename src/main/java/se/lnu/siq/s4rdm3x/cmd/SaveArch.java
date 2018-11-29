package se.lnu.siq.s4rdm3x.cmd;

import org.graphstream.graph.Graph;
import se.lnu.siq.s4rdm3x.cmd.hugme.HuGMe;
import se.lnu.siq.s4rdm3x.cmd.util.ArchCreator;
import se.lnu.siq.s4rdm3x.cmd.util.SystemModelReader;

import java.io.IOException;

public class SaveArch {
    private String m_file;
    private HuGMe.ArchDef m_arch;


    public SaveArch(HuGMe.ArchDef a_arch, String a_file) {
        m_file = a_file;
        m_arch = a_arch;
    }

    public void run(Graph a_g) throws IOException {
        ArchCreator ac = new ArchCreator();
        SystemModelReader sr = ac.createSystemModel(m_arch, a_g.getNodeSet());
        sr.writeFile(m_file);
    }

}
