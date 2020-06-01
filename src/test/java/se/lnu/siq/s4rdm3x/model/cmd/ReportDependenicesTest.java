package se.lnu.siq.s4rdm3x.model.cmd;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.NodeGenerator;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ReportDependenicesTest {

    private void assertDependencyReportFan(ReportDependencies.Dependency a_sut, int a_expectedInternal, int a_expectedExternal, int a_expectedUnmapped) {
        assertEquals(a_expectedInternal, a_sut.getInternalFan());
        assertEquals(a_expectedExternal, a_sut.getExternalFan());
        assertEquals(a_expectedUnmapped, a_sut.getUnmappedFan());
    }

    private void assertDependencyReportCouplingOut(ReportDependencies.Dependency a_sut, int a_expectedInternal, int a_expectedExternal, int a_expectedUnmapped) {
        assertEquals(a_expectedInternal, a_sut.getInternalCouplingOut());
        assertEquals(a_expectedExternal, a_sut.getExternalCouplingOut());
        assertEquals(a_expectedUnmapped, a_sut.getUnmappedCouplingOut());
    }

    private void assertDependencyReportCoupling(ReportDependencies.Dependency a_sut, int a_expectedInternal, int a_expectedExternal, int a_expectedUnmapped) {
        assertEquals(a_expectedInternal, a_sut.getInternalCoupling());
        assertEquals(a_expectedExternal, a_sut.getExternalCoupling());
        assertEquals(a_expectedUnmapped, a_sut.getUnmappedCoupling());
    }

    private void assertDependencyReportFanOut(ReportDependencies.Dependency a_sut, int a_expectedInternal, int a_expectedExternal, int a_expectedUnmapped) {
        assertEquals(a_expectedInternal, a_sut.getInternalFanOut());
        assertEquals(a_expectedExternal, a_sut.getExternalFanOut());
        assertEquals(a_expectedUnmapped, a_sut.getUnmappedFanOut());
    }

    @Test
    public void twoComponentTest() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(new String [] {"AB"});
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef arch = new se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef();
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef.Component cA = arch.addComponent("A");
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef.Component cB = arch.addComponent("B");

        cA.mapToNode(g.getNode("A"));
        cB.mapToNode(g.getNode("B"));

        ReportDependencies sut = new ReportDependencies();

        sut.run(g, arch);
        assertEquals(2, sut.m_dependencyReport.size());

        ReportDependencies.Dependency dA = getDependency(sut.m_dependencyReport, g.getNode("A"));
        ReportDependencies.Dependency dB = getDependency(sut.m_dependencyReport, g.getNode("B"));

        assertDependencyReportFan(dA, 0, 1, 0);
        assertDependencyReportFan(dB, 0, 1, 0);
        assertDependencyReportFanOut(dA, 0, 1, 0);
        assertDependencyReportFanOut(dB, 0, 0, 0);
        assertDependencyReportCouplingOut(dA, 0, 1, 0);
        assertDependencyReportCouplingOut(dB, 0, 0, 0);
        assertDependencyReportCoupling(dA, 0, 1, 0);
        assertDependencyReportCoupling(dB, 0, 1, 0);
    }

    @Test
    public void threeComponentTest() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(new String [] {"AB", "AC"});
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef arch = new se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef();
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef.Component cA = arch.addComponent("A");
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef.Component cB = arch.addComponent("B");
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef.Component cC = arch.addComponent("C");

        cA.mapToNode(g.getNode("A"));
        cB.mapToNode(g.getNode("B"));
        cC.mapToNode(g.getNode("C"));

        ReportDependencies sut = new ReportDependencies();

        sut.run(g, arch);
        assertEquals(3, sut.m_dependencyReport.size());

        ReportDependencies.Dependency dA = getDependency(sut.m_dependencyReport, g.getNode("A"));
        ReportDependencies.Dependency dB = getDependency(sut.m_dependencyReport, g.getNode("B"));
        ReportDependencies.Dependency dC = getDependency(sut.m_dependencyReport, g.getNode("C"));

        assertDependencyReportFan(dA, 0, 2, 0);
        assertDependencyReportFan(dB, 0, 1, 0);
        assertDependencyReportFan(dC, 0, 1, 0);

        assertDependencyReportFanOut(dA, 0, 2, 0);
        assertDependencyReportFanOut(dB, 0, 0, 0);
        assertDependencyReportFanOut(dC, 0, 0, 0);

        assertDependencyReportCouplingOut(dA, 0, 2, 0);
        assertDependencyReportCouplingOut(dB, 0, 0, 0);
        assertDependencyReportCouplingOut(dC, 0, 0, 0);

        assertDependencyReportCoupling(dA, 0, 2, 0);
        assertDependencyReportCoupling(dB, 0, 1, 0);
        assertDependencyReportCoupling(dB, 0, 1, 0);
    }

    @Test
    public void twoComponentMultipleDependenciesTest() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(new String [] {"AB", "BA", "AB"});
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef arch = new se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef();
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef.Component cA = arch.addComponent("A");
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef.Component cB = arch.addComponent("B");

        cA.mapToNode(g.getNode("A"));
        cB.mapToNode(g.getNode("B"));

        ReportDependencies sut = new ReportDependencies();

        sut.run(g, arch);
        assertEquals(2, sut.m_dependencyReport.size());

        ReportDependencies.Dependency dA = getDependency(sut.m_dependencyReport, g.getNode("A"));
        ReportDependencies.Dependency dB = getDependency(sut.m_dependencyReport, g.getNode("B"));

        assertDependencyReportFan(dA, 0, 3, 0);
        assertDependencyReportFan(dB, 0, 3, 0);

        assertDependencyReportFanOut(dA, 0, 2, 0);
        assertDependencyReportFanOut(dB, 0, 1, 0);

        assertDependencyReportCouplingOut(dA, 0, 1, 0);
        assertDependencyReportCouplingOut(dB, 0, 1, 0);

        assertDependencyReportCoupling(dA, 0, 1, 0);
        assertDependencyReportCoupling(dB, 0, 1, 0);
    }

    @Test
    public void oneComponentTest() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(new String [] {"AB"});
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef arch = new se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef();
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef.Component cA = arch.addComponent("A");

        cA.mapToNode(g.getNode("A"));
        cA.mapToNode(g.getNode("B"));

        ReportDependencies sut = new ReportDependencies();

        sut.run(g, arch);
        assertEquals(2, sut.m_dependencyReport.size());

        ReportDependencies.Dependency dA = getDependency(sut.m_dependencyReport, g.getNode("A"));
        ReportDependencies.Dependency dB = getDependency(sut.m_dependencyReport, g.getNode("B"));

        assertDependencyReportFan(dA, 1, 0, 0);
        assertDependencyReportFan(dB, 1, 0, 0);

        assertDependencyReportFanOut(dA, 1, 0, 0);
        assertDependencyReportFanOut(dB, 0, 0, 0);
        assertDependencyReportCouplingOut(dA, 1, 0, 0);
        assertDependencyReportCouplingOut(dB, 0, 0, 0);
        assertDependencyReportCoupling(dA, 1, 0, 0);
        assertDependencyReportCoupling(dB, 1, 0, 0);
    }

    @Test
    public void twoComponentTestWithInternal() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(new String [] {"AB", "AC"});
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef arch = new se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef();
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef.Component cA = arch.addComponent("A");
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef.Component cB = arch.addComponent("B");

        cA.mapToNode(g.getNode("A"));
        cA.mapToNode(g.getNode("C"));
        cB.mapToNode(g.getNode("B"));

        ReportDependencies sut = new ReportDependencies();

        sut.run(g, arch);
        assertEquals(3, sut.m_dependencyReport.size());

        ReportDependencies.Dependency dA = getDependency(sut.m_dependencyReport, g.getNode("A"));
        ReportDependencies.Dependency dB = getDependency(sut.m_dependencyReport, g.getNode("B"));
        ReportDependencies.Dependency dC = getDependency(sut.m_dependencyReport, g.getNode("C"));

        assertDependencyReportFan(dA, 1, 1, 0);
        assertDependencyReportFan(dB, 0, 1, 0);
        assertDependencyReportFan(dC, 1, 0, 0);

        assertDependencyReportFanOut(dA, 1, 1, 0);
        assertDependencyReportFanOut(dB, 0, 0, 0);
        assertDependencyReportFanOut(dC, 0, 0, 0);

        assertDependencyReportCouplingOut(dA, 1, 1, 0);
        assertDependencyReportCouplingOut(dB, 0, 0, 0);
        assertDependencyReportCouplingOut(dC, 0, 0, 0);

        assertDependencyReportCoupling(dA, 1, 1, 0);
        assertDependencyReportCoupling(dB, 0, 1, 0);
        assertDependencyReportCoupling(dC, 1, 0, 0);

    }

    @Test
    public void oneComponentTestWithExternal() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(new String [] {"AB"});
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef arch = new se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef();
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef.Component cA = arch.addComponent("A");

        cA.mapToNode(g.getNode("A"));

        ReportDependencies sut = new ReportDependencies();

        sut.run(g, arch);
        assertEquals(1, sut.m_dependencyReport.size());

        ReportDependencies.Dependency dA = getDependency(sut.m_dependencyReport, g.getNode("A"));
        assertDependencyReportFan(dA, 0, 0, 1);
        assertDependencyReportFanOut(dA, 0, 0, 1);
        assertDependencyReportCouplingOut(dA, 0, 0, 1);
        assertDependencyReportCoupling(dA, 0, 0, 1);
    }

    private ReportDependencies.Dependency getDependency(Iterable<ReportDependencies.Dependency> a_dependencyReport, CNode a_node) {
        ReportDependencies.Dependency ret = null;

        for (ReportDependencies.Dependency d : a_dependencyReport) {
            if (d.m_node == a_node) {
                ret = d;
                break;
            }
        }

        assertNotNull(ret);
        return ret;
    }
}
