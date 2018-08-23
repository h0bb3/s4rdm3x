import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import se.lnu.siq.s4rdm3x.experiments.JabRefRand;
import se.lnu.siq.s4rdm3x.experiments.RunFileSaver;
import se.lnu.siq.s4rdm3x.experiments.TeamMatesRand;

public class Main {

    public static void runJabRefExperiment(String a_ex) {
        Graph graph = new MultiGraph("main");
        RunFileSaver fs;

        if (a_ex.compareTo("1") == 0) {
            JabRefRand ex = new JabRefRand();
            fs = new RunFileSaver("JabRef_rand");
            ex.setRunListener(fs);
            System.out.println("Running JabRef experiment " + a_ex);
            ex.run(graph);
        } else {
            System.out.println("Unknown JabRef experiment: " + a_ex);
        }
    }

    public static void runTeamMatesExperiment(String a_ex) {
        Graph graph = new MultiGraph("main");
        RunFileSaver fs;

        if (a_ex.compareTo("1") == 0) {
            TeamMatesRand ex = new TeamMatesRand();
            fs = new RunFileSaver("TeamMates_rand");
            ex.setRunListener(fs);
            System.out.println("Running TeamMates experiment " + a_ex);
            ex.run(graph);
        } else {
            System.out.println("Unknown TeamMates experiment: " + a_ex);
        }
    }

    public static void main(String[] a_args) {


        System.out.println("Running Experiment...");

        if (a_args.length == 2) {
            if (a_args[0].compareToIgnoreCase("jabref") == 0) {
                runJabRefExperiment(a_args[1]);
            } else if (a_args[0].compareToIgnoreCase("teammates") == 0) {
                runTeamMatesExperiment(a_args[1]);
            } else {
                System.out.println("Unknown system: " + a_args[0]);
                System.out.println("Use: jabref|teammates");
            }
        } else {
            System.out.println("Wrong number of arguments supplied.\n Use: jabref|teammates 1|2|3...");
        }
    }
}
