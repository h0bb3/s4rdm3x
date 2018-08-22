package se.lnu.siq.s4rdm3x.experiments;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;
import se.lnu.siq.s4rdm3x.cmd.Selector;
import se.lnu.siq.s4rdm3x.cmd.util.FanInCache;

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

public abstract class ExperimentRunner {
    private final String m_metricTag = "metric";
    private String m_fileName;
    protected Random m_rand = new Random();

    protected ExperimentRunner(String a_fileName) {
        m_fileName = a_fileName;
    }

    public void run(Graph a_g) {

        Path fp = getFilePath(m_fileName);

        int i = 0;

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

        writeRow(fp, row);

        FanInCache fic = null;

        if (!load(a_g)) {
            return;
        }
        HuGMe.ArchDef arch = createAndMapArch(a_g);
        assignMetric(a_g, arch);

        while(true) {

            double mappingPercent = m_rand.nextDouble();
            double phi = m_rand.nextDouble();
            double omega = m_rand.nextDouble();

            arch.cleanNodeClusters(a_g);
            assignInitialClusters(a_g, arch, mappingPercent);

            String initalDist = getInitialClusterDistributionString(a_g, arch);

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

            if (fic == null) {
                fic = new FanInCache(arch.getMappedNodes(a_g.getNodeSet()));
            }

            while(true) {
                HuGMe c = new HuGMe(omega, phi, true, arch, fic);
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
            row = new ArrayList<>();
            row.add(sdfDate.format(new Date()));
            row.add("" + time);
            row.add("" + i);
            row.add("" + omega);
            row.add("" + phi);
            row.add("" + totalMapped);
            row.add("" + totalUnmapped);
            row.add(initalDist);
            row.add("" + iterations);
            row.add("" + totalManuallyMapped);
            row.add("" + totalAutoMapped);
            row.add("" + totalAutoWrong);
            row.add("" + totalFailedMappings);
            row.add("" + mappingPercent);

            writeRow(fp, row);

            i++;
        }
    }

    private void cleanClusters() {

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

    private void assignInitialClusters(Graph a_g, HuGMe.ArchDef a_arch, double a_percentage) {
        ArrayList<Node> sortedNodes = new ArrayList<>();
        for (Node n : a_g.getEachNode()) {
            if (a_arch.getMappedComponent(n) != null) {
                sortedNodes.add(n);
            }
        }

        // this sorts to lowest first
        sortedNodes.sort(Comparator.comparingDouble(a_n -> {
            return getMetric(a_n);
        }));

        int nodeCount = (int) ((double) sortedNodes.size() * a_percentage);
        if (nodeCount <= 0) {
            nodeCount = 1;
        }

        ArrayList<Node> workingSet = getWorkingSet(sortedNodes, nodeCount);

        // we may have added too many nodes (i.e. the last batch may be bigger)
        while (workingSet.size() > nodeCount) {
            int firstBatchSize = getFirstBatchSize(workingSet);
            workingSet.remove(Math.abs(m_rand.nextInt()) % firstBatchSize);
        }

        for (Node n : workingSet) {
            HuGMe.ArchDef.Component component = a_arch.getMappedComponent(n);
            component.clusterToNode(n);
        }
    }

    private void assignInitialClustersPerComponent(Graph a_g, HuGMe.ArchDef a_arch, double a_percentage) {
        // OBS this assigns a number of classes per component, this is not actually that realistic
        for (HuGMe.ArchDef.Component component : a_arch.getComponents()) {

            ArrayList<Node> sortedNodes = new ArrayList<>();
            for (Node n : a_g.getEachNode()) {
                if (component.isMappedTo(n)) {
                    sortedNodes.add(n);
                }
            }

            // this sorts to lowest first
            sortedNodes.sort(Comparator.comparingDouble(a_n -> {
                return getMetric(a_n);
            }));

            int nodeCount = (int) ((double) sortedNodes.size() * a_percentage);
            if (nodeCount <= 0) {
                nodeCount = 1;
            }

            ArrayList<Node> workingSet = getWorkingSet(sortedNodes, nodeCount);

            // we may have added too many nodes (i.e. the last batch may be bigger)
            while (workingSet.size() > nodeCount) {
                int firstBatchSize = getFirstBatchSize(workingSet);
                workingSet.remove(Math.abs(m_rand.nextInt()) % firstBatchSize);
            }

            for (Node n : workingSet) {
                component.clusterToNode(n);
            }
        }
    }

    protected abstract void assignMetric(Graph a_g,  HuGMe.ArchDef a_arch);

    protected abstract HuGMe.ArchDef createAndMapArch(Graph a_g);

    protected abstract boolean load(Graph a_g);

    protected void setMetric(Node a_node, double a_metric) {
        a_node.setAttribute(m_metricTag, a_metric);
    }

    protected double getMetric(Node a_node) {
        return a_node.getAttribute(m_metricTag);
    }

    private int getFirstBatchSize(ArrayList<Node> a_set) {
        int firstBatchSize = 1;
        double firstBatchFan = getMetric(a_set.get(0));
        while(firstBatchSize < a_set.size() && firstBatchFan == getMetric(a_set.get(firstBatchSize))) {
            firstBatchSize++;
        }

        return firstBatchSize;
    }

    private ArrayList<Node> getWorkingSet(ArrayList<Node> a_sortedList, int nodesToAdd) {
        // things can have the same metric so we need to count this
        ArrayList<Node> workingSet = new ArrayList<>();
        double  currentMetric = getMetric(a_sortedList.get(a_sortedList.size() - 1));
        int ix = a_sortedList.size() - 1;
        int count = 0;
        while(ix >= 0 && count < nodesToAdd) {

            if (currentMetric != getMetric(a_sortedList.get(ix))) {
                currentMetric = getMetric(a_sortedList.get(ix));
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

    protected HuGMe.ArchDef.Component createAddAndMapComponent(Graph a_g, HuGMe.ArchDef a_ad, String a_componentName, String[] a_packages) {
        HuGMe.ArchDef.Component c = a_ad.addComponent(a_componentName);
        for(String p : a_packages) {
            c.mapToNodes(a_g, new Selector.Pkg(p));
        }
        return c;
    }
}
