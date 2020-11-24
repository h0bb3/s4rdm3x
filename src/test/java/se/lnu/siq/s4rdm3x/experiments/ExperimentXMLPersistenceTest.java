package se.lnu.siq.s4rdm3x.experiments;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.experiments.metric.Metric;
import se.lnu.siq.s4rdm3x.experiments.system.System;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;



public class ExperimentXMLPersistenceTest {

    public static String getClassMethodLine(int a_callStackOffset) {

        String fullClassName = Thread.currentThread().getStackTrace()[a_callStackOffset].getClassName();
        String fullFileName = Thread.currentThread().getStackTrace()[a_callStackOffset].getFileName();
        String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
        String methodName = Thread.currentThread().getStackTrace()[a_callStackOffset].getMethodName();
        int lineNumber = Thread.currentThread().getStackTrace()[a_callStackOffset].getLineNumber();

        return fullClassName + "." + methodName + "(" + fullFileName + ":" + lineNumber +")";
    }

    @Test
    public void testPersistence_HuGMeExperiment() {
        ArrayList<System> systems = new ArrayList<>();
        ArrayList<Metric> metrics = new ArrayList<>();
        ExperimentRunner.RandomDoubleVariable initialSetSize = new ExperimentRunner.RandomDoubleVariable(0.5);

        ArrayList<ExperimentRun> experiments = new ArrayList<>();
        Map<dmDependency.Type, ExperimentRunner.RandomDoubleVariable> dw = new HashMap<>();
        dw.put(dmDependency.Type.Returns, new ExperimentRunner.RandomDoubleVariable(0.17, 0.2));
        ExperimentRun ex = new HuGMeExperimentRun(false, new ExperimentRunner.RandomDoubleVariable(0.3), new ExperimentRunner.RandomDoubleVariable(0.1), dw);
        experiments.add(ex);
        ex.setName("testName");

        ExperimentRunner r = new ExperimentRunner(systems, metrics, experiments, false, initialSetSize, false);
        ArrayList<ExperimentRunner> runners = new ArrayList<>();
        runners.add(r);

        ExperimentXMLPersistence sua = new ExperimentXMLPersistence();

        try {
            PipedOutputStream out = new PipedOutputStream();
            PipedInputStream in = new PipedInputStream(out);


            sua.saveExperiments(runners, out, null);
            out.close();
            ArrayList<ExperimentRunner> loadedRunners = sua.loadExperimentRunners(in, null);

            assertEquals(loadedRunners.size(), 1);
            assertTrue(equals(runners.get(0), loadedRunners.get(0)));

        } catch (Exception e) {
            e.printStackTrace();
            assert(false);
        }
    }

    private boolean equals(double a_expected, double a_actual) {
        return Math.abs(a_expected - a_actual) < 0.0001;
    }

    private boolean equals(ExperimentRunner.RandomDoubleVariable a_expected, ExperimentRunner.RandomDoubleVariable a_actual) {
        if (!equals(a_expected.getBase(), a_actual.getBase())) {
            logErr("RandomDoubleVariable.getBase() NOT equal -> expected: " + a_expected.getBase() +" actual: " + a_actual.getBase());
            return false;
        }

        if (!equals(a_expected.getScale(), a_actual.getScale())) {
            logErr("RandomDoubleVariable.getScale() NOT equal -> expected: " + a_expected.getScale() +" actual: " + a_actual.getScale());
            return false;
        }

        return true;
    }

    private boolean equals(ExperimentRunner a_expected, ExperimentRunner a_actual) {

        if (a_expected.doUseInitialMapping() != a_actual.doUseInitialMapping()) {
            logErr("doUseInitialMapping() NOT equal -> expected: " + a_expected.doUseInitialMapping() + " actual: " + a_actual.doUseInitialMapping());
            return false;
        }

        if (a_expected.initialSetPerComponent() != a_actual.initialSetPerComponent()) {
            logErr("initialSetPerComponent() NOT equal -> expected: " + a_expected.initialSetPerComponent() + " actual: " + a_actual.initialSetPerComponent());
            return false;
        }

        if (a_expected.getSystemCount() != a_actual.getSystemCount()) {
            logErr("getSystemCount() NOT equal -> expected:" + a_expected.getSystemCount() + " actual:" + a_actual.getSystemCount());
            return false;
        }

        if (!equals(a_expected.getInitialSetSize(), a_actual.getInitialSetSize())) {
            logErr("\tfrom getInitialSetSize()");
            return false;
        }

        if (((Collection<?>)a_expected.getMetrics()).size() != ((Collection<?>)a_actual.getMetrics()).size()) {
            logErr("getMetrics() size NOT equal -> expected:" + ((Collection<?>)a_expected.getMetrics()).size() + " actual:" + ((Collection<?>)a_actual.getMetrics()).size());
            return false;
        }

        Object [] expectedExperiments = (((Collection<ExperimentRun>) a_expected.getExperiments()).toArray());
        Object [] actualExperiments =  (((Collection<ExperimentRun>) a_actual.getExperiments()).toArray());
        if (expectedExperiments.length != actualExperiments.length) {
            logErr("getExperiments() size NOT equal -> expected:" + expectedExperiments.length + " actual:" + actualExperiments.length);
            return false;
        }

        for (int i = 0; i < expectedExperiments.length; i++) {
            if (!equals((ExperimentRun)expectedExperiments[i], (ExperimentRun)actualExperiments[i])) {
                logErr("\t\twhen comparing experiment no: " + i);
                return false;
            }
        }

        return true;
    }

    private boolean equals(ExperimentRun a_expected, ExperimentRun a_actual) {
        if (!a_expected.getClass().equals(a_actual.getClass())) {
            logErr("ExperimentRun.getClass() NOT equal -> expected:" + a_expected.getClass() + " actual:" + a_actual.getClass());
            return false;
        }

        a_expected.getName().equals(a_actual.getName());

        if (a_expected instanceof HuGMeExperimentRun) {
            HuGMeExperimentRun e = (HuGMeExperimentRun)a_expected;
            HuGMeExperimentRun a = (HuGMeExperimentRun)a_actual;

            if (!equals(e.getOmega(), a.getOmega())) {
                logErr("\twhen comparing Omega");
                return false;
            }

            if (!equals(e.getPhi(), a.getPhi())) {
                logErr("\twhen comparing Phi");
                return false;
            }

            if (e.getDependencyWeights().size() != a.getDependencyWeights().size()) {
                logErr("getDependencyWeights().size() NOT equal - expected" + e.getDependencyWeights().size() + " actual:" + a.getDependencyWeights().size());
                return false;
            }

            for (dmDependency.Type dt : e.getDependencyWeights().keySet()) {
                if (!a.getDependencyWeights().containsKey(dt)) {
                    logErr("getDependencyWeights() does not contain expected key:" + dt);
                    return false;
                }

                if (!equals(e.getDependencyWeights().get(dt), a.getDependencyWeights().get(dt))) {
                    logErr("\twhen comparing dependency weights for " + dt);
                    return false;
                }
            }
        }


        return true;
    }

    private void logErr(String a_msg) {
        // count initial tabs
        int tabCount = 0;
        while (tabCount < a_msg.length() && a_msg.charAt(tabCount) == '\t') {
            tabCount++;
        }
        // we always want one tab extra
        tabCount++;

        String tabString = "";
        while (tabCount > 0) {
            tabString +=" \t";
            tabCount--;
        }

        java.lang.System.err.println(a_msg + java.lang.System.lineSeparator() + tabString + "at " + getClassMethodLine(3));
    }
}
