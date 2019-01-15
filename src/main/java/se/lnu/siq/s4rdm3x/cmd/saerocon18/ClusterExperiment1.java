package se.lnu.siq.s4rdm3x.cmd.saerocon18;

import org.graphstream.graph.Graph;
import se.lnu.siq.s4rdm3x.model.CGraph;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ClusterExperiment1 extends ClusterExperiment {

    private final RowHandler m_handler;

    public ClusterExperiment1(RowHandler a_handler) {
        m_handler = a_handler;
    }

    public final String[] m_head = {"filter", "considered", "manual", "automatic", "incorrect"};
    public ArrayList<ArrayList<String>> m_rows = new ArrayList<>();
    public void run(CGraph a_g) {

        loadJabRef(a_g);

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy

        //double[] mappingPercents = {0.01, 0.05, 0.1, 0.2, 0.3, 0.4, 0.5};
        double[] mappingPercents = {0.5, 0.4, 0.3, 0.2, 0.1, 0.05, 0.01};

        for (double mappingPercent : mappingPercents) {
            double[] violationWeights = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
            for (double phi : violationWeights) {
                Path filePath = Paths.get("ce1_result_phi_" + phi + "_mp_" + mappingPercent + ".csv");
                if (Files.exists(filePath)) {
                    continue;
                }
                try {
                    //Files.deleteIfExists(filePath);
                    Files.write(filePath, "".getBytes(), StandardOpenOption.CREATE_NEW);
                } catch (Exception e) {
                    System.out.println("Could not Create file: " + filePath.toString());
                    System.out.println(e.getMessage());
                    System.out.println(e.getStackTrace());
                    return;
                }
                for (int i = 0; i < 1000; i++) {
                    prepare(a_g, mappingPercent);

                    double[] tresholds = {0.5, 0.6, 0.7, 0.8, 0.9};
                    int totalMapped = 0;
                    int totalUnmapped = 0;
                    int totalManuallyMapped = 0;
                    int totalAutoMapped = 0;
                    int totalAutoWrong = 0;
                    int iterations = 0;
                    int totalFailedMappings = 0;
                    long time = 0;

                    totalMapped = getMappedNodeCount(a_g);
                    totalUnmapped = getTotalNodeCount(a_g);

                    boolean risingFinished = false;
                    while (!risingFinished) {
                        for (double tresh : tresholds) {

                            Cluster1 c = new Cluster1(tresh, phi, true);
                            long start = System.nanoTime();
                            c.run(a_g);
                            time += System.nanoTime() - start;

                            totalManuallyMapped += c.m_manuallyMappedNodes;
                            totalAutoMapped += c.m_automaticallyMappedNodes;
                            totalAutoWrong += c.m_autoWrong;
                            totalFailedMappings += c.m_failedMappings;
                            iterations++;

                            if (c.m_manuallyMappedNodes == 0 && c.m_automaticallyMappedNodes == 0) {
                                if (tresh == tresholds[0]) {
                                    risingFinished = true;
                                }
                                break;
                            }
                        }
                    }

                    // go down use each step maximally
                    double[] downTresholds = {0.4, 0.3, 0.2, 0.1, 0.0};

                    for (double tresh : downTresholds) {
                        boolean done = false;
                        while (!done) {
                            Cluster1 c = new Cluster1(tresh, phi, true);
                            long start = System.nanoTime();
                            c.run(a_g);
                            time += System.nanoTime() - start;

                            totalManuallyMapped += c.m_manuallyMappedNodes;
                            totalAutoMapped += c.m_automaticallyMappedNodes;
                            totalAutoWrong += c.m_autoWrong;
                            totalFailedMappings += c.m_failedMappings;
                            iterations++;

                            if (c.m_manuallyMappedNodes == 0 && c.m_automaticallyMappedNodes == 0) {
                                done = true;
                            }
                        }
                    }


                    ArrayList<String> row = new ArrayList<>();
                    row.add(sdfDate.format(new Date()));
                    row.add("" + time);
                    row.add("" + i);
                    row.add("" + mappingPercent);
                    row.add("" + phi);
                    row.add("" + totalMapped);
                    row.add("" + totalUnmapped);
                    row.add("" + iterations);    // nothing for considered
                    row.add("" + totalManuallyMapped);
                    row.add("" + totalAutoMapped);
                    row.add("" + totalAutoWrong);
                    row.add("" + totalFailedMappings);

                    if (m_handler != null) {
                        m_handler.handle(row);
                    }

                    {
                        String txtRow = "";
                        for (String s : row) {
                            txtRow += s + "\t";
                        }
                        txtRow += "\r\n";
                        try {
                            Files.write(filePath, txtRow.getBytes(), StandardOpenOption.APPEND);
                        } catch (Exception e) {
                            System.out.println("Could not write row " + e.getMessage() + e.getStackTrace());
                        }
                    }
                    //m_rows.add(row);
                }
            }
        }
    }
}
