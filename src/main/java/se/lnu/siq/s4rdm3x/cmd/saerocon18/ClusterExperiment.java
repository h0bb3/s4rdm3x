package se.lnu.siq.s4rdm3x.cmd.saerocon18;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.*;
import se.lnu.siq.s4rdm3x.model.AttributeUtil;
import se.lnu.siq.s4rdm3x.model.Selector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class ClusterExperiment {


    public static interface RowHandler {
        public void handle(ArrayList<String> a_row);
    };


    protected void cleanse(Graph a_g) {
        AttributeUtil au = new AttributeUtil();
        for(Node n : a_g.getEachNode()) {
            au.clearAttributes(n);
        }
    }

    protected boolean writeRow(Path a_fp, Iterable<String> a_row) {
        String txtRow = "";
        for (String s : a_row) {
            txtRow += s + "\t";
        }
        txtRow += "\r\n";
        try {
            Files.write(a_fp, txtRow.getBytes(), StandardOpenOption.APPEND);
        } catch (Exception e) {
            System.out.println("Could not write row " + e.getMessage() + e.getStackTrace());
            return false;
        }

        return true;
    }

    protected boolean prepare(Graph a_g, double a_mappingPercent) {

        cleanse(a_g);


        String[] colors = {"rgb(144,32,86);", "rgb(229,169,107);", "rgb(153,204,153);", "rgb(255,255,102);", "rgb(255,116,140);", "rgb(88,252,193);"};
        String[][] packages = { {"net/sf/jabref/JabRefGUI", "net/sf/jabref/JabRefMain ", "net/sf/jabref/collab/Change", "net/sf/jabref/collab/ChangeDisplayDialog", "net/sf/jabref/collab/ChangeScanner", "net/sf/jabref/collab/EntryAddChange", "net/sf/jabref/collab/EntryChange", "net/sf/jabref/collab/EntryDeleteChange", "net/sf/jabref/collab/FileUpdatePanel", "net/sf/jabref/collab/GroupChange", "net/sf/jabref/collab/InfoPane", "net/sf/jabref/collab/MetaDataChange", "net/sf/jabref/collab/PreambleChange", "net/sf/jabref/collab/StringAddChange", "net/sf/jabref/collab/StringChange", "net/sf/jabref/collab/StringNameChange", "net/sf/jabref/collab/StringRemoveChange", "net/sf/jabref/gui", "net/sf/jabref/migrations/FileLinksUpgradeWarning", "net/sf/jabref/pdfimport/ImportDialog", "net/sf/jabref/pdfimport/PdfFileFilter", "net/sf/jabref/pdfimport/PdfImporter"},
                {"net/sf/jabref/JabRefException","net/sf/jabref/model","net/sf/jabref/shared/DBMSConnection.java","net/sf/jabref/shared/DBMSType","net/sf/jabref/shared/security/Password"},
                {"net/sf/jabref/JabRefExecutorService", "net/sf/jabref/collab/FileUpdateListener", "net/sf/jabref/collab/FileUpdateMonitor", "net/sf/jabref/logic", "net/sf/jabref/shared/DBMSProcessor", "net/sf/jabref/shared/DBMSSynchronizer", "net/sf/jabref/shared/MySQLProcessor", "net/sf/jabref/shared/OracleProcessor", "net/sf/jabref/shared/PostgreSQLProcessor", "net/sf/jabref/shared/event", "net/sf/jabref/shared/exception", "net/sf/jabref/shared/listener"},
                {"net/sf/jabref/migrations/PreferencesMigrations","net/sf/jabref/preferences","net/sf/jabref/shared/DBMSConnectionProperties","net/sf/jabref/shared/prefs"},
                {"net/sf/jabref/Globals"},
                {"net/sf/jabref/cli"}};

        for (int cIx = 0; cIx < Cluster1.g_originalMappingTags.length; cIx++) {
            mapPackages(a_g, packages[cIx], Cluster1.g_originalMappingTags[cIx], colors[cIx]);
            if (a_mappingPercent > 0) {
                AddNodeTagRandom c = new AddNodeTagRandom(Cluster1.g_clusterTags[cIx], new Selector.Tag(Cluster1.g_originalMappingTags[cIx]), a_mappingPercent);
                c.run(a_g);
            }
        }

        return true;
    }

    protected int getMappedNodeCount(Graph a_g) {
        AttributeUtil au = new AttributeUtil();
        int count = 0;
        for(Node n : a_g.getEachNode()) {
            if (au.hasAnyTag(n, Cluster1.g_clusterTags)) {
                count++;
            }
        }

        return count;
    }
    protected int getTotalNodeCount(Graph a_g) {
        AttributeUtil au = new AttributeUtil();
        int count = 0;
        for(Node n : a_g.getEachNode()) {
            if (au.hasAnyTag(n, Cluster1.g_originalMappingTags)) {
                count++;
            }
        }

        return count;
    }

    protected void mapPackages(Graph a_g, String[] a_packages, String a_clusterTag, String a_fillColor) {
        for(String pkg : a_packages) {
            AddNodeTag c = new AddNodeTag(a_clusterTag, new Selector.Pkg(pkg));
            c.run(a_g);
        }
        SetAttr c = new SetAttr("ui.style", "fill-color:" + a_fillColor, new Selector.Tag(a_clusterTag));
        c.run(a_g);
    }

    protected boolean loadJabRef(Graph a_g) {
        {
            LoadJar c = new LoadJar("data/jabref-3.7.jar", "net/sf/jabref/");
            try {
                c.run(a_g);
            } catch (IOException e) {
                System.out.println(e);
                return false;
            }
        }

        //hide NOT pkg:/jabref/
        {
            ShowNode c = new ShowNode(new Selector.Not(new Selector.Pkg("/jabref/")), false);
            c.run(a_g);
        }

        return false;
    }
}
