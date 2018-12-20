package se.lnu.siq.s4rdm3x.cmd.saerocon18;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.model.AttributeUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class ClusterExperiment3 extends ClusterExperiment {

    Random m_rand = new Random();

    private final RowHandler m_handler;

    public ClusterExperiment3(RowHandler a_handler) {
        m_handler = a_handler;
    }

    public void run(Graph a_g) {

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        loadJabRef(a_g);

        Path fp = Paths.get("ex3_0.csv");
        {
            int i = 1;
            while (Files.exists(fp)) {
                fp = Paths.get("ex3_" + i + ".csv");
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
            prepare(a_g, -1);
            double phi = m_rand.nextDouble();
            double omega = m_rand.nextDouble();

            int totalMapped = getMappedNodeCount(a_g);
            int totalUnmapped = getTotalNodeCount(a_g);
            int totalManuallyMapped = 0;
            int totalAutoMapped = 0;
            int totalAutoWrong = 0;
            int iterations = 0;
            int totalFailedMappings = 0;
            long time = 0;

            while(totalManuallyMapped + totalAutoMapped < totalUnmapped) {
                Cluster1 c = new Cluster1(omega, phi, false);
                long start = System.nanoTime();
                c.run(a_g);
                time += System.nanoTime() - start;

                totalAutoMapped += c.m_automaticallyMappedNodes;
                totalAutoWrong += c.m_autoWrong;
                if (c.m_automaticallyMappedNodes == 0) {
                    mapRandomNode(a_g);
                    totalManuallyMapped++;
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
            i++;

            writeRow(fp, row);

            if (m_handler != null) {
                m_handler.handle(row);
            }
        }
    }

    private void mapRandomNode(Graph a_g) {
        java.util.ArrayList<Node> unmapped = new ArrayList<>();
        AttributeUtil au = new AttributeUtil();

        for (Node n : a_g.getEachNode()) {
            if (au.hasAnyTag(n, Cluster1.g_clusterTags) != true) {
                unmapped.add(n);
            }
        }

        Node selected = unmapped.get(Math.abs(m_rand.nextInt()) % unmapped.size());
        for (int tIx = 0; tIx < Cluster1.g_originalMappingTags.length; tIx++) {
            if (au.hasAnyTag(selected, Cluster1.g_originalMappingTags[tIx])) {
                au.addTag(selected, Cluster1.g_clusterTags[tIx]);
                break;
            }
        }
    }

}
