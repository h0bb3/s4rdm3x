package se.lnu.siq.s4rdm3x;

import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.experiments.system.FileBased;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuickTest {

    @Test
    public void scriptTest() {
        ScriptEngineManager scm = new ScriptEngineManager();


        for (ScriptEngineFactory f : scm.getEngineFactories()) {
            System.out.println(f.getLanguageName());
        }

        ScriptEngine sce = scm.getEngineByName("java");



        if (sce != null) {
            try {
                sce.eval("public class ScriptMain { public static int main(String args[]){ System.out.println(\"Hello Script World\");} }");
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }


    }

    /*@Test
    public void dumpSystem() {
        try {

            FileBased system = new FileBased("C:/hObbE/projects/coding/github/s4rdm3x/data/systems/ProM6.9/ProM_6_9.sysmdl");
            //FileBased system = new FileBased("C:/hObbE/projects/coding/github/s4rdm3x/data/systems/teammates/teammates.sysmdl");
            CGraph g = new CGraph();
            system.load(g);
            ArchDef a = system.createAndMapArch(g);

            // org.processmining.framework.plugin.annotations.CLI

            CNode n = g.getNode("org/processmining/framework/plugin/annotations/CLI.java");
            assertEquals(1, n.getClassCount());


        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }*/
}
