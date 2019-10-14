package se.lnu.siq.s4rdm3x.experiments;

import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.IRAttractMapper;
import se.lnu.siq.s4rdm3x.model.cmd.util.FanInCache;

import java.util.Random;

public class IRAttractExperimentRun extends IRExperimentRunBase {
    ExperimentRunData.IRMapperData m_exData;

    public IRAttractExperimentRun(boolean a_doUseManualmapping, Data a_irData) {
        super(a_doUseManualmapping, a_irData);
    }

    @Override
    public ExperimentRun clone() {
        return new IRAttractExperimentRun(doUseManualMapping(), getData());
    }

    @Override
    public ExperimentRunData.BasicRunData createNewRunData(Random a_rand) {
        m_exData = new ExperimentRunData.IRMapperData();
        getData().setRunDataVariables(m_exData, a_rand);
        return m_exData;
    }

    @Override
    public boolean runClustering(CGraph a_g, ArchDef a_arch) {

        IRAttractMapper iram = new IRAttractMapper(a_arch, doUseManualMapping(), m_exData.m_doUseCDA, m_exData.m_doUseNodeText, m_exData.m_doUseNodeName, m_exData.m_doUseArchComponentName, m_exData.m_minWordSize);

        iram.run(a_g);

        m_exData.m_totalManuallyClustered += iram.m_manuallyMappedNodes;
        m_exData.m_totalAutoWrong  += iram.m_autoWrong;
        m_exData.m_totalFailedClusterings  += iram.m_failedMappings;

        iram.getAutoClusteredNodes().forEach(n -> m_exData.addAutoClusteredNode(n));

        if (iram.getAutoClusteredOrphanCount() + iram.m_manuallyMappedNodes == 0) {
            return true;
        }

        m_exData.m_iterations++;
        return false;
    }
}
