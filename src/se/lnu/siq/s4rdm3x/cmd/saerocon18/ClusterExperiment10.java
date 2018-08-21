package se.lnu.siq.s4rdm3x.cmd.saerocon18;

import org.graphstream.graph.Graph;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class ClusterExperiment10 extends ClusterExperiment {

    private final RowHandler m_handler;
    Random m_rand = new Random();

    public ClusterExperiment10(RowHandler a_handler) {
        m_handler = a_handler;
    }

    public ArrayList<ArrayList<String>> m_rows = new ArrayList<>();

    public void run(Graph a_g) {

        loadJabRef(a_g);


        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        loadJabRef(a_g);

        String fileName = "ex10_";


        Path fp = Paths.get(fileName + "0.csv");
        {
            int i = 1;
            while (Files.exists(fp)) {
                fp = Paths.get(fileName + i + ".csv");
                i++;
            }
            try {
                //Files.deleteIfExists(filePath);
                Files.write(fp, "".getBytes(), StandardOpenOption.CREATE_NEW);
            } catch (Exception e) {
                System.out.println("Could not Create file: " + fp.toString());
                System.out.println(e.getMessage());
                System.out.println(e.getStackTrace());
                return;
            }
        }

        int i = 0;
        while(true) {

            double mappingPercent = m_rand.nextDouble();
            double phi = m_rand.nextDouble();
            double omega = m_rand.nextDouble();

            prepare(a_g, mappingPercent);

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

            while(true) {
                Cluster1 c = new Cluster1(omega, phi, true);
                long start = System.nanoTime();
                c.run(a_g);
                time += System.nanoTime() - start;

                totalManuallyMapped += c.m_manuallyMappedNodes;
                totalAutoMapped += c.m_automaticallyMappedNodes;
                totalAutoWrong += c.m_autoWrong;
                totalFailedMappings += c.m_failedMappings;

                if (c.m_automaticallyMappedNodes + c.m_manuallyMappedNodes == 0) {
                    break;
                }

                iterations++;
            }


            ArrayList<String> row = new ArrayList<>();
            row.add(sdfDate.format(new Date()));
            row.add("" + time);
            row.add("" + i);
            row.add("" + omega);
            row.add("" + phi);
            row.add("" + totalMapped);
            row.add("" + totalUnmapped);
            row.add("" + iterations);    // nothing for considered
            row.add("" + totalManuallyMapped);
            row.add("" + totalAutoMapped);
            row.add("" + totalAutoWrong);
            row.add("" + totalFailedMappings);
            row.add("" + mappingPercent);

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
                    Files.write(fp, txtRow.getBytes(), StandardOpenOption.APPEND);
                } catch (Exception e) {
                    System.out.println("Could not write row " + e.getMessage() + e.getStackTrace());
                }
            }

            i++;
        }
    }
}
