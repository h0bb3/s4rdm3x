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
    @Test
    public void testTeammates_HugMe_1() {
        DumpBase teammates = new TeammatesDump();
        ArchDef a = teammates.m_a;
        CGraph g = teammates.m_g;

        assertEquals(teammates.getF1Score(0), runHuGMExperiment(g, a, getDW_1()));
    }

    @Test
    public void testTeammates_HugMe_2() {
        DumpBase teammates = new TeammatesDump();
        ArchDef a = teammates.m_a;
        CGraph g = teammates.m_g;
        assertEquals(teammates.getF1Score(1), runHuGMExperiment(g, a, getDW_2()));
    }

    @Test
    public void testTeammates_HugMe_3() {
        DumpBase teammates = new TeammatesDump();
        ArchDef a = teammates.m_a;
        CGraph g = teammates.m_g;
        assertEquals(teammates.getF1Score(2), runHuGMExperiment(g, a, getDW_3()));
    }

    @Test
    public void testProM_HugMe_1() {
        DumpBase prom = new ProMDump();
        ArchDef a = prom.m_a;
        CGraph g = prom.m_g;
        assertEquals(prom.getF1Score(0), runHuGMExperiment(g, a, getDW_1()));
    }

    @Test
    public void testProM_HugMe_2() {
        DumpBase prom = new ProMDump();
        ArchDef a = prom.m_a;
        CGraph g = prom.m_g;
        assertEquals(prom.getF1Score(1), runHuGMExperiment(g, a, getDW_2()));
    }

    @Test
    public void testProM_HugMe_3() {
        DumpBase prom = new ProMDump();
        ArchDef a = prom.m_a;
        CGraph g = prom.m_g;
        assertEquals(prom.getF1Score(2), runHuGMExperiment(g, a, getDW_3()));
    }

    public Map<dmDependency.Type, ExperimentRunner.RandomDoubleVariable> getDW_1() {
        Map<dmDependency.Type, ExperimentRunner.RandomDoubleVariable> dw = new HashMap<>();
        for (dmDependency.Type t : dmDependency.Type.values()) {
            dw.put(t, new ExperimentRunner.RandomDoubleVariable(1.0, 0));
        }

        return dw;
    }

    public Map<dmDependency.Type, ExperimentRunner.RandomDoubleVariable> getDW_2() {
        Map<dmDependency.Type, ExperimentRunner.RandomDoubleVariable> dw = new HashMap<>();
        for (dmDependency.Type t : dmDependency.Type.values()) {
            dw.put(t, new ExperimentRunner.RandomDoubleVariable(0.0, 0));
        }
        dw.put(dmDependency.Type.Extends, new ExperimentRunner.RandomDoubleVariable(1.0, 0));
        dw.put(dmDependency.Type.Implements, new ExperimentRunner.RandomDoubleVariable(0.07479101885668538, 0));
        dw.put(dmDependency.Type.Field, new ExperimentRunner.RandomDoubleVariable( 0.10568346526641301,  0));
        return dw;
    }

    public Map<dmDependency.Type, ExperimentRunner.RandomDoubleVariable> getDW_3() {
        Map<dmDependency.Type, ExperimentRunner.RandomDoubleVariable> dw = new HashMap<>();
        for (dmDependency.Type t : dmDependency.Type.values()) {
            dw.put(t, new ExperimentRunner.RandomDoubleVariable(0.0, 0));
        }
        dw.put(dmDependency.Type.Extends, new ExperimentRunner.RandomDoubleVariable(1.0, 0));
        return dw;
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

            FileBased system = new FileBased("C:/hObbE/projects/coding/github/s4rdm3x/data/systems/ProM6.9/ProM_6_9.sysmdl");
            String className = "ProMDump";

            //FileBased system = new FileBased("C:/hObbE/projects/coding/github/s4rdm3x/data/systems/teammates/teammates.sysmdl");
            //String className = "TeammatesDump";

            //FileBased system = new FileBased("C:/hObbE/projects/coding/github/s4rdm3x/data/systems/commons-imaging/commons-imaging.sysmdl");
            //String className = "CommonsImagingDump";

            CGraph g = new CGraph();
            system.load(g);
            ArchDef a = system.createAndMapArch(g);

            Random r = new Random(171717);
            se.lnu.siq.s4rdm3x.experiments.metric.Rand rm = new Rand();
            rm.assignMetric(a.getMappedNodes(g.getNodes()));

            InitialSetGenerator isg = new InitialSetGenerator();
            isg.assignInitialClusters(g, a, 0.75, new Rand(), r);

            // TODO: we could now calculate the magic numbers needed and add to the corresponding dump
            double [] scores = new double[3];
            scores[0] = runHuGMExperiment(g, a, getDW_1());
            a.cleanNodeClusters(g.getNodes(), true);
            scores[1] = runHuGMExperiment(g, a, getDW_2());
            a.cleanNodeClusters(g.getNodes(), true);
            scores[2] = runHuGMExperiment(g, a, getDW_3());
            a.cleanNodeClusters(g.getNodes(), true);

            File out = new File("C:/hobbe/projects/coding/github/s4rdm3x/src/test/java/se/lnu/siq/s4rdm3x/experiments/regression/dumps/" + className +".java");
            s2jd.dump(new PrintStream(out), className, g, a, scores);

            Comparator cgc = new Comparator();
            cgc.assertEquals(g, s2jd.m_shadow.m_g);



        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

}