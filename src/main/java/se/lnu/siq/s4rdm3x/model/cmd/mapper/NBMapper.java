package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import weka.attributeSelection.*;
import weka.classifiers.bayes.net.search.local.GeneticSearch;
import weka.core.*;
import weka.core.pmml.jaxbbindings.Cluster;
import weka.core.stemmers.SnowballStemmer;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private double m_clusteringThreshold = 2.0;

    private boolean m_doStemm = false;
    private Filter m_filter = new StringToWordVector();
    private double [] m_initialDistribution = null;

    public NBMapper(ArchDef a_arch, boolean a_doUseCDA, boolean a_doUseNodeText, boolean a_doUseNodeName, boolean a_doUseArchComponentName, int a_minWordLength) {
        super(a_arch, false, a_doUseCDA, a_doUseNodeText, a_doUseNodeName, a_doUseArchComponentName, a_minWordLength);
        ((StringToWordVector)m_filter).setOutputWordCounts(false);
        ((StringToWordVector) m_filter).setTFTransform(false);
        ((StringToWordVector) m_filter).setIDFTransform(false);
    }
    public NBMapper(ArchDef a_arch, boolean a_doManualMapping, boolean a_doUseCDA, boolean a_doUseNodeText, boolean a_doUseNodeName, boolean a_doUseArchComponentName, int a_minWordLength, double [] a_initialDistribution) {
        super(a_arch, a_doManualMapping, a_doUseCDA, a_doUseNodeText, a_doUseNodeName, a_doUseArchComponentName, a_minWordLength);
        m_initialDistribution = a_initialDistribution;
        ((StringToWordVector)m_filter).setOutputWordCounts(false);
        ((StringToWordVector) m_filter).setTFTransform(false);
        ((StringToWordVector) m_filter).setIDFTransform(false);
    }

    public void setClusteringThreshold(double a_thresholdMultiplier) {
        m_clusteringThreshold = a_thresholdMultiplier;
        if (m_clusteringThreshold < 1) {
            m_clusteringThreshold = 1;
        }
    }

    public double getClusteringThreshold() {
        return m_clusteringThreshold;
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
        //weka.core.Instances predictionData = getPredictionData(a_g);

        m_consideredNodes = orphans.size();


        //Classifier nbClassifier = new NaiveBayes();   // avg 28% wrong
        Classifier nbClassifier = new Classifier(); // avg 23% wrong
        //Classifier nbClassifier = new RandomForest(); // avg 25% wrong
        //Classifier nbClassifier = new RandomTree(); // avg 43% wrong



        try {

            nbClassifier.buildClassifier(trainingData);

            //SerializationHelper sh = new SerializationHelper();
            //SerializationHelper.write("C:\\hObbE\\projects\\coding\\research\\s4rdm3x\\testmodel", nbClassifier);


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

                ArchDef.Component autoClusteredTo = doAutoMappingAbsThreshold(orphanNode, m_arch, 0.9);
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

            /*class MappingNode {
                CNode m_node;
                ArchDef.Component m_clusterTo;
                double m_attractionDiff;

                MappingNode(CNode a_node, ArchDef.Component a_clusterTo, double a_attractionDiff) {
                    m_node = a_node;
                    m_clusterTo = a_clusterTo;
                    m_attractionDiff = a_attractionDiff;
                }
            }
            ArrayList<MappingNode> mappingCandidates = new ArrayList<>();
            for (CNode orphanNode : orphans) {
                // if the attraction is above some threshold then we cluster
                double [] attractions = orphanNode.getAttractions();
                int[] maxes= getMaxIndices(attractions);
                int maxIx = maxes[0];
                int maxIx2 = maxes[1];


                if (attractions[maxIx] > attractions[maxIx2] * m_clusteringThreshold) {
                    mappingCandidates.add(new MappingNode(orphanNode, m_arch.getComponent(maxIx), attractions[maxIx]- attractions[maxIx2]));
                } else if (false && doManualMapping()) {
                    manualMapping(orphanNode, m_arch);
                }
            }



            int maxMappings;

            // This is the code that maps 10% of the best candidates (max 10, min 1)
            // this basically creates a lot of iterations
            if (false && doManualMapping()) {
                mappingCandidates.sort((n1, n2) -> {return Double.compare(n2.m_attractionDiff, n1.m_attractionDiff);});
                maxMappings = (int)(0.1 * mappingCandidates.size());
                if (maxMappings > 10) {
                    maxMappings = 10;
                } else if (maxMappings <= 0) {
                    maxMappings = 1;
                }
            } else {
                maxMappings = mappingCandidates.size();
            }

            for (MappingNode n : mappingCandidates) {
                if (maxMappings <= 0) {
                    break;
                } else {
                    maxMappings--;
                    n.m_clusterTo.clusterToNode(n.m_node, ArchDef.Component.ClusteringType.Automatic);
                    addAutoClusteredOrphan(n.m_node);
                    //System.out.println("Clustered to: " + orphanNode.getClusteringComponentName() +" mapped to: " + orphanNode.getMapping());

                    if (m_arch.getComponent(n.m_node.getMapping()) != n.m_clusterTo) {
                        m_autoWrong++;
                    }
                }
            }*/

            //nbClassifier.classifyInstance();

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


 /*           StringToWordVector s2wv = (StringToWordVector)a_filter;
            Instance i = data.get(0);

            for (int aIx = 0; aIx < i.numAttributes(); aIx++) {
                Attribute a = i.attribute(aIx);
                if (i.value(a) > 0) {
                    System.out.print(a.name() + " ");
                }
            }
            System.out.println();*/


            return data;
        } catch (Exception e) {

            System.out.println(e.toString());
            e.printStackTrace();

            return null;
        }
    }



   /* private String getComponentComponentRelationString(String a_from, dmDependency.Type a_relation, String a_to) {
        return a_from.replace(".", "") + a_relation + a_to.replace(".", "");
    }

    private String getDependencyStringFromNode(CNode a_from, String a_nodeComponentName, Iterable<CNode> a_tos) {
        String relations = "";
        for (CNode to : a_tos) {
            if (to != a_from) {
                for (dmDependency d : a_from.getDependencies(to)) {
                    for (int i = 0; i < d.getCount(); i++) {
                        relations += getComponentComponentRelationString(a_nodeComponentName, d.getType(), to.getMapping()) + " ";
                    }
                }
            }
        }

        relations = relations.trim();

        return relations;
    }

    private String getDependencyStringToNode(CNode a_to, String a_nodeComponentName, Iterable<CNode> a_froms) {
        String relations = "";
        for (CNode from : a_froms) {
            if (a_to != from) {
                for (dmDependency d : from.getDependencies(a_to)) {
                    for (int i = 0; i < d.getCount(); i++) {
                        relations += getComponentComponentRelationString(from.getMapping(), d.getType(), a_nodeComponentName) + " ";//from.getMapping().replace(".", "") + d.getType() + a_nodeComponentName.replace(".", "") + " ";
                    }
                }
            }
        }

        relations = relations.trim();

        return relations;

    }

    String getDependencyStringToNode(CNode a_to, Iterable<CNode> a_froms) {
        return getDependencyStringToNode(a_to, a_to.getMapping(), a_froms);
    }

    String getDependencyStringFromNode(CNode a_from, Iterable<CNode>a_tos) {
        return getDependencyStringFromNode(a_from, a_from.getMapping(), a_tos);
    }*/

    public Instances getTrainingData(Iterable<ClusteredNode> a_nodes, ArchDef a_arch, Filter a_filter, weka.core.stemmers.Stemmer a_stemmer) {
        ArrayList<Attribute> attributes = new ArrayList<>();

        // first we have the architectural components
        List<String> components = Arrays.asList(a_arch.getComponentNames());


        attributes.add(new Attribute("hypothetical_component", components));
        attributes.add(new Attribute("model_features", (ArrayList<String>) null));

        Instances data = new Instances("TrainingData", attributes, 0);

        /*if (doUseCDA()) {
            for (ArchDef.Component from : a_arch.getComponents()) {
                double[] values = new double[data.numAttributes()];
                values[0] = components.indexOf(from.getName());
                String relations = "";

                // this adds all the allowed dependencies from the architectural definition to the training data
                // this does not seem to increase the accuracy of the model so we don't use it.
                if (doManualMapping()) {
                    for (ArchDef.Component to : a_arch.getComponents()) {
                        if (from == to || from.allowedDependency(to)) {
                            for (dmDependency.Type t : dmDependency.Type.values()) {
                                relations += getComponentComponentRelationString(from.getName(), t, to.getName()) + " ";
                            }
                        } else if (to.allowedDependency(from)) {
                            for (dmDependency.Type t : dmDependency.Type.values()) {
                                relations += getComponentComponentRelationString(to.getName(), t, from.getName()) + " ";
                            }
                        }
                    }
                }
                values[1] = data.attribute(1).addStringValue(relations);
                data.add(new DenseInstance(1.0, values));
            }
        }*/

        // we always allow self dependencies within the architectural components so we need to add these.
        /*if (doUseCDA()) {
            for (ArchDef.Component c : a_arch.getComponents()) {
                String relations = "";
                for (dmDependency.Type t : dmDependency.Type.values()) {
                    relations += getComponentComponentRelationString(c.getName(), t, c.getName()) + " ";
                }

                double[] values = new double[data.numAttributes()];
                values[0] = components.indexOf(c.getName());
                values[1] = data.attribute(1).addStringValue(relations);
                data.add(new DenseInstance(1.0, values));
            }
        }*/

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
