package se.lnu.siq.s4rdm3x.experiments.system;

/*
public class JabRef_3_7 extends System {

    @Override
    public String getName() {
        return "JabRef_3_7";
    }

    public HuGMe.ArchDef createAndMapArch(Graph a_g)  {

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

    public boolean load(Graph a_g) {
        LoadJar c = new LoadJar("data/JabRef-3.7.jar", "net/sf/jabref/");
        try {
            c.run(a_g);
        } catch (IOException e) {
            java.lang.System.out.println(e);
            return false;
        }
        return true;
    }

}
*/