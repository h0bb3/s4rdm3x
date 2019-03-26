package se.lnu.siq.s4rdm3x;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerPipe;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;

public class Main {



    public static void main(String[] a_args) {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");


        //C.io.setTitle("Hello World");
        GUIConsole guic = new GUIConsole();

        ArchDef arch = null;  // this is set by loading the arch

        //CGraph graph = new CGraph();
        Graph graph = new MultiGraph("main_graph");
        graph.addAttribute("ui.antialias");
        graph.addAttribute("ui.quality", 4);
        graph.setAttribute("ui.stylesheet", "url(data/style.css);");
        graph.addAttribute("ui.title", "Graph");
        graph.addAttribute("layout.stabilization-limit", 1.0);


        Viewer view = graph.display();
        graph.setAttribute("view", view);


        ViewerPipe vp = view.newViewerPipe();
        vp.addViewerListener(new ClickListener(graph));
        vp.addSink(graph);

        /*{
            JabRef_3_5 jr = new JabRef_3_5();
            jr.load(graph);
            jr.createAndMapArch(graph);
        }*/

        StringCommandHandler sch = new StringCommandHandler();


        while (true) {
            try {
                vp.pump();
                Thread.sleep(10);

                //view.
                //view.disableAutoLayout();
                /*for (Edge e : graph.getEachEdge()) {
                    if (!e.hasAttribute("layoutEdge")) {
                        GraphicEdge ge = view.getGraphicGraph().getEdge(e.getId());
                        GraphicNode g1, g2;
                        g1 = ge.getNode0();
                        g2 = ge.getNode1();

                        double x1 = g1.getX();
                        double y1 = g1.getY();
                        double x2 = g2.getX();
                        double y2 = g2.getY();
                        double len = Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)) * 0.99;
                        if (len < 7) {
                            len = 7;
                        }
                        e.setAttribute("layout.weight", len);
                    }
                }*/
                //view.enableAutoLayout();

                if (guic.hasInput()) {

                    String in = guic.popInput();
                    //sch.execute(in, graph).forEach(str -> guic.println(str));
                    System.out.println("This is deprecated and should be converted to a visualization only");
                }
            } catch (Exception e) {
                guic.println(e.toString());
                e.printStackTrace();
            }
        }
    }

}
