package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.ArrayList;

/**
 * Encapsulates the CountAttract function that maps based on graph dependency counts. It is described and evaluated by Andreas Christl, Rainer Koschke, and Margaret-Anne Storey. 2007. Automated clustering to support the reflexion method. Inf. Softw. Technol. 49, 3 (March 2007), 255–274. DOI:https://doi.org/10.1016/j.infsof.2006.10.015
 *
 */
public class HuGMe extends MapperBase {

    private double m_filterThreshold;   // omega in paper
    private double m_violationWeight;   // psi in paper

    private DependencyWeights m_weights;

    public int m_consideredNodes = 0;           // all nodes that pass the filter

    public int m_autoWrong = 0;
    public int m_unmappedNodesFromStart = 0;
    public int m_mappedNodesFromStart = 0;

    public HuGMe(double a_filterThreshold, double a_violationWeight, boolean a_doManualMapping, ArchDef a_arch, DependencyWeights a_dw) {
        super(a_doManualMapping, a_arch);
        m_violationWeight = a_violationWeight;
        m_filterThreshold = a_filterThreshold;
        m_arch = a_arch;

        m_weights = new DependencyWeights(a_dw);
    }

    public HuGMe(double a_filterThreshold, double a_violationWeight, boolean a_doManualMapping, ArchDef a_arch) {
        super(a_doManualMapping, a_arch);
        m_violationWeight = a_violationWeight;
        m_filterThreshold = a_filterThreshold;
        m_arch = a_arch;

        m_weights = new DependencyWeights(1.0);
    }


    public void run(CGraph a_g) {
        final String [] originalMappingTags = m_arch.getComponentNames();

        java.util.ArrayList<OrphanNode> unmapped = getOrphanNodes(a_g);

        // create the current clusters
        java.util.ArrayList<java.util.ArrayList<ClusteredNode>> clusters = new ArrayList<>();
        for(int i = 0; i < m_arch.getComponentCount(); i++) {
            ArrayList<ClusteredNode> c = new ArrayList<>();
            clusters.add(c);
            ArchDef.Component targetComponent = m_arch.getComponent(i);

            for(ClusteredNode n : getInitiallyMappedNodes(a_g)) {
                if (n.getClusteredComponent() == targetComponent) {
                    c.add(n);
                }
            }
            m_mappedNodesFromStart += c.size();
        }

        m_unmappedNodesFromStart = unmapped.size();

        java.util.ArrayList<OrphanNode> candidates = new ArrayList<>(unmapped);
        for (OrphanNode n : unmapped) {
            // count all dependencies to this class from all other unmapped classes
            int toMappedCountC = 0, totalCountC = 0;
            for (OrphanNode otherNode : unmapped) {
                if (n != otherNode) {
                    totalCountC += n.getDependencyCount(otherNode); //m_fic.getFanIn(n, otherNode);
                    totalCountC += otherNode.getDependencyCount(n); //m_fic.getFanIn(otherNode, n);
                }
            }

            for (ArrayList<ClusteredNode> cluster : clusters) {
                for (ClusteredNode nMapped : cluster) {

                    double fromClustered = nMapped.getDependencyCount(n);//m_fic.getFanIn(nMapped, n);
                    double toClustered = n.getDependencyCount(nMapped);//m_fic.getFanIn(n, nMapped);
                    toMappedCountC += toClustered;
                    toMappedCountC += fromClustered;
                    totalCountC += fromClustered;
                    totalCountC += toClustered;
                }
            }

            double ratio = 0.0;
            if (totalCountC > 0 && toMappedCountC > 0) {
                ratio = (double) toMappedCountC / (double) totalCountC;
            } else if (toMappedCountC > 0) {
                ratio = 1.0;
            }
            
            if (ratio < m_filterThreshold) {
                candidates.remove(n);
            }
        }

        m_consideredNodes = candidates.size();

        // 3 Count the attraction to the clusters, there will be one sum for each cluster
        for (OrphanNode n : candidates) {
            double attractions[] = new double[m_arch.getComponentCount()];
            for (int i = 0; i < m_arch.getComponentCount(); i++) {
                attractions[i] = CountAttractP(n, i, clusters);
            }
            n.setAttractions(attractions);
        }

        // 4 Find 2 candidate sets of attractions
        //      First set is based on >= mean of all attractions
        //      Second set is based on > standard deviation of all attractions

        for (OrphanNode n : candidates) {
            ArchDef.Component autoClusteredTo = doAutoMapping(n, m_arch);


            ArchDef.Component mappedC = m_arch.getMappedComponent(n.get());

            if (autoClusteredTo != null) {
                addAutoClusteredOrphan(n);
                if (autoClusteredTo != mappedC) {
                    m_autoWrong++;
                }
            } else if (doManualMapping()) {


                // we always map to the correct cluster using the oracle
                // we count the advice as a fail if the attraction is below the median attraction of the clusters
                // this is possibly more correct in relation to the paper
                boolean clustered = manualMapping(n, m_arch);
                /*ArchDef.Component targetC = m_arch.getMappedComponent(n);
                for(int i = 0; i < m_arch.getComponentCount(); i++){
                    if (m_arch.getComponent(i) == targetC) {
                        clustered = attractions[i] > stats.medianUnsorted(attractions);
                        ArchDef.Component.ClusteringType type = clustered ? ArchDef.Component.ClusteringType.Manual : ArchDef.Component.ClusteringType.ManualFailed;
                        targetC.clusterToNode(n, type);
                        m_manuallyMappedNodes++;
                        break;
                    }
                }*/







                // this is the old conservative manual mapping
                // no clear answer so we must ask the oracle...
                // i.e. if the original mapping is present in one of the available options...
                /*boolean clustered = false;
                if (c1.size() > 0) {
                    for(Integer i : c1) {
                        if (mappedC == m_arch.getComponent(i)) {
                            mappedC.clusterToNode(n, ArchDef.Component.ClusteringType.Manual);
                            clustered = true;
                            //Sys.out.println("Clustered by Oracle to: " + g_clusterTags[i]);
                            m_manuallyMappedNodes++;
                            break;
                        }
                    }
                }
                if (!clustered && c2.size() > 0) {
                    for (Integer i : c2) {
                        if (mappedC == m_arch.getComponent(i)) {
                            mappedC.clusterToNode(n, ArchDef.Component.ClusteringType.Manual);
                            //Sys.out.println("Clustered by Oracle to: " + g_clusterTags[i]);
                            m_manuallyMappedNodes++;
                            clustered = true;
                            break;
                        }
                    }
                }*/


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




    double CountAttractP(OrphanNode a_node, int a_cluster, ArrayList<ArrayList<ClusteredNode>> a_clusters) {
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
                ArchDef.Component from = m_arch.getComponent(a_cluster);
                ArchDef.Component to = m_arch.getComponent(i);
                final boolean violationToOther = !from.allowedDependency(to);
                // dependencies from a_cluster to other clusters (i) can incur violations
                if (violationToOther) {
                    // violations have the highest weight
                    weightFrom = violationWeight;
                } else {
                    weightFrom = allowedWeight;
                }

                // dependencies from other clusters (i) to a_cluster can incur violations
                final boolean violationFromOther = !to.allowedDependency(from);
                if (violationFromOther) {
                    weightTo = violationWeight;
                } else {
                    weightTo = allowedWeight;
                }

                toOthers += CountAttract(a_node, a_clusters.get(i), weightFrom, weightTo, violationToOther, violationFromOther);

                // remember the attraction contribution to the cluster will be reduced to 0 if there are violations
                // otherwise the attraction to the cluster will increase as allowed dependencies does not reduce the attraction.
                // this also means that the overall value is the maximum attraction we can get for the
                // node and should be the number of dependencies from the node to any other mapped node
            }

            overall += CountAttract(a_node, a_clusters.get(i), 1.0, 1.0, false, false);
        }

        return overall - toOthers;
    }

    private double CountAttract(OrphanNode a_node, Iterable<ClusteredNode> a_cluster, double a_weightFromNode, double a_weightFromCluster, boolean a_violationFromNode, boolean a_violationFromCluster) {
        double count = 0;

        double cCount = 0;
        for (ClusteredNode nTo : a_cluster) {
            cCount += a_node.getDependencyCount(nTo, m_weights, !a_violationFromNode) * a_weightFromNode; //m_fic.getFanIn(nTo, a_node) * a_weightFromNode;
            cCount += nTo.getDependencyCount(a_node, m_weights, !a_violationFromCluster) * a_weightFromCluster;//m_fic.getFanIn(a_node, nTo) * a_weightFromCluster;
        }

        count = cCount;

        return count;
    }

}
