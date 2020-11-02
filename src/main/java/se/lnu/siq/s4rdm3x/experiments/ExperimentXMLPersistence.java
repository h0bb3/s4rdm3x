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

    public interface ListenerB {
        public void onLoadedExperiment(Element a_experimentElement, ExperimentRunner a_runner, ExperimentRun a_loadedExperiment);
        public void onSavedExperiment(Document a_doc, Element a_experimentElement, ExperimentRun a_savedExperiment);
    }

    public ArrayList<ExperimentRunner> loadExperimentRunners(String a_fileName, ListenerB a_callback) throws Exception {
        ArrayList<ExperimentRunner> ret = new ArrayList<>();

        File inputFile = new File(a_fileName);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();


        NodeList nodes = doc.getElementsByTagName("runner");
        for (int nIx = 0 ; nIx < nodes.getLength(); nIx++) {
            Node n = nodes.item(nIx);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                ExperimentRunner exp = elementToRunner((Element)n, a_callback);
                ret.add(exp);
            }
        }

        return ret;
    }


    public void saveExperiments(Iterable<ExperimentRunner> a_runners, String a_fileName, ListenerB a_listener) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();

        // root element
        Element rootElement = doc.createElement("experiments");
        doc.appendChild(rootElement);

        for (ExperimentRunner runner : a_runners) {
            Element exrElement = runnerToElement(doc, runner, a_listener);
            rootElement.appendChild(exrElement);

        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(a_fileName));
        transformer.transform(source, result);
    }

    private ExperimentRunner elementToRunner(Element a_runner, ListenerB a_listener) throws Exception {
        ExperimentRunner.RandomDoubleVariable initialSetSize = elementToRandomDouble(a_runner, "initialSetSize");
        boolean useInitialMapping = getBoolAttribute(a_runner, "useInitialMapping");
        boolean initialSetPerComponent = getBoolAttribute(a_runner, "initialSetPerComponent");
        ArrayList<System> suas = new ArrayList<>();
        ArrayList<Metric> metrics = new ArrayList<>();
        ArrayList<ExperimentRun> experiments = new ArrayList<>();

        {
            MetricFactory mf = new MetricFactory();
            NodeList metricNodes = a_runner.getElementsByTagName("metric");
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
            NodeList systemNodes = a_runner.getElementsByTagName("system");
            for (int sIx = 0; sIx < systemNodes.getLength(); sIx++) {
                Element s = (Element)systemNodes.item(sIx);
                String file = s.hasAttribute("file") ? s.getAttribute("file") : "";

                if (file.length() > 0) {
                    String separator = s.hasAttribute("file_separator") ? s.getAttribute("file_separator") : "\\";



                    file = file.replace(separator, File.separator);
                    suas.add(new FileBased(file));
                } else {
                    throw new Exception("Only file based systems are supported");
                }
            }

        }


        NodeList nodes = a_runner.getElementsByTagName("experiment");
        class Pair {
            Pair(Element a_e, ExperimentRun a_exp) {
                m_element = a_e;
                m_exp = a_exp;
            }
            Element m_element;
            ExperimentRun m_exp;
        };
        ArrayList<Pair> createdExperimentsForCallback = new ArrayList<>();
        for (int nIx = 0 ; nIx < nodes.getLength(); nIx++) {
            Node n = nodes.item(nIx);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                ExperimentRun exp = elementToExperiment((Element)n);
                experiments.add(exp);

                if (a_listener != null) {
                    createdExperimentsForCallback.add(new Pair((Element)n, exp));
                }
            }
        }



        ExperimentRunner ret = new ExperimentRunner(suas, metrics, experiments, useInitialMapping, initialSetSize, initialSetPerComponent);
        ret.setName(a_runner.getAttribute("name"));
        for (Pair p : createdExperimentsForCallback) {
            a_listener.onLoadedExperiment(p.m_element, ret, p.m_exp);
        }


        return ret;
    }

    private ExperimentRun elementToExperiment(Element a_exr) throws Exception {
        ExperimentRun ret;

        boolean useManualMapping = getBoolAttribute(a_exr, "useManualMapping");


        String type = a_exr.getAttribute("type");


        if (type.equals("nbmapper")) {
            IRExperimentRunBase.Data irData = elementToIRData(a_exr);
            ExperimentRunner.RandomDoubleVariable threshold = elementToRandomDouble(a_exr, "threshold");
            ExperimentRunner.RandomBoolVariable wordcount = elementToRandomBool(a_exr, "wordcount");

            ret = new NBMapperExperimentRun(useManualMapping, irData, wordcount, threshold);
        } else if (type.equals("hugme")) {

            ExperimentRunner.RandomDoubleVariable omega = elementToRandomDouble(a_exr, "omega");
            ExperimentRunner.RandomDoubleVariable phi = elementToRandomDouble(a_exr, "phi");

            ret = new HuGMeExperimentRun(useManualMapping, omega, phi);
        } else if (type.equals("irattract")) {
            IRExperimentRunBase.Data irData = elementToIRData(a_exr);
            ret = new IRAttractExperimentRun(useManualMapping, irData);

        }  else if (type.equals("lsiattract")) {
            IRExperimentRunBase.Data irData = elementToIRData(a_exr);
            ret = new LSIAttractExperimentRun(useManualMapping, irData);

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

    private Element runnerToElement(Document a_doc, ExperimentRunner a_runner, ListenerB a_listener) {
        Element runnerNode = a_doc.createElement("runner");

        runnerNode.appendChild(randomDoubleToElement(a_doc, a_runner.getInitialSetSize(), "initialSetSize"));

        setBoolAttribute(runnerNode, "useInitialMapping", a_runner.doUseInitialMapping());
        setBoolAttribute(runnerNode, "initialSetPerComponent", a_runner.initialSetPerComponent());

        Element metricsNode = a_doc.createElement("metrics");
        runnerNode.appendChild(metricsNode);
        for (Metric m : a_runner.getMetrics()) {
            Element metricNode = a_doc.createElement("metric");
            metricsNode.appendChild(metricNode);
            metricNode.setAttribute("name", m.getName());
        }

        Element systemsNode = a_doc.createElement("systems");
        runnerNode.appendChild(systemsNode);
        for (System s : a_runner.getSystems()) {
            Element systemNode = a_doc.createElement("system");
            systemsNode.appendChild(systemNode);
            systemNode.setAttribute("name", s.getName());
            if (s instanceof FileBased) {

                systemNode.setAttribute("file_separator", File.separator);
                systemNode.setAttribute("file", ((FileBased)s).getFile());
            }
        }

        Element experimentsNode = a_doc.createElement("experiments");
        runnerNode.appendChild(experimentsNode);
        for (ExperimentRun ex : a_runner.getExperiments()) {
            Element exNode = experimentToElement(a_doc, ex);
            experimentsNode.appendChild(exNode);
            if (a_listener != null) {
                a_listener.onSavedExperiment(a_doc, exNode, ex);
            }
        }

        runnerNode.setAttribute("name", a_runner.getName());

        return runnerNode;
    }

    private Element experimentToElement(Document a_doc, ExperimentRun a_exr) {
        Element exrNode = a_doc.createElement("experiment");

        exrNode.setAttribute("name", a_exr.getName());

        setBoolAttribute(exrNode, "useManualMapping", a_exr.doUseManualMapping());

        if (a_exr instanceof NBMapperExperimentRun) {
            exrNode.setAttribute("type", "nbmapper");
            NBMapperExperimentRun nbexr = (NBMapperExperimentRun)a_exr;
            exrNode.appendChild(randomDoubleToElement(a_doc, nbexr.getThreshold(), "threshold"));
            exrNode.appendChild(randomBoolToElement(a_doc, nbexr.getWordCount(), "wordcount"));
        } else if (a_exr instanceof HuGMeExperimentRun) {
            exrNode.setAttribute("type", "hugme");
            HuGMeExperimentRun hugexr = (HuGMeExperimentRun)a_exr;
            exrNode.appendChild(randomDoubleToElement(a_doc, hugexr.getOmega(), "omega"));
            exrNode.appendChild(randomDoubleToElement(a_doc, hugexr.getPhi(), "phi"));
        } else if (a_exr instanceof IRAttractExperimentRun) {
            exrNode.setAttribute("type", "irattract");
            //IRAttractExperimentRun irexr = (IRAttractExperimentRun)a_exr;
        } else if (a_exr instanceof LSIAttractExperimentRun) {
            exrNode.setAttribute("type", "lsiattract");
        }

        // Here we save the IRBase data
        if (a_exr instanceof IRExperimentRunBase) {
            exrNode.appendChild(iRBaseToElement(a_doc, (IRExperimentRunBase)a_exr));
        }

        return exrNode;
    }

    private void setBoolAttribute(Element a_element, String a_attribute, boolean a_bool) {
        a_element.setAttribute(a_attribute, (a_bool ? "yes" : "no"));
    }

    private IRExperimentRunBase.Data elementToIRData(Element a_parent) {
        Element irBaseDataElement = (Element)a_parent.getElementsByTagName("irbase").item(0);

        IRExperimentRunBase.Data ret = new IRExperimentRunBase.Data();


        ret.doStemming(elementToRandomBool(irBaseDataElement, "stemming"));
        ret.doUseCDA(elementToRandomBool(irBaseDataElement, "cda"));
        ret.doUseArchComponentName(elementToRandomBool(irBaseDataElement, "archcomponentname"));
        ret.doUseNodeName(elementToRandomBool(irBaseDataElement, "nodename"));
        ret.doUseNodeText(elementToRandomBool(irBaseDataElement, "nodetext"));
        ret.minWordSize(elementToRandomInt(irBaseDataElement, "minwordlength"));

        return ret;
    }

    private Element iRBaseToElement(Document a_doc, IRExperimentRunBase a_irbase) {
        Element rbvNode = a_doc.createElement("irbase");

        rbvNode.appendChild(randomBoolToElement(a_doc, a_irbase.getData().doStemming(), "stemming"));
        rbvNode.appendChild(randomBoolToElement(a_doc, a_irbase.getData().doUseArchComponentName(), "archcomponentname"));
        rbvNode.appendChild(randomBoolToElement(a_doc, a_irbase.getData().doUseCDA(), "cda"));
        rbvNode.appendChild(randomBoolToElement(a_doc, a_irbase.getData().doUseNodeName(), "nodename"));
        rbvNode.appendChild(randomBoolToElement(a_doc, a_irbase.getData().doUseNodeText(), "nodetext"));
        rbvNode.appendChild(randomIntToElement(a_doc, a_irbase.getData().minWordSize(), "minwordlength"));

        return rbvNode;
    }

    private ExperimentRunner.RandomIntVariable elementToRandomInt(Element a_parent, String a_tagName) {
        int min, max;

        Element e = (Element)a_parent.getElementsByTagName(a_tagName).item(0);

        min = Integer.parseInt(e.getAttribute("min"));
        max = Integer.parseInt(e.getAttribute("max"));
        return new ExperimentRunner.RandomIntVariable(min, max);
    }

    private Node randomIntToElement(Document a_doc, ExperimentRunner.RandomIntVariable a_riv, String a_elementName) {
        Element rbvNode = a_doc.createElement(a_elementName);

        setIntAttribute(rbvNode, "min", a_riv.getMin());
        setIntAttribute(rbvNode, "max", a_riv.getMax());

        return rbvNode;
    }

    private void setIntAttribute(Element a_element, String a_attribute, int a_number) {
        a_element.setAttribute(a_attribute, Integer.toString(a_number));
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
