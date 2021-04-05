package se.lnu.siq.s4rdm3x.g3n3z;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.MagicInvoker;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.experiments.ExperimentRunData;
import se.lnu.siq.s4rdm3x.experiments.ExperimentRunner;
import se.lnu.siq.s4rdm3x.experiments.HuGMeExperimentRun;
import se.lnu.siq.s4rdm3x.experiments.InitialSetGenerator;
import se.lnu.siq.s4rdm3x.experiments.metric.Rand;
import se.lnu.siq.s4rdm3x.experiments.system.FileBased;
import se.lnu.siq.s4rdm3x.experiments.system.System;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.MapperBase;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;


class IndividualTest {

    @Test
    public void testF1() {
        try {
            FileBased system = new FileBased("C:/hObbE/projects/coding/github/s4rdm3x/data/systems/ProM6.9/ProM_6_9.sysmdl");
            //FileBased system = new FileBased("C:/hObbE/projects/coding/github/s4rdm3x/data/systems/teammates/teammates.sysmdl");
            CGraph g = new CGraph();
            system.load(g);
            ArchDef a = system.createAndMapArch(g);
            Random r = new Random(171717);
            Rand rm = new Rand();
            rm.assignMetric(a.getMappedNodes(g.getNodes()));

            InitialSetGenerator isg = new InitialSetGenerator();
            isg.assignInitialClusters(g, a, 0.75, new Rand(), r);
            int mappedNodeCount = a.getMappedNodeCount(g.getNodes());
            int initialSetSize = a.getClusteredNodeCount(g.getNodes());

            class SUTIndividual extends Individual {

                SUTIndividual() {
                    super(g, a, 17);
                }

                private double runExperimentGetF1Score(int a_totalPossibleOrphans) {
                    MagicInvoker mi = new MagicInvoker(this);
                    return (double)mi.invokeMethodMagic(a_totalPossibleOrphans);
                }
            }

            SUTIndividual sut = new SUTIndividual();
            double sutF1 = sut.runExperimentGetF1Score(mappedNodeCount - initialSetSize);

            a.cleanNodeClusters(g.getNodes(), true);
            Map<dmDependency.Type, ExperimentRunner.RandomDoubleVariable> dw = new HashMap<>();
            for (dmDependency.Type t : dmDependency.Type.values()) {
                dw.put(t, new ExperimentRunner.RandomDoubleVariable(sut.getDW(t), 0));
            }

            HuGMeExperimentRun exp = new HuGMeExperimentRun(false, new ExperimentRunner.RandomDoubleVariable(0, 0), new ExperimentRunner.RandomDoubleVariable(1, 0), dw);
            ExperimentRunData.BasicRunData rd = exp.createNewRunData(r);
            rd.m_totalMapped = a.getMappedNodeCount(g.getNodes());
            a.getClusteredNodes(g.getNodes(), ArchDef.Component.ClusteringType.Initial).forEach(n -> rd.addInitialClusteredNode(n));
            rd.m_initialClusteringPercent = (double) rd.getInitialClusteringNodeCount() / (double) rd.m_totalMapped;
            while (!exp.runClustering(g, a))

            java.lang.System.out.println("" + sutF1);

            assertEquals(rd.calcF1Score(), sutF1);
        } catch (IOException | System.NoMappedNodesException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testTeammates_HugMe_1() {
        TeammatesDump teammates = new TeammatesDump();
        ArchDef a = teammates.m_a;
        CGraph g = teammates.m_g;
        Map<dmDependency.Type, ExperimentRunner.RandomDoubleVariable> dw = new HashMap<>();
        for (dmDependency.Type t : dmDependency.Type.values()) {
            dw.put(t, new ExperimentRunner.RandomDoubleVariable(1.0, 0));
        }

        HuGMeExperimentRun exp = new HuGMeExperimentRun(false, new ExperimentRunner.RandomDoubleVariable(0, 0), new ExperimentRunner.RandomDoubleVariable(1, 0), dw);
        final ExperimentRunData.BasicRunData rd = exp.createNewRunData(new Random());
        rd.m_totalMapped = a.getMappedNodeCount(g.getNodes());
        a.getClusteredNodes(g.getNodes(), ArchDef.Component.ClusteringType.Initial).forEach(n -> rd.addInitialClusteredNode(n));
        rd.m_initialClusteringPercent = (double) rd.getInitialClusteringNodeCount() / (double) rd.m_totalMapped;
        while (!exp.runClustering(g, a));

        assertEquals(0.303886925795053, rd.calcF1Score());

        a.cleanNodeClusters(g.getNodes(), true);
        for (dmDependency.Type t : dmDependency.Type.values()) {
            dw.put(t, new ExperimentRunner.RandomDoubleVariable(0.0, 0));
        }
        dw.put(dmDependency.Type.Extends, new ExperimentRunner.RandomDoubleVariable(1.0, 0));
        final ExperimentRunData.BasicRunData rd2= exp.createNewRunData(new Random());
        rd2.m_totalMapped = a.getMappedNodeCount(g.getNodes());
        a.getClusteredNodes(g.getNodes(), ArchDef.Component.ClusteringType.Initial).forEach(n -> rd2.addInitialClusteredNode(n));
        rd2.m_initialClusteringPercent = (double) rd.getInitialClusteringNodeCount() / (double) rd2.m_totalMapped;
        while (!exp.runClustering(g, a));

        assertEquals(0.303886925795053, rd2.calcF1Score());


    }

    @Test
    public void testTeammates_HugMe_2() {
        TeammatesDump teammates = new TeammatesDump();
        ArchDef a = teammates.m_a;
        CGraph g = teammates.m_g;
        Map<dmDependency.Type, ExperimentRunner.RandomDoubleVariable> dw = new HashMap<>();
        for (dmDependency.Type t : dmDependency.Type.values()) {
            dw.put(t, new ExperimentRunner.RandomDoubleVariable(0.0, 0));
        }
        dw.put(dmDependency.Type.Extends, new ExperimentRunner.RandomDoubleVariable(1.0, 0));
        dw.put(dmDependency.Type.Implements, new ExperimentRunner.RandomDoubleVariable(0.07479101885668538, 0));
        //dw.put(dmDependency.Type.Returns, new ExperimentRunner.RandomDoubleVariable( 0.22679069305507837,  0));
        //dw.put(dmDependency.Type.ConstructorCall, new ExperimentRunner.RandomDoubleVariable( 0.16623343264692425,  0));
        //dw.put(dmDependency.Type.OwnFieldUse, new ExperimentRunner.RandomDoubleVariable( 0.06420987518192389,  0));
        dw.put(dmDependency.Type.Field, new ExperimentRunner.RandomDoubleVariable( 0.10568346526641301,  0));

        HuGMeExperimentRun exp = new HuGMeExperimentRun(false, new ExperimentRunner.RandomDoubleVariable(0, 0), new ExperimentRunner.RandomDoubleVariable(1, 0), dw);
        final ExperimentRunData.BasicRunData rd = exp.createNewRunData(new Random());
        rd.m_totalMapped = a.getMappedNodeCount(g.getNodes());
        a.getClusteredNodes(g.getNodes(), ArchDef.Component.ClusteringType.Initial).forEach(n -> rd.addInitialClusteredNode(n));
        rd.m_initialClusteringPercent = (double) rd.getInitialClusteringNodeCount() / (double) rd.m_totalMapped;
        while (!exp.runClustering(g, a));

        assertEquals(0.8476454293628809, rd.calcF1Score());
    }

    @Test
    public void testTeammates_HugMe_3() {
        TeammatesDump teammates = new TeammatesDump();
        ArchDef a = teammates.m_a;
        CGraph g = teammates.m_g;
        Map<dmDependency.Type, ExperimentRunner.RandomDoubleVariable> dw = new HashMap<>();
        for (dmDependency.Type t : dmDependency.Type.values()) {
            dw.put(t, new ExperimentRunner.RandomDoubleVariable(0.0, 0));
        }
        dw.put(dmDependency.Type.Extends, new ExperimentRunner.RandomDoubleVariable(1.0, 0));
        //dw.put(dmDependency.Type.Implements, new ExperimentRunner.RandomDoubleVariable(0.07479101885668538, 0));
        //dw.put(dmDependency.Type.Returns, new ExperimentRunner.RandomDoubleVariable( 0.22679069305507837,  0));
        //dw.put(dmDependency.Type.ConstructorCall, new ExperimentRunner.RandomDoubleVariable( 0.16623343264692425,  0));
        //dw.put(dmDependency.Type.OwnFieldUse, new ExperimentRunner.RandomDoubleVariable( 0.06420987518192389,  0));
        //dw.put(dmDependency.Type.Field, new ExperimentRunner.RandomDoubleVariable( 0.10568346526641301,  0));

        HuGMeExperimentRun exp = new HuGMeExperimentRun(false, new ExperimentRunner.RandomDoubleVariable(0, 0), new ExperimentRunner.RandomDoubleVariable(1, 0), dw);
        final ExperimentRunData.BasicRunData rd = exp.createNewRunData(new Random());
        rd.m_totalMapped = a.getMappedNodeCount(g.getNodes());
        a.getClusteredNodes(g.getNodes(), ArchDef.Component.ClusteringType.Initial).forEach(n -> rd.addInitialClusteredNode(n));
        rd.m_initialClusteringPercent = (double) rd.getInitialClusteringNodeCount() / (double) rd.m_totalMapped;
        while (!exp.runClustering(g, a));

        assertEquals(0.8228228228228228, rd.calcF1Score());
    }


    @Test
    public void dumpSystem() {
        /*try {
            System2JavaDumper s2jd = new System2JavaDumper();

            //FileBased system = new FileBased("C:/hObbE/projects/coding/github/s4rdm3x/data/systems/ProM6.9/ProM_6_9.sysmdl");
            FileBased system = new FileBased("C:/hObbE/projects/coding/github/s4rdm3x/data/systems/teammates/teammates.sysmdl");
            CGraph g = new CGraph();
            system.load(g);
            ArchDef a = system.createAndMapArch(g);

            Random r = new Random(171717);
            Rand rm = new Rand();
            rm.assignMetric(a.getMappedNodes(g.getNodes()));

            InitialSetGenerator isg = new InitialSetGenerator();
            isg.assignInitialClusters(g, a, 0.75, new Rand(), r);

            File out = new File("C:/hobbe/projects/coding/github/s4rdm3x/g3n3z/src/test/java/se/lnu/siq/s4rdm3x/g3n3z/TeammatesDump.java");
            s2jd.dump(new PrintStream(out), "Testing", g, a);


        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }*/
    }

}