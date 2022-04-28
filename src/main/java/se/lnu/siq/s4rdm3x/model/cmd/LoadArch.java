package se.lnu.siq.s4rdm3x.model.cmd;

import se.lnu.siq.s4rdm3x.experiments.system.FileBased;
import se.lnu.siq.s4rdm3x.experiments.system.System;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.util.ArchCreator;
import se.lnu.siq.s4rdm3x.model.cmd.util.SystemModelReader;
import se.lnu.siq.s4rdm3x.model.CGraph;

import java.io.IOException;

public class LoadArch {
    String m_file;
    public ArchDef m_arch;
    public System.NoMappedNodesException m_unmappedNodesException;

    public LoadArch(String a_file) {
        m_file = a_file;
    }

    public void run(CGraph a_g) throws IOException {

        FileBased sys = new FileBased(m_file);
        if (sys.load(a_g)) {
            try {
                m_arch = sys.createAndMapArch(a_g);
            } catch (System.NoMappedNodesException e) {
                m_arch = e.m_arch;
                m_unmappedNodesException = e;
            }
        }
    }
}
