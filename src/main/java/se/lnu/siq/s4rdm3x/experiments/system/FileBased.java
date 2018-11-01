package se.lnu.siq.s4rdm3x.experiments.system;

import org.graphstream.graph.Graph;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;
import se.lnu.siq.s4rdm3x.cmd.LoadJar;
import se.lnu.siq.s4rdm3x.cmd.util.ArchCreator;
import se.lnu.siq.s4rdm3x.cmd.util.SystemModelReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileBased extends System {

    String m_file;
    SystemModelReader m_smr;

    public FileBased(String a_file) throws IOException {
        m_file = a_file;
        m_smr = new SystemModelReader();
        if (!m_smr.readFile(m_file)) {
            throw new IOException("Could not read System File: " + m_file);
        }
    }

    public HuGMe.ArchDef createAndMapArch(Graph a_g) {
        ArchCreator ac = new ArchCreator();
        HuGMe.ArchDef ret = ac.createArch(m_smr);

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

    public boolean load(Graph a_g) {

        String [] roots = new String[m_smr.m_roots.size()];
        m_smr.m_roots.toArray(roots);
        for (String jar : m_smr.m_jars) {
            Path p = Paths.get(Paths.get(m_file).getParent().toString(), jar);
            LoadJar c = new LoadJar(p.toString(), roots);
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
