package se.lnu.siq.s4rdm3x.experiments;

import se.lnu.siq.s4rdm3x.experiments.ExperimentRunData;
import se.lnu.siq.s4rdm3x.experiments.ExperimentRunner;
import se.lnu.siq.s4rdm3x.experiments.metric.Metric;
import se.lnu.siq.s4rdm3x.experiments.system.System;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.HuGMe;
import se.lnu.siq.s4rdm3x.model.cmd.util.FanInCache;

import java.util.ArrayList;
import java.util.Random;

public class HuGMeExperimentRunner extends ExperimentRunner {

    private ExperimentRunData.HuGMEData m_exData;
    private RandomDoubleVariable m_omega;
    private RandomDoubleVariable m_phi;

    public HuGMeExperimentRunner(System a_sua, Metric a_metric, boolean a_doManualMapping, RandomDoubleVariable a_initialSetSize, RandomDoubleVariable a_omega, RandomDoubleVariable a_phi) {
        super (a_sua, a_metric, a_doManualMapping, a_initialSetSize);
        m_omega = new RandomDoubleVariable(a_omega);
        m_phi = new RandomDoubleVariable(a_phi);
    }

    public HuGMeExperimentRunner(Iterable<System> a_suas, Iterable<Metric> a_metrics, boolean a_doManualMapping, RandomDoubleVariable a_initialSetSize, RandomDoubleVariable a_omega, RandomDoubleVariable a_phi) {
        super (a_suas, a_metrics, a_doManualMapping, a_initialSetSize);
        m_omega = new RandomDoubleVariable(a_omega);
        m_phi = new RandomDoubleVariable(a_phi);
    }

    @Override
    protected ExperimentRunData.BasicRunData createNewRunData(Random m_rand) {
        m_exData = new ExperimentRunData.HuGMEData();
        m_exData.m_phi = m_phi.generate(m_rand);
        m_exData.m_omega = m_omega.generate(m_rand);
        return m_exData;
    }


    @Override
    protected boolean runClustering(CGraph a_g, FanInCache fic, ArchDef arch) {

        HuGMe c = new HuGMe(m_exData.m_omega, m_exData.m_phi, m_doUseManualmapping, arch, fic);

        c.run(a_g);


        m_exData.m_totalManuallyClustered += c.m_manuallyMappedNodes;
        m_exData.m_totalAutoWrong  += c.m_autoWrong;
        m_exData.m_totalFailedClusterings  += c.m_failedMappings;

        if (c.m_automaticallyMappedNodes + c.m_manuallyMappedNodes == 0) {
            return true;
        }

        m_exData.m_iterations++;
        return false;
    }

    public RandomDoubleVariable getOmega() {
        return m_omega;
    }

    public RandomDoubleVariable getPhi() {
        return m_phi;
    }
}



