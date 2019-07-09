package se.lnu.siq.s4rdm3x.experiments;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import se.lnu.siq.s4rdm3x.experiments.metric.Metric;
import se.lnu.siq.s4rdm3x.experiments.metric.MetricFactory;
import se.lnu.siq.s4rdm3x.experiments.metric.Rand;
import se.lnu.siq.s4rdm3x.experiments.metric.aggregated.RelativeLineCount;
import se.lnu.siq.s4rdm3x.experiments.system.FileBased;
import se.lnu.siq.s4rdm3x.experiments.system.System;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;

public class ExperimentXMLPersistence {

    public Node vec4ToElement(Document a_doc, float[] a_vec4, String a_elementName) {
        Element e = a_doc.createElement(a_elementName);

        e.setAttribute("x", "" + a_vec4[0]);
        e.setAttribute("y", "" + a_vec4[1]);
        e.setAttribute("z", "" + a_vec4[2]);
        e.setAttribute("w", "" + a_vec4[3]);

        return e;
    }

    public float[] elementToVec4(Element a_parent, String a_tagName) {
        float[] v = {0,0,0,0};

        Element e = (Element)a_parent.getElementsByTagName(a_tagName).item(0);

        try {
            v[0] = Float.parseFloat(e.getAttribute("x"));
            v[1] = Float.parseFloat(e.getAttribute("y"));
            v[2] = Float.parseFloat(e.getAttribute("z"));
            v[3] = Float.parseFloat(e.getAttribute("w"));
        } catch (Exception ex) {

        }

        return v;
    }

    public interface Listener {
        public void onLoadedExperiment(Element a_experimentElement, ExperimentRunner a_loadedExperiment);
        public void onSavedExperiment(Document a_doc, Element a_experimentElement, ExperimentRunner a_loadedExperiment);
    }

    public ArrayList<ExperimentRunner> loadExperiments(String a_fileName, Listener a_callback) throws Exception {
        ArrayList<ExperimentRunner> ret = new ArrayList<>();

        File inputFile = new File(a_fileName);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();

        NodeList nodes = doc.getElementsByTagName("experiment");
        for (int nIx = 0 ; nIx < nodes.getLength(); nIx++) {
            Node n = nodes.item(nIx);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                ExperimentRunner exp = elementToExperiment((Element)n);
                ret.add(exp);
                if (a_callback != null) {
                    a_callback.onLoadedExperiment((Element)n, exp);
                }
            }
        }

        return ret;
    }


    public void saveExperiments(Iterable<ExperimentRunner> a_experiments, String a_fileName, Listener a_listener) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();

        // root element
        Element rootElement = doc.createElement("experiments");
        doc.appendChild(rootElement);

        for (ExperimentRunner exr : a_experiments) {
            Element exrElement = experimentToElement(doc, exr);
            rootElement.appendChild(exrElement);
            a_listener.onSavedExperiment(doc, exrElement, exr);
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(a_fileName));
        transformer.transform(source, result);
    }

    private ExperimentRunner elementToExperiment(Element a_exr) throws Exception {
        ExperimentRunner ret;
        ExperimentRunner.RandomDoubleVariable initialSetSize = elementToRandomDouble(a_exr, "initialSetSize");
        boolean useManualMapping = getBoolAttribute(a_exr, "useManualMapping");
        ArrayList<System> suas = new ArrayList<>();
        ArrayList<Metric> metrics = new ArrayList<>();

        {
            MetricFactory mf = new MetricFactory();
            NodeList metricNodes = a_exr.getElementsByTagName("metric");
            for (int mIx = 0; mIx < metricNodes.getLength(); mIx++) {
                Element m = (Element) metricNodes.item(mIx);

                String metricName = m.getAttribute("name");

                Metric metric = mf.getMetric(metricName);
                if (metric == null) {
                    if (metricName.equals("rand")) {
                        metric = new Rand();
                    } else if (metricName.endsWith(RelativeLineCount.g_nameSuffix)) {
                        metricName = metricName.replace(RelativeLineCount.g_nameSuffix, "");
                        metric = mf.getMetric(metricName);
                        if (metric != null) {
                            metric = new RelativeLineCount(metric);
                        }
                    }
                }

                if (metric == null) {
                    throw new Exception("Unknown metric: " + metricName);
                }else {
                    metrics.add(metric);
                }
            }
        }

        {
            NodeList systemNodes = a_exr.getElementsByTagName("system");
            for (int sIx = 0; sIx < systemNodes.getLength(); sIx++) {
                Element s = (Element)systemNodes.item(sIx);
                String name = s.getAttribute("name");
                String file = s.hasAttribute("file") ? s.getAttribute("file") : "";

                if (file.length() > 0) {
                    suas.add(new FileBased(file));
                } else {
                    throw new Exception("Only file based systems are supported");
                }
            }

        }

        String type = a_exr.getAttribute("type");
        if (type.equals("nbmapper")) {
            ExperimentRunner.RandomDoubleVariable threshold = elementToRandomDouble(a_exr, "threshold");
            ExperimentRunner.RandomBoolVariable stemming = elementToRandomBool(a_exr, "stemming");
            ExperimentRunner.RandomBoolVariable wordcount = elementToRandomBool(a_exr, "wordcount");

            ret = new NBMapperExperimentRunner(suas, metrics, useManualMapping, initialSetSize, stemming, wordcount, threshold);
        } else if (type.equals("hugme")) {
            ExperimentRunner.RandomDoubleVariable omega = elementToRandomDouble(a_exr, "omega");
            ExperimentRunner.RandomDoubleVariable phi = elementToRandomDouble(a_exr, "phi");

            ret = new HuGMeExperimentRunner(suas, metrics, useManualMapping, initialSetSize, omega, phi);
        } else if (type.equals("irattract")) {
            ret = new IRAttractExperimentRunner(suas, metrics, useManualMapping, initialSetSize);
        } else {
            throw new Exception("Unknown mapping experiment: " + type);
        }

        ret.setName(a_exr.getAttribute("name"));


        return ret;
    }

    private boolean getBoolAttribute(Element a_parent, String a_attributeName) {
        return a_parent.getAttribute(a_attributeName).equals("yes");
    }

    private ExperimentRunner.RandomBoolVariable elementToRandomBool(Element a_parent, String a_tagName) {
        boolean random, value;

        Element e = (Element)a_parent.getElementsByTagName(a_tagName).item(0);
        random = getBoolAttribute(e, "random");
        value = getBoolAttribute(e, "value");

        if (!random) {
            return new ExperimentRunner.RandomBoolVariable(value);
        }

        return new ExperimentRunner.RandomBoolVariable();
    }

    private ExperimentRunner.RandomDoubleVariable elementToRandomDouble(Element a_parent, String a_tagName) {
        double base, scale;

        Element e = (Element)a_parent.getElementsByTagName(a_tagName).item(0);

        base = Double.parseDouble(e.getAttribute("base"));
        scale = Double.parseDouble(e.getAttribute("scale"));
        return new ExperimentRunner.RandomDoubleVariable(base, scale);
    }

    private Element experimentToElement(Document a_doc, ExperimentRunner a_exr) {
        Element exrNode = a_doc.createElement("experiment");

        exrNode.setAttribute("name", a_exr.getName());

        exrNode.appendChild(randomDoubleToElement(a_doc, a_exr.getInitialSetSize(), "initialSetSize"));
        setBoolAttribute(exrNode, "useManualMapping", a_exr.doUseManualmapping());

        Element metricsNode = a_doc.createElement("metrics");
        exrNode.appendChild(metricsNode);
        for (Metric m : a_exr.getMetrics()) {
            Element metricNode = a_doc.createElement("metric");
            metricsNode.appendChild(metricNode);
            metricNode.setAttribute("name", m.getName());
        }

        Element systemsNode = a_doc.createElement("systems");
        exrNode.appendChild(systemsNode);
        for (System s : a_exr.getSystems()) {
            Element systemNode = a_doc.createElement("system");
            systemsNode.appendChild(systemNode);
            systemNode.setAttribute("name", s.getName());
            if (s instanceof FileBased) {
                systemNode.setAttribute("file", ((FileBased)s).getFile());
            }
        }

        if (a_exr instanceof  NBMapperExperimentRunner) {
            exrNode.setAttribute("type", "nbmapper");
            NBMapperExperimentRunner nbexr = (NBMapperExperimentRunner)a_exr;
            exrNode.appendChild(randomDoubleToElement(a_doc, nbexr.getThreshold(), "threshold"));
            exrNode.appendChild(randomBoolToElement(a_doc, nbexr.getStemming(), "stemming"));
            exrNode.appendChild(randomBoolToElement(a_doc, nbexr.getWordCount(), "wordcount"));
        } else if (a_exr instanceof HuGMeExperimentRunner) {
            exrNode.setAttribute("type", "hugme");
            HuGMeExperimentRunner hugexr = (HuGMeExperimentRunner)a_exr;
            exrNode.appendChild(randomDoubleToElement(a_doc, hugexr.getOmega(), "omega"));
            exrNode.appendChild(randomDoubleToElement(a_doc, hugexr.getPhi(), "phi"));
        } else if (a_exr instanceof IRAttractExperimentRunner) {
            exrNode.setAttribute("type", "irattract");
            HuGMeExperimentRunner hugexr = (HuGMeExperimentRunner)a_exr;
        }

        return exrNode;
    }

    private void setBoolAttribute(Element a_element, String a_attribute, boolean a_bool) {
        a_element.setAttribute(a_attribute, (a_bool ? "yes" : "no"));
    }

    private Element randomBoolToElement(Document a_doc, ExperimentRunner.RandomBoolVariable a_rbv, String a_elementName) {
        Element rbvNode = a_doc.createElement(a_elementName);

        setBoolAttribute(rbvNode, "random", a_rbv.isRandom());
        setBoolAttribute(rbvNode, "value", a_rbv.getValue());

        return rbvNode;
    }

    private Element randomDoubleToElement(Document a_doc, ExperimentRunner.RandomDoubleVariable a_rdv, String a_elementName) {
        Element rdvNode = a_doc.createElement(a_elementName);

        rdvNode.setAttribute("base", "" + a_rdv.getBase());
        rdvNode.setAttribute("scale", "" + a_rdv.getScale());

        return rdvNode;

    }
}
