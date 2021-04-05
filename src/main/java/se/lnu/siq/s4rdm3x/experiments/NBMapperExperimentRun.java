package se.lnu.siq.s4rdm3x.experiments;

import se.lnu.siq.s4rdm3x.experiments.ExperimentRunner.RandomBoolVariable;
import se.lnu.siq.s4rdm3x.experiments.ExperimentRunner.RandomDoubleVariable;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.NBMapper;

import java.util.Random;

/**
 * Encapsulates the Naive Bayes Mapper parameters, and runs an instance of NBMapper. Manages parameters if word frequencies should be used or not and the mapping threshold.
 */
public class NBMapperExperimentRun extends IRExperimentRunBase {

    protected ExperimentRunData.NBMapperData m_exData;

    RandomBoolVariable m_doWordCount;
    ExperimentRunner.RandomDoubleVariable m_threshold;
    double m_thresholdValue;


    public NBMapperExperimentRun(boolean a_doUseManualMapping, IRExperimentRunBase.Data a_irData, RandomBoolVariable a_doWordCount, RandomDoubleVariable a_threshold) {
        super (a_doUseManualMapping, a_irData);
        m_doWordCount = new RandomBoolVariable(a_doWordCount);
        if (a_threshold.getMin() < 0 || a_threshold.getMax() > 1) {
            m_threshold = new RandomDoubleVariable(0.9, 0);
        } else {
            m_threshold = new RandomDoubleVariable(a_threshold);
        }
    }

    @Override
    public ExperimentRunData.BasicRunData createNewRunData(Random a_rand) {
        m_exData = new ExperimentRunData.NBMapperData();
        m_exData.m_threshold = m_thresholdValue = m_threshold.generate(a_rand);
        getData().setRunDataVariables(m_exData, a_rand);
        m_exData.m_doWordCount = m_doWordCount.generate(a_rand);
        return m_exData;
    }

    @Override
    public boolean runClustering(CGraph a_g, ArchDef arch) {
        NBMapper c = new NBMapper(arch, doUseManualMapping(), m_exData.m_doUseCDA, m_exData.m_doUseNodeText, m_exData.m_doUseNodeName, m_exData.m_doUseArchComponentName, m_exData.m_minWordSize, null, m_thresholdValue);
        c.setMappingThreshold(m_thresholdValue);
        c.doStemming(m_exData.m_doStemming);
        c.doWordCount(m_exData.m_doWordCount);
        c.run(a_g);

        m_exData.m_totalManuallyClustered += c.m_manuallyMappedNodes;
        m_exData.m_totalAutoWrong  += c.m_autoWrong;
        m_exData.m_totalFailedClusterings  += c.m_failedMappings;

        c.getAutoClusteredNodes().forEach(n -> m_exData.addAutoClusteredNode(n));

        if (c.getAutoClusteredOrphanCount() + c.m_manuallyMappedNodes == 0) {
            return true;
        }

        m_exData.m_iterations++;
        return false;
    }

    @Override
    protected ExperimentRun subClone() {
        return new NBMapperExperimentRun(doUseManualMapping(), getData(), m_doWordCount, m_threshold);
    }

    public RandomDoubleVariable getThreshold() {
        return m_threshold;
    }

    public RandomBoolVariable getWordCount() {
        return m_doWordCount;
    }
}
