package se.lnu.siq.s4rdm3x.experiments;

import se.lnu.siq.s4rdm3x.experiments.metric.Metric;
import se.lnu.siq.s4rdm3x.experiments.system.System;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.HuGMe;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.NBMapper;
import se.lnu.siq.s4rdm3x.model.cmd.util.FanInCache;

import java.util.ArrayList;
import java.util.Random;

public class NBMapperExperimentRunner extends ExperimentRunner {

    private ExperimentRunData.NBMapperData m_exData;

    RandomBoolVariable m_doStemming;
    RandomBoolVariable m_doWordCount;
    RandomDoubleVariable m_threshold;


    public NBMapperExperimentRunner(System a_sua, Metric a_metric, RandomDoubleVariable a_initialSetSize) {
        super (a_sua, a_metric, false, a_initialSetSize);
        m_doStemming = new RandomBoolVariable(false);
        m_doWordCount = new RandomBoolVariable(false);
        m_threshold = new RandomDoubleVariable(0.9, 0);
    }

    public NBMapperExperimentRunner(System a_sua, Metric a_metric, boolean a_doUseManualMapping, RandomDoubleVariable a_initialSetSize, RandomBoolVariable a_doStemming, RandomBoolVariable a_doWordCount, RandomDoubleVariable a_threshold) {
        super (a_sua, a_metric, a_doUseManualMapping, a_initialSetSize);
        m_doStemming = new RandomBoolVariable(a_doStemming);
        m_doWordCount = new RandomBoolVariable(a_doWordCount);
        m_threshold = new RandomDoubleVariable(a_threshold);
    }

    public NBMapperExperimentRunner(Iterable<System> a_suas, Iterable<Metric> a_metrics, boolean a_doUseManualMapping, RandomDoubleVariable a_initialSetSize, RandomBoolVariable a_doStemming, RandomBoolVariable a_doWordCount, RandomDoubleVariable a_threshold) {
        super (a_suas, a_metrics, a_doUseManualMapping, a_initialSetSize);
        m_doStemming = new RandomBoolVariable(a_doStemming);
        m_doWordCount = new RandomBoolVariable(a_doWordCount);
        m_threshold = new RandomDoubleVariable(a_threshold);
    }

    @Override
    protected ExperimentRunData.BasicRunData createNewRunData(Random a_rand) {
        m_exData = new ExperimentRunData.NBMapperData();
        m_exData.m_threshold = m_threshold.generate(a_rand);
        m_exData.m_doStemming = m_doStemming.generate(a_rand);
        m_exData.m_doWordCount = m_doWordCount.generate(a_rand);
        return m_exData;
    }

    @Override
    protected boolean runClustering(CGraph a_g, FanInCache fic, ArchDef arch) {
        NBMapper c = new NBMapper(arch, m_doUseManualmapping, null);
        c.setClusteringThreshold(m_exData.m_threshold);
        c.doStemming(m_exData.m_doStemming);
        c.doWordCount(m_exData.m_doWordCount);
        c.run(a_g);

        m_exData.m_totalManuallyClustered += c.m_manuallyMappedNodes;
        m_exData.m_totalAutoWrong  += c.m_autoWrong;
        m_exData.m_totalFailedClusterings  += c.m_failedMappings;

        if (c.m_automaticallyMappedNodes + c.m_manuallyMappedNodes == 0) {
            return true;
        }

        m_exData.m_iterations++;
        return true;
    }

    public RandomDoubleVariable getThreshold() {
        return m_threshold;
    }

    public RandomBoolVariable getWordCount() {
        return m_doWordCount;
    }

    public RandomBoolVariable getStemming() {
        return m_doStemming;
    }
}
