package se.lnu.siq.s4rdm3x.experiments;

import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.util.FanInCache;

import java.util.Random;

/**
 * Encapsulates basic parameters for running a mapper and provides an interface that needs to be fulfilled by subclasses.
 */
public abstract class ExperimentRun {

    private boolean m_doUseManualMapping;
    private String m_name;

    protected ExperimentRun(boolean a_doUseManualMapping) {
        m_doUseManualMapping = a_doUseManualMapping;
    }

    public boolean doUseManualMapping() {
        return m_doUseManualMapping;
    }

    /**
     * Called once for every experiment run. This is where specific mapping parameters are set.
     * @param m_rand Random object to be used when assigning random variables a value
     * @return an initiated instance of run data, this is probably a subclass with mapper specific parameters set
     */
    public abstract ExperimentRunData.BasicRunData createNewRunData(Random m_rand);

    /**
     * Called for every iteration in the mapping process. Should basically instantiate a concrete mapper and run the mapping, then collect the relevant data for mapped nodes etc.
     * @param a_g Graph that contains all nodes of the systems, of particular interest are orphans and mapped nodes.
     * @param arch Describes the architectural modules and their relations
     * @return false as long as there something is mapped.
     */
    public abstract boolean runClustering(CGraph a_g, ArchDef arch);

    public String getName() {
        return m_name;
    }

    public void setName(String a_name) {
        m_name = a_name;
    }

    /**
     * @return a new deep copied instance
     */
    public ExperimentRun clone() {
        ExperimentRun clone = subClone();

        clone.setName(getName());
        clone.m_doUseManualMapping = m_doUseManualMapping;

        return clone;
    }

    /**
     * @return a new copied instance of a concrete ExperimentRun. All member should be deep copied and not referenced.
     */
    protected abstract ExperimentRun subClone();
}
