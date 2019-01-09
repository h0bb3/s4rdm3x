package se.lnu.siq.s4rdm3x.experiments.system;

import org.graphstream.graph.Graph;
import se.lnu.siq.s4rdm3x.cmd.hugme.HuGMe;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.Selector;

import java.nio.file.Path;

public abstract class System {
    public abstract HuGMe.ArchDef createAndMapArch(CGraph a_g);
    public abstract boolean load(CGraph a_g);
    public abstract String getName();

    public Path getCustomMetricsFile() { return null; }

    protected HuGMe.ArchDef.Component createAddAndMapComponent(CGraph a_g, HuGMe.ArchDef a_ad, String a_componentName, String[] a_packages) {
        HuGMe.ArchDef.Component c = a_ad.addComponent(a_componentName);
        for(String p : a_packages) {
            c.mapToNodes(a_g, new Selector.Pkg(p));
        }
        return c;
    }
}
