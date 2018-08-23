package se.lnu.siq.s4rdm3x.experiments;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;
import se.lnu.siq.s4rdm3x.cmd.LoadJar;
import se.lnu.siq.s4rdm3x.cmd.Selector;

import java.io.IOException;

public class JabRefRand extends ExperimentRunner {


    public JabRefRand() {
    }

    @Override
    protected String getMetricName() {
        return "rand";
    }


    @Override
    protected void assignMetric(Graph a_g, HuGMe.ArchDef a_arch) {

        for(Node n : a_g.getEachNode()) {
            if (a_arch.getMappedComponent(n) != null) {
                setMetric(n, m_rand.nextDouble());
            }
        }
    }

    private void mapPackage(Graph a_g, HuGMe.ArchDef.Component a_c, String[] a_packages) {
        for(String pkg : a_packages) {
            a_c.mapToNodes(a_g, new Selector.Pkg(pkg));
        }
    }

    protected HuGMe.ArchDef createAndMapArch(Graph a_g) {

        String[][] packages = { {"net/sf/jabref/JabRefGUI", "net/sf/jabref/JabRefMain ", "net/sf/jabref/collab/Change", "net/sf/jabref/collab/ChangeDisplayDialog", "net/sf/jabref/collab/ChangeScanner", "net/sf/jabref/collab/EntryAddChange", "net/sf/jabref/collab/EntryChange", "net/sf/jabref/collab/EntryDeleteChange", "net/sf/jabref/collab/FileUpdatePanel", "net/sf/jabref/collab/GroupChange", "net/sf/jabref/collab/InfoPane", "net/sf/jabref/collab/MetaDataChange", "net/sf/jabref/collab/PreambleChange", "net/sf/jabref/collab/StringAddChange", "net/sf/jabref/collab/StringChange", "net/sf/jabref/collab/StringNameChange", "net/sf/jabref/collab/StringRemoveChange", "net/sf/jabref/gui", "net/sf/jabref/migrations/FileLinksUpgradeWarning", "net/sf/jabref/pdfimport/ImportDialog", "net/sf/jabref/pdfimport/PdfFileFilter", "net/sf/jabref/pdfimport/PdfImporter"},
                {"net/sf/jabref/JabRefException","net/sf/jabref/model","net/sf/jabref/shared/DBMSConnection.java","net/sf/jabref/shared/DBMSType","net/sf/jabref/shared/security/Password"},
                {"net/sf/jabref/JabRefExecutorService", "net/sf/jabref/collab/FileUpdateListener", "net/sf/jabref/collab/FileUpdateMonitor", "net/sf/jabref/logic", "net/sf/jabref/shared/DBMSProcessor", "net/sf/jabref/shared/DBMSSynchronizer", "net/sf/jabref/shared/MySQLProcessor", "net/sf/jabref/shared/OracleProcessor", "net/sf/jabref/shared/PostgreSQLProcessor", "net/sf/jabref/shared/event", "net/sf/jabref/shared/exception", "net/sf/jabref/shared/listener"},
                {"net/sf/jabref/migrations/PreferencesMigrations","net/sf/jabref/preferences","net/sf/jabref/shared/DBMSConnectionProperties","net/sf/jabref/shared/prefs"},
                {"net/sf/jabref/Globals"},
                {"net/sf/jabref/cli"}};

        HuGMe.ArchDef arch = new HuGMe.ArchDef();
        HuGMe.ArchDef.Component gui = createAddAndMapComponent(a_g, arch, "gui", packages[0]);
        HuGMe.ArchDef.Component model = createAddAndMapComponent(a_g, arch, "model", packages[1]);
        HuGMe.ArchDef.Component logic = createAddAndMapComponent(a_g, arch, "logic", packages[2]);
        HuGMe.ArchDef.Component pref = createAddAndMapComponent(a_g, arch, "pref", packages[3]);
        HuGMe.ArchDef.Component global = createAddAndMapComponent(a_g, arch, "global", packages[4]);
        HuGMe.ArchDef.Component cli = createAddAndMapComponent(a_g, arch, "cli", packages[5]);

        gui.addDependencyTo(model);gui.addDependencyTo(logic);
        cli.addDependencyTo(gui);cli.addDependencyTo(model);cli.addDependencyTo(logic);cli.addDependencyTo(pref);cli.addDependencyTo(global);
        logic.addDependencyTo(model);
        pref.addDependencyTo(model);pref.addDependencyTo(logic);
        global.addDependencyTo(gui);global.addDependencyTo(model);global.addDependencyTo(logic);global.addDependencyTo(pref);global.addDependencyTo(cli);

        return arch;
    }

    protected boolean load(Graph a_g) {
        LoadJar c = new LoadJar("data/jabref-3.7.jar", "net/sf/jabref/");
        try {
            c.run(a_g);
        } catch (IOException e) {
            System.out.println(e);
            return false;
        }
        return true;
    }
}
