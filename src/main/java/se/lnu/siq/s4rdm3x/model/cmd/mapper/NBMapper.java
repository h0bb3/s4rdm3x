package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import se.lnu.siq.s4rdm3x.model.CGraph;
import weka.core.*;
import weka.core.stemmers.SnowballStemmer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Mapper based on textual analysis of nodes using a naive Bayes classifier. Evaluated by Tobias Olsson, Morgan Ericsson, and Anna Wingkvist. 2019. Semi-automatic mapping of source code using naive Bayes. In Proceedings of the 13th European Conference on Software Architecture - Volume 2 (ECSA ’19). Association for Computing Machinery, New York, NY, USA, 209–216. DOI:https://doi.org/10.1145/3344948.3344984
 *
 */
public class NBMapper extends IRMapperBase {

    private boolean m_addRawArchitectureTrainingData = false;


    public void doStemming(boolean a_doStemming) {
        m_doStemm = a_doStemming;
    }

    public void doWordCount(boolean a_doWordCount) {
        ((StringToWordVector)m_filter).setOutputWordCounts(a_doWordCount);
    }



    public static class Classifier extends weka.classifiers.bayes.NaiveBayesMultinomial {

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
    private Filter m_filter = new StringToWordVector();
    private double [] m_initialDistribution = null;

    public NBMapper(ArchDef a_arch, boolean a_doUseCDA, boolean a_doUseNodeText, boolean a_doUseNodeName, boolean a_doUseArchComponentName, int a_minWordLength, double a_mappingThreshold) {
        super(a_arch, false, a_doUseCDA, a_doUseNodeText, a_doUseNodeName, a_doUseArchComponentName, a_minWordLength);
        ((StringToWordVector)m_filter).setOutputWordCounts(false);
        ((StringToWordVector) m_filter).setTFTransform(false);
        ((StringToWordVector) m_filter).setIDFTransform(false);
        setMappingThreshold(a_mappingThreshold);
    }
    public NBMapper(ArchDef a_arch, boolean a_doManualMapping, boolean a_doUseCDA, boolean a_doUseNodeText, boolean a_doUseNodeName, boolean a_doUseArchComponentName, int a_minWordLength, double [] a_initialDistribution, double a_mappingThreshold) {
        super(a_arch, a_doManualMapping, a_doUseCDA, a_doUseNodeText, a_doUseNodeName, a_doUseArchComponentName, a_minWordLength);
        m_initialDistribution = a_initialDistribution;
        ((StringToWordVector)m_filter).setOutputWordCounts(false);
        ((StringToWordVector) m_filter).setTFTransform(false);
        ((StringToWordVector) m_filter).setIDFTransform(false);
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

    public void run(CGraph a_g) {

        // get the mapped nodes
        // compute word frequencies
        // train model
        // execute model and compute attractions (i.e. probabilities)
        // map automatically or manually
        // iterate

        ArrayList<OrphanNode> orphans = getOrphanNodes(a_g);
        ArrayList<ClusteredNode> initiallyMapped = getInitiallyMappedNodes(a_g);

        weka.core.stemmers.Stemmer stemmer = null;
        if (m_doStemm) {
            stemmer = new weka.core.stemmers.SnowballStemmer();
            do {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {

                }
            } while (!((SnowballStemmer) stemmer).stemmerTipText().contains("english"));  // when using multiple threads this is apparently needed...
        }

        weka.core.Instances trainingData = getTrainingData(initiallyMapped, m_arch, getFilter(), stemmer);

        m_consideredNodes = orphans.size();
        
        Classifier nbClassifier = new Classifier(); // avg 23% wrong

        try {

            nbClassifier.buildClassifier(trainingData);


            if (m_initialDistribution != null && m_initialDistribution.length == nbClassifier.getProbabilityOfClass().length) {
                for (int dIx = 0; dIx < nbClassifier.getProbabilityOfClass().length; dIx++) {
                    nbClassifier.getProbabilityOfClass()[dIx] = m_initialDistribution[dIx];
                }
            }

            //System.out.print(" the expression for the input data as per algorithm is ");
            //System.out.println(nbClassifier);

            for (OrphanNode orphanNode : orphans) {
                double [] attraction = new double[m_arch.getComponentCount()];

                for (int i = 0; i < m_arch.getComponentCount(); i++) {
//                    double index = nbClassifier.classifyInstance(data.instance(i));
                    Instances data = getPredictionDataForNode(orphanNode, initiallyMapped, m_arch.getComponentNames(), m_arch.getComponent(i), getFilter(), stemmer);



                    double [] distribution = nbClassifier.distributionForInstance(data.instance(0));
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


    private Instances getPredictionDataForNode(OrphanNode a_node, Iterable<ClusteredNode> a_mappedNodes, String[] a_componentNames, ArchDef.Component a_component, Filter a_filter, weka.core.stemmers.Stemmer a_stemmer) {
        ArrayList<Attribute> attributes = new ArrayList<>();

        // first we have the architectural components
        List<String> componentNames = Arrays.asList(a_componentNames);
        attributes.add(new Attribute("hypothetical_component", componentNames));
        attributes.add(new Attribute("model_features", (ArrayList<String>) null));

        Instances data = new Instances("PredictionData", attributes, 0);

        String nodeText = getNodeWords(a_node.get(), a_stemmer);

        double[] values = new double[data.numAttributes()];
        String relations = getUnmappedCDAWords(a_node, a_component, a_mappedNodes);

        relations += " " + nodeText;

        values[0] = componentNames.indexOf(a_node.getMapping());
        values[1] = data.attribute(1).addStringValue(relations);
        data.add(new DenseInstance(1.0, values));

        data.setClassIndex(0);

        try {
            //filter.setInputFormat(data);
            data = Filter.useFilter(data, a_filter);


            return data;
        } catch (Exception e) {

            System.out.println(e.toString());
            e.printStackTrace();

            return null;
        }
    }


    public Instances getTrainingData(Iterable<ClusteredNode> a_nodes, ArchDef a_arch, Filter a_filter, weka.core.stemmers.Stemmer a_stemmer) {
        ArrayList<Attribute> attributes = new ArrayList<>();

        // first we have the architectural components
        List<String> components = Arrays.asList(a_arch.getComponentNames());


        attributes.add(new Attribute("hypothetical_component", components));
        attributes.add(new Attribute("model_features", (ArrayList<String>) null));

        Instances data = new Instances("TrainingData", attributes, 0);

        // add the component names
        if (doUseArchComponentName()) {
            for (ArchDef.Component c : a_arch.getComponents()) {
                double[] values = new double[data.numAttributes()];
                values[0] = components.indexOf(c.getName());
                String relations = getArchComponentWords(c, a_stemmer);
                if (relations.length() > 0) {
                    values[1] = data.attribute(1).addStringValue(relations);
                    data.add(new DenseInstance(1.0, values));
                }
            }
        }

        // add the node stuff
        String relations = "";
        for (ClusteredNode n : a_nodes) {
            double[] values = new double[data.numAttributes()];
            values[0] = components.indexOf(n.getClusteringComponentName());

            // add the cda for the node
            //relations = getDependencyStringFromNode(n, a_nodes) + " " +  getDependencyStringToNode(n, a_nodes) + " ";
            relations = getMappedCDAWords(n, a_nodes);
            relations += " ";

            // add the identifier texts for the node
            relations += getNodeWords(n.get(), a_stemmer);

            values[1] = data.attribute(1).addStringValue(relations);
            data.add(new DenseInstance(1.0, values));
        }

        data.setClassIndex(0);

        try {
            a_filter.setInputFormat(data);
            data = Filter.useFilter(data, a_filter);
            return data;
        } catch (Exception e) {

            System.out.println(relations);
            System.out.println(e.toString());
            e.printStackTrace();

            return null;
        }
    }
}
