package se.lnu.siq.s4rdm3x;

import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.experiments.system.System;
import se.lnu.siq.s4rdm3x.model.cmd.*;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.GetComponentFan;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.GetNodeComponentCoupling;
import se.lnu.siq.s4rdm3x.model.cmd.metrics.ComputeMetrics;
import se.lnu.siq.s4rdm3x.model.cmd.metrics.GetMetric;
import se.lnu.siq.s4rdm3x.model.cmd.metrics.GetMetrics;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.Selector;
import se.lnu.siq.s4rdm3x.model.cmd.util.SelectorBuilder;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;

import java.util.ArrayList;

public class StringCommandHandler {



    public interface ICommand {
        public static class Result {
            public boolean m_handled = false;
            public ArrayList<String> m_output = new ArrayList<>();
        }
        Result execute(String a_command, CGraph a_g, ArchDef a_arch);
    }


    ArrayList<ICommand> m_commands = new ArrayList<>();
    ArchDef m_arch = null;


    public void addCommand(ICommand a_command) {
        m_commands.add(a_command);
    }

    private String join(String [] a_parts, int a_startIx, String a_delim) {

        String ret = a_parts[a_startIx];

        for(int sIx = a_startIx + 1; sIx < a_parts.length; sIx++) {
            ret += a_delim + a_parts[sIx];
        }

        return ret;
    }

    public ArchDef getArchDef() {
        return m_arch;
    }

    public ArrayList<String> execute(String a_command, CGraph a_g) {
        String in = a_command;
        CGraph graph = a_g;
        ArrayList<String> ret = new ArrayList<>();
        final String sep = "\t";
        try {

            if (in.startsWith("load_jar")) {
                String[] cargs = in.split(" ");
                String rootPkg = "";
                if (cargs.length > 2) {
                    rootPkg = cargs[2];
                }
                LoadJar c = new LoadJar(cargs[1], rootPkg);
                c.run(graph);
            } else if (in.compareTo("clear_graph") == 0) {

                graph.clear();

            } else if (in.startsWith("add_node_tag_rnd")) {
                String[] cargs = in.split(" ");
                SelectorBuilder bs = new SelectorBuilder();
                Selector.ISelector s = bs.buildFromString(join(cargs, 3, " "));
                AddNodeTagRandom c = new AddNodeTagRandom(cargs[1], s, Double.parseDouble(cargs[2]));
                c.run(graph);
            } else if (in.startsWith("add_node_tag")) {
                String[] cargs = in.split(" ");
                SelectorBuilder bs = new SelectorBuilder();
                Selector.ISelector s = bs.buildFromString(join(cargs, 2, " "));
                AddNodeTag c = new AddNodeTag(cargs[1], s);
                c.run(graph);
            } else if (in.startsWith("set_attr")) {
                String[] cargs = in.split(" ");
                SelectorBuilder bs = new SelectorBuilder();
                Selector.ISelector s = bs.buildFromString(join(cargs, 3, " "));
                //SetAttr c = new SetAttr(cargs[1], cargs[2], s);
                //c.run(graph);
                ret.add("Command Deprecated");
            } else if (in.startsWith("delete")) {
                SelectorBuilder bs = new SelectorBuilder();
                Selector.ISelector s = bs.buildFromString(in.substring(in.indexOf(" ")));
                DeleteNode c = new DeleteNode(s);
                c.run(graph);
                ret.add("Command does nothing...");

            } else if (in.startsWith("count_n")) {
                SelectorBuilder bs = new SelectorBuilder();
                Selector.ISelector s = bs.buildFromString(in.substring(in.indexOf(" ")));
                CountNodes c = new CountNodes(s);
                c.run(graph);

                ret.add("Nodes: " + c.m_count);
            } else if (in.startsWith("export")) {

                String[] cargs = in.split(" ");
                Selector.ISelector s = new Selector.All();
                if (cargs.length > 2) {
                    SelectorBuilder bs = new SelectorBuilder();
                    s = bs.buildFromString(join(cargs, 2, " "));
                }

                Exporter c = new Exporter(cargs[1], s);
                c.run(graph);

            } else if (in.startsWith("load_arch ")) {
                String[] cargs = in.split(" ");
                LoadArch la = new LoadArch(cargs[1]);
                try {
                    la.run(graph);
                } catch (System.NoMappedNodesException e) {
                    for (ArchDef.Component c : e.m_components) {
                        ret.add("Warning: Architectural module: " + c.getName() + " has no mapped nodes");
                    }
                }
                m_arch = la.m_arch;
                if (m_arch != null) {
                    ret.add("Architecture Loaded from: " + cargs[1]);
                } else {
                    ret.add("Failed to load Architecture from: " + cargs[1]);
                }
            } else if (in.startsWith("save_arch ")) {
                String[] cargs = in.split(" ");
                SaveArch la = new SaveArch(m_arch, cargs[1]);
                la.run(graph);

            } else if (in.startsWith("report_violations")) {
                CheckViolations cv = new CheckViolations();
                cv.run(graph, m_arch);
                ret.add("Divergencies:");
                for(CheckViolations.Violation v : cv.m_divergencies) {
                    String lines = "";
                    for (int line : v.m_dependency.lines()) {
                        ret.add(v.m_source.m_component.getName() + sep + v.m_source.m_class.getFileName() + sep + v.m_dest.m_component.getName() + sep + v.m_dest.m_class.getFileName() + sep + v.m_dependency.getType() + sep + line);
                    }

                }
            } else if (in.startsWith("print_nodes")) {
                String[] cargs = in.split(" ");
                Selector.ISelector s = new Selector.All();
                if (cargs.length > 1) {
                    SelectorBuilder bs = new SelectorBuilder();
                    s = bs.buildFromString(join(cargs, 1, " "));
                }

                GetNodes c = new GetNodes(s);
                c.run(graph);
                for (CNode n : c.m_nodes) {
                    ret.add(n.getName());
                    ret.add(sep + "tags: " + n.getTags());
                    String classesStr = "";
                    for (dmClass dmc: n.getClasses()) {
                        if (classesStr.length() > 0) {
                            classesStr += ", ";
                        }
                        classesStr += dmc.getName();
                    }
                    ret.add(sep + "classes: " + classesStr);
                }
            } else if (in.startsWith("print_dependencies")) {
                String[] cargs = in.split(" ");
                Selector.ISelector s = new Selector.All();
                if (cargs.length > 1) {
                    SelectorBuilder bs = new SelectorBuilder();
                    s = bs.buildFromString(join(cargs, 1, " "));
                }

                GetNodes c = new GetNodes(s);
                c.run(graph);
                for (CNode sn : c.m_nodes) {
                    ret.add(sn.getName());

                    sn.getClasses().forEach(cl -> {
                        ret.add(sep + cl.getClassName() + ":" + cl.getLineCount());
                    });

                    for (CNode tn : c.m_nodes) {
                        if (sn != tn) {
                            for (dmDependency d : sn.getDependencies(tn)) {
                                ret.add(sep + d.getCount() + ":" + d.getType() + ":" + tn.getName());
                            }
                        }
                    }
                }
            }
            else if (in.startsWith("//")) {
                // skip comment
            } else if (in.startsWith("info")) {

                int classes = 0;
                int nodes = 0;
                for (CNode n : graph.getNodes()) {
                    classes += n.getClassCount();
                    nodes++;
                }
                ret.add("Nodes: " + nodes);
                ret.add("Classes: " + classes);
            }  else if (in.startsWith("compute_metrics")) {

                String[] cargs = in.split(" ");
                Selector.ISelector s = new Selector.All();
                if (cargs.length > 1) {
                    SelectorBuilder bs = new SelectorBuilder();
                    s = bs.buildFromString(join(cargs, 1, " "));
                }

                ComputeMetrics c = new ComputeMetrics(s);
                c.run(graph);

            } else if (in.startsWith("print_metrics")) {

                String[] cargs = in.split(" ");
                Selector.ISelector s = new Selector.All();
                if (cargs.length > 1) {
                    SelectorBuilder bs = new SelectorBuilder();
                    s = bs.buildFromString(join(cargs, 1, " "));
                }

                GetMetrics c = new GetMetrics(s);
                c.run(graph);

                String header = "file";
                for (String metric : c.m_result.getMetrics()) {
                    header +=sep + metric;
                }

                ret.add(header);

                for (CNode n : c.m_result.getNodes()) {
                    String row = n.getName();
                    for (String m : c.m_result.getMetrics()) {
                        row += sep + c.m_result.get(n, m);
                    }

                    ret.add(row);
                }

            } else if (in.startsWith("print_metric")) {

                String[] cargs = in.split(" ");
                String metric = cargs[1];
                Selector.ISelector s = new Selector.All();
                if (cargs.length > 2) {
                    SelectorBuilder bs = new SelectorBuilder();
                    s = bs.buildFromString(join(cargs, 2, " "));
                }

                GetMetric c = new GetMetric(metric, s);
                c.run(graph);

                ret.add(metric +": " + c.m_result);

            } else if (in.startsWith("print_node_component_coupling")) {
                if (m_arch != null) {
                    ReportDependencies rd = new ReportDependencies();
                    rd.run(graph, m_arch);
                    ret.add("Node" + sep + "fan-internal" + sep + "fan-external" + sep + "fan-unmaped" + sep + "fanout-internal" + sep + "fanout-external" + sep + "fanout-unmaped" + sep + "couplingout-internal" + sep + "couplingout-external" + sep + "couplingout-unmaped" + sep + "coupling-internal" + sep + "coupling-external" + sep + "coupling-unmaped");
                    for (ReportDependencies.Dependency d : rd.m_dependencyReport) {
                        ret.add(d.m_node.getName() + sep + d.getInternalFan() + sep + d.getExternalFan() + sep + d.getUnmappedFan() + sep + d.getInternalFanOut() + sep + d.getExternalFanOut() + sep + d.getUnmappedFanOut() + sep + d.getInternalCouplingOut() + sep + d.getExternalCouplingOut() + sep + d.getUnmappedCouplingOut()+sep + d.getInternalCoupling() + sep + d.getExternalCoupling() + sep + d.getUnmappedCoupling());
                    }
                } else {
                    ret.add("No Architecture Defined, consider loading one.");
                }
            } else if (in.startsWith("print_node_component_fan")) {

                GetNodeComponentCoupling c = new GetNodeComponentCoupling(m_arch);
                c.run(graph);

                String header = "file";
                for (String metric : c.m_result.getMetrics()) {
                    header += sep + metric;
                }

                ret.add(header);

                for (CNode n : c.m_result.getNodes()) {
                    String row = n.getName();
                    for (String m : c.m_result.getMetrics()) {
                        row += sep + c.m_result.get(n, m);
                    }

                    ret.add(row);
                }

            } else if (in.startsWith("print_component_fan")) {

                String[] cargs = in.split(" ");

                GetComponentFan c = new GetComponentFan(m_arch);
                c.run(graph);

                ret.add("Architectural Components Fan Out");
                String header = "from/to";
                for (ArchDef.Component component : c.m_fanOut.getColumnObjects()) {
                    header += sep + component.getName();
                }

                ret.add(header);

                for (ArchDef.Component row : c.m_fanOut.getRowObjects()) {
                    String rowStr = row.getName();

                    for (ArchDef.Component col : c.m_fanOut.getColumnObjects()) {
                        rowStr += sep + c.m_fanOut.get(row, col);
                    }
                    ret.add(rowStr);
                }


                ret.add("Architectural Components Fan In");
                header = "from/to";
                for (ArchDef.Component component : c.m_fanIn.getColumnObjects()) {
                    header += sep + component.getName();
                }

                ret.add(header);

                for (ArchDef.Component row : c.m_fanIn.getRowObjects()) {
                    String rowStr = row.getName();

                    for (ArchDef.Component col : c.m_fanIn.getColumnObjects()) {
                        rowStr += sep + c.m_fanIn.get(row, col);
                    }
                    ret.add(rowStr);
                }

            } else if (in.length() > 0) {
                ICommand.Result r = null;
                for (ICommand c : m_commands) {
                     r = c.execute(in, a_g, m_arch);
                    if (r.m_handled) {
                       break;
                    }
                }

                if (r != null && r.m_handled) {
                    ret.addAll(r.m_output);
                } else {
                    ret.add("Unknown command: " + in);
                }
            }
        } catch(Exception e) {
            ret.add("Something when wrong when executing command: " + in);
            ret.add(e.toString());
            e.printStackTrace();
        }

        return ret;
    }
}
