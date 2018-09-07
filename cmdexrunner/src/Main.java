import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import se.lnu.siq.s4rdm3x.cmd.saerocon18.ClusterExperiment10;
import se.lnu.siq.s4rdm3x.experiments.ExperimentRunner;
import se.lnu.siq.s4rdm3x.experiments.RunFileSaver;
import se.lnu.siq.s4rdm3x.experiments.metric.*;
import se.lnu.siq.s4rdm3x.experiments.system.System;
import se.lnu.siq.s4rdm3x.experiments.system.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    final static String[] g_metrics = {"rand", "fanin", "fanout", "fan", "maxfan", "minfan", "avgfan"};

    public static Metric getMetric(String a_metric) {
        if (a_metric.equalsIgnoreCase(g_metrics[0])) {
            return new Rand();
        } else if (a_metric.equalsIgnoreCase(g_metrics[1])) {
            return new FanIn();
        } else if (a_metric.equalsIgnoreCase(g_metrics[2])) {
            return new FanOut();
        } else if (a_metric.equalsIgnoreCase(g_metrics[3])) {
            return new Fan();
        } else if (a_metric.equalsIgnoreCase(g_metrics[4])) {
            return new MaxFan();
        } else if (a_metric.equalsIgnoreCase(g_metrics[5])) {
            return new MinFan();
        } else if (a_metric.equalsIgnoreCase(g_metrics[6])) {
            return new AvgFan();
        }

        return null;
    }

    public static System getSystem(String a_systemName)  {

        if (a_systemName.compareToIgnoreCase("jabref") == 0) {
            return new JabRef_3_7();

        } else if (a_systemName.compareToIgnoreCase("teammates") == 0) {
            return new TeamMates();
        } else if (a_systemName.compareToIgnoreCase("jabrefsaerocon18") == 0) {
            return new JabRefSAEroCon18();
        } else {
            try {
                return new FileBased(a_systemName);
            } catch (Exception e) {
                java.lang.System.out.println(e.toString());
                return null;
            }
        }
    }

    private static class ExThread extends Thread {
        private int m_ix;
        Metric m_metric;
        System m_sua;
        RunFileSaver m_fs;
        ExperimentRunner m_exr;

        public ExThread(System a_sua, Metric a_metric, int a_index) {
            m_ix = a_index;
            m_metric = a_metric;
            m_sua = a_sua;
        }

        public void run() {
            java.lang.System.out.println("Running Experiment " + m_ix + "...");
            Graph graph = new MultiGraph("main" + m_ix);
            m_fs = new RunFileSaver(m_sua.getName(), m_metric.getName(), true);
            m_exr = new ExperimentRunner(m_sua, m_metric);
            m_exr.setRunListener(m_fs);
            m_exr.run(graph);
        }

        public ExperimentRunner.State getExState() {
            if (m_exr != null) {
                return m_exr.getState();
            } else {
                return ExperimentRunner.State.Idle;
            }
        }

        public void halt() {
            if (m_exr != null) {
                m_exr.stop();
            }
        }

        public int getRows() {
            if (m_fs != null) {
                return m_fs.getRunCount();
            } else {
                return 0;
            }
        }

    }

    private static ArrayList<ExThread> startThreads(int a_theadCount, System a_sua, Metric a_metric) {
        ArrayList<ExThread> ret = new ArrayList<>();
        for(int i = 0; i < a_theadCount; i++) {
            final int ix = i;

            // need to make a class of this so we can check te no rows
            ExThread r = new ExThread(a_sua, a_metric, ix);
            ret.add(r);
            Thread t = new Thread(r);
            t.start();
        }
        return ret;
    }

    private static String getMetricsString() {
        String metrics = "";
        for (String m : g_metrics) {
            if (metrics.length() > 0) {
                metrics += "|";
            }
            metrics += m;
        }
        return metrics;
    }

    private static int sumRows(Iterable<ExThread> a_threads) {
        int rows = 0;

        for (ExThread et: a_threads) {
            rows += et.getRows();
        }

        return rows;
    }

    private static boolean allIdle(Iterable<ExThread> a_threads) {
        for (ExThread et: a_threads) {
            if (et.getExState() != ExperimentRunner.State.Idle) {
                return false;
            }
        }
        return true;
    }

    private static int getInitialRows(String a_dir1, String a_dir2) {
        String fileName = a_dir1 + File.separator + a_dir2; // This is a hidden dependency to RunFileSaver
        try {
            int ret = 0;
            Stream<Path> paths = Files.walk(Paths.get(fileName));

            for (Path p : paths.filter(Files::isRegularFile).filter((path) -> path.toString().endsWith(".csv")).collect(Collectors.toList())) {
                ret += Files.readAllLines(p).size() - 1;    // first row is header...
            }

            return ret;

        } catch (Exception e) {
            return 0;
        }
    }


    public static void main(String[] a_args) {

        if (a_args.length == 3) {
            int threadCount = Integer.parseInt(a_args[0]);
            if (threadCount < 1) {
                threadCount = 1;
            }
            System sua = getSystem(a_args[1]);
            if (sua != null) {
                Metric m = getMetric(a_args[2]);
                if (m != null) {
                    startThreads(threadCount, sua, m);
                    while (true) {
                        try{Thread.sleep(1000);} catch (Exception e) {};
                    }
                } else {
                    java.lang.System.out.println("Unknow metric: " + a_args[2]);
                    java.lang.System.out.println("Use: " + getMetricsString());
                }
            } else {
                java.lang.System.out.println("Unknown system: " + a_args[1]);
                java.lang.System.out.println("Use: jabref|jabrefsaerocon18|teammates");
            }
        } else if (a_args.length == 2) {
            int threadCount = Integer.parseInt(a_args[0]);
            if (threadCount < 1) {
                threadCount = 1;
            }
            System sua = getSystem(a_args[1]);
            if (sua != null) {
                java.lang.System.out.println("Running experiments on all metrics for 500000 rows: " + sua.getName());

                for (String mStr : g_metrics) {
                    Metric m = getMetric(mStr);
                    if (m != null) {
                        final int rowLimit = 50000;
                        int initialRows = getInitialRows(sua.getName(), m.getName());
                        if (initialRows < rowLimit) {
                            java.lang.System.out.println("Running experiments on metrics: " + m.getName());


                            ArrayList<ExThread> threads = startThreads(threadCount, sua, m);

                            while (initialRows + sumRows(threads) < rowLimit) {
                                try {Thread.sleep(1000);} catch (Exception e) {}
                            }
                            for (ExThread et : threads) {
                                et.halt();
                            }
                            while (!allIdle(threads)) {
                                try {Thread.sleep(100);} catch (Exception e) {}
                            }
                        }
                    }
                }
                java.lang.System.out.println("All Done!");

            } else {
                java.lang.System.out.println("Unknown system: " + a_args[1]);
                java.lang.System.out.println("Use: jabref|jabrefsaerocon18|teammates");
            }
        } else if (a_args.length == 1) {
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
        } else {
                java.lang.System.out.println("Wrong number of arguments supplied.\n Use: threads jabref|teammates " + getMetricsString());
        }
    }
}
