package se.lnu.siq.s4rdm3x.cmd;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;
import se.lnu.siq.s4rdm3x.cmd.util.ArchCreator;
import se.lnu.siq.s4rdm3x.cmd.util.Selector;
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

            ac.mapArch(ac.createArch(smr), smr, a_g);
        }
    }
}
