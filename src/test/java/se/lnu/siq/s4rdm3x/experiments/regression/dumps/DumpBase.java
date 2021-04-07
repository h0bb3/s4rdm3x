package se.lnu.siq.s4rdm3x.experiments.regression.dumps;

import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.dmodel.dmFile;
import se.lnu.siq.s4rdm3x.experiments.ExperimentRunner;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;

import java.util.ArrayList;
import java.util.HashMap;

public class DumpBase {

    public static class Params {
        public double m_f1 = -1;
        public boolean m_doManualMapping;
    }

    public static class HuGMeParams extends Params {
        public double m_omega, m_phi;

        public double m_weights[] = new double[dmDependency.Type.values().length];
    }

    public static class IRParams extends Params {
        public boolean m_doStemming;
        public boolean m_doUseCDA;
        public boolean m_doUseNodeText;
        public boolean m_doUseNodeName;
        public boolean m_doUseArchComponentName;
        public int m_minWordSize;
    }

    public static class NBParams extends IRParams {
        public boolean m_doWordCount;
        public double m_threshold;
    }

    public CGraph m_g = new CGraph();
    public ArchDef m_a = new ArchDef();
    public HashMap<String, dmClass> m_classes = new HashMap<>();
    public dmFile.dmDirectory m_root = new dmFile.dmDirectory("root", null);

    protected dmClass createClass(final String a_name, CNode a_parentNode) {
        dmClass ret =  new dmClass(a_name, m_root.createFile(dmClass.toJavaSourceFile(a_name)));
        a_parentNode.addClass(ret);
        m_classes.put(a_name, ret);
        return ret;
    }

    public void d(dmClass a_source, dmClass a_target, dmDependency.Type a_dt, int[] a_lines) {
        for (int i : a_lines) {
            a_source.addDependency(a_target, a_dt, i);
        }
    }
    public void d(dmClass a_source, dmClass a_target, dmDependency.Type a_dt, int a_line) {
        a_source.addDependency(a_target, a_dt, a_line);
    }

    public HuGMeParams generateHugMeParams() {
        java.util.Random r = new java.util.Random();
        HuGMeParams ret = new HuGMeParams();
        ret.m_doManualMapping = false;
        ret.m_omega = r.nextDouble();
        ret.m_phi = r.nextDouble();
        for (int i = 0; i < ret.m_weights.length; i++) {
            ret.m_weights[i] = r.nextDouble();
        }

        return ret;
    }

    public IRParams generateIRParams() {
        java.util.Random r = new java.util.Random();
        IRParams ret = new IRParams();
        ret.m_doManualMapping = false;

        ret.m_doStemming = true;    // irmapper needs this or it will often map nothing
        ret.m_doUseCDA = r.nextBoolean();
        ret.m_doUseNodeText = true; // LSI and IRmappers need this or they will most likely not map anything
        ret.m_doUseNodeName = true;    // irmapper needs this or it will often map nothing
        ret.m_doUseArchComponentName = true;    // irmapper needs this or it will often map nothing
        ret.m_minWordSize = r.nextInt(4) + 1;
        return ret;
    }

    public NBParams generateNBParams() {
        java.util.Random r = new java.util.Random();
        NBParams ret = new NBParams();
        ret.m_doManualMapping = false;

        ret.m_doStemming = r.nextBoolean();
        ret.m_doUseCDA = r.nextBoolean();
        ret.m_doUseNodeText = r.nextBoolean();
        ret.m_doUseNodeName = r.nextBoolean();
        ret.m_doUseArchComponentName = r.nextBoolean();
        ret.m_minWordSize = r.nextInt(4) + 1;
        ret.m_threshold = 0.8 + r.nextDouble() * 0.19;
        ret.m_doWordCount = r.nextBoolean();

        return ret;
    }

    public HuGMeParams getHuGMeParams(int a_index) {
        return null;
    }

    public NBParams getNBParams(int a_index) {
        return null;
    }

    public IRParams getIRParams(int a_index) {
        return null;
    }

    public IRParams getLSIParams(int a_index) {
        return null;
    }
}
