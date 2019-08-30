package se.lnu.siq.s4rdm3x.experiments;

import se.lnu.siq.s4rdm3x.experiments.metric.Metric;
import se.lnu.siq.s4rdm3x.experiments.system.System;

import java.util.Random;

public abstract class IRExperimentRunnerBase extends ExperimentRunner {
    public IRExperimentRunnerBase(System a_sua, Metric a_metric, boolean a_doUseManualMapping, boolean a_useInitialMapping, RandomDoubleVariable a_initialSetSize, IRExperimentRunnerBase.Data a_irData) {
        super(a_sua, a_metric, a_doUseManualMapping, a_useInitialMapping, a_initialSetSize);

        m_data = new Data(a_irData);
    }

    public IRExperimentRunnerBase(Iterable<System> a_suas, Iterable<Metric> a_metrics, boolean a_doUseManualMapping, boolean a_useInitialMapping, RandomDoubleVariable a_initialSetSize, Data a_irData) {
        super(a_suas, a_metrics, a_doUseManualMapping, a_useInitialMapping, a_initialSetSize);
        m_data = new Data(a_irData);
    }

    public Data getIRDataClone() {
        return new Data(m_data);
    }

    public static class Data {


        private RandomBoolVariable m_doStemming;
        private RandomBoolVariable m_doUseCDA;
        private RandomBoolVariable m_doUseNodeText;
        private RandomBoolVariable m_doUseNodeName;
        private RandomBoolVariable m_doUseArchComponentName;
        private RandomIntVariable m_minWordSize;

        public Data(Data a_cpy) {
            m_doStemming = new RandomBoolVariable(a_cpy.m_doStemming);
            m_doUseCDA = new RandomBoolVariable(a_cpy.m_doUseCDA);
            m_doUseNodeText = new RandomBoolVariable(a_cpy.m_doUseNodeText);
            m_doUseNodeName = new RandomBoolVariable(a_cpy.m_doUseNodeName);
            m_doUseArchComponentName = new RandomBoolVariable(a_cpy.m_doUseArchComponentName);
            m_minWordSize = new RandomIntVariable(a_cpy.m_minWordSize);
        }

        public Data() {
            m_doStemming = new RandomBoolVariable(true);
            m_doUseCDA = new RandomBoolVariable(true);
            m_doUseNodeText = new RandomBoolVariable(true);
            m_doUseNodeName = new RandomBoolVariable(true);
            m_doUseArchComponentName = new RandomBoolVariable(true);
            m_minWordSize = new RandomIntVariable(3);
        }

        public RandomBoolVariable doStemming() {
            return m_doStemming;
        }

        public void doStemming(RandomBoolVariable m_doStemming) {
            this.m_doStemming = new RandomBoolVariable(m_doStemming);
        }

        public RandomBoolVariable doUseCDA() {
            return m_doUseCDA;
        }

        public void doUseCDA(RandomBoolVariable m_doUseCDA) {
            this.m_doUseCDA = new RandomBoolVariable(m_doUseCDA);
        }

        public RandomBoolVariable doUseNodeText() {
            return m_doUseNodeText;
        }

        public void doUseNodeText(RandomBoolVariable m_doUseNodeText) {
            this.m_doUseNodeText = new RandomBoolVariable(m_doUseNodeText);
        }

        public RandomBoolVariable doUseNodeName() {
            return m_doUseNodeName;
        }

        public void doUseNodeName(RandomBoolVariable m_doUseNodeName) {
            this.m_doUseNodeName = new RandomBoolVariable(m_doUseNodeName);
        }

        public RandomBoolVariable doUseArchComponentName() {
            return m_doUseArchComponentName;
        }

        public void doUseArchComponentName(RandomBoolVariable m_doUseArchComponentName) {
            this.m_doUseArchComponentName = new RandomBoolVariable(m_doUseArchComponentName);
        }

        public RandomIntVariable minWordSize() {
            return m_minWordSize;
        }

        public void minWordSize(RandomIntVariable m_minWordSize) {
            this.m_minWordSize = new RandomIntVariable(m_minWordSize);
        }


        public void setRunDataVariables(ExperimentRunData.IRMapperData a_data, Random a_rand) {
            a_data.m_doStemming = m_doStemming.generate(a_rand);
            a_data.m_doUseCDA = m_doUseCDA.generate(a_rand);
            a_data.m_doUseNodeText = m_doUseNodeText.generate(a_rand);
            a_data.m_doUseNodeName = m_doUseNodeName.generate(a_rand);
            a_data.m_doUseArchComponentName = m_doUseArchComponentName.generate(a_rand);
            a_data.m_minWordSize = m_minWordSize.generate(a_rand);
        }

        public Data(RandomBoolVariable a_doStemming, RandomBoolVariable a_doUseCDA, RandomBoolVariable a_doUseNodeText, RandomBoolVariable a_doUseNodeName, RandomBoolVariable a_doUseArchComponentName, RandomIntVariable a_minWordSize) {
            m_doStemming = new RandomBoolVariable(a_doStemming);
            m_doUseCDA = new RandomBoolVariable(a_doUseCDA);
            m_doUseNodeText = new RandomBoolVariable(a_doUseNodeText);
            m_doUseNodeName = new RandomBoolVariable(a_doUseNodeName);
            m_doUseArchComponentName = new RandomBoolVariable(a_doUseArchComponentName);
            m_minWordSize = new RandomIntVariable(m_minWordSize);
        }
    }

    private Data m_data;
    Data getData() {
        return m_data;
    }

}
