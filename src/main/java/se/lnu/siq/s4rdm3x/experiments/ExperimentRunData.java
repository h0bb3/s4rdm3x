package se.lnu.siq.s4rdm3x.experiments;

import se.lnu.siq.s4rdm3x.experiments.metric.Metric;

public class ExperimentRunData {
    public static class BasicRunData {
        public Metric m_metric;
        public String m_system;
        public long m_time;
        public int m_id;
        public double m_initialClusteringPercent;
        public int m_iterations;
        public int m_totalManuallyClustered;
        public int m_totalAutoClustered;
        public int m_totalAutoWrong;
        public int m_totalFailedClusterings;

        public String m_date;
        public int m_initialClustered;
        public int m_totalMapped;
        public String m_initialDistribution;


        public double calcAutoPerformance() {
            return (m_totalAutoClustered - m_totalAutoWrong) / (double)m_totalMapped;
        }
    }

    public static class HuGMEData extends BasicRunData {
        public double m_omega;  // specific for HuGMe
        public double m_phi;    // specific for HuGMe
    }

    public static class NBMapperData extends BasicRunData {
        public double m_threshold;
        public boolean m_doStemming;
        public boolean m_doWordCount;
    }


}
