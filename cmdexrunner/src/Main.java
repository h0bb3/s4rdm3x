import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import se.lnu.siq.s4rdm3x.cmd.saerocon18.ClusterExperiment10;
import se.lnu.siq.s4rdm3x.experiments.ExperimentRunner;
import se.lnu.siq.s4rdm3x.experiments.RunFileSaver;
import se.lnu.siq.s4rdm3x.experiments.metric.*;
import se.lnu.siq.s4rdm3x.experiments.system.System;
import se.lnu.siq.s4rdm3x.experiments.system.*;

import java.util.ArrayList;

public class Main {

    public static Metric getMetric(String a_metric) {
        if (a_metric.equalsIgnoreCase("rand")) {
            return new Rand();
        } else if (a_metric.equalsIgnoreCase("fanin")) {
            return new FanIn();
        } else if (a_metric.equalsIgnoreCase("fanout")) {
            return new FanOut();
        }

        return null;
    }

    public static System getSystem(String a_systemName)  {

        if (a_systemName.compareToIgnoreCase("jabref") == 0) {
            return new JabRef();

        } else if (a_systemName.compareToIgnoreCase("teammates") == 0) {
            return new TeamMates();
        } else if (a_systemName.compareToIgnoreCase("jabrefsaerocon18") == 0) {
            return new JabRefSAEroCon18();
        }

        return null;
    }

    public static void main(String[] a_args) {



        if (a_args.length == 2) {
            System sua = getSystem(a_args[0]);
            if (sua != null) {
                Metric m = getMetric(a_args[1]);
                if (m != null) {
                    Graph graph = new MultiGraph("main");
                    RunFileSaver fs = new RunFileSaver(sua.getName() + "_" + m.getName());
                    ExperimentRunner exr = new ExperimentRunner(sua, m);
                    exr.setRunListener(fs);

                    java.lang.System.out.println("Running Experiment...");
                    exr.run(graph);
                } else {
                    java.lang.System.out.println("Unknown metric: " + a_args[0]);
                    java.lang.System.out.println("Use: rand|fanin");
                }
            } else {
                java.lang.System.out.println("Unknown system: " + a_args[0]);
                java.lang.System.out.println("Use: jabref|jabrefsaerocon18|teammates");
            }
        } if (a_args.length == 1) {
            if (a_args[0].equalsIgnoreCase("ex10")) {
                java.lang.System.out.println("Running Old Experiment Ex10");
                ClusterExperiment10 c = new ClusterExperiment10((ArrayList<String> a_row) -> {
                    String out = "";
                    for (String s : a_row) {
                        out += s + "\t";
                    }
                    //java.lang.System.out.println(out);
                });
                Graph graph = new MultiGraph("main");
                c.run(graph);
            }
        }
        else {
            java.lang.System.out.println("Wrong number of arguments supplied.\n Use: jabref|teammates rand|fanin|...");
        }
    }
}
