import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import se.lnu.siq.metrics.CSVRow;
import se.lnu.siq.s4rdm3x.experiments.ExperimentRunner;
import se.lnu.siq.s4rdm3x.experiments.RunFileSaver;
import se.lnu.siq.s4rdm3x.experiments.metric.*;
import se.lnu.siq.s4rdm3x.experiments.metric.aggregated.*;
import se.lnu.siq.s4rdm3x.experiments.system.System;
import se.lnu.siq.s4rdm3x.experiments.system.*;
import se.lnu.siq.s4rdm3x.model.CGraph;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {



    private static String[] getExperimentMetricsArray() {
        final String[] metrics = {"rand", "linecount", "fanin", "fanout", "noc", "nof", "nom", "nop", "rank", "cin", "cout", "bccc", "bci", "lcomhs"};


        String[] ret = new String[metrics.length + metrics.length - 2];
        for (int ix = 0; ix < metrics.length; ix++) {
            ret[ix] = metrics[ix];
        }
        for (int ix = 2; ix < metrics.length; ix++) {
            ret[metrics.length + ix - 2] = "lcrel_" + metrics[ix];
        }

        return ret;
    }

    private static String[] getInternalMetricsArray() {
        final String[] metrics = {"rand", "linecount", "fanin", "fanout", "fan", "maxfan", "minfan", "avgfan", "noc", "nof", "nom", "nop", "rank", "cin", "cout", "bccc", "bci", "lcomhs"};

        String[] ret = new String[metrics.length + metrics.length - 2];
        for (int ix = 0; ix < metrics.length; ix++) {
            ret[ix] = metrics[ix];
        }
        for (int ix = 2; ix < metrics.length; ix++) {
            ret[metrics.length + ix - 2] = "lcrel_" + metrics[ix];
        }

        return ret;
    }

    public static Metric getMetric(String a_metric) {
        String[] metrics = getInternalMetricsArray();
        int ix = 0;
        if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new Rand();
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new LineCount();
        }else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new FanIn();
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new FanOut();
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new Fan();
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new MaxFan();
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new MinFan();
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new AvgFan();
        }  else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new NumberOfChildren();
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new NumberOfFields();
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new NumberOfMethods();
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new NumberOfParents();
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new Rank();
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new CouplingIn();
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new CouplingOut();
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new ByteCodeCyclomaticComplexity();
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new ByteCodeInstructions();
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new LCOMHS();
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new RelativeLineCount(new FanIn());
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new RelativeLineCount(new FanOut());
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new RelativeLineCount(new Fan());
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new RelativeLineCount(new MaxFan());
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new RelativeLineCount(new MinFan());
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new RelativeLineCount(new AvgFan());
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new RelativeLineCount(new NumberOfChildren());
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new RelativeLineCount(new NumberOfFields());
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new RelativeLineCount(new NumberOfMethods());
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new RelativeLineCount(new NumberOfParents());
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new RelativeLineCount(new Rank());
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new RelativeLineCount(new CouplingIn());
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new RelativeLineCount(new CouplingOut());
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new RelativeLineCount(new ByteCodeCyclomaticComplexity());
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new RelativeLineCount(new ByteCodeInstructions());
        } else if (a_metric.equalsIgnoreCase(metrics[ix++])) {
            return new RelativeLineCount(new LCOMHS());
        }

        return null;
    }

    public static System getSystem(String a_systemName)  {

        if (a_systemName.compareToIgnoreCase("jabref") == 0) {
            //return new JabRef_3_7();
            return null;

        } else if (a_systemName.compareToIgnoreCase("teammates") == 0) {
            //return new TeamMates();
            return null;
        } else if (a_systemName.compareToIgnoreCase("jabrefsaerocon18") == 0) {
            //return new JabRefSAEroCon18();
            return null;
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
        boolean m_doSaveMappings;

        public ExThread(System a_sua, Metric a_metric, int a_index, boolean a_doSaveMappings) {
            m_ix = a_index;
            m_metric = a_metric;
            m_sua = a_sua;
            m_doSaveMappings = a_doSaveMappings;
        }

        public void run() {
            java.lang.System.out.print("" + m_ix + ", ");
            CGraph graph = new CGraph();
            m_fs = new RunFileSaver(m_sua.getName(), m_metric.getName(), m_doSaveMappings);
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

    private static ArrayList<ExThread> startThreads(int a_threadCount, System a_sua, Metric a_metric, boolean a_doSaveMappings) {
        ArrayList<ExThread> ret = new ArrayList<>();
        java.lang.System.out.print("Running experiments: ");
        for(int i = 0; i < a_threadCount; i++) {
            final int ix = i;

            // need to make a class of this so we can check te no rows
            ExThread r = new ExThread(a_sua, a_metric, ix, a_doSaveMappings);
            ret.add(r);
            Thread t = new Thread(r);
            t.start();
        }
        return ret;
    }

    private static String getMetricsString() {
        String metrics = "";
        for (String m : getInternalMetricsArray()) {
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

    public static void main2(String[] a_args) {
        // accepted arguments
        // -threads thread count number optional default 1
        // -sys system or system model file
        // -metric metric (optional)
        // -mapping saves mappings (optional)
        // -count the number of rows to (optional)
        final CmdArgsHandler args = new CmdArgsHandler(a_args);
        final String system = args.getArgumentString("-system");
        final String metric = args.getArgumentString("-metric");
        boolean saveMappings = args.getArgumentBool("-mapping", false);
        final int rowLimit = args.getArgumentInt("-count", 50000);
        final int threadCount = args.getArgumentInt("-threads", 1);

        System sua = getSystem(system);
        Metric m = getMetric(metric);

        if (sua != null) {
            if (m == null && metric.length() > 0) {
                // could not find any metric with that name
            } else if (m != null) {
                int initialRows = getInitialRows(sua.getName(), m.getName());
                run(threadCount, rowLimit - initialRows, sua, m, saveMappings);
            } else {

                ArrayList<String> metricNames = new ArrayList<>();
                //for(String mStr : getInternalMetricsArray()) metricNames.add(mStr);
                for(String mStr : getExperimentMetricsArray()) metricNames.add(mStr);

                if (sua.getCustomMetricsFile() != null) {
                    CSVRow mRow = new CSVRow();
                    for(String mStr : mRow.getMetricsArray()) metricNames.add(mStr);
                    for(String mStr : mRow.getMetricsArray()) metricNames.add("lcrel_"+mStr);   // all metrics relative to the line count also...
                }

                for (String mStr : metricNames) {
                    m = getMetric(mStr);
                    if (m == null && sua.getCustomMetricsFile() != null) {
                        try {
                            String[] mStrParts = mStr.split("_");
                            if (mStrParts.length > 1) {
                                mStr = mStrParts[1];
                            }
                            List<String> lines = Files.readAllLines(sua.getCustomMetricsFile());
                            CustomMetric cm = new CustomMetric(mStr);
                            int[] globalHeader = null;
                            for (String line : lines) {
                                String parts[] = line.split("\t");
                                if (globalHeader == null) {
                                    CSVRow r = new CSVRow();

                                    globalHeader = r.getHeaderOrder(parts);
                                } else {
                                    CSVRow r = new CSVRow();
                                    r.fromStrings(parts, globalHeader);
                                    cm.addMetric(r.getFileName(), r.getMetric(mStr));
                                }
                            }
                            if (mStrParts.length > 1) {
                                m = new RelativeLineCount(cm);
                            } else {
                                m = cm;
                            }

                        } catch (IOException ioe) {
                            java.lang.System.out.println(ioe.getMessage());
                        }
                    }
                    if (m != null) {

                        int initialRows = getInitialRows(sua.getName(), m.getName());
                        run(threadCount, rowLimit - initialRows, sua, m, saveMappings);
                    }
                }
            }

        } else {
            //printInstructions();
        }

    }

    public static void run(int a_threadCount, int a_rows, System a_sua, Metric a_m, boolean a_doSaveMappings) {
        if (a_rows > 0) {
            java.lang.System.out.println("Running " + a_threadCount + " experiment threads on: " +a_sua.getName() + ":" +  a_m.getName() + " for " + a_rows + " additional rows.");
            if (a_doSaveMappings) {
                java.lang.System.out.println("Also saving experiment mappings.");
            }
            ArrayList<ExThread> threads = startThreads(a_threadCount, a_sua, a_m, a_doSaveMappings);
            java.lang.System.out.println("\nAll experiment threads Started!");

            while (sumRows(threads) < a_rows) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }
            for (ExThread et : threads) {
                et.halt();
            }
            while (!allIdle(threads)) {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }
            }
            java.lang.System.out.println("\nAll experiment threads Done!");
        }
    }


    public static void main(String[] a_args) {

        main2(a_args);

        /*if (a_args.length == 3) {
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
                final int rowLimit = 50000;
                java.lang.System.out.println("Running experiments on all metrics for " + rowLimit + " rows: " + sua.getName());

                ArrayList<String> metricNames = new ArrayList<>();


                for(String mStr : getInternalMetricsArray()) metricNames.add(mStr);

                if (sua.getCustomMetricsFile() != null) {
                    CSVRow mRow = new CSVRow();
                    for(String mStr : mRow.getMetricsArray()) metricNames.add(mStr);
                    for(String mStr : mRow.getMetricsArray()) metricNames.add("lcrel_"+mStr);   // all metrics relative to the line count also...
                }

                for (String mStr : metricNames) {
                    Metric m = getMetric(mStr);
                    if (m == null && sua.getCustomMetricsFile() != null) {
                        try {
                            String[] mStrParts = mStr.split("_");
                            if (mStrParts.length > 1) {
                                mStr = mStrParts[1];
                            }
                            List<String> lines = Files.readAllLines(sua.getCustomMetricsFile());
                            CustomMetric cm = new CustomMetric(mStr);
                            int[] globalHeader = null;
                            for (String line : lines) {
                                String parts[] = line.split("\t");
                                if (globalHeader == null) {
                                    CSVRow r = new CSVRow();

                                    globalHeader = r.getHeaderOrder(parts);
                                } else {
                                    CSVRow r = new CSVRow();
                                    r.fromStrings(parts, globalHeader);
                                    cm.addMetric(r.getFileName(), r.getMetric(mStr));
                                }
                            }
                            if (mStrParts.length > 1) {
                                m = new RelativeLineCount(cm);
                            } else {
                                m = cm;
                            }

                        } catch (IOException ioe) {
                            java.lang.System.out.println(ioe.getMessage());
                        }
                    }
                    if (m != null) {

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
        }*/
    }

}
