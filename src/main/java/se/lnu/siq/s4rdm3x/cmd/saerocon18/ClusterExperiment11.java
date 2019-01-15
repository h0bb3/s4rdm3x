package se.lnu.siq.s4rdm3x.cmd.saerocon18;

import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;

public class ClusterExperiment11 extends ClusterExperiment  {
    private final ClusterExperiment.RowHandler m_handler;
    Random m_rand = new Random();

    public ClusterExperiment11(ClusterExperiment.RowHandler a_handler) {
        m_handler = a_handler;
        for(String c:Cluster1.g_originalMappingTags) {
            m_fanInCaches.add(new ArrayList<NodeIntPair>());
        }
    }


    public static class NodeIntPair{
        CNode m_node;
        int m_fanIn;
        int m_fanOut;
        int m_totalFan;
    }
    private ArrayList<ArrayList<NodeIntPair>> m_fanInCaches = new ArrayList<>();



    public void run(CGraph a_g) {

        test_getWorkingSet();
        test_getFirstBatchSize();

        loadJabRef(a_g);


        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        loadJabRef(a_g);

        String fileName = "ex11_";

        prepare(a_g, 0);
        Cluster1.fillTheCache(a_g);
        populateCaches(a_g);



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

            prepare(a_g, 0);
            prepareFanInMapping(mappingPercent);

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

    void populateCaches(CGraph a_g) {
        for (CNode target : a_g.getNodes()) {
            if (target.hasAnyTag(Cluster1.g_originalMappingTags) && !target.hasAnyTag(Cluster1.g_clusterTags)) {
                int fanIn = 0;
                int fanOut = 0;

                /*for (dmClass cTarget : au.getClasses(target)) {
                    for (Node n : a_g.getEachNode()) {
                        if (n != target && au.hasAnyTag(n, Cluster1.g_originalMappingTags) && !au.hasAnyTag(n, Cluster1.g_clusterTags)) {
                            for (dmClass cN : au.getClasses(n)) {
                                fanIn += countDependenciesTo(cN, cTarget);
                                fanOut += countDependenciesTo(cTarget, cN);
                            }
                        }
                    }
                }*/

                /*int fanIn = 0;
                int fanOut = 0;*/
                for (CNode n : a_g.getNodes()) {
                    if (n != target && n.hasAnyTag(Cluster1.g_originalMappingTags) && !n.hasAnyTag(Cluster1.g_clusterTags)) {
                        fanIn += Cluster1.getFanIn(target, n);
                        fanOut += Cluster1.getFanIn(n, target);
                    }
                }

                NodeIntPair nip = new NodeIntPair();
                nip.m_node = target;
                nip.m_fanIn = fanIn;
                nip.m_fanOut = fanOut;
                nip.m_totalFan = nip.m_fanIn + nip.m_fanOut;

                int clusterIndex = getNodeCluster(target);
                m_fanInCaches.get(clusterIndex).add(nip);
            }
        }

        for (ArrayList<NodeIntPair> cache : m_fanInCaches) {
            cache.sort(Comparator.comparingInt((NodeIntPair a_nip) -> a_nip.m_fanOut));
        }
    }

    int getNodeCluster(CNode a_n) {
        int ix = 0;
        for (String cluster : Cluster1.g_originalMappingTags) {
            if (a_n.hasTag(cluster)) {
                return ix;
            }
            ix++;
        }

        return -1;
    }

    void prepareFanInMapping(double a_percentToMap) {
        int casheIx = 0;
        for (String cluster : Cluster1.g_clusterTags) {
            ArrayList<NodeIntPair> cache = m_fanInCaches.get(casheIx);

            int nodesToAdd = (int)((double)cache.size() * a_percentToMap);

            if (nodesToAdd <= 0) {
                nodesToAdd = 1;
            }
            ArrayList<NodeIntPair> workingSet = getWorkingSet(cache, nodesToAdd);


            // we may have added too many nodes (i.e. the last batch may be bigger)
            while (workingSet.size() > nodesToAdd) {
                int firstBatchSize = getFirstBatchSize(workingSet);
                workingSet.remove(Math.abs(m_rand.nextInt()) % firstBatchSize);
            }

            for(NodeIntPair nip : workingSet) {
                nip.m_node.addTag(cluster);
            }
            casheIx++;
        }
    }

    private int getFirstBatchSize(ArrayList<NodeIntPair> a_set) {
        int firstBatchSize = 1;
        int firstBatchFan = a_set.get(0).m_fanOut;
        while(firstBatchSize < a_set.size() && firstBatchFan == a_set.get(firstBatchSize).m_fanOut) {
            firstBatchSize++;
        }

        return firstBatchSize;
    }

    private ArrayList<NodeIntPair> getWorkingSet(ArrayList<NodeIntPair> a_cache, int nodesToAdd) {
        // things can have the same metric so we need to count this
        ArrayList<NodeIntPair> workingSet = new ArrayList<>();
        int currentFanIn = a_cache.get(a_cache.size() - 1).m_fanOut;
        int ix = a_cache.size() - 1;
        int count = 0;
        while(ix >= 0 && count < nodesToAdd) {

            if (currentFanIn != a_cache.get(ix).m_fanOut) {
                currentFanIn = a_cache.get(ix).m_fanOut;
                count = a_cache.size() - ix - 1;  // we have completed the whole batch (at ix - 1) with the same fan
            }
            ix--;
        }
        if (ix >= 0) {
            ix += 2;   // we need to move one index up 2 positions as this is the last index at the valid count.
        } else {
            ix = 0; // we went to the end
        }

        for (; ix < a_cache.size(); ix++) {
            workingSet.add(a_cache.get(ix));
        }

        return workingSet;
    }

// Tests below

    private void test_getFirstBatchSize() {

        ArrayList<NodeIntPair> c;
        int s;

        //3,3,4,5,5,6,7,7,8,8,8,9,10,10
        c = test_getNipTestData();
        s = getFirstBatchSize(c);
        assert(s == 2);

        //3,4,5,5,6,7,7,8,8,8,9,10,10
        c = test_getNipTestData();
        c.remove(0);
        s = getFirstBatchSize(c);
        assert(s == 1);

        //3,3
        c = test_getNipTestData();
        while (c.size() > 2) c.remove(2);
        s = getFirstBatchSize(c);
        assert(s == 2);

        //3
        c = test_getNipTestData();
        while (c.size() > 1) c.remove(1);
        s = getFirstBatchSize(c);
        assert(s == 1);
    }

    private ArrayList<NodeIntPair> test_getNipTestData() {
        ArrayList<NodeIntPair> c = new ArrayList<>();
        {
            NodeIntPair nip = new NodeIntPair();
            nip.m_fanOut = 3;
            c.add(nip);
            nip = new NodeIntPair();
            nip.m_fanOut = 3;
            c.add(nip);
            nip = new NodeIntPair();
            nip.m_fanOut = 4;
            c.add(nip);
            nip = new NodeIntPair();
            nip.m_fanOut = 5;
            c.add(nip);
            nip = new NodeIntPair();
            nip.m_fanOut = 5;
            c.add(nip);
            nip = new NodeIntPair();
            nip.m_fanOut = 6;
            c.add(nip);
            nip = new NodeIntPair();
            nip.m_fanOut = 7;
            c.add(nip);
            nip = new NodeIntPair();
            nip.m_fanOut = 7;
            c.add(nip);
            nip = new NodeIntPair();
            nip.m_fanOut = 8;
            c.add(nip);
            nip = new NodeIntPair();
            nip.m_fanOut = 8;
            c.add(nip);
            nip = new NodeIntPair();
            nip.m_fanOut = 8;
            c.add(nip);
            nip = new NodeIntPair();
            nip.m_fanOut = 9;
            c.add(nip);
            nip = new NodeIntPair();
            nip.m_fanOut = 10;
            c.add(nip);
            nip = new NodeIntPair();
            nip.m_fanOut = 10;
            c.add(nip);
        }

        return c;
    }

    private void test_getWorkingSet() {
        //3,3,4,5,5,6,7,7,8,8,8,9,10,10
        ArrayList<NodeIntPair> c;
        ArrayList<NodeIntPair> w;

        //3,3,4,5,5,6,7,7,8,8,8,9,10,10
        c = test_getNipTestData();
        w = getWorkingSet(c, 1);
        assert(w.size() == 2);
        assert(w.get(0).m_fanOut == 10);
        assert(w.get(1).m_fanOut == 10);

        //3,3,4,5,5,6,7,7,8,8,8,9,10
        c = test_getNipTestData();
        c.remove(c.size() - 1);
        w = getWorkingSet(c, 1);
        assert(w.size() == 1);
        assert(w.get(0).m_fanOut == 10);

        //3,3,4,5,5,6,7,7,8,8,8,9,10,10
        c = test_getNipTestData();
        w = getWorkingSet(c, 2);
        assert(w.size() == 2);
        assert(w.get(0).m_fanOut == 10);
        assert(w.get(1).m_fanOut == 10);

        //3,3,4,5,5,6,7,7,8,8,8,9,10,10
        c = test_getNipTestData();
        w = getWorkingSet(c, 3);
        assert(w.size() == 3);
        assert(w.get(0).m_fanOut == 9);
        assert(w.get(1).m_fanOut == 10);
        assert(w.get(2).m_fanOut == 10);

        //3,3,4,5,5,6,7,7,8,8,8,9,10,10
        c = test_getNipTestData();
        w = getWorkingSet(c, 4);
        assert(w.size() == 6);
        assert(w.get(0).m_fanOut == 8);
        assert(w.get(3).m_fanOut == 9);
        assert(w.get(4).m_fanOut == 10);

        c = test_getNipTestData();
        w = getWorkingSet(c, 14);
        assert(w.size() == 14);
        assert(w.get(0).m_fanOut == 3);
        assert(w.get(2).m_fanOut == 4);
        assert(w.get(5).m_fanOut == 6);

        c = test_getNipTestData();
        w = getWorkingSet(c, 15);
        assert(w.size() == 14);
        assert(w.get(0).m_fanOut == 3);
        assert(w.get(2).m_fanOut == 4);
        assert(w.get(5).m_fanOut == 6);

        c = test_getNipTestData();
        w = getWorkingSet(c, 13);
        assert(w.size() == 14);
        assert(w.get(0).m_fanOut == 3);

        c = test_getNipTestData();
        w = getWorkingSet(c, 12);
        assert(w.size() == 12);
        assert(w.get(0).m_fanOut == 4);

    }


}
