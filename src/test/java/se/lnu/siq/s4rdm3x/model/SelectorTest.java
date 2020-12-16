package se.lnu.siq.s4rdm3x.model;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;

import static org.junit.jupiter.api.Assertions.*;

class SelectorTest {

    @Test
    public void testPatternSelector1() {
        Selector.Pat sut = new Selector.Pat("org\\.lang\\..*");
        CNode n1 = new CNode("n1", -1);

        n1.addClass(new dmClass("org.lang.myclass"));

        assertTrue(sut.isSelected(n1));
    }

    @Test
    public void testPatternSelector2() {
        Selector.Pat sut = new Selector.Pat("org\\.lang\\.myclass");
        CNode n1 = new CNode("n1", -1);

        n1.addClass(new dmClass("org.lang.myclass"));

        assertTrue(sut.isSelected(n1));
    }

    @Test
    public void testPatternSelector3() {
        Selector.Pat sut = new Selector.Pat("org\\.lang\\..*");
        CNode n1 = new CNode("n1", -1);

        n1.addClass(new dmClass("org.langmyclass"));

        assertFalse(sut.isSelected(n1));
    }

    @Test
    public void testPatternSelector4() {
        Selector.Pat sut = new Selector.Pat("org\\.apache\\.tools\\.ant\\.taskdefs(?!(\\.optional|\\.compilers|\\.condition|\\.rmic|\\.cvslib|\\.email|\\.repository))\\.*");
        CNode n1 = new CNode("n1", -1);

        n1.addClass(new dmClass("org.apache.tools.ant.taskdefs.Javadoc"));

        assertTrue(sut.isSelected(n1));
    }


}