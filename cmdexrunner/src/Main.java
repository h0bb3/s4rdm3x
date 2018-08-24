import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import se.lnu.siq.s4rdm3x.experiments.ExperimentRunner;
import se.lnu.siq.s4rdm3x.experiments.RunFileSaver;
import se.lnu.siq.s4rdm3x.experiments.metric.Metric;
import se.lnu.siq.s4rdm3x.experiments.metric.Rand;
import se.lnu.siq.s4rdm3x.experiments.system.JabRef;
import se.lnu.siq.s4rdm3x.experiments.system.System;
import se.lnu.siq.s4rdm3x.experiments.system.TeamMates;

public class Main {

    public static Metric getMetric(String a_metric) {
        if (a_metric.equalsIgnoreCase("rand")) {
            return new Rand();
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


        java.lang.System.out.println("Running Experiment...");

        if (a_args.length == 2) {
            System sua = getSystem(a_args[1]);
            if (sua != null) {
                Metric m = getMetric(a_args[2]);
                if (m != null) {
                    Graph graph = new MultiGraph("main");
                    RunFileSaver fs = new RunFileSaver(sua.getName() + "_" + m.getName());
                    ExperimentRunner exr = new ExperimentRunner(sua, m);
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
