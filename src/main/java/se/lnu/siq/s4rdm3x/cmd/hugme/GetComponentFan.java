package se.lnu.siq.s4rdm3x.cmd.hugme;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import se.lnu.siq.s4rdm3x.model.AttributeUtil;
import se.lnu.siq.s4rdm3x.experiments.metric.FanHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GetComponentFan {

    HuGMe.ArchDef m_arch;
    public Table m_fanIn;
    public Table m_fanOut;

    public GetComponentFan(HuGMe.ArchDef a_arch) {
        m_arch = a_arch;
        m_fanIn = new Table();
        m_fanOut = new Table();
    }

    public void run(Graph a_g)  {
        FanHelper fh = new FanHelper(a_g.getNodeSet());
        HashMap<HuGMe.ArchDef.Component, ArrayList<Node>> mappedNodes = new HashMap<>();
        AttributeUtil au = new AttributeUtil();

        for (HuGMe.ArchDef.Component c : m_arch.getComponents()) {

            ArrayList<Node> nodes = new ArrayList<>();
            mappedNodes.put(c, nodes);

            for (Node n : a_g.getNodeSet()) {
                if (c.isMappedTo(n, au)) {
                    nodes.add(n);
                }
            }
        }

        for (HuGMe.ArchDef.Component from : m_arch.getComponents()) {
            for (HuGMe.ArchDef.Component to : m_arch.getComponents()) {

                double fanIn = fh.getFanIn(mappedNodes.get(from), mappedNodes.get(to));
                double fanOut = fh.getFanOut(mappedNodes.get(from), mappedNodes.get(to));

                m_fanIn.add(from, to, fanIn);
                m_fanOut.add(from, to, fanOut);
            }
        }
    }


    public static class Table {

        double [] m_metrics = new double[0];
        int m_rowCount;
        int m_colCount;

        HashMap<HuGMe.ArchDef.Component, Integer> m_component2Row = new HashMap<>();
        HashMap<HuGMe.ArchDef.Component, Integer> m_component2Col = new HashMap<>();


        public int addRow(HuGMe.ArchDef.Component a_c) {
            if (!m_component2Row.containsKey(a_c)) {
                m_metrics = Arrays.copyOf(m_metrics, (m_rowCount + 1) * m_colCount);
                m_component2Row.put(a_c, m_rowCount);
                m_rowCount++;
            }

            return m_component2Row.get(a_c);
        }

        public int addColumn(HuGMe.ArchDef.Component a_c) {
            if (!m_component2Col.containsKey(a_c)) {
                m_metrics = Arrays.copyOf(m_metrics, m_rowCount * (m_colCount + 1));
                m_component2Col.put(a_c, m_colCount);
                m_colCount++;
            }

            return m_component2Col.get(a_c);
        }

        public void add(HuGMe.ArchDef.Component a_row, HuGMe.ArchDef.Component a_col, double a_value) {
            int row = addRow(a_row);
            int col = addColumn(a_col);

            m_metrics[row * m_colCount + col] = a_value;
        }


        public Iterable<HuGMe.ArchDef.Component> getRowObjects() {
            return m_component2Row.keySet();
        }

        public Iterable<HuGMe.ArchDef.Component> getColumnObjects() {
            return m_component2Col.keySet();
        }

        public double get(HuGMe.ArchDef.Component a_row, HuGMe.ArchDef.Component a_col) {
            int rowIx = m_component2Row.get(a_row);
            int colIx = m_component2Col.get(a_col);
            return m_metrics[colIx + rowIx * m_colCount];
        }
    }


}
