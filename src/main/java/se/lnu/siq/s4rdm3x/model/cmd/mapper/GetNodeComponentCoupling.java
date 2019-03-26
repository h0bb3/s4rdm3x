package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import se.lnu.siq.s4rdm3x.experiments.metric.FanHelper;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GetNodeComponentCoupling {

    ArchDef m_arch;
    public Table m_result;

    public GetNodeComponentCoupling(ArchDef a_arch) {
        m_arch = a_arch;
        m_result = new Table();
    }

    public void run(CGraph a_g)  {
        FanHelper fh = new FanHelper(a_g.getNodes());

        Iterable<CNode> mappedNodes = m_arch.getMappedNodes(a_g.getNodes());

        for (CNode n : mappedNodes) {
            double fanIn = 0, fanOut = 0;
            double couplingIn = 0, couplingOut = 0;
            double violationFanIn = 0, violationCouplingIn = 0;
            double violationFanOut = 0, violationCouplingOut = 0;
            ArchDef.Component c = m_arch.getMappedComponent(n);

            ArrayList<CNode> couplings = fh.getCoupledNodes(n, mappedNodes);
            for (CNode cNode : couplings) {
                ArchDef.Component otherComponent = m_arch.getMappedComponent(cNode);
                if (c != otherComponent) {
                    final boolean isViolationOut = c.allowedDependency(otherComponent);
                    final boolean isViolationIn = otherComponent.allowedDependency(c);
                    final double fin = fh.getFanIn(n, cNode);
                    final double fout = fh.getFanOut(n, cNode);
                    fanIn += fin;
                    fanOut += fout;

                    if (isViolationOut) {
                        violationFanOut += fout;
                    }
                    if (isViolationIn) {
                        violationFanIn += fin;
                    }

                    if (fin > 0) {
                        couplingIn += 1;
                        if (isViolationIn) {
                            violationCouplingIn += 1;
                        }
                    }

                    if (fout > 0) {
                        couplingOut += 1;
                        if (isViolationOut) {
                            violationCouplingOut += 1;
                        }
                    }

                }
            }


            m_result.add(n, "mapping", c.getName());
            m_result.add(n, "componentFanOut", fanOut);
            m_result.add(n, "componentFanIn", fanIn);
            m_result.add(n, "componentFan", fanOut + fanIn);
            m_result.add(n, "componentCouplingOut", couplingOut);
            m_result.add(n, "componentCouplingIn", couplingIn);
            m_result.add(n, "componentCoupling", couplingOut + couplingIn);
            m_result.add(n, "violationFanIn", violationFanIn);
            m_result.add(n, "violationFanOut", violationFanOut);
            m_result.add(n, "violationFan", violationFanOut + violationFanIn);
            m_result.add(n, "violationCouplingIn", violationCouplingIn);
            m_result.add(n, "violationCouplingOut", violationCouplingOut);
            m_result.add(n, "violationCoupling", violationCouplingOut + violationCouplingIn);
        }
    }

    public static class Table {

        Object [] m_metrics = new Object[0];
        int m_rowCount;
        int m_colCount;

        HashMap<CNode, Integer> m_node2Row = new HashMap<>();
        HashMap<String, Integer> m_metric2Col = new HashMap<>();


        public int addRow(CNode a_node) {
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

        public void add(CNode a_n, String a_metric, Object a_value) {
            int row = addRow(a_n);
            int col = addColumn(a_metric);

            m_metrics[row * m_colCount + col] = a_value;
        }


        public Iterable<CNode> getNodes() {
            return m_node2Row.keySet();
        }
        public Iterable<String> getMetrics() {
            return m_metric2Col.keySet();
        }


        public Object get(CNode a_row, String a_col) {
            int rowIx = m_node2Row.get(a_row);
            int colIx = m_metric2Col.get(a_col);
            return m_metrics[colIx + rowIx * m_colCount];
        }
    }

}
