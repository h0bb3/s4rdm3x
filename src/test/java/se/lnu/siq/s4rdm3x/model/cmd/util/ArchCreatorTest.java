package se.lnu.siq.s4rdm3x.model.cmd.util;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.Selector;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ArchCreatorTest {

    @Test
   public void createSystemModelCheckSameMapping() {

        SystemModelReader expected = new SystemModelReader();

        expected.m_name = "test with spaces";
        expected.m_modules.add(new SystemModelReader.Module("C1"));
        expected.m_modules.add(new SystemModelReader.Module("C2"));
        expected.m_mappings.add(new SystemModelReader.Mapping(expected.m_modules.get(0).m_name, "java\\.node\\.test(?!(2))"));
        expected.m_mappings.add(new SystemModelReader.Mapping(expected.m_modules.get(1).m_name, "java\\.node\\.test2"));

        ArchCreator sut = new ArchCreator();
        ArchDef arch = new ArchDef();
        ArchDef.Component c2 = arch.addComponent(expected.m_modules.get(1).m_name);
        ArchDef.Component c1 = arch.addComponent(expected.m_modules.get(0).m_name);

        ArrayList<CNode> nodes = new ArrayList<CNode>();
        CNode n1 = new CNode("n1", -1);
        n1.addClass(new dmClass("java.node.test"));
        CNode n2 = new CNode("n2", -1);
        n2.addClass(new dmClass("java.node.test2"));

        c1.mapToNode(n1);
        c2.mapToNode(n2);
        nodes.add(n1);
        nodes.add(n2);


        SystemModelReader actual = sut.createSystemModel(arch, nodes, expected.m_name);

        assertEqualsSMR(expected, actual);
        assertEqualMapping(expected, actual, new String[]{"java.node.test", "java.node.test2"});

    }

    private void assertEqualMapping(SystemModelReader a_expected, SystemModelReader a_actual, String [] a_nodeNames) {
        for (String nodeName : a_nodeNames) {

            assertEquals(getMapping(a_expected, nodeName), getMapping(a_actual, nodeName));
        }
    }

    private String getMapping(SystemModelReader a_sysModel, String a_nodeName) {
        String ret = "n/a";
        CNode n = new CNode("n", -1);
        n.addClass(new dmClass(a_nodeName));
        for (SystemModelReader.Mapping m : a_sysModel.m_mappings) {
            Selector.Pat p = new Selector.Pat(m.m_regexp);
            if (p.isSelected(n)) {
                ret = m.m_moduleName;
            }
        }

        return ret;
    }

    private void assertEqualsSMR(SystemModelReader a_expected, SystemModelReader a_actual) {
        assertEquals(a_expected.m_name, a_actual.m_name);
        assertEquals(a_expected.m_modules.size(), a_actual.m_modules.size());
    }
}