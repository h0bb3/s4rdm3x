package archviz;

import glm_.vec4.Vec4;
import org.graphstream.graph.Graph;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import se.lnu.siq.s4rdm3x.StringCommandHandler;
import se.lnu.siq.s4rdm3x.cmd.HuGMe;

import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;;

public class Command implements StringCommandHandler.ICommand {

    HNode.VisualsManager m_visuals;

    public Command(HRoot.State a_visuals) {
        m_visuals = a_visuals.m_nvm;
    }

    private Vec4 getColorElement(Element a_parent, String a_tagName) {

        Element e = (Element)a_parent.getElementsByTagName(a_tagName).item(0);

        Vec4 ret = new Vec4();

        ret.setX(Float.valueOf(e.getAttribute("r")));
        ret.setY(Float.valueOf(e.getAttribute("g")));
        ret.setZ(Float.valueOf(e.getAttribute("b")));
        ret.setW(Float.valueOf(e.getAttribute("a")));

        return ret;
    }

    private Element createColorElement(Document a_doc, Vec4 a_color, String a_tagName) {
        Element color = a_doc.createElement(a_tagName);
        color.setAttribute("r", a_color.getX().toString());
        color.setAttribute("g", a_color.getY().toString());
        color.setAttribute("b", a_color.getZ().toString());
        color.setAttribute("a", a_color.getW().toString());

        return color;
    }

    public StringCommandHandler.ICommand.Result execute(String a_command, Graph a_g, HuGMe.ArchDef a_arch) {
        Result ret = new Result();

        String[] cargs = a_command.split(" ");

        if (cargs.length > 1) {
            if (cargs[0].contentEquals("save_arch_visuals")) {
                try {
                ret.m_handled = true;
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.newDocument();

                // root element
                Element rootElement = doc.createElement("nodes");
                doc.appendChild(rootElement);

                m_visuals.m_nodeState.forEach( (key, value) -> {
                    Element node = doc.createElement("node");
                    rootElement.appendChild(node);
                    node.setAttribute("key", key);
                    node.appendChild(createColorElement(doc, value.m_bgColor, "bg_color"));
                    node.appendChild(createColorElement(doc, value.m_textColor, "text_color"));
                });


                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(new File(cargs[1]));
                transformer.transform(source, result);

                ret.m_output.add("Architecture Visuals Saved to: " + cargs[1]);
                } catch (Exception e) {
                    ret.m_output.add("Error when saving xml: " + e.getMessage());
                    e.printStackTrace(System.out);
                }

            } else if (cargs[0].contentEquals("load_arch_visuals")) {

                try {
                    File inputFile = new File(cargs[1]);
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(inputFile);
                    doc.getDocumentElement().normalize();

                    NodeList nodes = doc.getElementsByTagName("nodes").item(0).getChildNodes();
                    for (int nIx = 0 ; nIx < nodes.getLength(); nIx++) {
                        Node n = nodes.item(nIx);


                        if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().contentEquals("node")) {
                            Element e = (Element)n;
                            String key = e.getAttribute("key");

                            Vec4 bgColor = getColorElement(e, "bg_color");
                            Vec4 textColor = getColorElement(e, "text_color");

                            if (!m_visuals.m_nodeState.containsKey(key)) {
                                m_visuals.m_nodeState.put(key, new HNode.Visuals());
                            }

                            HNode.Visuals visuals = m_visuals.m_nodeState.get(key);
                            visuals.m_bgColor = bgColor;
                            visuals.m_textColor = textColor;
                        }
                    }


                    ret.m_handled = true;
                    ret.m_output.add("Arch Visuals Loaded from: " + cargs[1]);
                }  catch (Exception e) {
                    ret.m_handled = true;
                    ret.m_output.add("Error when loading xml: " + e.getMessage());
                    e.printStackTrace(System.out);
                }
            }
        }

        return ret;
    }
}
