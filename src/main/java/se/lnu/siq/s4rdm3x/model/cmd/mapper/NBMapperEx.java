package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import weka.core.*;
import weka.core.stemmers.SnowballStemmer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * Mapper based on textual analysis of nodes using a naive Bayes classifier. Evaluated by Tobias Olsson, Morgan Ericsson, and Anna Wingkvist. 2019. Semi-automatic mapping of source code using naive Bayes. In Proceedings of the 13th European Conference on Software Architecture - Volume 2 (ECSA ’19). Association for Computing Machinery, New York, NY, USA, 209–216. DOI:https://doi.org/10.1145/3344948.3344984
 *
 */
public class NBMapperEx extends IRMapperBase {

    private boolean m_addRawArchitectureTrainingData = false;
    private Classifier m_nbClassifier;
    private SnowballStemmer m_stemmer;

    public Classifier getClassifier() {
        return m_nbClassifier;
    }


    public void doStemming(boolean a_doStemming) {
        m_doStemm = a_doStemming;
    }

    public void doWordCount(boolean a_doWordCount) {
        m_filter.setOutputWordCounts(a_doWordCount);
        //m_filter.setTFTransform(a_doWordCount);
        // m_filter.setIDFTransform(a_doWordCount);
    }



    public static class Classifier extends weka.classifiers.bayes.NaiveBayesMultinomial {
        //public static class Classifier extends weka.classifiers.bayes.NaiveBayes {

        public double [] getProbabilityOfClass() {
            return m_probOfClass;
        }

        public double getProbabilityOfWord(int a_wordIx, int a_classIx) {

            // this is from the implementation of classifier.toString
            if (m_probOfWordGivenClass != null && a_classIx < m_probOfWordGivenClass.length && a_wordIx < m_probOfWordGivenClass[a_classIx].length) {
                return Math.exp(m_probOfWordGivenClass[a_classIx][a_wordIx]);
            } else {
                return -1;
            }
        }
    }

    public boolean doAddRawArchitectureTrainingData() {
        return m_addRawArchitectureTrainingData;
    }



    public int m_consideredNodes = 0;
    public int m_autoWrong = 0;

    private double m_mappingThreshold = 0.9;

    private boolean m_doStemm = false;
    private StringToWordVector m_filter = new StringToWordVector();
    private double [] m_initialDistribution = null;
    private double m_cdaWeight = 0.5f;

    public NBMapperEx(ArchDef a_arch, boolean a_doUseCDA, boolean a_doUseNodeText, boolean a_doUseNodeName, boolean a_doUseArchComponentName, int a_minWordLength, double a_mappingThreshold) {
        super(a_arch, false, a_doUseCDA, a_doUseNodeText, a_doUseNodeName, a_doUseArchComponentName, a_minWordLength);
        m_filter.setOutputWordCounts(false);
        m_filter.setTFTransform(false);
        m_filter.setIDFTransform(false);
        setMappingThreshold(a_mappingThreshold);
    }
    public NBMapperEx(ArchDef a_arch, boolean a_doManualMapping, boolean a_doUseCDA, boolean a_doUseNodeText, boolean a_doUseNodeName, boolean a_doUseArchComponentName, int a_minWordLength, double [] a_initialDistribution, double a_mappingThreshold) {
        super(a_arch, a_doManualMapping, a_doUseCDA, a_doUseNodeText, a_doUseNodeName, a_doUseArchComponentName, a_minWordLength);
        m_initialDistribution = a_initialDistribution;
        m_filter.setOutputWordCounts(false);
        m_filter.setTFTransform(false);
        m_filter.setIDFTransform(false);
        setMappingThreshold(a_mappingThreshold);
    }

    public void setMappingThreshold(double a_threshold) {
        m_mappingThreshold = a_threshold;
        if (m_mappingThreshold > 1) {
            m_mappingThreshold = 0.9;
        } else if (m_mappingThreshold < 0.0) {
            m_mappingThreshold = 0.9;
        }
    }

    public double getClusteringThreshold() {
        return m_mappingThreshold;
    }

    public Filter getFilter() {
        return m_filter;
    }

    public void buildClassifier(CGraph a_g) {
        ArrayList<ClusteredNode> initiallyMapped = getInitiallyMappedNodes(a_g);

        m_stemmer = null;
        if (m_doStemm) {
            m_stemmer = new weka.core.stemmers.SnowballStemmer();
            do {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {

                }
            } while (!(m_stemmer).stemmerTipText().contains("english"));  // when using multiple threads this is apparently needed...
        }

        weka.core.Instances trainingData = getTrainingData(initiallyMapped, m_arch, getFilter(), m_stemmer);


        // avg 23% wrong
        m_nbClassifier = new Classifier();
        try {
            m_nbClassifier.buildClassifier(trainingData);
            adjustClassProbabilities(initiallyMapped, m_nbClassifier);
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }

    }

    public void run(CGraph a_g) {

        // get the mapped nodes
        // compute word frequencies
        // train model
        // execute model and compute attractions (i.e. probabilities)
        // map automatically or manually
        // iterate

        ArrayList<OrphanNode> orphans = getOrphanNodes(a_g);
        ArrayList<ClusteredNode> initiallyMapped = getInitiallyMappedNodes(a_g);


        m_consideredNodes = orphans.size();
        try {

            //System.out.print(" the expression for the input data as per algorithm is ");
            //System.out.println(nbClassifier);

            for (OrphanNode orphanNode : orphans) {
                double [] attraction = new double[m_arch.getComponentCount()];

                for (int i = 0; i < m_arch.getComponentCount(); i++) {
//                    double index = nbClassifier.classifyInstance(data.instance(i));
                    Instances data = getPredictionDataForNode(orphanNode, initiallyMapped, m_arch.getComponentNames(), m_arch.getComponent(i), getFilter(), m_stemmer);

                    double [] distribution = m_nbClassifier.distributionForInstance(data.instance(0));
                    /*System.out.print("[");
                    for (int dIx = 0; dIx < distribution.length; dIx++) {
                        System.out.print(distribution[dIx] + " ");
                    }
                    System.out.println("]");*/

                    attraction[i] = distribution[i];
                }

                orphanNode.setAttractions(attraction);

                ArchDef.Component autoClusteredTo = doAutoMappingAbsThreshold(orphanNode, m_arch, m_mappingThreshold);
                if (autoClusteredTo != null) {
                    addAutoClusteredOrphan(orphanNode);
                    if (autoClusteredTo != m_arch.getMappedComponent(orphanNode.get())) {
                        m_autoWrong++;
                    }

                } else if (doManualMapping()) {
                    boolean clustered = manualMapping(orphanNode, m_arch);
                    if (clustered == false) {
                        m_failedMappings++;
                    }
                }
            }

        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }

    protected void adjustClassProbabilities(ArrayList<ClusteredNode> a_initiallyMapped, Classifier a_classifier) {
        if (m_initialDistribution == null || m_initialDistribution.length == a_classifier.getProbabilityOfClass().length) {

            m_initialDistribution = new double[m_arch.getComponentCount()];
            // each instance is counted as a separate document and this is not the case really as only every node should be counted as a document as this will affect the probabilities of the class
            double initialSetCount = a_initiallyMapped.size();
            for (int i = 0; i < m_arch.getComponentCount(); i++) {
                double [] count = {0};
                ArchDef.Component c = m_arch.getComponent(i);

                a_initiallyMapped.forEach( n -> {if (n.getClusteredComponent() == c) {count[0] += 1.0;}});
                m_initialDistribution[i] = count[0] / initialSetCount;
            }
        }

        for (int dIx = 0; dIx < a_classifier.getProbabilityOfClass().length; dIx++) {
            a_classifier.getProbabilityOfClass()[dIx] = m_initialDistribution[dIx];
        }
    }


    protected Instances getPredictionDataForNode(OrphanNode a_node, Iterable<ClusteredNode> a_mappedNodes, String[] a_componentNames, ArchDef.Component a_component, Filter a_filter, weka.core.stemmers.Stemmer a_stemmer) {
        ArrayList<Attribute> attributes = new ArrayList<>();

        // first we have the architectural components
        List<String> componentNames = Arrays.asList(a_componentNames);
        attributes.add(new Attribute("concrete_component", componentNames));
        Attribute features = new Attribute("model_features", (ArrayList<String>) null);
        attributes.add(features);

        Instances data = new Instances("PredictionData", attributes, 0);

        String nodeText = getNodeWords(a_node.get(), a_stemmer);
        String relations = getUnmappedCDAWords(a_node, a_component, a_mappedNodes);

        relations +=  nodeText;

        // the mapping is used for the concrete component, this is the actual answer but it is only used for confusion matrix stuff in weka and does not affect the prediction
        data.add(createDenseInstance(features, 2, componentNames.indexOf(a_node.getMapping()), relations, 1.0));

        data.setClassIndex(0);

        try {
            //filter.setInputFormat(data);  // filter data format is set in getTrainingData this means that it must be called first
            data = Filter.useFilter(data, a_filter);
            return data;
        } catch (Exception e) {

            if (e instanceof IllegalStateException) {
                System.out.println("Warning: Filter.setInputFormat could differ between training and prediction data. Consider creating the prediction data first");

                try {
                    a_filter.setInputFormat(data);  // filter data format is set in getTrainingData this means that it must be called first
                    data = Filter.useFilter(data, a_filter);
                    return data;
                } catch (Exception ex) {
                    System.out.println(ex.toString());
                    ex.printStackTrace();
                }
            } else {
                System.out.println(e.toString());
                e.printStackTrace();
            }

            return null;
        }
    }

    protected DenseInstance createDenseInstance(Attribute a_attrib, int a_numAttributes, int a_componentIndex, String a_words, double a_weight) {
        double[] values = new double[a_numAttributes];
        values[0] = a_componentIndex;
        values[1] = a_attrib.addStringValue(a_words);

        return new DenseInstance(a_weight, values);
    }

    protected Instances getTrainingData(Iterable<ClusteredNode> a_nodes, ArchDef a_arch, Filter a_filter, weka.core.stemmers.Stemmer a_stemmer) {
        ArrayList<Attribute> attributes = new ArrayList<>();

        // first we have the architectural components
        List<String> components = Arrays.asList(a_arch.getComponentNames());


        attributes.add(new Attribute("concrete_component", components));
        Attribute features = new Attribute("model_features", (ArrayList<String>) null);
        attributes.add(features);

        Instances data = new Instances("TrainingData", attributes, 0);

        // add the component names
        if (doUseArchComponentName()) {
            for (ArchDef.Component c : a_arch.getComponents()) {
                String relations = getArchComponentWords(c, a_stemmer);
                if (relations.length() > 0) {
                    data.add(createDenseInstance(features, 2, components.indexOf(c.getName()), relations, 1.0));
                }
            }
        }

        // add the allowed cda pattern
        if (doUseCDA()) {

            for (ArchDef.Component fromC : a_arch.getComponents()) {
                String allCdaCombos = "";
                for (ArchDef.Component toC : a_arch.getComponents()) {
                    if (fromC != toC) {
                        if (fromC.allowedDependency(toC) || toC.allowedDependency(fromC)) {
                            for (dmDependency.Type dt : dmDependency.Type.values()) {
                                allCdaCombos += getComponentComponentRelationString(fromC.getName(), dt, toC.getName()) + " ";
                            }
                        }
                    }
                }

                data.add(createDenseInstance(features, 2, components.indexOf(fromC.getName()), allCdaCombos, 1.0));
            }
        }

        // add the node stuff
        String relations = "";
        for (ClusteredNode n : a_nodes) {

            // add the cda for the node
            //relations = getDependencyStringFromNode(n, a_nodes) + " " +  getDependencyStringToNode(n, a_nodes) + " ";
            relations = getMappedCDAWords(n, a_nodes);
            data.add(createDenseInstance(features, 2, components.indexOf(n.getClusteringComponentName()), relations, 1.0));
            // apparently weighting at the instance level seems to work... question is how :P
            // however the results seem very small, also my guess is that we have many interdependent attributes overall making binary filtering more successful
            // it would be a good idea to filter the attributes based on their correlation and remove highly correlated features (i.e. remove one and keep one)


            // add the identifier texts for the node
            relations = getNodeWords(n.get(), a_stemmer);

            data.add(createDenseInstance(features, 2, components.indexOf(n.getClusteringComponentName()), relations, 1.0));
        }

        data.setClassIndex(0);

        try {
            a_filter.setInputFormat(data);
            data = Filter.useFilter(data, a_filter);

            Enumeration<Attribute> attribs = data.enumerateAttributes();
            /*while(attribs.hasMoreElements()) {
                Attribute a = attribs.nextElement();
                if (isCDAWord(a.name())) {
                    //a.setWeight(0.5);
                }
            }*/

            return data;
        } catch (Exception e) {

            System.out.println(relations);
            System.out.println(e.toString());
            e.printStackTrace();

            return null;
        }
    }

    // this is just a hack
    private boolean isCDAWord(String a_word) {
        for (dmDependency.Type dt : dmDependency.Type.values()) {
            if (a_word.indexOf(dt.toString()) > 0) {
                return true;
            }
        }
        return false;
    }
}
