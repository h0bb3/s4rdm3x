package se.lnu.siq.s4rdm3x.cmd.metrics;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.cmd.util.Selector;

import java.util.Arrays;
import java.util.HashMap;

public class GetMetrics {

    public Table m_result;

    private Selector.ISelector m_selection;

    public GetMetrics(Selector.ISelector a_selection) {
        m_selection = a_selection;
    }

    public void run(Graph a_g) {
        m_result = new Table();

        for (org.graphstream.graph.Node n : a_g.getEachNode()) {
            if (m_selection.isSelected(n)) {
                ComputeMetrics.Map map = n.getAttribute(ComputeMetrics.g_metricsMapKey);

                for (String metric : map.keySet()) {
                    m_result.add(n, metric, map.get(metric));
                }
            }
        }

    }

    public static class Table {

        double [] m_metrics = new double[0];
        int m_rowCount;
        int m_colCount;

        HashMap<Node, Integer> m_node2Row = new HashMap<>();
        HashMap<String, Integer> m_metric2Col = new HashMap<>();


        public int addRow(Node a_node) {
            if (!m_node2Row.containsKey(a_node)) {
                m_metrics = Arrays.copyOf(m_metrics, (m_rowCount + 1) * m_colCount);
                m_node2Row.put(a_node, m_rowCount);
                m_rowCount++;
            }

            return m_node2Row.get(a_node);
        }

        public int addColumn(String a_metricName) {
            if (!m_metric2Col.containsKey(a_metricName)) {
                m_metrics = Arrays.copyOf(m_metrics, m_rowCount * (m_colCount + 1));
                m_metric2Col.put(a_metricName, m_colCount);
                m_colCount++;
            }

            return m_metric2Col.get(a_metricName);
        }

        public void add(Node a_n, String a_metric, double a_value) {
            int row = addRow(a_n);
            int col = addColumn(a_metric);

            m_metrics[row * m_colCount + col] = a_value;
        }


        public Iterable<Node> getNodes() {
            return m_node2Row.keySet();
        }
        public Iterable<String> getMetrics() {
            return m_metric2Col.keySet();
        }

        public double[] getRow(Node a_node) {
            if (m_node2Row.containsKey(a_node)) {
                int ix = m_node2Row.get(a_node) * m_colCount;
                return Arrays.copyOfRange(m_metrics, ix, ix + m_colCount);
            }
            return null;
        }

        public double get(Node a_row, String a_col) {
            int rowIx = m_node2Row.get(a_row);
            int colIx = m_metric2Col.get(a_col);
            return m_metrics[colIx + rowIx * m_colCount];
        }
    }



}
