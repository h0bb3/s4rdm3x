package se.lnu.siq.s4rdm3x.experiments;

import se.lnu.siq.s4rdm3x.experiments.system.System;
import se.lnu.siq.s4rdm3x.experiments.metric.Metric;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.ArrayList;
import java.util.Collection;

public class ExperimentRunData {
    public static class BasicRunData {
        public Metric m_metric;
        public System m_system;
        public long m_time;
        public int m_id;
        public double m_initialClusteringPercent;
        public int m_iterations;
        public int m_totalManuallyClustered;
        public int m_totalAutoWrong;
        public int m_totalFailedClusterings;

        public String m_date;
        public String m_mapperName;
        public int m_totalMapped;
        public String m_initialDistribution;
        private ArrayList<CNode> m_initialClustering = new ArrayList<>();
        private ArrayList<CNode> m_autoClustered = new ArrayList<>();

        public void addInitialClusteredNode(CNode a_node) {
            m_initialClustering.add(new CNode(a_node));
        }

        public void addAutoClusteredNode(CNode a_node) {
            m_autoClustered.add(new CNode(a_node));
        }

        public int getAutoClusteredNodeCount() {
            return m_autoClustered.size();
        }

        public int getInitialClusteringNodeCount() {
            return m_initialClustering.size();
        }

        public Iterable<CNode> getAutoClusteredNodes() {
            return m_autoClustered;
        }

        public Iterable<CNode> getInitialClusteringNodes() {
            return m_initialClustering;
        }


        public double calcAutoPerformance() {
            return (m_autoClustered.size() - m_totalAutoWrong) / (double)m_totalMapped;
        }

        public double calcAutoPrecision() {
            if (m_autoClustered.size() > 0) {
                return 1 - (m_totalAutoWrong / (double) m_autoClustered.size());
            } else {
                return 0;
            }
        }

        public double calcAutoRecall() {
            return (m_autoClustered.size() - m_totalAutoWrong) / (double)(m_totalMapped - m_initialClustering.size() - m_totalManuallyClustered);
        }

        /*public void addInitialClusteredNodes(Iterable<? extends CNode> a_nodes) {
            for (CNode n : a_nodes) {
                addInitialClusteredNode(n);
            }
        }*/
    }

    public static class HuGMEData extends BasicRunData {
        public double m_omega;  // specific for HuGMe
        public double m_phi;    // specific for HuGMe
    }

    public static class IRMapperData extends BasicRunData {
        public boolean m_doStemming;
        public boolean m_doUseCDA;
        public boolean m_doUseNodeText;
        public boolean m_doUseNodeName;
        public boolean m_doUseArchComponentName;
        public int m_minWordSize;
    }

    public static class NBMapperData extends IRMapperData {
        public double m_threshold;
        public boolean m_doWordCount;
    }


}
