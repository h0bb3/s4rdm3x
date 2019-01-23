package se.lnu.siq.s4rdm3x.model.cmd.saerocon18;

import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClusterExperiment4 extends ClusterExperiment {

    Random m_rand = new Random();

    private final RowHandler m_handler;

    public enum Variant {
        GlobalTotalFan,
        GlobalFanIn,
        GlobalFanOut
    };

    private Variant m_variant;

    public ClusterExperiment4(RowHandler a_handler, Variant a_variant) {
        m_handler = a_handler;
        m_variant = a_variant;
    }

    //private HashMap<Node, Integer> m_fanInMap = new HashMap<>();
    public static class NodeIntPair{
        CNode m_node;
        int m_fanIn;
        int m_fanOut;
        int m_totalFan;
    }
    private ArrayList<NodeIntPair> m_fanInCache;
    private ArrayList<NodeIntPair> m_workingCache;

    public void run(CGraph a_g) {

        m_fanInCache = new ArrayList<>();
        m_workingCache = new ArrayList<>();

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        loadJabRef(a_g);

        String fileName = "error";
        if (m_variant == Variant.GlobalTotalFan) {
            fileName = "ex4_";
        } else if (m_variant == Variant.GlobalFanIn) {
            fileName = "ex5_";
        } else if (m_variant == Variant.GlobalFanOut) {
            fileName = "ex6_";
        }


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


        prepare(a_g, -1);
        computeFanInCache(a_g);


        int i = 0;
        while(true) {
            prepare(a_g, -1);
            double phi = m_rand.nextDouble();
            double omega = m_rand.nextDouble();
            copyWorkingFanInCache();

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

                    mapHighestFanInNode(a_g);
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

    private int countDependenciesTo(dmClass a_from, dmClass a_to) {
        int count = 0;
        // TODO: we should have some weight here

        for(dmDependency d : a_from.getDependencies()) {
            if (d.getTarget() == a_to) {
                count += d.getCount();
            }
        }

        return count;
    }

    private void copyWorkingFanInCache() {
        m_workingCache.clear();
        m_workingCache.addAll(m_fanInCache);
    }

    private void computeFanInCache(CGraph a_g) {
        for (CNode target : a_g.getNodes()) {
            if (target.hasAnyTag(Cluster1.g_originalMappingTags) && !target.hasAnyTag(Cluster1.g_clusterTags)) {
                int fanIn = 0;
                int fanOut = 0;

                for (dmClass cTarget : target.getClasses()) {
                    for (CNode n : a_g.getNodes()) {
                        if (n != target && n.hasAnyTag(Cluster1.g_originalMappingTags) && !n.hasAnyTag(Cluster1.g_clusterTags)) {
                            for (dmClass cN : n.getClasses()) {
                                fanIn += countDependenciesTo(cN, cTarget);
                                fanOut += countDependenciesTo(cTarget, cN);
                            }
                        }
                    }
                }

                /*int fanIn = 0;
                int fanOut = 0;
                for (Node n : a_g.getEachNode()) {
                    if (n != target && au.hasAnyTag(n, Cluster1.g_originalMappingTags) && !au.hasAnyTag(n, Cluster1.g_clusterTags)) {
                        fanIn += Cluster1.getFanIn(target, n);
                        fanOut += Cluster1.getFanIn(n, target);
                    }
                }*/



                NodeIntPair nip = new NodeIntPair();
                nip.m_node = target;
                nip.m_fanIn = fanIn;
                nip.m_fanOut = fanOut;
                nip.m_totalFan = nip.m_fanIn + nip.m_fanOut;
                m_fanInCache.add(nip);
            }
        }

        if (m_variant == Variant.GlobalTotalFan) {
            m_fanInCache.sort(Comparator.comparingInt((NodeIntPair a_nip) -> a_nip.m_totalFan));
        } else if (m_variant == Variant.GlobalFanIn) {
            m_fanInCache.sort(Comparator.comparingInt((NodeIntPair a_nip) -> a_nip.m_fanIn));
        } else if (m_variant == Variant.GlobalFanOut) {
            m_fanInCache.sort(Comparator.comparingInt((NodeIntPair a_nip) -> a_nip.m_fanOut));
        } else {
            m_workingCache.get(m_workingCache.size() + 1); // out of bounds exception!
        }
    }

    private void mapHighestFanInNode(CGraph a_g) {

        // purge any mapped nodes
        m_workingCache.removeIf((nip)->{return nip.m_node.hasAnyTag(Cluster1.g_clusterTags);});

        int selection = (int)((double)m_workingCache.size() * 0.01);
        if (selection == 0){
            selection = m_workingCache.size();
        }
        if (selection == 0) {
            return;
        }

        int ix = m_workingCache.size() - Math.abs(m_rand.nextInt() % selection) - 1;
        //int ix = Math.abs(m_rand.nextInt() % selection);
        NodeIntPair nip = m_workingCache.get(ix);

        for (int tIx = 0; tIx < Cluster1.g_originalMappingTags.length; tIx++) {
            if (nip.m_node.hasTag(Cluster1.g_originalMappingTags[tIx])) {
                //Sys.out.println("FanIn" + nip.m_fanIn);
                nip.m_node.addTag("manual");
                nip.m_node.addTag(Cluster1.g_clusterTags[tIx]);
                break;
            }
        }

    }

    private void mapRandomNode(CGraph a_g) {
        java.util.ArrayList<CNode> unmapped = new ArrayList<>();

        for (CNode n : a_g.getNodes()) {
            if (n.hasAnyTag(Cluster1.g_clusterTags) != true) {
                unmapped.add(n);
            }
        }

        CNode selected = unmapped.get(Math.abs(m_rand.nextInt()) % unmapped.size());
        for (int tIx = 0; tIx < Cluster1.g_originalMappingTags.length; tIx++) {
            if (selected.hasTag(Cluster1.g_originalMappingTags[tIx])) {
                selected.addTag(Cluster1.g_clusterTags[tIx]);
                break;
            }
        }
    }

}
