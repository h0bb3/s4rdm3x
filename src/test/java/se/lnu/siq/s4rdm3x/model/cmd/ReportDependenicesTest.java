package se.lnu.siq.s4rdm3x.model.cmd;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.NodeGenerator;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ReportDependenicesTest {

    private void assertDependencyReportCounts(ReportDependencies.Dependency a_sut, int a_expectedInternalDependencyCount, int a_expectedExternalDependencyCount, int a_expectedUnmappedDependencyCount) {
        assertEquals(a_expectedInternalDependencyCount, a_sut.getInternalDependencyCount());
        assertEquals(a_expectedExternalDependencyCount, a_sut.getExternalDependencyCount());
        assertEquals(a_expectedUnmappedDependencyCount, a_sut.getUnmappedDependencyCount());
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

        assertDependencyReportCounts(sut.m_dependencyReport.get(0), 0, 1, 0);
        assertDependencyReportCounts(sut.m_dependencyReport.get(1), 0, 1, 0);
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

        assertDependencyReportCounts(getDependency(sut.m_dependencyReport, g.getNode("A")), 0, 2, 0);
        assertDependencyReportCounts(getDependency(sut.m_dependencyReport, g.getNode("B")), 0, 1, 0);
        assertDependencyReportCounts(getDependency(sut.m_dependencyReport, g.getNode("C")), 0, 1, 0);
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

        assertDependencyReportCounts(sut.m_dependencyReport.get(0), 0, 3, 0);
        assertDependencyReportCounts(sut.m_dependencyReport.get(1), 0, 3, 0);
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

        assertDependencyReportCounts(sut.m_dependencyReport.get(0), 1, 0, 0);
        assertDependencyReportCounts(sut.m_dependencyReport.get(1), 1, 0, 0);
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

        assertDependencyReportCounts(getDependency(sut.m_dependencyReport, g.getNode("A")), 1, 1, 0);
        assertDependencyReportCounts(getDependency(sut.m_dependencyReport, g.getNode("B")), 0, 1, 0);
        assertDependencyReportCounts(getDependency(sut.m_dependencyReport, g.getNode("C")), 1, 0, 0);
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
