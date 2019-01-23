package se.lnu.siq.s4rdm3x.model.cmd.saerocon18;

import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.stats;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by tohto on 2018-04-20.
 */
public class Cluster1 {


    private double m_filterThreshold;
    private double m_violationWeight;

    public int m_consideredNodes = 0;           // all nodes that pass the filter
    public int m_automaticallyMappedNodes = 0;
    public int m_manuallyMappedNodes = 0;
    public int m_failedMappings = 0;
    public int m_autoWrong = 0;
    public int m_unmappedNodesFromStart = 0;
    public int m_mappedNodesFromStart = 0;

    public static final String[] g_clusterTags = {"gui_c", "model_c", "logic_c", "pref_c", "global_c", "cli_c"};
    public static final String[] g_originalMappingTags = {"gui", "model", "logic", "pref", "global", "cli"};

    public ArrayList<CNode> m_clusteredElements;

    private boolean m_doManualMapping;

    public Cluster1(double a_filterThreshold, double a_violationWeight, boolean a_doManualMapping) {
        m_violationWeight = a_violationWeight;
        m_filterThreshold = a_filterThreshold;
        m_doManualMapping = a_doManualMapping;
    }

    private static HashMap<CNode, HashMap<CNode, Double>> g_nodeFanInMap = null;

    public static void resetTheCache() {
        g_nodeFanInMap = null;
    }

    public static void fillTheCache(CGraph a_g) {
        if (g_nodeFanInMap == null) {
            g_nodeFanInMap = new HashMap<>();

            for (CNode to : a_g.getNodes()) {
                if (to.hasAnyTag(g_originalMappingTags)) {

                    HashMap<CNode, Double> toMap = new HashMap<>();
                    g_nodeFanInMap.put(to, toMap);

                    for (CNode from : a_g.getNodes()) {
                        if (to != from && from.hasAnyTag(g_originalMappingTags)) {
                            double fanIn = 0;

                            for (dmClass cTo : to.getClasses()) {
                                for (dmClass cFrom : from.getClasses()) {
                                    fanIn += CountDependenciesTo(cFrom, cTo);
                                }
                            }
                            if (fanIn > 0) {
                                toMap.put(from, fanIn);
                            }
                        }
                    }
                }
            }
        }
    }

    public static double getFanIn(CNode a_to, CNode a_from) {
        HashMap<CNode, Double> toMap = g_nodeFanInMap.get(a_to);

        assert(a_to != null);
        if (toMap.containsKey(a_from)) {
            return toMap.get(a_from);
        }

        return 0;
    };

    public void run(CGraph a_g) {

        fillTheCache(a_g);

        m_clusteredElements = new ArrayList<>();

        // 1. first we need the current clusters, these are formed by the architecture and the currently mapped files
        // 2. next we filter the unmapped files so that we do not consider files that have too many relations to unmapped files
        // 3. next we compute the attraction matrix, the attraction for each unmapped file to the architecture
        //      this is done by computing a weighted sum for each relation and cluster.
        // 4. select the most attractive mapping - done
        // finally we do a mapping for the files that seem to stand out (ie. have a high enough attraction to a single cluster)

        // 1. This is completely hard coded for now. Ie we rely on a mapping performed for JabRef3.7
        final int NC = 6;


        /*
        Allowed Dependencies
            gui -> model, logic
            CLI -> gui, model, logic, globals, pref
            logic -> model
            pref -> model, logic
         */
                                            //   g  m  l  p  gl c
        final int[][] allowedDependencies = {   {1, 1, 1, 0, 0, 0},     // gui
                                                {0, 1, 0, 0, 0, 0},     // model
                                                {0, 1, 1, 0, 0, 0},     // logic
                                                {0, 1, 1, 1, 0, 0},     // pref
                                                {0, 0, 0, 0, 1, 0},     // global
                                                {1, 1, 1, 1, 1, 1}};    // cli


        // all all considered nodes to unmapped
        java.util.ArrayList<CNode> unmapped = new ArrayList<>();
        for (CNode n : a_g.getNodes()) {
            if (n.hasAnyTag(g_originalMappingTags)) {
                unmapped.add(n);
            }
        }
        // check the architecture
        {
            final int[][] ad = allowedDependencies;
            final int g = 0, m = 1, l = 2, p = 3, gl = 4, c = 5;
            assert(ad[g][g] == 1 && ad[g][m] == 1 && ad[g][l] == 1 && ad[g][p] == 0 && ad[g][gl] == 0 && ad[g][c] == 0);
            assert(ad[m][g] == 0 && ad[m][m] == 1 && ad[m][l] == 0 && ad[m][p] == 0 && ad[m][gl] == 0 && ad[m][c] == 0);
            assert(ad[l][g] == 0 && ad[l][m] == 1 && ad[l][l] == 1 && ad[l][p] == 0 && ad[l][gl] == 0 && ad[l][c] == 0);
            assert(ad[gl][g] == 1 && ad[gl][m] == 1 && ad[gl][l] == 1 && ad[gl][p] == 1 && ad[gl][gl] == 1 && ad[gl][c] == 1);
            assert(ad[c][g] == 1 && ad[c][m] == 1 && ad[c][l] == 1 && ad[c][p] == 1 && ad[c][gl] == 1 && ad[c][c] == 1);
        }


        // create the current clusters and remove all clustered nodes from the unmapped
        java.util.ArrayList<java.util.ArrayList<CNode>> clusters = new ArrayList<>();
        for(int i = 0; i < NC; i++) {
            ArrayList<CNode> c = new ArrayList<>();
            clusters.add(c);

            for(CNode n : a_g.getNodes()) {
                if (n.hasTag(g_clusterTags[i])) {
                    c.add(n);
                    unmapped.remove(n);
                }
            }
            m_mappedNodesFromStart += c.size();
        }

        m_unmappedNodesFromStart = unmapped.size();

        // 2. we iterate the unmapped files and compute the filter function, we remove if below threshold
        // basically this involves computing [Number of dependenices to mapped files]/[Number of dependencies to all files]

        java.util.ArrayList<CNode> candidates = new ArrayList<>(unmapped);
        for (CNode n : unmapped) {
            int toMappedCount = 0, totalCount = 0;

            /*for(dmClass c : au.getClasses(n)) {
                // All dependencies should be counted here not just the fan out from c
                // I.e the paper specifically mentions undirected edges between the entites


                // count all dependencies to this class from all unmapped classes
                for (Node otherNode : unmapped) {
                    if (otherNode != n) {
                        for (dmClass fromC : au.getClasses(otherNode)) {
                            totalCount += CountDependenciesTo(fromC, c);
                            totalCount += CountDependenciesTo(c, fromC);
                        }
                    }
                }

                // count the dependencies from and to the mapped classes
                for (ArrayList<Node> cluster : clusters) {
                    for (Node nMapped : cluster) {
                        for (dmClass toC : au.getClasses(nMapped)) {
                            double fromClustered = CountDependenciesTo(c, toC);
                            toMappedCount += CountDependenciesTo(toC, c);
                            toMappedCount += fromClustered;
                            totalCount += fromClustered;    // these should also be counted on the total
                        }
                    }
                }
            }*/

            // count all dependencies to this class from all unmapped classes
            int toMappedCountC = 0, totalCountC = 0;
            for (CNode otherNode : unmapped) {
                totalCountC += getFanIn(n, otherNode);
                totalCountC += getFanIn(otherNode, n);
            }

            for (ArrayList<CNode> cluster : clusters) {
                for (CNode nMapped : cluster) {

                        double fromClustered = getFanIn(nMapped, n);
                        toMappedCountC += getFanIn(n, nMapped);
                        toMappedCountC += fromClustered;
                        totalCountC += fromClustered;    // these should also be counted on the total
                }
            }

            //assert(toMappedCountC == toMappedCount);
            //assert(totalCountC == totalCount);
            totalCount = totalCountC;
            toMappedCount = toMappedCountC;


            double ratio = 0.0;
            if (totalCount > 0 && toMappedCount > 0) {
                ratio = (double) toMappedCount / (double) totalCount;
            }
            //Sys.out.println("Dependency Ratio: " + ratio);
            if (ratio >= m_filterThreshold) {
                //n.setAttribute("ui.style", "fill-color:rgb(0,0,255);");
            } else {
                candidates.remove(n);
            }
        }

        m_consideredNodes = candidates.size();

        // 3 Count the attraction to the clusters, there will be one sum for each cluster
        for (CNode n : candidates) {
            double attractions[] = new double[NC];
            for (int i = 0; i < NC; i++) {
                // TODO: Implement weights for different types of relations
                //attractions[i] = CountAttract(n, clusters.get(i));
                attractions[i] = CountAttractP(n, i, clusters, allowedDependencies);
            }
            n.setAttractions(attractions);
        }

        // dump to file
        /*BufferedWriter writer = null;
        try {
            File exportFile = new File("to_countattract_jr.csv");
            writer = new BufferedWriter(new FileWriter(exportFile));
            final String newLine = "\r\n";

            writer.write(";Concrete;");
            for(String tag : g_originalMappingTags) {
                writer.write(tag + ";");
            }
            writer.write(newLine);

            for (Node n : candidates) {
                for (dmClass c : au.getClasses(n)) {
                    double attractions[] = new double[NC];
                    for (int i = 0; i < NC; i++) {
                        attractions[i] = CountAttractP(c, i, clusters, allowedDependencies);
                        //double attraction = CountAttract(c, clusters.get(i), 1.0, 1.0);
                    }

                    writer.write(c.getName() + ";");
                    for(double a : attractions) {
                        writer.write(a + ";");
                    }
                    writer.write(newLine);

                }
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }*/


        // 4 Find 2 candidate sets of attractions
        //      First set is based on >= mean of all attractions
        //      Second set is based on > standard deviation of all attractions

        for (CNode n : candidates) {
            double attractions[] = n.getAttractions();
            double mean = stats.mean(attractions);
            double sd = stats.stdDev(attractions, mean);

            ArrayList<Integer> c1 = new ArrayList<>();
            ArrayList<Integer> c2 = new ArrayList<>();

            for(int i = 0; i < NC; i++) {
                if (attractions[i] >= mean) {
                    c2.add(i);
                }
                if (attractions[i] - mean > sd) {
                    c1.add(i);
                }
            }

            if (c1.size() == 1) {
                n.addTag(g_clusterTags[c1.get(0)]);
                //Sys.out.println("Clustered to: " + g_clusterTags[c1.get(0)]);
                n.addTag( "automatic");
                m_clusteredElements.add(n);
                if (!n.hasTag(g_originalMappingTags[c1.get(0)])) {
                    m_autoWrong++;
                }
                m_automaticallyMappedNodes++;
            } else if (c2.size() == 1) {
                n.addTag(g_clusterTags[c2.get(0)]);
                //Sys.out.println("Clustered to: " + g_clusterTags[c2.get(0)]);
                n.addTag("automatic");
                m_clusteredElements.add(n);
                if (!n.hasTag(g_originalMappingTags[c2.get(0)])) {
                    m_autoWrong++;
                }
                m_automaticallyMappedNodes++;
            } else if (m_doManualMapping){

                // no clear answer so we must ask the oracle...
                // i.e. if the original mapping is present in one of the available options...
                boolean clustered = false;
                if (c1.size() > 0) {
                    for(Integer i : c1) {
                        if (n.hasTag(g_originalMappingTags[i])) {
                            n.addTag(g_clusterTags[i]);
                            n.addTag("manual");
                            clustered = true;
                            //Sys.out.println("Clustered by Oracle to: " + g_clusterTags[i]);
                            m_manuallyMappedNodes++;
                            break;
                        }
                    }
                }
                if (!clustered && c2.size() > 0) {
                    for (Integer i : c2) {
                        if (n.hasTag(g_originalMappingTags[i])) {
                            n.addTag(g_clusterTags[i]);
                            n.addTag("manual");
                            //Sys.out.println("Clustered by Oracle to: " + g_clusterTags[i]);
                            m_manuallyMappedNodes++;
                            clustered = true;
                            break;
                        }
                    }
                }
                if (clustered == false) {
                    //Sys.out.println("No attraction... this one is dead...");
                    m_failedMappings++;
                    // we force a mapping
                    /*for(int i =0; i < g_originalMappingTags.length; i++) {
                        if (au.hasAnyTag(n, g_originalMappingTags[i])) {
                            au.addTag(n, g_clusterTags[i]);
                            au.addTag(n, "manual");
                            au.addTag(n, "forced");
                        }
                    }*/
                }
            }
        }
    }


    private double CountAttractP(dmClass a_class, int a_cluster, ArrayList<ArrayList<CNode>> a_clusters, int[][] a_allowedDependencies) {
        double overall = 0;
        double toOthers = 0;

        for (int i = 0; i < a_clusters.size(); i++) {
            final double allowedWeight = 0.5;    // TODO: this should be a parameter however 0.5 is suggested as a practical value in the article.
            final double violationWeight = 1.0;
            double weightFrom, weightTo;

            if (i != a_cluster) {
                // dependencies to other clusters than a_cluster can incur violations both from the class to the stuff in the cluster
                // and from the stuff in the cluster to the class. a_cluster is the attraction we want so this is from, i some other cluster
                if (a_allowedDependencies[a_cluster][i] == 0) {
                    // violations have the highest weight
                    weightFrom = violationWeight;
                } else {
                    weightFrom = allowedWeight;
                }

                if (a_allowedDependencies[i][a_cluster] == 0) {
                    weightTo = violationWeight;
                } else {
                    weightTo = allowedWeight;
                }
                double attract = CountAttract(a_class, a_clusters.get(i), weightFrom, weightTo);
                //double attract = CountAttract(a_class, a_clusters.get(i), weightTo, weightFrom);
                toOthers += attract;
            }

            overall += CountAttract(a_class, a_clusters.get(i), 1.0, 1.0);
        }

        //return toOthers;
        return overall - toOthers;
    }

    private double CountAttractP(CNode a_node, int a_cluster, ArrayList<ArrayList<CNode>> a_clusters, int[][] a_allowedDependencies) {
        double overall = 0;
        double toOthers = 0;

        for (int i = 0; i < a_clusters.size(); i++) {
            final double allowedWeight = m_violationWeight;
            final double violationWeight = 1.0;
            double weightFrom, weightTo;

            if (i == a_cluster) {
                // internal dependencies are of course allowed
                weightFrom = 1.0f;
                weightTo = 1.0f;
            } else {
                // dependencies to other clusters than a_cluster can incur violations
                if (a_allowedDependencies[a_cluster][i] == 0) {
                    // violations have the highest weight
                    weightFrom = violationWeight;
                } else {
                    weightFrom = allowedWeight;
                }

                if (a_allowedDependencies[i][a_cluster] == 0) {
                    weightTo = violationWeight;
                } else {
                    weightTo = allowedWeight;
                }

                toOthers += CountAttract(a_node, a_clusters.get(i), weightFrom, weightTo);
            }

            overall += CountAttract(a_node, a_clusters.get(i), 1.0, 1.0);
        }

        return overall - toOthers;
    }

    private double CountAttract(dmClass a_class, Iterable<CNode> a_cluster, double a_weightFromClass, double a_weightFromCluster) {
        double count = 0;

        for (CNode nTo : a_cluster) {
            for (dmClass cTo : nTo.getClasses()) {
                // these are specifically denoted by {ci, cj} in the paper which correspond to the undirectional relations
                count += CountDependenciesTo(a_class, cTo) * a_weightFromClass;
                count += CountDependenciesTo(cTo, a_class) * a_weightFromCluster;
            }
        }


        return count;
    }

    private double CountAttract(CNode a_node, Iterable<CNode> a_cluster, double a_weightFromNode, double a_weightFromCluster) {
        double count = 0;

        double cCount = 0;
        for (CNode nTo:a_cluster) {
            cCount += getFanIn(nTo, a_node) * a_weightFromNode;
            cCount += getFanIn(a_node, nTo) * a_weightFromCluster;
        }

        /*for (dmClass cFrom : au.getClasses(a_node)) {
            count += CountAttract(cFrom, a_cluster, a_weightFromNode, a_weightFromCluster);
        }*/

        //assert(count == cCount);
        count = cCount;

        return count;
    }

    // this cache actually seem to make things worse... :P
    /*private static HashMap<dmClass, HashMap<dmClass, Double>> g_classDependencyCount = new HashMap<>();
    private double CountDependenciesTo(dmClass a_from, dmClass a_to) {
        double count = 0;
        // TODO: we should have some weight here


        HashMap<dmClass, Double> fromHashed = g_classDependencyCount.get(a_from);
        if (fromHashed == null) {
            fromHashed = new HashMap<>();
            g_classDependencyCount.put(a_from, fromHashed);
        }

        Double fanIn = fromHashed.get(a_to);
        if (fanIn != null) {
            //Sys.out.println("Cashe Hit");
            return fanIn;
        }

        // we need to compute it
        //Sys.out.println("Cashe Miss");
        for(dmDependency d : a_from.getDependencies()) {
            if (d.getTarget() == a_to) {
                count += d.getCount();
            }
        }

        fromHashed.put(a_to, count);

        return count;
    }*/

    private static double CountDependenciesTo(dmClass a_from, dmClass a_to) {
        double count = 0;
        // TODO: we should have some weight here



        for(dmDependency d : a_from.getDependencies()) {
            if (d.getTarget() == a_to) {
                count += d.getCount();
            }
        }

        return count;
    }


    /*private int CountDependencies(dmClass a_c) {
        int count = 0;

        for(dmDependency d : a_c.getDependencies()) {
            if (d.getType() != dmDependency.Type.OwnFieldUse) {
                count += d.getCount();
            }
        }

        return count;
    }*/

}

