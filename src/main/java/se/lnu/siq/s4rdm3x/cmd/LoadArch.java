package se.lnu.siq.s4rdm3x.cmd;

import org.graphstream.graph.Graph;
import se.lnu.siq.s4rdm3x.cmd.hugme.HuGMe;
import se.lnu.siq.s4rdm3x.cmd.util.ArchCreator;
import se.lnu.siq.s4rdm3x.cmd.util.SystemModelReader;

public class LoadArch {
    String m_file;
    public HuGMe.ArchDef m_arch;

    public LoadArch(String a_file) {
        m_file = a_file;
    }

    public void run(Graph a_g) {
        SystemModelReader smr = new SystemModelReader();
        if (smr.readFile(m_file)) {
            ArchCreator ac = new ArchCreator();

            m_arch = ac.createArch(smr);
            ac.mapArch(m_arch, smr, a_g);

        }
    }
}
