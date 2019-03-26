package se.lnu.siq.s4rdm3x.experiments;

import se.lnu.siq.s4rdm3x.experiments.metric.Metric;
import se.lnu.siq.s4rdm3x.experiments.system.System;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.HuGMe;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.NBMapper;
import se.lnu.siq.s4rdm3x.model.cmd.util.FanInCache;

public class NBMapperExperimentRunner extends ExperimentRunner {


    public NBMapperExperimentRunner(System a_sua, Metric a_metric) {
        super (a_sua, a_metric);
    }

    @Override
    protected boolean runClustering(CGraph a_g, FanInCache fic, ArchDef arch, BasicRunData rd) {
        NBMapper c = new NBMapper(arch);
        long start = java.lang.System.nanoTime();
        c.run(a_g);
        rd.m_time = java.lang.System.nanoTime() - start;

        rd.m_totalManuallyClustered += c.m_manuallyMappedNodes;
        rd.m_totalAutoClustered += c.m_automaticallyMappedNodes;
        rd.m_totalAutoWrong  += c.m_autoWrong;
        rd.m_totalFailedClusterings  += c.m_failedMappings;

        if (c.m_automaticallyMappedNodes + c.m_manuallyMappedNodes == 0) {
            return true;
        }

        rd.m_iterations++;
        return true;
    }
}
