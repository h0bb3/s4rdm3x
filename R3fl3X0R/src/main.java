import org.w3c.dom.Document;
import org.w3c.dom.Element;
import se.lnu.siq.s4rdm3x.experiments.system.FileBased;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.model.cmd.CheckViolations;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.ArchDef;
import se.lnu.siq.s4rdm3x.model.cmd.util.SystemModelReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class main {

    public static void main(String [] a_args) {
        System.out.println("Starting Reflexion Analysis...");

        final CmdArgsHandler args = new CmdArgsHandler(a_args);

        String implementation = args.getArgumentString("-impl");
        String roots = args.getArgumentString("-root");
        String archStr = args.getArgumentString("-arch");
        String out = args.getArgumentString("-report");

        if (out.length() == 0) {
            out = "report.xml";
        }

        SystemModelReader smr = new SystemModelReader();
        if (smr.readFile(archStr)) {
            if (implementation.length() > 0) {
                smr.m_jars.addAll(Arrays.asList(implementation.split(",")));
            }
            if (roots.length() > 0) {
                smr.m_jars.addAll(Arrays.asList(roots.split(",")));
            }
            FileBased fb = new FileBased(smr);


            CGraph g = new CGraph();


            fb.load(g);
            ArchDef arch = fb.createAndMapArch(g);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

                Document doc = dBuilder.newDocument();

                // root element
                Element rootElement = doc.createElement("report");
                doc.appendChild(rootElement);

                rootElement.setAttribute("arch", archStr);
                rootElement.setAttribute("impl", String.join(",", smr.m_jars));

                SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                rootElement.setAttribute("time", sdfDate.format(new Date()));

                Element violationsElement = doc.createElement("violations");
                Element unmapped = doc.createElement("unmapped");

                rootElement.appendChild(unmapped);
                rootElement.appendChild(violationsElement);


                for (CNode n : g.getNodes()) {
                    if (n.getMapping() == null || n.getMapping().length() == 0) {
                        Element f = doc.createElement("unmapped_file");
                        f.setAttribute("file", n.getFileName());
                        unmapped.appendChild(f);
                    }
                }

                CheckViolations violations = new CheckViolations();
                violations.run(g, arch);
                for (CheckViolations.Violation v : violations.m_divergencies) {
                    Element vElement = doc.createElement("violation");
                    Element source = doc.createElement("source");
                    Element dest = doc.createElement("dest");
                    violationsElement.appendChild(vElement);
                    vElement.appendChild(source);
                    vElement.appendChild(dest);

                    vElement.setAttribute("lines", String.join(",", StreamSupport.stream(v.m_dependency.lines().spliterator(), false).map(i -> i.toString()).collect(Collectors.toList())) );
                    vElement.setAttribute("type", v.m_dependency.getType().toString());

                    source.setAttribute("file", v.m_source.m_node.getFileName());
                    source.setAttribute("component", v.m_source.m_node.getFileName());

                    dest.setAttribute("file", v.m_dest.m_node.getFileName());
                    dest.setAttribute("component", v.m_dest.m_node.getFileName());

                    //System.out.println("Violation: " + v.m_source.m_node.getFileName() + " (" + v.m_source.m_component.getName() + ")" + " lines: " + lines + " - " + v.m_dest.m_node.getFileName() + " (" + v.m_dest.m_component.getName() + ")");
                }

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(new File(out));
                transformer.transform(source, result);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Could not find file: " + archStr);

        }
    }

}
