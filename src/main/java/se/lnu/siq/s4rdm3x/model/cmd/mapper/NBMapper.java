package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.experiments.ExperimentRunData;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.bayes.NaiveBayesMultinomialText;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class NBMapper {

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

    public ArrayList<CNode> m_clusteredElements = new ArrayList<>();
    protected ArchDef m_arch;


    public int m_consideredNodes = 0;
    public int m_automaticallyMappedNodes = 0;
    public int m_manuallyMappedNodes = 0;
    public int m_failedMappings = 0;    // if the manual advice fails
    public int m_autoWrong = 0;

    private double m_clusteringThreshold = 0.90;

    private boolean m_doStemm = false;
    private Filter m_filter = new StringToWordVector();
    private double [] m_initialDistribution = null;

    public NBMapper(ArchDef a_arch) {
        m_arch = a_arch;
        ((StringToWordVector)m_filter).setOutputWordCounts(false);
    }
    public NBMapper(ArchDef a_arch, double [] a_initialDistribution) {
        m_arch = a_arch;
        m_initialDistribution = a_initialDistribution;
        ((StringToWordVector)m_filter).setOutputWordCounts(false);
    }

    public void setClusteringThreshold(double a_probability) {
        m_clusteringThreshold = a_probability;
    }

    public double getClusteringThreshold() {
        return m_clusteringThreshold;
    }

    protected java.util.ArrayList<CNode> getOrphanNodes(CGraph a_g) {

        java.util.ArrayList<CNode> ret = new ArrayList<>();
        for (CNode n : a_g.getNodes()) {
            if (m_arch.getMappedComponent(n) != null && m_arch.getClusteredComponent(n) == null) {
                ret.add(n);
            }
        }

        return ret;
    }

    java.util.ArrayList<CNode> getInitiallyMappedNodes(CGraph a_g) {
        java.util.ArrayList<CNode> ret = new ArrayList<>();
        for (CNode n : a_g.getNodes()) {
            if (m_arch.getMappedComponent(n) != null && m_arch.getClusteredComponent(n) != null && m_arch.getClusteredComponent(n).getClusteringType(n) == ArchDef.Component.ClusteringType.Initial) {
                ret.add(n);
            }
        }

        return ret;
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

        ArrayList<CNode> orphans = getOrphanNodes(a_g);
        ArrayList<CNode> initiallyMapped = getInitiallyMappedNodes(a_g);
        m_clusteredElements.clear();

        weka.core.Instances trainingData = getTrainingData(initiallyMapped, m_arch, getFilter());
        //weka.core.Instances predictionData = getPredictionData(a_g);

        m_consideredNodes = orphans.size();


        //Classifier nbClassifier = new NaiveBayes();   // avg 28% wrong
        Classifier nbClassifier = new Classifier(); // avg 23% wrong
        //Classifier nbClassifier = new RandomForest(); // avg 25% wrong
        //Classifier nbClassifier = new RandomTree(); // avg 43% wrong

        try {
            nbClassifier.buildClassifier(trainingData);



            if (m_initialDistribution != null && m_initialDistribution.length == nbClassifier.getProbabilityOfClass().length) {
                for (int dIx = 0; dIx < nbClassifier.getProbabilityOfClass().length; dIx++) {
                    nbClassifier.getProbabilityOfClass()[dIx] = m_initialDistribution[dIx];
                }
            }

            //System.out.print(" the expression for the input data as per algorithm is ");
            //System.out.println(nbClassifier);

            for (CNode orphanNode : orphans) {
                double [] attraction = new double[m_arch.getComponentCount()];

                for (int i = 0; i < m_arch.getComponentCount(); i++) {
//                    double index = nbClassifier.classifyInstance(data.instance(i));
                    Instances data = getPredictionDataForNode(orphanNode, initiallyMapped, m_arch.getComponentNames(), m_arch.getComponent(i), getFilter());
                    double [] distribution = nbClassifier.distributionForInstance(data.instance(0));
                    /*System.out.print("[");
                    for (int dIx = 0; dIx < distribution.length; dIx++) {
                        System.out.print(distribution[dIx] + " ");
                    }
                    System.out.println("]");*/

                    attraction[i] = distribution[i];
                }

                orphanNode.setAttractions(attraction);
            }

            for (CNode orphanNode : orphans) {
                // if the attraction is above some threshold then we cluster
                double [] attractions = orphanNode.getAttractions();
                int maxIx = 0;
                for (int cIx = 1; cIx < attractions.length; cIx++) {
                    if (attractions[maxIx] < attractions[cIx]) {
                        maxIx = cIx;
                    }
                }

                if (attractions[maxIx] > m_clusteringThreshold) {
                    m_arch.getComponent(maxIx).clusterToNode(orphanNode, ArchDef.Component.ClusteringType.Automatic);
                    m_clusteredElements.add(orphanNode);
                    m_automaticallyMappedNodes++;
                    //System.out.println("Clustered to: " + orphanNode.getClusteringComponentName() +" mapped to: " + orphanNode.getMapping());

                    if (m_arch.getComponent(orphanNode.getMapping()) != m_arch.getComponent(maxIx)) {
                        m_autoWrong++;
                    }
                }
            }

            //nbClassifier.classifyInstance();

        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }

    private Instances getPredictionDataForNode(CNode a_node, Iterable<CNode> a_mappedNodes, String[] a_componentNames, ArchDef.Component a_component, Filter a_filter) {
        ArrayList<Attribute> attributes = new ArrayList<>();

        // first we have the architectural components
        List<String> componentNames = Arrays.asList(a_componentNames);
        attributes.add(new Attribute("component_blarg17", componentNames));
        attributes.add(new Attribute("relations_blarg17", (ArrayList<String>) null));

        Instances data = new Instances("PredictionData", attributes, 0);

        String nodeText = " ";

        for (dmClass c : a_node.getClasses()) {
            for (String t : c.getTexts()) {
                nodeText += deCamelCase(t, 3, m_doStemm) + " ";
            }
        }

        nodeText += deCamelCase(a_node.getLogicName().replace(".", " "), 3, m_doStemm);

        //for (int i = 0; i < a_arch.getComponentCount(); i++) {
            double[] values = new double[data.numAttributes()];
            values[0] = componentNames.indexOf(a_component);
            String relations = getDependencyStringFromNode(a_node, a_component.getName(), a_mappedNodes);
            relations += " " + getDependencyStringToNode(a_node, a_component.getName(), a_mappedNodes);

            relations += " " + nodeText;
            //String relations = nodeText;

            values[0] = data.attribute(1).addStringValue(relations);
            data.add(new DenseInstance(1.0, values));
        //}


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

    public String deCamelCase(String a_string, int a_minLength, boolean a_doStemm) {

        weka.core.stemmers.Stemmer stemmer = null;

        if (a_doStemm) {
            //stemmer = new weka.core.stemmers.LovinsStemmer();
            stemmer = new weka.core.stemmers.SnowballStemmer();
        }

        String ret = "";
        for (int i = 0; i < 10; i++) {
            a_string = a_string.replace("" + i, " ");
        }
        a_string = a_string.replace("_", " ");
        a_string = a_string.replace("-", " ");
        for (String p : a_string.split(" ")) {
            // https://stackoverflow.com/questions/7593969/regex-to-split-camelcase-or-titlecase-advanced
            for (String w : p.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
                w = w.toLowerCase();
                if (w.length() >= a_minLength && !w.contains("$")) {
                    if (stemmer != null) {
                        w = stemmer.stem(w);
                    }

                    if (w.equals("tmp")) {
                        w = "temp";
                    }

                    ret += w + " ";
                }
            }
        }

        return ret.trim();
    }

    private String getComponentComponentRelationString(String a_from, dmDependency.Type a_relation, String a_to) {
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
    }

    public Instances getTrainingData(Iterable<CNode>a_nodes, ArchDef a_arch, Filter a_filter) {
        ArrayList<Attribute> attributes = new ArrayList<>();

        // first we have the architectural components
        List<String> components = Arrays.asList(a_arch.getComponentNames());


        attributes.add(new Attribute("component_blarg17", components));
        attributes.add(new Attribute("relations_blarg17", (ArrayList<String>) null));

        Instances data = new Instances("TrainingData", attributes, 0);

        if (m_addRawArchitectureTrainingData) {
            for (ArchDef.Component from : a_arch.getComponents()) {
                double[] values = new double[data.numAttributes()];
                values[0] = components.indexOf(from.getName());
                String relations = deCamelCase(from.getName(), 3, m_doStemm);
                for (ArchDef.Component to : a_arch.getComponents()) {
                    if (from == to || from.allowedDependency(to)) {


                        for (dmDependency.Type t : dmDependency.Type.values()) {
                             relations +=  getComponentComponentRelationString(from.getName(), t, to.getName()) + " ";
                        }
                    } else if (to.allowedDependency(from)) {
                        for (dmDependency.Type t : dmDependency.Type.values()) {
                            relations +=  getComponentComponentRelationString(to.getName(), t, from.getName()) + " ";
                        }
                    }
                }
                values[1] = data.attribute(1).addStringValue(relations);
                data.add(new DenseInstance(1.0, values));
            }
        }

        String relations = "";
        for (CNode n : a_arch.getMappedNodes(a_nodes)) {
            double[] values = new double[data.numAttributes()];
            values[0] = components.indexOf(n.getMapping());
            relations = getDependencyStringFromNode(n, a_nodes) + " " +  getDependencyStringToNode(n, a_nodes) + " ";

            for (dmClass c : n.getClasses()) {
                for (String t : c.getTexts()) {
                    relations += deCamelCase(t, 3, m_doStemm) + " ";
                }
            }
            relations += " " + deCamelCase(n.getLogicName().replace(".", " "), 3, m_doStemm);

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
