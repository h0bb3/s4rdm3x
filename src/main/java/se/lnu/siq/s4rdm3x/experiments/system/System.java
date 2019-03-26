package se.lnu.siq.s4rdm3x.experiments.system;

import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.Selector;

import java.nio.file.Path;

public abstract class System {
    public abstract ArchDef createAndMapArch(CGraph a_g);
    public abstract boolean load(CGraph a_g);
    public abstract String getName();

    public Path getCustomMetricsFile() { return null; }

    protected ArchDef.Component createAddAndMapComponent(CGraph a_g, ArchDef a_ad, String a_componentName, String[] a_packages) {
        ArchDef.Component c = a_ad.addComponent(a_componentName);
        for(String p : a_packages) {
            c.mapToNodes(a_g, new Selector.Pkg(p));
        }
        return c;
    }
}
