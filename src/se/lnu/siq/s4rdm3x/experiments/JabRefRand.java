package se.lnu.siq.s4rdm3x.experiments;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;
import se.lnu.siq.s4rdm3x.cmd.LoadJar;
import se.lnu.siq.s4rdm3x.cmd.Selector;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.write;

public class JabRefRand {

    private final String m_metricTag = "metric";
    Random m_rand = new Random();
    String m_fileName = "JabRef1";

    public void run(Graph a_g) {

        Path fp = getFilePath(m_fileName);

        int i = 0;
        while(true) {

            double mappingPercent = m_rand.nextDouble();
            double phi = m_rand.nextDouble();
            double omega = m_rand.nextDouble();

            load(a_g);
            HuGMe.ArchDef arch = createAndMapArch(a_g);
            assignMetric(a_g, arch);
            assignInitialClusters(a_g, arch,mappingPercent);

            int totalMapped = 0;
            int totalUnmapped = 0;
            int totalManuallyMapped = 0;
            int totalAutoMapped = 0;
            int totalAutoWrong = 0;
            int iterations = 0;
            int totalFailedMappings = 0;
            long time = 0;

            totalMapped = arch.getClusteredNodeCount(a_g.getNodeSet());
            totalUnmapped = arch.getMappedNodeCount(a_g.getNodeSet());

            while(true) {
                HuGMe c = new HuGMe(omega, phi, true, arch);
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

            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
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

            writeRow(fp, row);

            i++;
        }
    }

    private void writeRow(Path a_filePath, Iterable<String> m_strings) {

        String txtRow = "";
        for (String s : m_strings) {
            txtRow += s + "\t";
        }
        txtRow += "\r\n";
        try {
            write(a_filePath, txtRow.getBytes(), StandardOpenOption.APPEND);
        } catch (Exception e) {
            System.out.println("Could not write row " + e.getMessage() + e.getStackTrace());
        }
    }

    private Path getFilePath(String a_fileName) {
        java.nio.file.Path fp = Paths.get(a_fileName + "0.csv");
        {
            int i = 1;
            while (exists(fp)) {
                fp = Paths.get(a_fileName + i + ".csv");
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

    private void assignMetric(Graph a_g, HuGMe.ArchDef a_arch) {

        for(Node n : a_g.getEachNode()) {
            if (a_arch.getMappedComponent(n) != null) {
                int rand = (int)(Math.random() * 171717) % 100;
                n.setAttribute(m_metricTag, (double)rand);
            }
        }
    }

    private void assignInitialClusters(Graph a_g, HuGMe.ArchDef a_arch, double a_percentage) {
        Random rand = new Random();
        for (HuGMe.ArchDef.Component component : a_arch.getComponents()) {

            ArrayList<Node> sortedNodes = new ArrayList<>();
            for (Node n : a_g.getEachNode()) {
                if (component.isMappedTo(n)) {
                    sortedNodes.add(n);
                }
            }

            // this sorts to lowest first
            sortedNodes.sort(Comparator.comparingDouble(a_n -> {
                return a_n.getAttribute(m_metricTag);
            }));

            int nodeCount = (int) ((double) sortedNodes.size() * a_percentage);
            if (nodeCount <= 0) {
                nodeCount = 1;
            }

            ArrayList<Node> workingSet = getWorkingSet(sortedNodes, nodeCount);

            // we may have added too many nodes (i.e. the last batch may be bigger)
            while (workingSet.size() > nodeCount) {
                int firstBatchSize = getFirstBatchSize(workingSet);
                workingSet.remove(Math.abs(rand.nextInt()) % firstBatchSize);
            }

            for (Node n : workingSet) {
                component.clusterToNode(n);
            }
        }
    }

    private int getFirstBatchSize(ArrayList<Node> a_set) {
        int firstBatchSize = 1;
        int firstBatchFan = a_set.get(0).getAttribute(m_metricTag);
        while(firstBatchSize < a_set.size() && firstBatchFan == (double)a_set.get(firstBatchSize).getAttribute(m_metricTag)) {
            firstBatchSize++;
        }

        return firstBatchSize;
    }

    private ArrayList<Node> getWorkingSet(ArrayList<Node> a_sortedList, int nodesToAdd) {
        // things can have the same metric so we need to count this
        ArrayList<Node> workingSet = new ArrayList<>();
        double  currentMetric = a_sortedList.get(a_sortedList.size() - 1).getAttribute(m_metricTag);
        int ix = a_sortedList.size() - 1;
        int count = 0;
        while(ix >= 0 && count < nodesToAdd) {

            if (currentMetric != (double)a_sortedList.get(ix).getAttribute(m_metricTag)) {
                currentMetric = a_sortedList.get(ix).getAttribute(m_metricTag);
                count = a_sortedList.size() - ix - 1;  // we have completed the whole batch (at ix - 1) with the same metric
            }
            ix--;
        }
        if (ix >= 0) {
            ix += 2;   // we need to move one index up 2 positions as this is the last index at the valid count.
        } else {
            ix = 0; // we went to the end
        }

        for (; ix < a_sortedList.size(); ix++) {
            workingSet.add(a_sortedList.get(ix));
        }

        return workingSet;
    }

    private HuGMe.ArchDef createAndMapArch(Graph a_g) {
        HuGMe.ArchDef arch = new HuGMe.ArchDef();
        HuGMe.ArchDef.Component gui = arch.addComponent("gui");
        HuGMe.ArchDef.Component model = arch.addComponent("model");
        HuGMe.ArchDef.Component logic = arch.addComponent("logic");
        HuGMe.ArchDef.Component pref = arch.addComponent("pref");
        HuGMe.ArchDef.Component global = arch.addComponent("global");
        HuGMe.ArchDef.Component cli = arch.addComponent("cli");
        gui.addDependencyTo(model);gui.addDependencyTo(logic);

        cli.addDependencyTo(gui);cli.addDependencyTo(model);cli.addDependencyTo(logic);cli.addDependencyTo(pref);cli.addDependencyTo(global);
        logic.addDependencyTo(model);
        pref.addDependencyTo(model);pref.addDependencyTo(logic);
        global.addDependencyTo(gui);global.addDependencyTo(model);global.addDependencyTo(logic);global.addDependencyTo(pref);global.addDependencyTo(cli);

        return arch;
    }

    private void load(Graph a_g) {
        LoadJar c = new LoadJar("data/jabref-3.7.jar", "net/sf/jabref/");
        try {
            c.run(a_g);
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
