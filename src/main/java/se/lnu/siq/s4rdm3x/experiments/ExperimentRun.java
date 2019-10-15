package se.lnu.siq.s4rdm3x.experiments;

import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.util.FanInCache;

import java.util.Random;

public abstract class ExperimentRun {

    private boolean m_doUseManualMapping;
    private String m_name;

    protected ExperimentRun(boolean a_doUseManualMapping) {
        m_doUseManualMapping = a_doUseManualMapping;
    }

    public boolean doUseManualMapping() {
        return m_doUseManualMapping;
    }

    public abstract ExperimentRunData.BasicRunData createNewRunData(Random m_rand);

    public abstract boolean runClustering(CGraph a_g, ArchDef arch);

    public String getName() {
        return m_name;
    }

    public void setName(String a_name) {
        m_name = a_name;
    }

    public ExperimentRun clone() {
        ExperimentRun clone = subClone();

        clone.setName(getName());
        clone.m_doUseManualMapping = m_doUseManualMapping;

        return clone;
    }

    public abstract ExperimentRun subClone();
}
