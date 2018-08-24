import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import se.lnu.siq.s4rdm3x.experiments.ExperimentRunner;
import se.lnu.siq.s4rdm3x.experiments.RunFileSaver;
import se.lnu.siq.s4rdm3x.experiments.metric.*;
import se.lnu.siq.s4rdm3x.experiments.system.System;
import se.lnu.siq.s4rdm3x.experiments.system.*;

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
                java.lang.System.out.println("Use: jabref|teammates");
            }
        } else {
            java.lang.System.out.println("Wrong number of arguments supplied.\n Use: jabref|teammates rand|fanin|...");
        }
    }
}
