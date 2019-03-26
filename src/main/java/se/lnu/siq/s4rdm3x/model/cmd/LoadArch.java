package se.lnu.siq.s4rdm3x.model.cmd;

import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.util.ArchCreator;
import se.lnu.siq.s4rdm3x.model.cmd.util.SystemModelReader;
import se.lnu.siq.s4rdm3x.model.CGraph;

public class LoadArch {
    String m_file;
    public ArchDef m_arch;

    public LoadArch(String a_file) {
        m_file = a_file;
    }

    public void run(CGraph a_g) {
        SystemModelReader smr = new SystemModelReader();
        if (smr.readFile(m_file)) {
            ArchCreator ac = new ArchCreator();

            m_arch = ac.createArch(smr);
            ac.mapArch(m_arch, smr, a_g);

        }
    }
}
