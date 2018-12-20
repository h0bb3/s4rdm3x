package se.lnu.siq.s4rdm3x.experiments;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.model.NodeUtil;
import se.lnu.siq.s4rdm3x.experiments.metric.Metric;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ExperimentRunnerTest {

    private class MetricSetter extends Metric {

        void setMetricValue(Node a_node, double a_metricValue) {
            setMetric(a_node, a_metricValue);
        }

        public String getName() { return "MetricTestHelper"; };
        public void assignMetric(Iterable<Node> a_nodes) {}
        public void reassignMetric(Iterable<Node> a_nodes) {}

    }


    @Test
    public void getWorkingSetTest() {
        Graph g = new MultiGraph("Test");
        MetricSetter ms = new MetricSetter();
        ExperimentRunner er = new ExperimentRunner(null, ms);
        NodeUtil nu = new NodeUtil(g);

                           //1  1  2  4  6  6  6  7  7
                           //0  1  2  3  4  5  6  7  8
        double [] metrics = {1, 1, 2, 4, 6, 6, 7, 6, 7};
        ArrayList<Node> nodes = new ArrayList<>();

        for(double m : metrics) {
            Node n = nu.createNode("n" + nodes.size());
            ms.setMetricValue(n, m);
            nodes.add(n);
        }

        ArrayList<Node> ws = er.getWorkingSetTestHelper(nodes, 1);
        assertTrue(ws.get(0) == nodes.get(6) || ws.get(0) == nodes.get(8));
        assertEquals(ms.getMetric(ws.get(0)), 7);

        ws = er.getWorkingSetTestHelper(nodes, 3);
        assertTrue(ws.get(0) == nodes.get(4) || ws.get(0) == nodes.get(5) || ws.get(0) == nodes.get(7));
        assertTrue(ws.get(1) == nodes.get(6) || ws.get(1) == nodes.get(8));
        assertTrue(ws.get(2) == nodes.get(6) || ws.get(2) == nodes.get(8));
        assertEquals(ms.getMetric(ws.get(0)), 6);
        assertEquals(ms.getMetric(ws.get(1)), 7);
        assertEquals(ms.getMetric(ws.get(2)), 7);

        ws = er.getWorkingSetTestHelper(nodes, 6);
        assertTrue(ws.get(0) == nodes.get(3));
        assertTrue(ws.get(1) == nodes.get(4));
        assertTrue(ws.get(2) == nodes.get(5));
        assertTrue(ws.get(3) == nodes.get(7));
        assertTrue(ws.get(4) == nodes.get(6));
        assertTrue(ws.get(5) == nodes.get(8));
        assertEquals(ms.getMetric(ws.get(0)), 4);

        ws = er.getWorkingSetTestHelper(nodes, 9);
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
        Graph g = new MultiGraph("Test");
        MetricSetter ms = new MetricSetter();
        ExperimentRunner er = new ExperimentRunner(null, ms);
        NodeUtil nu = new NodeUtil(g);


                           //0  1  2  3  4  5  6  7  8
        double [] metrics = {7, 4, 6, 6, 7, 7, 1};
        ArrayList<Node> nodes = new ArrayList<>();

        for(double m : metrics) {
            Node n = nu.createNode("n" + nodes.size());
            ms.setMetricValue(n, m);
            nodes.add(n);
        }

        ArrayList<Node> ws = er.getWorkingSetTestHelper(nodes, 1);
        assertTrue(ws.get(0) == nodes.get(0) || ws.get(0) == nodes.get(4) || ws.get(0) == nodes.get(5));
        assertEquals(ms.getMetric(ws.get(0)), 7);

        ws = er.getWorkingSetTestHelper(nodes, 3);
        assertTrue(ws.get(0) == nodes.get(0));
        assertTrue(ws.get(1) == nodes.get(4));
        assertTrue(ws.get(2) == nodes.get(5));
        assertEquals(ms.getMetric(ws.get(0)), 7);
        assertEquals(ms.getMetric(ws.get(1)), 7);
        assertEquals(ms.getMetric(ws.get(2)), 7);

        ws = er.getWorkingSetTestHelper(nodes, 4);
        assertTrue(ws.get(0) == nodes.get(2) || ws.get(0) == nodes.get(3));
        assertTrue(ws.get(1) == nodes.get(0));
        assertTrue(ws.get(2) == nodes.get(4));
        assertTrue(ws.get(3) == nodes.get(5));
    }

}