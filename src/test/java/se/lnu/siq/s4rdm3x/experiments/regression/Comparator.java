package se.lnu.siq.s4rdm3x.experiments.regression;

import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class Comparator {

    public boolean assertEquals(CGraph a_expected, CGraph a_actual) {

        assertAllNodesFound(a_expected, a_actual);
        assertNoUnexpectedNodes(a_expected, a_actual);

        a_expected.getNodes().forEach(eNode -> {
            assertAllClassesFound(eNode, a_actual.getNodeByName(eNode.getName()));});
        a_expected.getNodes().forEach(eNode -> {
            assertNoUnexpectedClasses(eNode, a_actual.getNodeByName(eNode.getName()));});

        a_expected.getNodes().forEach(eNode -> {
            assertAllClassesEqualText(eNode, a_actual.getNodeByName(eNode.getName()));});

        a_expected.getNodes().forEach(eNode -> {
            assertAllClassesEqualDependencies(eNode, a_actual.getNodeByName(eNode.getName()));});

        return false;
    }

    private void assertAllClassesEqualText(CNode a_expected, CNode a_actual) {

        for(dmClass ec : a_expected.getClasses()) {
            dmClass ac = a_actual.getClassByName(ec.getName());
            Iterable<String> expected = ec.getTexts();
            Iterable<String> actual = ac.getTexts();

            Iterator<String> aIt = actual.iterator();

            expected.forEach( et -> {
                String at = aIt.next();
                org.junit.jupiter.api.Assertions.assertEquals(et, at, "Could not find text: " + et + " in class: " + ac.getName() + " in Node: " + a_actual.getName());
            });

            assertFalse(aIt.hasNext(), "Class: " + ac.getName() + " has more texts than expected.");
        }
    }

    private void assertAllClassesEqualDependencies(CNode a_expected, CNode a_actual) {

        for(dmClass ec : a_expected.getClasses()) {
            dmClass ac = a_actual.getClassByName(ec.getName());
            Iterable<dmDependency> expected = ec.getDependencies();
            Iterable<dmDependency> actual = ac.getDependencies();

            Iterator<dmDependency> aIt = actual.iterator();

            String classNode = " in class: " + ac.getName() + " in Node: " + a_actual.getName();

            expected.forEach( ed -> {
                dmDependency ad = aIt.next();
                org.junit.jupiter.api.Assertions.assertEquals(ed.getTarget().getName(), ad.getTarget().getName(), "Could not find dependency to: " + ed.getTarget().getName() + classNode);
                org.junit.jupiter.api.Assertions.assertEquals(ed.getType(), ad.getType(), "Different dependency types in " + classNode);
                org.junit.jupiter.api.Assertions.assertEquals(ed.getCount(), ad.getCount(), "Different dependency count in " + classNode);
            });

            assertFalse(aIt.hasNext(), "More dependencies than expected in " + classNode);
        }
    }

    private void assertAllClassesFound(CNode a_expected, CNode a_actual) {
        for(dmClass ec : a_expected.getClasses()) {
            assertNotNull(a_actual.getClassByName(ec.getName()), "Could not find class: " + ec.getName() + " in actual node: " + a_actual.getName());
        }
    }

    private void assertAllNodesFound(final CGraph a_expected, final CGraph a_actual) {
        // check that we can find all nodes
        for (CNode eNode : a_expected.getNodes()) {
            assertNotNull(a_actual.getNodeByName(eNode.getName()), "Could not find node: " + eNode.getName() + " in actual.");
        }
    }

    private void assertNoUnexpectedNodes(final CGraph a_expected, final CGraph a_actual) {
        // check that we do not have any unexpected nodes
        for (CNode aNode : a_actual.getNodes()) {
            assertNotNull(a_expected.getNodeByName(aNode.getName()), "Unexpected node found in actual: " + aNode.getName());
        }
    }

    private void assertNoUnexpectedClasses(CNode a_expected, CNode a_actual) {
        // check that we do not have any unexpected nodes
        for(dmClass ac : a_actual.getClasses()) {
            assertNotNull(a_expected.getClassByName(ac.getName()), "Unexpected class found: " + ac.getName() + ", in actual node: " + a_actual.getName());
        }
    }


}
