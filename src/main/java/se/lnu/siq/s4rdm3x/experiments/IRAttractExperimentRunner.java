package se.lnu.siq.s4rdm3x.experiments;

import se.lnu.siq.s4rdm3x.experiments.metric.Metric;
import se.lnu.siq.s4rdm3x.experiments.system.System;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.IRAttractMapper;
import se.lnu.siq.s4rdm3x.model.cmd.util.FanInCache;

import java.util.Random;

public class IRAttractExperimentRunner extends ExperimentRunner {
    ExperimentRunData.BasicRunData m_exData;

    public IRAttractExperimentRunner(System a_sua, Metric a_metric, boolean a_doUseManualmapping, RandomDoubleVariable a_initialSetSize) {
        super(a_sua, a_metric, a_doUseManualmapping, a_initialSetSize);
    }

    public IRAttractExperimentRunner(Iterable<System> a_suas, Iterable<Metric> a_metrics, boolean a_doUseManualmapping, RandomDoubleVariable a_initialSetSize) {
        super(a_suas, a_metrics, a_doUseManualmapping, a_initialSetSize);
    }

    @Override
    public ExperimentRunner clone() {
        return new IRAttractExperimentRunner(getSystems(), getMetrics(), doUseManualmapping(), getInitialSetSize());
    }

    @Override
    protected ExperimentRunData.BasicRunData createNewRunData(Random m_rand) {
        m_exData = new ExperimentRunData.BasicRunData();
        return m_exData;
    }

    @Override
    protected boolean runClustering(CGraph a_g, FanInCache fic, ArchDef a_arch) {

        IRAttractMapper iram = new IRAttractMapper(a_arch, m_doUseManualmapping);

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
