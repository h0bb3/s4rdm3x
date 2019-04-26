package se.lnu.siq.s4rdm3x.experiments;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.experiments.metric.Metric;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ExperimentRunnerTest {

    private class MetricSetter extends Metric {

        void setMetricValue(CNode a_node, double a_metricValue) {
            a_node.setMetric(getName(), a_metricValue);
        }

        public String getName() { return "MetricTestHelper"; };
        public void assignMetric(Iterable<CNode> a_nodes) {}
        public void reassignMetric(Iterable<CNode> a_nodes) {}

    }


    @Test
    public void getWorkingSetTest() {
        CGraph g = new CGraph();
        MetricSetter ms = new MetricSetter();
        ExperimentRunner er = new HuGMeExperimentRunner(null, ms, false, new ExperimentRunner.RandomDoubleVariable(0.1, 0.1), new ExperimentRunner.RandomDoubleVariable(0.5, 0.5), new ExperimentRunner.RandomDoubleVariable(0.5, 0.5));

                           //1  1  2  4  6  6  6  7  7
                           //0  1  2  3  4  5  6  7  8
        double [] metrics = {1, 1, 2, 4, 6, 6, 7, 6, 7};
        ArrayList<CNode> nodes = new ArrayList<>();

        for(double m : metrics) {
            CNode n = g.createNode("n" + nodes.size());
            ms.setMetricValue(n, m);
            nodes.add(n);
        }

        ArrayList<CNode> ws = er.getWorkingSetTestHelper(nodes, 1, ms);
        assertTrue(ws.get(0) == nodes.get(6) || ws.get(0) == nodes.get(8));
        assertEquals(ms.getMetric(ws.get(0)), 7);

        ws = er.getWorkingSetTestHelper(nodes, 3, ms);
        assertTrue(ws.get(0) == nodes.get(4) || ws.get(0) == nodes.get(5) || ws.get(0) == nodes.get(7));
        assertTrue(ws.get(1) == nodes.get(6) || ws.get(1) == nodes.get(8));
        assertTrue(ws.get(2) == nodes.get(6) || ws.get(2) == nodes.get(8));
        assertEquals(ms.getMetric(ws.get(0)), 6);
        assertEquals(ms.getMetric(ws.get(1)), 7);
        assertEquals(ms.getMetric(ws.get(2)), 7);

        ws = er.getWorkingSetTestHelper(nodes, 6, ms);
        assertTrue(ws.get(0) == nodes.get(3));
        assertTrue(ws.get(1) == nodes.get(4));
        assertTrue(ws.get(2) == nodes.get(5));
        assertTrue(ws.get(3) == nodes.get(7));
        assertTrue(ws.get(4) == nodes.get(6));
        assertTrue(ws.get(5) == nodes.get(8));
        assertEquals(ms.getMetric(ws.get(0)), 4);

        ws = er.getWorkingSetTestHelper(nodes, 9, ms);
        assertTrue(ws.get(0) == nodes.get(0));
        assertTrue(ws.get(1) == nodes.get(1));
        assertTrue(ws.get(2) == nodes.get(2));
        assertTrue(ws.get(3) == nodes.get(3));
        assertTrue(ws.get(4) == nodes.get(4));
        assertTrue(ws.get(5) == nodes.get(5));
        assertTrue(ws.get(6) == nodes.get(7));
        assertTrue(ws.get(7) == nodes.get(6));
        assertTrue(ws.get(8) == nodes.get(8));
        assertEquals(ms.getMetric(ws.get(0)), 1);
    }

    @Test
    public void getWorkingSetTest2() {
        CGraph g = new CGraph();
        MetricSetter ms = new MetricSetter();
        ExperimentRunner er = new HuGMeExperimentRunner(null, ms, false, new ExperimentRunner.RandomDoubleVariable(0.1, 0.1), new ExperimentRunner.RandomDoubleVariable(0.5, 0.5), new ExperimentRunner.RandomDoubleVariable(0.5, 0.5));


                           //0  1  2  3  4  5  6  7  8
        double [] metrics = {7, 4, 6, 6, 7, 7, 1};
        ArrayList<CNode> nodes = new ArrayList<>();

        for(double m : metrics) {
            CNode n = g.createNode("n" + nodes.size());
            ms.setMetricValue(n, m);
            nodes.add(n);
        }

        ArrayList<CNode> ws = er.getWorkingSetTestHelper(nodes, 1, ms);
        assertTrue(ws.get(0) == nodes.get(0) || ws.get(0) == nodes.get(4) || ws.get(0) == nodes.get(5));
        assertEquals(ms.getMetric(ws.get(0)), 7);

        ws = er.getWorkingSetTestHelper(nodes, 3, ms);
        assertTrue(ws.get(0) == nodes.get(0));
        assertTrue(ws.get(1) == nodes.get(4));
        assertTrue(ws.get(2) == nodes.get(5));
        assertEquals(ms.getMetric(ws.get(0)), 7);
        assertEquals(ms.getMetric(ws.get(1)), 7);
        assertEquals(ms.getMetric(ws.get(2)), 7);

        ws = er.getWorkingSetTestHelper(nodes, 4, ms);
        assertTrue(ws.get(0) == nodes.get(2) || ws.get(0) == nodes.get(3));
        assertTrue(ws.get(1) == nodes.get(0));
        assertTrue(ws.get(2) == nodes.get(4));
        assertTrue(ws.get(3) == nodes.get(5));
    }

}