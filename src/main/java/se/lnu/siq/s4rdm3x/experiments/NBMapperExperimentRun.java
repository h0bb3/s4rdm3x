package se.lnu.siq.s4rdm3x.experiments;

import se.lnu.siq.s4rdm3x.experiments.ExperimentRunner.RandomBoolVariable;
import se.lnu.siq.s4rdm3x.experiments.ExperimentRunner.RandomDoubleVariable;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.NBMapper;
import se.lnu.siq.s4rdm3x.model.cmd.util.FanInCache;

import java.util.Random;

public class NBMapperExperimentRun extends IRExperimentRunBase {

    private ExperimentRunData.NBMapperData m_exData;

    RandomBoolVariable m_doWordCount;
    ExperimentRunner.RandomDoubleVariable m_threshold;


    public NBMapperExperimentRun(boolean a_doUseManualMapping, IRExperimentRunBase.Data a_irData, RandomBoolVariable a_doWordCount, RandomDoubleVariable a_threshold) {
        super (a_doUseManualMapping, a_irData);
        m_doWordCount = new RandomBoolVariable(a_doWordCount);
        m_threshold = new RandomDoubleVariable(a_threshold);
    }

    @Override
    public ExperimentRunData.BasicRunData createNewRunData(Random a_rand) {
        m_exData = new ExperimentRunData.NBMapperData();
        m_exData.m_threshold = m_threshold.generate(a_rand);
        getData().setRunDataVariables(m_exData, a_rand);
        m_exData.m_doWordCount = m_doWordCount.generate(a_rand);
        return m_exData;
    }

    @Override
    public boolean runClustering(CGraph a_g, ArchDef arch) {
        NBMapper c = new NBMapper(arch, doUseManualMapping(), m_exData.m_doUseCDA, m_exData.m_doUseNodeText, m_exData.m_doUseNodeName, m_exData.m_doUseArchComponentName, m_exData.m_minWordSize, null);
        c.setClusteringThreshold(m_exData.m_threshold);
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
        return true;
    }

    @Override
    public ExperimentRun clone() {
        return new NBMapperExperimentRun(doUseManualMapping(), getData(), m_doWordCount, m_threshold);
    }

    public RandomDoubleVariable getThreshold() {
        return m_threshold;
    }

    public RandomBoolVariable getWordCount() {
        return m_doWordCount;
    }
}
