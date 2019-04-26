package se.lnu.siq.s4rdm3x.experiments;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class ExperimentXMLPersistence {

    ExperimentRunner loadExperiment(String a_fileName) {

        return null;
    }

    void saveExperiment(Iterable<ExperimentRunner> a_experiments, String a_fileName) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();

        // root element
        Element rootElement = doc.createElement("experiments");
        doc.appendChild(rootElement);

        for (ExperimentRunner exr : a_experiments) {
            rootElement.appendChild(experimentToElement(doc, exr));
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(a_fileName));
        transformer.transform(source, result);
    }

    private Element experimentToElement(Document a_doc, ExperimentRunner a_exr) {
        Element exrNode = a_doc.createElement("experiment");

        exrNode.appendChild(randomDoubleToElement(a_doc, a_exr.getInitialSetSize(), "initialSetSize"));
        exrNode.setAttribute("useManualMapping", (a_exr.doUseManualmapping() ? "yes" : "no"));


        return exrNode;
    }

    private Element randomDoubleToElement(Document a_doc, ExperimentRunner.RandomDoubleVariable a_rdv, String a_elementName) {
        Element rdvNode = a_doc.createElement(a_elementName);

        rdvNode.setAttribute("base", "" + a_rdv.getBase());
        rdvNode.setAttribute("scale", "" + a_rdv.getScale());

        return rdvNode;

    }
}
