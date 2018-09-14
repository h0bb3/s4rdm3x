package se.lnu.siq.s4rdm3x.experiments;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;
import se.lnu.siq.s4rdm3x.cmd.util.AttributeUtil;

import java.io.File;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.write;

public class RunFileSaver implements ExperimentRunner.RunListener {

    Path m_filePath;
    Path m_mappingsFilePath;
    RunData m_rd;
    int m_errorCounter;
    int m_runCount;

    public static class Mapping {
        public int m_runId;
        public int m_runFile;
        public String m_node;
        public String m_mapping;
        public String m_clustering;
        public String m_clusteringType;
    }

    public static class RunData {
        public String m_date;
        public int m_initialClustered;
        public int m_totalMapped;
        public String m_initialDistribution;

    }

    public int getRunCount() {
        return m_runCount;
    }

    public RunFileSaver(String a_system, String a_metric, boolean a_doSaveMappings) {
        m_errorCounter = 0;
        m_rd = null;
        m_runCount = 0;
        m_filePath = createFile(a_system, a_metric, a_system + "_" + a_metric);


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
        row.add("system");

        writeRow(m_filePath, row);

        if (a_doSaveMappings) {
            m_mappingsFilePath = createFile(a_system, a_metric + File.separator + "mappings", a_system + "_" + a_metric + "_mappings");
            row = new ArrayList<>();
            row.add("runId");
            row.add("nodeId");
            row.add("mapping" );
            row.add("clustering");
            row.add("clusteringType");
            row.add("metric");
            row.add("metricValue");

            writeRow(m_mappingsFilePath, row);
        } else {
            m_mappingsFilePath = null;
        }
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
        row.add(a_rd.m_metric.getName());
        row.add(a_rd.m_system);

        writeRow(m_filePath, row);

        if (m_mappingsFilePath != null) {
            AttributeUtil au = new AttributeUtil();
            for(Node n: a_g.getEachNode()) {
                HuGMe.ArchDef.Component mapped;

                mapped = a_arch.getMappedComponent(n);

                if (mapped != null)  {
                    HuGMe.ArchDef.Component clustered;
                    clustered = a_arch.getClusteredComponent(n);

                    row = new ArrayList<>();
                    row.add("" + a_rd.m_id);
                    row.add(m_filePath.toString());
                    row.add(au.getName(n));
                    row.add(mapped.getName());
                    if (clustered !=  null) {
                        row.add(clustered.getName());
                    } else {
                        row.add("n/a");
                    }
                    row.add(mapped.getClusteringType(n).toString());
                    row.add(a_rd.m_metric.getName());
                    row.add("" + a_rd.m_metric.getMetric(n));

                    writeRow(m_mappingsFilePath, row);
                }
            }
        }

        m_runCount++;
    }

    private void writeRow(Path a_filePath, String a_row) {
        try {
            write(a_filePath, a_row.getBytes(), StandardOpenOption.APPEND);
            m_errorCounter = 0;
        } catch (Exception e) {
            if (m_errorCounter > 100) {
                System.out.println("Could not write row " + e.getMessage() + e.getStackTrace());
                System.out.println("Exiting!");
                System.exit(-1);
            } else {
                m_errorCounter++;
                Random r = new Random();
                try{Thread.sleep((long)(r.nextDouble() * 1717));} catch (Exception e2) {};
                writeRow(a_filePath, a_row);
            }
        }
    }

    private void writeRow(Path a_filePath, Iterable<String> m_strings) {

        String txtRow = "";
        for (String s : m_strings) {
            txtRow += s + "\t";
        }

        txtRow = txtRow.substring(0, txtRow.length() - 1);
        txtRow += "\r\n";
        writeRow(a_filePath, txtRow);

    }

    private void handleError(String a_errorText, Exception a_e) {
        if (m_errorCounter > 100) {
            System.out.println(a_errorText);
            System.out.println(a_e.getMessage());
            System.out.println(a_e.getStackTrace());
            System.out.println("Exiting!");
            System.exit(-1);
        } else {
            m_errorCounter++;
        }
    }

    private void createDir(Path a_p) {
        if (a_p != null) {
            try {
                Files.createDirectories(a_p);
                m_errorCounter = 0;
            } catch (FileAlreadyExistsException a_faee) {
                // this is fine
            } catch (Exception a_e) {
                handleError("Could not create Dir: " + a_p.toString(), a_e);
                createDir(a_p);
            }
        }
    }

    private Path createFile(String a_dir1, String a_dir2, String a_fileName) {
        String fileName = a_dir1 + File.separator + a_dir2 + File.separator + a_fileName;
        java.nio.file.Path fp = Paths.get(fileName + "_0.csv");

        createDir(fp.getParent());

        int i = 1;
        while (exists(fp)) {
            fp = Paths.get(fileName + "_" + i + ".csv");
            i++;
        }

        try {
            Files.createFile(fp);
            m_errorCounter = 0;
        } catch (Exception e) {
            if (m_errorCounter > 100) {
                System.out.println("Could not Create file: " + fp.toString());
                System.out.println(e.getMessage());
                System.out.println(e.getStackTrace());
                System.out.println("Exiting!");
                System.exit(-1);
                return null;
            } else {
                m_errorCounter++;
                Random r = new Random();
                try {
                    Thread.sleep((long) (r.nextDouble() * 1717));
                } catch (Exception e2) {
                }
                ;
                return createFile(a_dir1, a_dir2, a_fileName);
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
