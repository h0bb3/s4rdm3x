package se.lnu.siq.s4rdm3x.cmd.saerocon18;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.AttributeUtil;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClusterExperiment7 extends ClusterExperiment {

    Random m_rand = new Random();

    private final RowHandler m_handler;

    public enum Variant {
        TotalFan,
        FanIn,
        FanOut
    };

    private Variant m_variant;

    public ClusterExperiment7(RowHandler a_handler, Variant a_variant) {
        m_handler = a_handler;
        m_variant = a_variant;
    }

    //private HashMap<Node, Integer> m_fanInMap = new HashMap<>();
    public static class NodeIntPair{
        Node m_node;
        int m_fanIn;
        int m_fanOut;
        int m_totalFan;
    }
    private ArrayList<NodeIntPair> m_fanInCache;
    private ArrayList<NodeIntPair> m_workingCache;

    public void run(Graph a_g) {

        m_fanInCache = new ArrayList<>();
        m_workingCache = new ArrayList<>();

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        loadJabRef(a_g);

        String fileName = "error";
        if (m_variant == Variant.TotalFan) {
            fileName = "ex7_";
        } else if (m_variant == Variant.FanIn) {
            fileName = "ex8_";
        } else if (m_variant == Variant.FanOut) {
            fileName = "ex9_";
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
        ArrayList<Node> clusteredNodes = new ArrayList<>();

        int i = 0;
        while(true) {
            prepare(a_g, -1);
            copyWorkingFanInCache();

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
                clusteredNodes.addAll(c.m_clusteredElements);
                if (c.m_automaticallyMappedNodes == 0) {

                    updateWorkingFanInCache(clusteredNodes, a_g);

                    //computeFanInCache(a_g);
                    //copyWorkingFanInCache();
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

    private void updateWorkingFanInCache(ArrayList<Node> a_clusteredNodes, Graph a_g) {
        AttributeUtil au = new AttributeUtil();
        // remove anything mapped
        m_workingCache.removeIf((nip)->{return au.hasAnyTag(nip.m_node, Cluster1.g_clusterTags);});

        // update the cantrality
        for(NodeIntPair nip : m_workingCache) {

            for (Node target : a_clusteredNodes) {
                nip.m_fanIn -= Cluster1.getFanIn(nip.m_node, target);
                nip.m_fanOut -= Cluster1.getFanIn(target, nip.m_node);
            }
            nip.m_totalFan = nip.m_fanIn + nip.m_fanOut;

            /*int fanIn = 0;
            int fanOut = 0;
            for (Node n : a_g.getEachNode()) {
                if (n != nip.m_node && au.hasAnyTag(n, Cluster1.g_originalMappingTags) && !au.hasAnyTag(n, Cluster1.g_clusterTags)) {
                    fanIn += Cluster1.getFanIn(nip.m_node, n);
                    fanOut += Cluster1.getFanIn(n, nip.m_node);
                }
            }
            assert(fanIn == nip.m_fanIn);*/
        }

        sortCache(m_workingCache);
    }

    private void sortCache(ArrayList<NodeIntPair> a_cache) {
        if (m_variant == Variant.TotalFan) {
            a_cache.sort(Comparator.comparingInt((NodeIntPair a_nip) -> a_nip.m_totalFan));
        } else if (m_variant == Variant.FanIn) {
            a_cache.sort(Comparator.comparingInt((NodeIntPair a_nip) -> a_nip.m_fanIn));
        } else if (m_variant == Variant.FanOut) {
            a_cache.sort(Comparator.comparingInt((NodeIntPair a_nip) -> a_nip.m_fanOut));
        } else {
            m_workingCache.get(m_workingCache.size() + 1); // out of bounds exception!
        }
    }

    private void computeFanInCache(Graph a_g) {
        AttributeUtil au = new AttributeUtil();
        for (Node target : a_g.getEachNode()) {
            if (au.hasAnyTag(target, Cluster1.g_originalMappingTags) && !au.hasAnyTag(target, Cluster1.g_clusterTags)) {
                int fanIn = 0;
                int fanOut = 0;

                for (dmClass cTarget : au.getClasses(target)) {
                    for (Node n : a_g.getEachNode()) {
                        if (n != target && au.hasAnyTag(n, Cluster1.g_originalMappingTags) && !au.hasAnyTag(n, Cluster1.g_clusterTags)) {
                            for (dmClass cN : au.getClasses(n)) {
                                fanIn += countDependenciesTo(cN, cTarget);
                                fanOut += countDependenciesTo(cTarget, cN);
                            }
                        }
                    }
                }

                /*int fanIn = 0;
                int fanOut = 0;*/
                /*for (Node n : a_g.getEachNode()) {
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

       sortCache(m_fanInCache);
    }

    private void mapHighestFanInNode(Graph a_g) {
        AttributeUtil au = new AttributeUtil();

        // purge any mapped nodes
        m_workingCache.removeIf((nip)->{return au.hasAnyTag(nip.m_node, Cluster1.g_clusterTags);});

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
            if (au.hasAnyTag(nip.m_node, Cluster1.g_originalMappingTags[tIx])) {
                //System.out.println("FanIn" + nip.m_fanIn);
                au.addTag(nip.m_node, "manual");
                au.addTag(nip.m_node, Cluster1.g_clusterTags[tIx]);
                break;
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

