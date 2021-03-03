package se.lnu.siq.s4rdm3x.model.cmd;

import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.util.ArchCreator;
import se.lnu.siq.s4rdm3x.model.cmd.util.SystemModelReader;
import se.lnu.siq.s4rdm3x.model.CGraph;

import java.io.IOException;

public class SaveArch {
    private String m_file;
    private ArchDef m_arch;


    public SaveArch(ArchDef a_arch, String a_file) {
        m_file = a_file;
        m_arch = a_arch;
    }

    public void run(CGraph a_g) throws IOException {
        ArchCreator ac = new ArchCreator();
        SystemModelReader sr = ac.createSystemModel(m_arch, a_g.getNodes(), "generated");
        sr.writeFile(m_file);
    }

}
