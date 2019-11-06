package se.lnu.siq.s4rdm3x.experiments.system;

import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.Selector;

import java.nio.file.Path;
import java.util.ArrayList;

public abstract class System {
    public abstract ArchDef createAndMapArch(CGraph a_g) throws NoMappedNodesException;
    public void setInitialMapping(CGraph a_g, ArchDef a_arch) {};
    public abstract boolean load(CGraph a_g);
    public abstract String getName();

    public Path getCustomMetricsFile() { return null; }

    public static class NoMappedNodesException extends Exception {
        public NoMappedNodesException(ArchDef a_arch) {
            m_components = new ArrayList<>();
            m_arch = a_arch;
        }
        public ArrayList<ArchDef.Component> m_components;
        public ArchDef m_arch;
    }

    protected ArchDef.Component createAddAndMapComponent(CGraph a_g, ArchDef a_ad, String a_componentName, String[] a_packages) throws NoMappedNodesException {
        ArchDef.Component c = a_ad.addComponent(a_componentName);
        int mapped = 0;
        for(String p : a_packages) {
            mapped += c.mapToNodes(a_g, new Selector.Pkg(p));
        }

        if (mapped == 0) {
            NoMappedNodesException ex = new NoMappedNodesException(a_ad);
            ex.m_components.add(c);
        }


        return c;
    }
}
