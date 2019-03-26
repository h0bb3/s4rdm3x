package se.lnu.siq.s4rdm3x;

import org.junit.jupiter.api.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

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
}
