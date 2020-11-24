package experimenting;

import glm_.vec4.Vec4;
import gui.ImGuiWrapper;
import gui.JavaProperty;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.experiments.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static experimenting.ExperimentRunnerViewThread.*;

/**
 * Encapsulates the settings of one mapper this includes specific mapper parameters and generic ones (as the color to use for the mapper)
 */
public class MapperView {
    // ir experiment parameters
    IRExperimentRunBase.Data m_irData = new IRExperimentRunBase.Data();

    // nbmapper experiment parameters
    ExperimentRunner.RandomBoolVariable m_doWordCount = new ExperimentRunner.RandomBoolVariable();
    ExperimentRunner.RandomDoubleVariable m_threshold = new ExperimentRunner.RandomDoubleVariable(0.9, 0);

    // hugme experiment parameters
    ExperimentRunner.RandomDoubleVariable m_omega = new ExperimentRunner.RandomDoubleVariable(0.5, 0.5);
    ExperimentRunner.RandomDoubleVariable m_phi = new ExperimentRunner.RandomDoubleVariable(0.5, 0.5);
    boolean m_doUseDependencyWeights = false;
    Map<dmDependency.Type, ExperimentRunner.RandomDoubleVariable> m_dependencyWeights = new HashMap<>();

    private int m_experimentIx;
    private String m_name = "";
    private Vec4 m_currentColor = new Vec4(0.75, 0.75, 0.75, 1);

    static int g_id = 0;
    int m_id;
    private boolean m_useManualmapping = false;

    ExperimentRun m_experimentRun = null;

    MapperView() {
        m_id = g_id; g_id++;
    }

    MapperView(ExperimentRun a_ex) {
        m_id = g_id; g_id++;
        setExperiment(a_ex);
    }

    MapperView(MapperView a_toBeCopied) {
        m_id = g_id; g_id++;
        m_irData = new IRExperimentRunBase.Data(a_toBeCopied.m_irData);
        m_doWordCount = new ExperimentRunner.RandomBoolVariable(a_toBeCopied.m_doWordCount);
        m_threshold = new ExperimentRunner.RandomDoubleVariable(a_toBeCopied.m_threshold);
        m_omega = new ExperimentRunner.RandomDoubleVariable(a_toBeCopied.m_omega);
        m_phi = new ExperimentRunner.RandomDoubleVariable(a_toBeCopied.m_phi);
        m_currentColor = new Vec4(a_toBeCopied.m_currentColor);
        m_name = new String(a_toBeCopied.m_name);
        m_experimentIx = a_toBeCopied.m_experimentIx;
        m_useManualmapping = a_toBeCopied.m_useManualmapping;
        m_doUseDependencyWeights = a_toBeCopied.m_doUseDependencyWeights;
        for (dmDependency.Type dt : a_toBeCopied.m_dependencyWeights.keySet()) {
            m_dependencyWeights.put(dt, new ExperimentRunner.RandomDoubleVariable(a_toBeCopied.m_dependencyWeights.get(dt)));
        }
    }

    /**
     * @param a_exr A new ExperimentRun the parameters of the MapperView should be set to match this new experiment.
     */
    void setExperiment(ExperimentRun a_exr) {

        m_experimentRun = a_exr;
        // generic experiment runners
        if (a_exr instanceof IRExperimentRunBase) {
            IRExperimentRunBase irexr = (IRExperimentRunBase)a_exr;
            m_irData = irexr.getIRDataClone();
        }

        // specific experiment runners
        if (a_exr instanceof NBMapperExperimentRun) {
            NBMapperExperimentRun nbexr = (NBMapperExperimentRun)a_exr;
            m_threshold = nbexr.getThreshold();
            m_doWordCount = nbexr.getWordCount();
            m_experimentIx = g_nbmapper_ex;
        } else if (a_exr instanceof HuGMeExperimentRun) {
            HuGMeExperimentRun hugme = (HuGMeExperimentRun)a_exr;
            m_omega = hugme.getOmega();
            m_phi = hugme.getPhi();
            m_dependencyWeights = new HashMap<>();
            for (dmDependency.Type dt : hugme.getDependencyWeights().keySet()) {
                m_dependencyWeights.put(dt, new ExperimentRunner.RandomDoubleVariable(hugme.getDependencyWeights().get(dt)));
                m_doUseDependencyWeights = true;
            }
            m_experimentIx = g_hugmemapper_ex;
        } else if (a_exr instanceof IRAttractExperimentRun) {
            m_experimentIx = g_irattract_ex;
        } else if (a_exr instanceof LSIAttractExperimentRun) {
            m_experimentIx = g_lsiattract_ex;
        }

        m_name = a_exr.getName();
        if (m_name == null) {
            m_name = "";
        }

        m_useManualmapping = a_exr.doUseManualMapping();
    }

    /**
     * Called every frame when it is time to visualize the experiment. Depending on if an experiment is set different parameter settings need to be provided.
     * @param a_imgui Visualization wrapper class.
     * @param a_isRunning   True if the experiment is running, i.e. the user should be able so see parameters but not change them.
     * @return an action telling if the user wants to do something to the experiment, e.g. delete it.
     */
    public ExperimentRunnerViewThread.DoExperimentAction doExperiment(ImGuiWrapper a_imgui, boolean a_isRunning) {
        DoExperimentAction ret = DoExperimentAction.None;
        if (a_imgui.imgui().collapsingHeader("Mapper: " + m_name + "###Header" + m_id, 0)) {
            //Vec2 size = new Vec2(a_imgui.imgui().getContentRegionAvailWidth(), a_imgui.getTextLineHeightWithSpacing() * 2 + a_imgui.imgui().getContentRegionAvailWidth() / 3);

            if (a_isRunning) {
                a_imgui.pushDisableWidgets();
            }

            m_name = a_imgui.inputTextSingleLine("Name###Name" + m_id, m_name);
            {
                boolean[] manualMappnig = {m_useManualmapping};
                if (a_imgui.imgui().checkbox("Use Manual Mapping##" + m_id, manualMappnig)) {
                    m_useManualmapping = manualMappnig[0];
                }
            }
            a_imgui.imgui().colorEdit3("Plot Color##" + m_id, m_currentColor, 0);

            {
                String[] experiments = {"Naive Bayes Mapping", "HuGMe", "IRAttract", "LSIAttract"};
                int[] exIx = {m_experimentIx};
                if (a_imgui.imgui().combo("Experiment Type" + "##" + m_id, exIx, Arrays.asList(experiments), experiments.length)) {
                    m_experimentIx = exIx[0];
                }
            }

            a_imgui.imgui().indent(3);
            {
                if (m_experimentIx == g_nbmapper_ex || m_experimentIx == g_irattract_ex || m_experimentIx == g_lsiattract_ex) {

                    m_irData.doStemming(doRandomBoolVariable(a_imgui, "Use Stemming", m_irData.doStemming()));
                    m_irData.doUseCDA(doRandomBoolVariable(a_imgui, "Use CDA", m_irData.doUseCDA()));
                    m_irData.doUseNodeText(doRandomBoolVariable(a_imgui, "Use Code Text", m_irData.doUseNodeText()));
                    m_irData.doUseNodeName(doRandomBoolVariable(a_imgui, "Use Code Name", m_irData.doUseNodeName()));
                    m_irData.doUseArchComponentName(doRandomBoolVariable(a_imgui, "Use Architecture Name", m_irData.doUseArchComponentName()));
                    m_irData.minWordSize(doRandomIntVariable(a_imgui, "Min Word Length", m_irData.minWordSize()));

                    if (m_experimentIx == g_nbmapper_ex) {
                        m_doWordCount = doRandomBoolVariable(a_imgui, "Use Word Counts", m_doWordCount);
                        m_threshold = doRandomDoubleVariable(a_imgui, "Threshold", m_threshold, 0, 1);
                    }
                } else if (m_experimentIx == g_hugmemapper_ex) {
                    m_omega = doRandomDoubleVariable(a_imgui, "Omega Threshold", m_omega);
                    m_phi = doRandomDoubleVariable(a_imgui, "Phi", m_phi);

                    {
                        boolean[] useDependencyWeights = {m_doUseDependencyWeights};
                        if (a_imgui.imgui().checkbox("Use Dependency Weights##" + m_id, useDependencyWeights)) {
                            m_doUseDependencyWeights = useDependencyWeights[0];
                        }
                    }

                    if (m_doUseDependencyWeights) {
                        for (dmDependency.Type dt : dmDependency.Type.values()) {
                            ExperimentRunner.RandomDoubleVariable weight;
                            if (m_dependencyWeights.containsKey(dt)) {
                                weight = m_dependencyWeights.get(dt);
                            } else {
                                weight = new ExperimentRunner.RandomDoubleVariable(1.0, 0.0);
                                m_dependencyWeights.put(dt, weight);
                            }

                            m_dependencyWeights.replace(dt, doRandomDoubleVariable(a_imgui, dt.toString(), weight));
                        }
                    }


                } else if (m_experimentIx == g_irattract_ex) {
                    // add parameters here
                }
            }
            a_imgui.imgui().indent(-3);

            if (a_imgui.button("Copy Mapper##" + m_id, 0)) {
                ret = ExperimentRunnerViewThread.DoExperimentAction.Copy;
            }
            a_imgui.imgui().sameLine(0, 0);
            if (a_imgui.button("Delete Mapper##" + m_id, 0)) {
                ret = ExperimentRunnerViewThread.DoExperimentAction.Delete;
            }

            if (a_isRunning) {
                a_imgui.popDisableWidgets();
            }
        }

        return ret;
    }


    private ExperimentRunner.RandomBoolVariable doRandomBoolVariable(ImGuiWrapper a_imgui, String a_label, ExperimentRunner.RandomBoolVariable a_var) {
        String [] randomBoolLabels = {"Yes", "No", "Random"};

        int[] currentItem = {a_var.isRandom() ? 2 : a_var.getValue() ? 0 : 1};
        if (a_imgui.imgui().combo(a_label + "##" + m_id, currentItem, Arrays.asList(randomBoolLabels), 3)) {
            switch (currentItem[0]) {
                case 0:
                    a_var = new ExperimentRunner.RandomBoolVariable(true);
                    break;
                case 1:
                    a_var = new ExperimentRunner.RandomBoolVariable(false);
                    break;
                case 2:
                    a_var = new ExperimentRunner.RandomBoolVariable();
                    break;
                default:
                    System.out.println("Unhandled Case in Switch: " + currentItem[0]);
                    assert (false);
                    break;
            }
        }

        return a_var;
    }

    private ExperimentRunner.RandomDoubleVariable doRandomDoubleVariable(ImGuiWrapper a_imgui, String a_label, ExperimentRunner.RandomDoubleVariable a_threshold, float a_min, float a_max) {
        Float[] minArray = new Float[1]; minArray[0] = (float)a_threshold.getMin();
        Float[] maxArray = new Float[1]; maxArray[0] = (float)a_threshold.getMax();

        if (a_imgui.imgui().dragFloatRange2(a_label+"##"+m_id, new JavaProperty<>(minArray), new JavaProperty<>(maxArray), 0.01f, a_min, a_max, "%.2f", "%.2f", 1)) {
            double scale = (maxArray[0] - minArray[0]) / 2.0;
            a_threshold = new ExperimentRunner.RandomDoubleVariable(minArray[0] + scale, scale);
        }
        return a_threshold;
    }


    private ExperimentRunner.RandomDoubleVariable doRandomDoubleVariable(ImGuiWrapper a_imgui, String a_label, ExperimentRunner.RandomDoubleVariable a_threshold) {
        Float[] minArray = new Float[1]; minArray[0] = (float)a_threshold.getMin();
        Float[] maxArray = new Float[1]; maxArray[0] = (float)a_threshold.getMax();

        if (a_imgui.imgui().dragFloatRange2(a_label+"##"+m_id, new JavaProperty<>(minArray), new JavaProperty<>(maxArray), 0.01f, 0f, 1f, "%.2f", "%.2f", 1)) {
            double scale = (maxArray[0] - minArray[0]) / 2.0;
            a_threshold = new ExperimentRunner.RandomDoubleVariable(minArray[0] + scale, scale);
        }
        return a_threshold;
    }

    private ExperimentRunner.RandomIntVariable doRandomIntVariable(ImGuiWrapper a_imgui, String a_label, ExperimentRunner.RandomIntVariable a_threshold) {
        Integer[] minArray = new Integer[1]; minArray[0] = a_threshold.getMin();
        Integer[] maxArray = new Integer[1]; maxArray[0] = a_threshold.getMax();

        if (a_imgui.imgui().dragIntRange2(a_label+"##"+m_id, new JavaProperty<>(minArray), new JavaProperty<>(maxArray), 1.0f, 0, 255, "min:%d", "max:%d")) {
            a_threshold = new ExperimentRunner.RandomIntVariable(minArray[0], maxArray[0]);
        }
        return a_threshold;
    }

    /**
     * Creates a new experiment based on the parameters provided.
     * @return the newly created experiment.
     */
    public ExperimentRun createExperiment() {
        m_experimentRun = null;

        if (m_experimentIx == g_nbmapper_ex) {
            m_experimentRun = new NBMapperExperimentRun(m_useManualmapping, m_irData, m_doWordCount, m_threshold);
        } else if (m_experimentIx == g_hugmemapper_ex) {
            if (m_doUseDependencyWeights) {
                m_experimentRun = new HuGMeExperimentRun(m_useManualmapping, m_omega, m_phi, m_dependencyWeights);
            } else {
                m_experimentRun = new HuGMeExperimentRun(m_useManualmapping, m_omega, m_phi);
            }
        } else if (m_experimentIx == g_irattract_ex) {
            m_experimentRun = new IRAttractExperimentRun(m_useManualmapping, m_irData);
        } else if ( m_experimentIx == g_lsiattract_ex) {
            m_experimentRun = new LSIAttractExperimentRun(m_useManualmapping, m_irData);
        }

        m_experimentRun.setName(getName());

        return m_experimentRun;
    }

    public Vec4 getColor() {
        return m_currentColor;
    }

    public ExperimentRun getExperimentRun() {
        return m_experimentRun;
    }

    public String getName() {
        return m_name;
    }
}
