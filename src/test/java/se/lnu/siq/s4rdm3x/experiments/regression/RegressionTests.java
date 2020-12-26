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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


class RegressionTests {

    static final int g_HuGMe_test_count = 3;
    @Test
    public void testTeammates_HuGMe() {
        DumpBase db = new TeammatesDump();
        ArchDef a = db.m_a;
        CGraph g = db.m_g;
        for (int i = 0; i < g_HuGMe_test_count; i++) {
            assertEquals(db.getHuGMeParams(i).m_f1, runHuGMExperiment(g, a, db.getHuGMeParams(i)), "Regression test " + i + " for Teammates failed.");
            a.cleanNodeClusters(g.getNodes(), true);
        }
    }

    @Test
    public void testProM_HuGMe() {
        DumpBase db = new ProMDump();
        ArchDef a = db.m_a;
        CGraph g = db.m_g;
        for (int i = 0; i < g_HuGMe_test_count; i++) {
            assertEquals(db.getHuGMeParams(i).m_f1, runHuGMExperiment(g, a, db.getHuGMeParams(i)), "Regression test " + i + " for ProM failed.");
            a.cleanNodeClusters(g.getNodes(), true);
        }
    }

    @Test
    public void testCommonsImg_HuGMe() {
        DumpBase db = new CommonsImagingDump();
        ArchDef a = db.m_a;
        CGraph g = db.m_g;
        for (int i = 0; i < g_HuGMe_test_count; i++) {
            assertEquals(db.getHuGMeParams(i).m_f1, runHuGMExperiment(g, a, db.getHuGMeParams(i)), "Regression test " + i + " for CommonsImaging failed.");
            a.cleanNodeClusters(g.getNodes(), true);
        }
    }

    private double runHuGMExperiment(CGraph a_g, ArchDef a_arch, Map<dmDependency.Type, ExperimentRunner.RandomDoubleVariable> dw) {
        HuGMeExperimentRun exp = new HuGMeExperimentRun(false, new ExperimentRunner.RandomDoubleVariable(0, 0), new ExperimentRunner.RandomDoubleVariable(1, 0), dw);
        final ExperimentRunData.BasicRunData rd = exp.createNewRunData(new Random());
        rd.m_totalMapped = a_arch.getMappedNodeCount(a_g.getNodes());
        a_arch.getClusteredNodes(a_g.getNodes(), ArchDef.Component.ClusteringType.Initial).forEach(n -> rd.addInitialClusteredNode(n));
        rd.m_initialClusteringPercent = (double) rd.getInitialClusteringNodeCount() / (double) rd.m_totalMapped;
        while (!exp.runClustering(a_g, a_arch));

        return rd.calcF1Score();
    }

    private double runHuGMExperiment(CGraph a_g, ArchDef a_arch, DumpBase.HuGMeParams a_params) {

        Map<dmDependency.Type, ExperimentRunner.RandomDoubleVariable> dw = new HashMap<>();
        for (int i = 0; i < dmDependency.Type.values().length; i++) {
            dw.put(dmDependency.Type.values()[i], new ExperimentRunner.RandomDoubleVariable(a_params.m_weights[i], 0));
        }

        HuGMeExperimentRun exp = new HuGMeExperimentRun(false, new ExperimentRunner.RandomDoubleVariable(a_params.m_omega, 0), new ExperimentRunner.RandomDoubleVariable(a_params.m_phi, 0), dw);
        final ExperimentRunData.BasicRunData rd = exp.createNewRunData(new Random());
        rd.m_totalMapped = a_arch.getMappedNodeCount(a_g.getNodes());
        a_arch.getClusteredNodes(a_g.getNodes(), ArchDef.Component.ClusteringType.Initial).forEach(n -> rd.addInitialClusteredNode(n));
        rd.m_initialClusteringPercent = (double) rd.getInitialClusteringNodeCount() / (double) rd.m_totalMapped;
        while (!exp.runClustering(a_g, a_arch));

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

    //@Test // only use in development to add system dumps
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
            isg.assignInitialClusters(g, a, 0.75, new Rand(), r);

            // TODO: we could now calculate the magic numbers needed and add to the corresponding dump
            DumpBase db = new DumpBase();
            DumpBase.HuGMeParams [] tests = new DumpBase.HuGMeParams[g_HuGMe_test_count];
            for (int i = 0; i < g_HuGMe_test_count; i++) {
                tests[i] = db.generateHugMeParams();
                tests[i].m_f1 = runHuGMExperiment(g, a, tests[i]);
                a.cleanNodeClusters(g.getNodes(), true);
            }

            File out = new File("C:/hobbe/projects/coding/github/s4rdm3x/src/test/java/se/lnu/siq/s4rdm3x/experiments/regression/dumps/" + className +".java");
            s2jd.dump(new PrintStream(out), className, g, a, tests);

            Comparator cgc = new Comparator();
            cgc.assertEquals(g, s2jd.m_shadow.m_g);



        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

}