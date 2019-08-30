package se.lnu.siq.s4rdm3x.experiments;

import se.lnu.siq.s4rdm3x.experiments.metric.Metric;
import se.lnu.siq.s4rdm3x.experiments.system.System;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.IRAttractMapper;
import se.lnu.siq.s4rdm3x.model.cmd.util.FanInCache;

import java.util.Random;

public class IRAttractExperimentRunner extends IRExperimentRunnerBase {
    ExperimentRunData.IRMapperData m_exData;

    public IRAttractExperimentRunner(System a_sua, Metric a_metric, boolean a_doUseManualmapping, boolean a_useInitialMapping, RandomDoubleVariable a_initialSetSize, Data a_irData) {
        super(a_sua, a_metric, a_doUseManualmapping, a_useInitialMapping, a_initialSetSize, a_irData);
    }

    public IRAttractExperimentRunner(Iterable<System> a_suas, Iterable<Metric> a_metrics, boolean a_doUseManualmapping, boolean a_useInitialMapping, RandomDoubleVariable a_initialSetSize, Data a_irData) {
        super(a_suas, a_metrics, a_doUseManualmapping, a_useInitialMapping, a_initialSetSize, a_irData);
    }

    @Override
    public ExperimentRunner clone() {
        return new IRAttractExperimentRunner(getSystems(), getMetrics(), doUseManualmapping(), useInitialMapping(), getInitialSetSize(), getData());
    }

    @Override
    protected ExperimentRunData.BasicRunData createNewRunData(Random a_rand) {
        m_exData = new ExperimentRunData.IRMapperData();
        getData().setRunDataVariables(m_exData, a_rand);
        return m_exData;
    }

    @Override
    protected boolean runClustering(CGraph a_g, FanInCache fic, ArchDef a_arch) {

        IRAttractMapper iram = new IRAttractMapper(a_arch, m_doUseManualmapping, m_exData.m_doUseCDA, m_exData.m_doUseNodeText, m_exData.m_doUseNodeName, m_exData.m_doUseArchComponentName, m_exData.m_minWordSize);

        iram.run(a_g);

        m_exData.m_totalManuallyClustered += iram.m_manuallyMappedNodes;
        m_exData.m_totalAutoWrong  += iram.m_autoWrong;
        m_exData.m_totalFailedClusterings  += iram.m_failedMappings;

        if (iram.m_automaticallyMappedNodes + iram.m_manuallyMappedNodes == 0) {
            return true;
        }

        m_exData.m_iterations++;
        return false;
    }
}
