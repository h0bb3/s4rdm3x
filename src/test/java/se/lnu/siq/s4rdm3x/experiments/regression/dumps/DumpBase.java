package se.lnu.siq.s4rdm3x.experiments.regression.dumps;

import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;

import java.util.ArrayList;
import java.util.HashMap;

public class DumpBase {

    public static class HuGMeParams {
        public double m_omega, m_phi;
        public boolean m_doManualMapping;
        public double m_weights[] = new double[dmDependency.Type.values().length];
        public double m_f1 = -1;
    }

    public CGraph m_g = new CGraph();
    public ArchDef m_a = new ArchDef();
    public HashMap<String, dmClass> m_classes = new HashMap<>();
    public ArrayList<Double> m_scores = new ArrayList<>();

    public void d(dmClass a_source, dmClass a_target, dmDependency.Type a_dt, int[] a_lines) {
        for (int i : a_lines) {
            a_source.addDependency(a_target, a_dt, i);
        }
    }
    public void d(dmClass a_source, dmClass a_target, dmDependency.Type a_dt, int a_line) {
        a_source.addDependency(a_target, a_dt, a_line);
    }

    public double getF1Score(int i) {
        return m_scores.get(i);
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

    public HuGMeParams getHuGMeParams(int a_index) {
        return null;
    }
}
