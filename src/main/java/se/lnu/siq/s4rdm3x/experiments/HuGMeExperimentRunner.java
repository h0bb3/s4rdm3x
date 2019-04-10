package se.lnu.siq.s4rdm3x.experiments;

import se.lnu.siq.s4rdm3x.experiments.ExperimentRunData;
import se.lnu.siq.s4rdm3x.experiments.ExperimentRunner;
import se.lnu.siq.s4rdm3x.experiments.metric.Metric;
import se.lnu.siq.s4rdm3x.experiments.system.System;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.HuGMe;
import se.lnu.siq.s4rdm3x.model.cmd.util.FanInCache;

import java.util.Random;

public class HuGMeExperimentRunner extends ExperimentRunner {

    private ExperimentRunData.HuGMEData m_exData;

    public HuGMeExperimentRunner(System a_sua, Metric a_metric) {
        super (a_sua, a_metric);
    }

    @Override
    protected ExperimentRunData.BasicRunData createNewRunData(Random m_rand) {
        m_exData = new ExperimentRunData.HuGMEData();
        m_exData.m_phi = m_rand.nextDouble();
        m_exData.m_omega = m_rand.nextDouble();
        return m_exData;
    }


    @Override
    protected boolean runClustering(CGraph a_g, FanInCache fic, ArchDef arch) {

        HuGMe c = new HuGMe(m_exData.m_omega, m_exData.m_phi, true, arch, fic);
        long start = java.lang.System.nanoTime();
        c.run(a_g);
        m_exData.m_time = java.lang.System.nanoTime() - start;

        m_exData.m_totalManuallyClustered += c.m_manuallyMappedNodes;
        m_exData.m_totalAutoClustered += c.m_automaticallyMappedNodes;
        m_exData.m_totalAutoWrong  += c.m_autoWrong;
        m_exData.m_totalFailedClusterings  += c.m_failedMappings;

        if (c.m_automaticallyMappedNodes + c.m_manuallyMappedNodes == 0) {
            return true;
        }

        m_exData.m_iterations++;
        return false;
    }
}



