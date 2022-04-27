package se.lnu.siq.s4rdm3x.experiments.regression;

import se.lnu.siq.s4rdm3x.experiments.regression.dumps.*;

import se.lnu.siq.s4rdm3x.experiments.*;
import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.experiments.metric.Rand;
import se.lnu.siq.s4rdm3x.experiments.system.FileBased;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;

import java.io.File;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


class RegressionTests {

    static final int g_HuGMe_test_count = 3;
    static final int g_nb_test_count = 3;
    static final int g_ir_test_count = 3;
    static final int g_lsi_test_count = 3;

    //@Test
    public void testProM_HuGMe() {
        DumpBase db = new ProMDump();
        ArchDef a = db.m_a;
        CGraph g = db.m_g;
        for (int i = 0; i < g_HuGMe_test_count; i++) {
            assertEquals(db.getHuGMeParams(i).m_f1, runHuGMExperiment(g, a, db.getHuGMeParams(i)), "Regression test " + i + " for ProM failed.");
            a.cleanNodeClusters(g.getNodes(), true);
        }
    }

    //@Test
    public void testProM_NB() {
        DumpBase db = new ProMDump();
        ArchDef a = db.m_a;
        CGraph g = db.m_g;
        for (int i = 0; i < g_nb_test_count; i++) {
            assertEquals(db.getNBParams(i).m_f1, runNBExperiment(g, a, db.getNBParams(i)), "Regression test " + i + " for ProM failed.");
            a.cleanNodeClusters(g.getNodes(), true);
        }
    }

    //@Test
    public void testProM_IR() {
        DumpBase db = new ProMDump();
        ArchDef a = db.m_a;
        CGraph g = db.m_g;
        for (int i = 0; i < g_ir_test_count; i++) {
            assertEquals(db.getIRParams(i).m_f1, runIRExperiment(g, a, db.getIRParams(i)), "Regression test " + i + " for ProM failed.");
            a.cleanNodeClusters(g.getNodes(), true);
        }
    }

    //@Test
    public void testProM_LSI() {
        DumpBase db = new ProMDump();
        ArchDef a = db.m_a;
        CGraph g = db.m_g;
        for (int i = 0; i < g_lsi_test_count; i++) {
            assertEquals(db.getLSIParams(i).m_f1, runLSIExperiment(g, a, db.getLSIParams(i)), "Regression test " + i + " for ProM failed.");
            a.cleanNodeClusters(g.getNodes(), true);
        }
    }


    //@Test
    public void testCommonsImg_HuGMe() {
        DumpBase db = new CommonsImagingDump();
        ArchDef a = db.m_a;
        CGraph g = db.m_g;
        for (int i = 0; i < g_HuGMe_test_count; i++) {
            assertEquals(db.getHuGMeParams(i).m_f1, runHuGMExperiment(g, a, db.getHuGMeParams(i)), "Regression test " + i + " for CommonsImaging failed.");
            a.cleanNodeClusters(g.getNodes(), true);
        }
    }

    //@Test
    public void testCommonsImg_NB() {
        DumpBase db = new CommonsImagingDump();
        ArchDef a = db.m_a;
        CGraph g = db.m_g;
        for (int i = 0; i < g_nb_test_count; i++) {
            assertEquals(db.getNBParams(i).m_f1, runNBExperiment(g, a, db.getNBParams(i)), "Regression test " + i + " for CommonsImaging failed.");
            a.cleanNodeClusters(g.getNodes(), true);
        }
    }

    //@Test
    public void testCommonsImg_IR() {
        DumpBase db = new CommonsImagingDump();
        ArchDef a = db.m_a;
        CGraph g = db.m_g;
        for (int i = 0; i < g_nb_test_count; i++) {
            assertEquals(db.getIRParams(i).m_f1, runIRExperiment(g, a, db.getIRParams(i)), "Regression test " + i + " for CommonsImaging failed.");
            a.cleanNodeClusters(g.getNodes(), true);
        }
    }

    //@Test
    public void testCommonsImg_LSI() {
        DumpBase db = new CommonsImagingDump();
        ArchDef a = db.m_a;
        CGraph g = db.m_g;
        for (int i = 0; i < g_nb_test_count; i++) {
            assertEquals(db.getLSIParams(i).m_f1, runLSIExperiment(g, a, db.getLSIParams(i)), "Regression test " + i + " for CommonsImaging failed.");
            a.cleanNodeClusters(g.getNodes(), true);
        }
    }

    //@Test
    public void testTeammates_HuGMe() {
        DumpBase db = new TeammatesDump();
        ArchDef a = db.m_a;
        CGraph g = db.m_g;
        for (int i = 0; i < g_HuGMe_test_count; i++) {
            assertEquals(db.getHuGMeParams(i).m_f1, runHuGMExperiment(g, a, db.getHuGMeParams(i)), "Regression test " + i + " for Teammates failed.");
            a.cleanNodeClusters(g.getNodes(), true);
        }
    }

    //@Test
    public void testTeammates_NB() {
        DumpBase db = new TeammatesDump();
        ArchDef a = db.m_a;
        CGraph g = db.m_g;
        for (int i = 0; i < g_nb_test_count; i++) {
            assertEquals(db.getNBParams(i).m_f1, runNBExperiment(g, a, db.getNBParams(i)),"Regression test " + i + " for Teammates failed.");
            a.cleanNodeClusters(g.getNodes(), true);
        }
    }

    //@Test
    public void testTeammates_IR() {
        DumpBase db = new TeammatesDump();
        ArchDef a = db.m_a;
        CGraph g = db.m_g;
        for (int i = 0; i < g_nb_test_count; i++) {
            assertEquals(db.getIRParams(i).m_f1, runIRExperiment(g, a, db.getIRParams(i)), "Regression test " + i + " for Teammates failed.");
            a.cleanNodeClusters(g.getNodes(), true);
        }
    }

    //@Test
    public void testTeammates_LSI() {
        DumpBase db = new TeammatesDump();
        ArchDef a = db.m_a;
        CGraph g = db.m_g;
        for (int i = 0; i < g_nb_test_count; i++) {
            assertEquals(db.getLSIParams(i).m_f1, runLSIExperiment(g, a, db.getLSIParams(i)), "Regression test " + i + " for Teammates failed.");
            a.cleanNodeClusters(g.getNodes(), true);
        }
    }


    private double runHuGMExperiment(CGraph a_g, ArchDef a_arch, DumpBase.HuGMeParams a_params) {

        Map<dmDependency.Type, ExperimentRunner.RandomDoubleVariable> dw = new HashMap<>();
        for (int i = 0; i < dmDependency.Type.values().length; i++) {
            dw.put(dmDependency.Type.values()[i], new ExperimentRunner.RandomDoubleVariable(a_params.m_weights[i], 0));
        }

        HuGMeExperimentRun exp = new HuGMeExperimentRun(a_params.m_doManualMapping, new ExperimentRunner.RandomDoubleVariable(a_params.m_omega, 0), new ExperimentRunner.RandomDoubleVariable(a_params.m_phi, 0), dw);

        return runExperiment(a_g, a_arch, exp);
    }

    IRExperimentRunBase.Data getIrData(DumpBase.IRParams a_params) {
        IRExperimentRunBase.Data irData = new IRExperimentRunBase.Data();
        irData.doStemming(new ExperimentRunner.RandomBoolVariable(a_params.m_doStemming));
        irData.doUseArchComponentName(new ExperimentRunner.RandomBoolVariable(a_params.m_doUseArchComponentName));
        irData.doUseCDA(new ExperimentRunner.RandomBoolVariable(a_params.m_doUseCDA));
        irData.doUseNodeName(new ExperimentRunner.RandomBoolVariable(a_params.m_doUseNodeName));
        irData.doUseNodeText(new ExperimentRunner.RandomBoolVariable(a_params.m_doUseNodeText));

        return irData;
    }

    private double runNBExperiment(CGraph a_g, ArchDef a_arch, DumpBase.NBParams a_params) {
        IRExperimentRunBase.Data irData = getIrData(a_params);
        NBMapperExperimentRun exp = new NBMapperExperimentRun(a_params.m_doManualMapping, irData, new ExperimentRunner.RandomBoolVariable(a_params.m_doWordCount),  new ExperimentRunner.RandomDoubleVariable(a_params.m_threshold, 0));

        return runExperiment(a_g, a_arch, exp);
    }

    private double runIRExperiment(CGraph a_g, ArchDef a_arch, DumpBase.IRParams a_params) {
        IRExperimentRunBase.Data irData = getIrData(a_params);
        IRAttractExperimentRun exp = new IRAttractExperimentRun(a_params.m_doManualMapping, irData);

        return runExperiment(a_g, a_arch, exp);
    }

    private double runLSIExperiment(CGraph a_g, ArchDef a_arch, DumpBase.IRParams a_params) {
        IRExperimentRunBase.Data irData = getIrData(a_params);
        LSIAttractExperimentRun exp = new LSIAttractExperimentRun(a_params.m_doManualMapping, irData);

        return runExperiment(a_g, a_arch, exp);
    }


    private double runExperiment(CGraph a_g, ArchDef a_arch, ExperimentRun exp) {
        final ExperimentRunData.BasicRunData rd = exp.createNewRunData(new Random());
        rd.m_totalMapped = a_arch.getMappedNodeCount(a_g.getNodes());
        a_arch.getClusteredNodes(a_g.getNodes(), ArchDef.Component.ClusteringType.Initial).forEach(n -> rd.addInitialClusteredNode(n));
        rd.m_initialClusteringPercent = (double) rd.getInitialClusteringNodeCount() / (double) rd.m_totalMapped;
        while (!exp.runClustering(a_g, a_arch));

        if (rd.getAutoClusteredNodeCount() < 1) {
            return 0.0;
        }

        return rd.calcF1Score();
    }


    @Test
    void stringEscape() {
        System2JavaDumper s2jd = new System2JavaDumper();

        // desired expected: [ ]*(\r?\n[ ]*)+[ ]*
        // what should be printed: [ ]*(\\r?\\n[ ]*)+[ ]*
        // what should be deescaped [ ]*(\\\\r?\\\\n[ ]*)+[ ]*

        String expected = "\\f"; // \f
        String actual = s2jd.deEscape("\\\\f");
        java.lang.System.out.println("\\f");
        assertEquals(expected, actual);

        //String expected =  "[ ]*(\\r?\\n[ ]*)+[ ]*";
        //String actual = s2jd.deEscape("[ ]*(\\\\r?\\\\n[ ]*)+[ ]*");
        //assertEquals(expected, actual);    // this sends \\r and \\n to descape

        expected = ".*\\balert alert-danger\\b.*";
        assertEquals(expected, s2jd.deEscape(".*\\\\balert alert-danger\\\\b.*"));
        java.lang.System.out.println(expected);

        assertEquals("Test", s2jd.escape("Test"));
        assertEquals("Test\\n", s2jd.escape("Test\n"));
        assertEquals("\\\"", s2jd.escape("\""));

        assertEquals("Test", s2jd.deEscape("Test"));
        assertEquals("Test\n", s2jd.deEscape("Test\\n"));

        expected = "<\\.>";  // prints <\.> -> escape("<\\.>") prints("<\\.>")

        java.lang.System.out.println(s2jd.escape(expected));
        assertEquals("<\\\\.>", s2jd.escape(expected));
        assertEquals(expected, s2jd.deEscape("<\\\\.>"));

        expected = ".*\\balert alert-danger\\b.*";
        assertEquals(expected, s2jd.deEscape(".*\\\\balert alert-danger\\\\b.*"));
        java.lang.System.out.println(s2jd.escape(expected));
    }

    //@Test // decomment only when you want to generate new dumps in development to add system dumps
    public void dumpSystem() {
        try {
           System2JavaDumper s2jd = new System2JavaDumper();

            //FileBased system = new FileBased("C:/hObbE/projects/coding/github/s4rdm3x/data/systems/ProM6.9/ProM_6_9.sysmdl");
            //String className = "ProMDump";

            //FileBased system = new FileBased("C:/hObbE/projects/coding/github/s4rdm3x/data/systems/teammates/teammates.sysmdl");
            //String className = "TeammatesDump";

            FileBased system = new FileBased("C:/hObbE/projects/coding/github/s4rdm3x/data/systems/commons-imaging/commons-imaging.sysmdl");
            String className = "CommonsImagingDump";

            CGraph g = new CGraph();
            system.load(g);
            ArchDef a = system.createAndMapArch(g);

            Random r = new Random(171717);
            se.lnu.siq.s4rdm3x.experiments.metric.Rand rm = new Rand();
            rm.assignMetric(a.getMappedNodes(g.getNodes()));

            InitialSetGenerator isg = new InitialSetGenerator();
            isg.assignInitialClustersPerComponent(g, a, 0.75, new Rand(), r);

            DumpBase db = new DumpBase();


            DumpBase.HuGMeParams [] hugmeTests = new DumpBase.HuGMeParams[g_HuGMe_test_count];
            for (int i = 0; i < g_HuGMe_test_count; i++) {
                hugmeTests[i] = db.generateHugMeParams();
                hugmeTests[i].m_f1 = runHuGMExperiment(g, a, hugmeTests[i]);
                a.cleanNodeClusters(g.getNodes(), true);
            }

            DumpBase.NBParams [] nbTests = new DumpBase.NBParams[g_nb_test_count];
            for (int i = 0; i < g_nb_test_count; i++) {
                nbTests[i] = db.generateNBParams();
                nbTests[i].m_f1 = runNBExperiment(g, a, nbTests[i]);
                a.cleanNodeClusters(g.getNodes(), true);
            }
            DumpBase.IRParams [] irTests = new DumpBase.IRParams[g_ir_test_count];
            for (int i = 0; i < g_ir_test_count; i++) {
               irTests[i] = db.generateIRParams();
               irTests[i].m_f1 = runIRExperiment(g, a, irTests[i]);
               a.cleanNodeClusters(g.getNodes(), true);
            }

            DumpBase.IRParams [] lsiTests = new DumpBase.IRParams[g_lsi_test_count];
            for (int i = 0; i < g_lsi_test_count; i++) {
                lsiTests[i] = db.generateIRParams();
                lsiTests[i].m_f1 = runLSIExperiment(g, a, lsiTests[i]);
                a.cleanNodeClusters(g.getNodes(), true);
            }

            File out = new File("C:/hobbe/projects/coding/github/s4rdm3x/src/test/java/se/lnu/siq/s4rdm3x/experiments/regression/dumps/" + className +".java");
            s2jd.dump(new PrintStream(out, "UTF8"), className, g, a, hugmeTests, nbTests, irTests, lsiTests);

            Comparator cgc = new Comparator();
            cgc.assertEquals(g, s2jd.m_shadow.m_g);



        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

}