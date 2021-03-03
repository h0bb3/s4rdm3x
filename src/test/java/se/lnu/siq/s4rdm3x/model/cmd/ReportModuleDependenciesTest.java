package se.lnu.siq.s4rdm3x.model.cmd;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.NodeGenerator;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;

import static org.junit.jupiter.api.Assertions.*;

class ReportModuleDependenciesTest {

    @Test
    public void oneInternalDep() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.MethodCall, new String [] {"AB"});
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef arch = new se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef();
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef.Component cA = arch.addComponent("A");
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef.Component cB = arch.addComponent("B");

        cA.mapToNode(g.getNode("A"));
        cA.mapToNode(g.getNode("B"));

        ReportModuleDependencies sut = new ReportModuleDependencies();

        sut.run(g, arch);

        assertEquals(1, sut.countInternalDeps());
        assertEquals(1, sut.countInternalDeps(dmDependency.Type.MethodCall));
        assertEquals(0, sut.countExternalDeps());
    }

    @Test
    public void twoInternalDep() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.MethodCall, new String [] {"AB"});
        ng.addToGraph(g, dmDependency.Type.Extends, new String[] {"AB"});
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef arch = new se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef();
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef.Component cA = arch.addComponent("A");
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef.Component cB = arch.addComponent("B");

        cA.mapToNode(g.getNode("A"));
        cA.mapToNode(g.getNode("B"));

        ReportModuleDependencies sut = new ReportModuleDependencies();

        sut.run(g, arch);

        assertEquals(2, sut.countInternalDeps());
        assertEquals(1, sut.countInternalDeps(dmDependency.Type.MethodCall));
        assertEquals(1, sut.countInternalDeps(dmDependency.Type.Extends));
        assertEquals(0, sut.countExternalDeps());
    }


    @Test
    public void oneExternalDep() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.MethodCall, new String [] {"AB"});
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef arch = new se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef();
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef.Component cA = arch.addComponent("A");
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef.Component cB = arch.addComponent("B");

        cA.mapToNode(g.getNode("A"));
        cB.mapToNode(g.getNode("B"));

        ReportModuleDependencies sut = new ReportModuleDependencies();

        sut.run(g, arch);

        assertEquals(0, sut.countInternalDeps());
        assertEquals(1, sut.countExternalDeps());
        assertEquals(1, sut.countExternalDeps(dmDependency.Type.MethodCall));
    }

    @Test
    public void twoExternalDep() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.MethodCall, new String [] {"AB"});
        ng.addToGraph(g, dmDependency.Type.Extends, new String[] {"AB"});
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef arch = new se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef();
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef.Component cA = arch.addComponent("A");
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef.Component cB = arch.addComponent("B");

        cA.mapToNode(g.getNode("A"));
        cB.mapToNode(g.getNode("B"));

        ReportModuleDependencies sut = new ReportModuleDependencies();

        sut.run(g, arch);

        assertEquals(0, sut.countInternalDeps());
        assertEquals(2, sut.countExternalDeps());
        assertEquals(1, sut.countExternalDeps(dmDependency.Type.MethodCall));
        assertEquals(1, sut.countExternalDeps(dmDependency.Type.Extends));
    }

    @Test
    public void circularExternalDep() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.MethodCall, new String [] {"AB"});
        ng.addToGraph(g, dmDependency.Type.Extends, new String[] {"BA"});
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef arch = new se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef();
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef.Component cA = arch.addComponent("A");
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef.Component cB = arch.addComponent("B");

        cA.mapToNode(g.getNode("A"));
        cB.mapToNode(g.getNode("B"));

        ReportModuleDependencies sut = new ReportModuleDependencies();

        sut.run(g, arch);

        assertEquals(0, sut.countInternalDeps());
        assertEquals(2, sut.countExternalDeps());
        assertEquals(1, sut.countExternalDeps(dmDependency.Type.MethodCall));
        assertEquals(1, sut.countExternalDeps(dmDependency.Type.Extends));
    }

    @Test
    public void circularInternalDep() {
        NodeGenerator ng = new NodeGenerator();
        CGraph g = ng.generateGraph(dmDependency.Type.MethodCall, new String [] {"AB", "BA"});
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef arch = new se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef();
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef.Component cA = arch.addComponent("A");
        se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef.Component cB = arch.addComponent("B");

        cA.mapToNode(g.getNode("A"));
        cA.mapToNode(g.getNode("B"));

        ReportModuleDependencies sut = new ReportModuleDependencies();

        sut.run(g, arch);

        assertEquals(2, sut.countInternalDeps());
        assertEquals(2, sut.countInternalDeps(dmDependency.Type.MethodCall));
        assertEquals(0, sut.countExternalDeps());
    }

}