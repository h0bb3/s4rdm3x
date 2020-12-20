package se.lnu.siq.s4rdm3x.experiments.system;

import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.LoadJar;
import se.lnu.siq.s4rdm3x.model.cmd.util.ArchCreator;
import se.lnu.siq.s4rdm3x.model.cmd.util.SystemModelReader;
import se.lnu.siq.s4rdm3x.model.CGraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileBased extends System {

    String m_file;
    SystemModelReader m_smr;

    public String getFile() {
        return m_file;
    }

    public FileBased(SystemModelReader a_smr) {
        m_file = "";
        m_smr = a_smr;
    }

    public FileBased(String a_file) throws IOException {
        m_file = a_file;
        m_smr = new SystemModelReader();
        if (!m_smr.readFile(m_file)) {
            throw new IOException("Could not read System File: " + m_file);
        }
    }

    @Override
    public void setInitialMapping(CGraph a_g, ArchDef a_arch) {
        ArchCreator ac = new ArchCreator();
        ac.setInitialMapping(a_arch, m_smr, a_g);
    }

    @Override
    public int getInitialMappingCount(CGraph a_g, ArchDef a_arch) {
        ArchCreator ac = new ArchCreator();
        ac.countNodesToBeMapped(a_arch, m_smr.m_initialMappings, a_g);
        return m_smr.m_initialMappings.size();
    }

    public ArchDef createAndMapArch(CGraph a_g) throws System.NoMappedNodesException {
        ArchCreator ac = new ArchCreator();
        ArchDef ret = ac.createArch(m_smr);

        ac.mapArch(ret, m_smr, a_g);
        return ret;
    }

    public Path getCustomMetricsFile() {
        Path p = Paths.get(Paths.get(m_file).getParent().toString(), m_smr.getMetricsFile());
        if (Files.exists(p)) {
            return p;
        }

        return null;
    }

    @Override
    public boolean load(CGraph a_g) {

        String [] roots = new String[m_smr.m_roots.size()];
        m_smr.m_roots.toArray(roots);

        LoadJar c = new LoadJar("", roots);
        for (String jar : m_smr.m_jars) {
            Path p;
            if (m_file.length() > 0) {
                p = Paths.get(Paths.get(m_file).getParent().toString(), jar);
            } else {
                p = Paths.get(jar);
            }
            c.setFile(p.toString());
            try {
                c.run(a_g);
            } catch (IOException e) {
                java.lang.System.out.println(e);
                return false;
            }
        }


        return true;
    }

    public String getName() {
        return m_smr.m_name;
    }
}
