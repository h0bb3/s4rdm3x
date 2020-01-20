package se.lnu.siq.s4rdm3x;

import se.lnu.siq.s4rdm3x.model.CGraph;

public class Main {



    public static void main(String[] a_args) {

        GUIConsole guic = new GUIConsole();

        StringCommandHandler sch = new StringCommandHandler();
        CGraph graph = new CGraph();

        while (true) {
            try {
                Thread.sleep(10);
                if (guic.hasInput()) {

                    String in = guic.popInput();
                    sch.execute(in, graph).forEach(str -> guic.println(str));
                }
            } catch (Exception e) {
                guic.println(e.toString());
                e.printStackTrace();
            }
        }
    }

}
