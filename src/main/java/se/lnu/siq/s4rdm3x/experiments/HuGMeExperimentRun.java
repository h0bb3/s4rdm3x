package se.lnu.siq.s4rdm3x.experiments;

import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.HuGMe;
import se.lnu.siq.s4rdm3x.model.cmd.util.FanInCache;

import java.util.Random;
/**
 * Encapsulates the Count Attract Mapper parameters, and runs an instance of HuGMe. Manages the omega and phi mapping parameters.
 */
public class HuGMeExperimentRun extends ExperimentRun {

    private ExperimentRunData.HuGMEData m_exData;
    private ExperimentRunner.RandomDoubleVariable m_omega;
    private ExperimentRunner.RandomDoubleVariable m_phi;

    public HuGMeExperimentRun(boolean a_doManualMapping, ExperimentRunner.RandomDoubleVariable a_omega, ExperimentRunner.RandomDoubleVariable a_phi) {
        super (a_doManualMapping);
        m_omega = new ExperimentRunner.RandomDoubleVariable(a_omega);
        m_phi = new ExperimentRunner.RandomDoubleVariable(a_phi);
    }

    @Override
    public ExperimentRun subClone() {
        return new HuGMeExperimentRun(doUseManualMapping(), m_omega, m_phi);
    }

    @Override
    public ExperimentRunData.BasicRunData createNewRunData(Random m_rand) {
        m_exData = new ExperimentRunData.HuGMEData();
        m_exData.m_phi = m_phi.generate(m_rand);
        m_exData.m_omega = m_omega.generate(m_rand);
        return m_exData;
    }


    @Override
    public boolean runClustering(CGraph a_g, ArchDef arch) {

        HuGMe c = new HuGMe(m_exData.m_omega, m_exData.m_phi, doUseManualMapping(), arch);

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

    public ExperimentRunner.RandomDoubleVariable getOmega() {
        return m_omega;
    }

    public ExperimentRunner.RandomDoubleVariable getPhi() {
        return m_phi;
    }
}



