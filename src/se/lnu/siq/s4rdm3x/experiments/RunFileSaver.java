package se.lnu.siq.s4rdm3x.experiments;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.write;

public class RunFileSaver implements ExperimentRunner.RunListener {

    Path m_filePath;
    RunData m_rd;

    public static class RunData {
        public String m_date;
        public int m_initialClustered;
        public int m_totalMapped;
        public String m_initialDistribution;
    }

    public RunFileSaver(String a_baseFileName) {

        m_rd = null;
        m_filePath = getFilePath(a_baseFileName);

        ArrayList<String> row = new ArrayList<>();
        row.add("date");
        row.add("time");
        row.add("localId");
        row.add("omega" );
        row.add("phi");
        row.add("initialClustered");
        row.add("totalMapped");
        row.add("initialDistribution");
        row.add("iterations");
        row.add("totalManuallyClustered");
        row.add("totalAutoClustered");
        row.add("totalAutoWrong");
        row.add("totalFailedClusterings");
        row.add("mappingPercent");
        row.add("metric");

        writeRow(m_filePath, row);
    }


    public ExperimentRunner.BasicRunData OnRunInit(ExperimentRunner.BasicRunData a_rd, Graph a_g, HuGMe.ArchDef a_arch) {
        m_rd = new RunData();

        m_rd.m_initialClustered = a_arch.getClusteredNodeCount(a_g.getNodeSet());
        m_rd.m_totalMapped= a_arch.getMappedNodeCount(a_g.getNodeSet());

        m_rd.m_initialDistribution = getInitialClusterDistributionString(a_g, a_arch);

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        m_rd.m_date = sdfDate.format(new Date());

        return a_rd;
    }

    public void OnRunCompleted(ExperimentRunner.BasicRunData a_rd, Graph a_g, HuGMe.ArchDef a_arch) {
        ArrayList<String> row = new ArrayList<>();
        row.add(m_rd.m_date);
        row.add("" + a_rd.m_time);
        row.add("" + a_rd.m_id);
        row.add("" + a_rd.m_omega);
        row.add("" + a_rd.m_phi);
        row.add("" + m_rd.m_initialClustered);
        row.add("" + m_rd.m_totalMapped);
        row.add(m_rd.m_initialDistribution);
        row.add("" + a_rd.m_iterations);
        row.add("" + a_rd.m_totalManuallyClustered);
        row.add("" + a_rd.m_totalAutoClustered);
        row.add("" + a_rd.m_totalAutoWrong);
        row.add("" + a_rd.m_totalFailedClusterings);
        row.add("" + a_rd.m_initialClusteringPercent);
        row.add(a_rd.m_metric);

        writeRow(m_filePath, row);
    }

    private void writeRow(Path a_filePath, Iterable<String> m_strings) {

        String txtRow = "";
        for (String s : m_strings) {
            txtRow += s + "\t";
        }

        txtRow = txtRow.substring(0, txtRow.length() - 1);

        txtRow += "\r\n";
        try {
            write(a_filePath, txtRow.getBytes(), StandardOpenOption.APPEND);
        } catch (Exception e) {
            System.out.println("Could not write row " + e.getMessage() + e.getStackTrace());
        }
    }

    private Path getFilePath(String a_fileName) {
        java.nio.file.Path fp = Paths.get(a_fileName + "_0.csv");
        {
            int i = 1;
            while (exists(fp)) {
                fp = Paths.get(a_fileName + "_" + i + ".csv");
                i++;
            }
            try {
                //Files.deleteIfExists(filePath);
                write(fp, "".getBytes(), StandardOpenOption.CREATE_NEW);
            } catch (Exception e) {
                System.out.println("Could not Create file: " + fp.toString());
                System.out.println(e.getMessage());
                System.out.println(e.getStackTrace());
                return null;
            }
        }

        return fp;
    }

    private String getInitialClusterDistributionString(Graph a_g, HuGMe.ArchDef a_arch) {
        String ret="";

        for (HuGMe.ArchDef.Component c : a_arch.getComponents()) {
            int count = 0;
            for (Node a_n : a_g.getEachNode()) {
                if (c == a_arch.getClusteredComponent(a_n)) {
                    count++;
                }
            }
            ret += "," + c.getName() + ":" + count;
        }

        ret = ret.substring(1); // removes first ","
        ret = "[" + ret + "]";

        return ret;
    }
}
