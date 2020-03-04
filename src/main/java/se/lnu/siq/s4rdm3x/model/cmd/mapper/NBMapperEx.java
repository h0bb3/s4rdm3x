package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import weka.attributeSelection.*;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.stemmers.SnowballStemmer;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NBMapperEx extends IRMapperBase {

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

    private double m_clusteringThreshold = 0.9;

    private boolean m_doStemm = false;
    private Filter m_filter = new StringToWordVector();
    private double [] m_initialDistribution = null;

    public NBMapperEx(ArchDef a_arch, boolean a_doUseCDA, boolean a_doUseNodeText, boolean a_doUseNodeName, boolean a_doUseArchComponentName, int a_minWordLength) {
        super(a_arch, false, a_doUseCDA, a_doUseNodeText, a_doUseNodeName, a_doUseArchComponentName, a_minWordLength);
        ((StringToWordVector)m_filter).setOutputWordCounts(false);
        ((StringToWordVector) m_filter).setTFTransform(false);
        ((StringToWordVector) m_filter).setIDFTransform(false);
    }
    public NBMapperEx(ArchDef a_arch, boolean a_doManualMapping, boolean a_doUseCDA, boolean a_doUseNodeText, boolean a_doUseNodeName, boolean a_doUseArchComponentName, int a_minWordLength, double [] a_initialDistribution) {
        super(a_arch, a_doManualMapping, a_doUseCDA, a_doUseNodeText, a_doUseNodeName, a_doUseArchComponentName, a_minWordLength);
        m_initialDistribution = a_initialDistribution;
        ((StringToWordVector)m_filter).setOutputWordCounts(false);
        ((StringToWordVector) m_filter).setTFTransform(false);
        ((StringToWordVector) m_filter).setIDFTransform(false);
    }

    public void setClusteringThreshold(double a_threshold) {
        m_clusteringThreshold = a_threshold;
        if (m_clusteringThreshold > 1) {
            m_clusteringThreshold = 0.9;
        } else if (m_clusteringThreshold < 0.0) {
            m_clusteringThreshold = 0.9;
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

        //weka.core.Instances trainingData = getTrainingData(initiallyMapped, m_arch, getFilter(), stemmer);
        //weka.core.Instances predictionData = getPredictionData(a_g);

        m_consideredNodes = orphans.size();


        //Classifier nbClassifier = new NaiveBayes();   // avg 28% wrong
        Classifier nbClassifier = new Classifier(); // avg 23% wrong
        //Classifier nbClassifier = new RandomForest(); // avg 25% wrong
        //Classifier nbClassifier = new RandomTree(); // avg 43% wrong



        try {

            //ASEvaluation eval = new GainRatioAttributeEval();
            /* Create a Weka's AS Search algorithm */
            //ASSearch search = new Ranker();
            /* Wrap WEKAs' Algorithms in bridge */
            //WekaAttributeSelection wekaattrsel = new WekaAttributeSelection(eval, search);
            /* Apply the algorithm to the data set */
            //wekaattrsel.build(data);
            /* Print out the score and rank  of each attribute */
            //for (int i = 0; i < wekaattrsel.noAttributes(); i++)
            //System [2].out.println("Attribute  " +  i +  "  Ranks  " + wekaattrsel.rank(i) + " and Scores " + wekaattrsel.score(i) );

            // This is some code that can use filters to remove attributes
            // remember to use the same filter on the prediction data
            AttributeSelection attrSel = new AttributeSelection();
            Ranker r = new Ranker();
            //r.setThreshold(0.01);
            //r.setNumToSelect(trainingData.numAttributes() / 100);
            r.setNumToSelect(1 + m_arch.getComponentCount() * m_arch.getComponentCount());

            GreedyStepwise gs = new GreedyStepwise();
            gs.setSearchBackwards(true);
            gs.setNumToSelect(1 + m_arch.getComponentCount() * m_arch.getComponentCount());

            BestFirst bf = new BestFirst();
            bf.setDirection(new SelectedTag(BestFirst.TAGS_SELECTION[2].getID(), BestFirst.TAGS_SELECTION));
            bf.setSearchTermination(100);

            ClassifierAttributeEval cae = new ClassifierAttributeEval();
            cae.setClassifier(new NBMapper.Classifier());


            //attrSel.setEvaluator(cae);
            //attrSel.setEvaluator(new GainRatioAttributeEval());
            //attrSel.setEvaluator(new SymmetricalUncertAttributeEval());
            //attrSel.setEvaluator(new OneRAttributeEval());
            //attrSel.setEvaluator(new ReliefFAttributeEval());
            attrSel.setEvaluator(new InfoGainAttributeEval());
            //attrSel.setEvaluator(new CorrelationAttributeEval());
            //attrSel.setEvaluator(new CfsSubsetEval());
            //attrSel.setEvaluator(new PrincipalComponents()); // This one takes ages
            attrSel.setSearch(r);
            //attrSel.setInputFormat(trainingData);


            //System.out.print("Attribute Count: " + trainingData.numAttributes());
            //trainingData = Filter.useFilter(trainingData, attrSel);
            //trainingData.setClassIndex(trainingData.numAttributes() - 1);
            //System.out.println(" vs : " + trainingData.numAttributes());

           //nbClassifier.buildClassifier(trainingData);

            //SerializationHelper sh = new SerializationHelper();
            //SerializationHelper.write("C:\\hObbE\\projects\\coding\\research\\s4rdm3x\\testmodel", nbClassifier);


            /*if (m_initialDistribution != null && m_initialDistribution.length == nbClassifier.getProbabilityOfClass().length) {
                for (int dIx = 0; dIx < nbClassifier.getProbabilityOfClass().length; dIx++) {
                    nbClassifier.getProbabilityOfClass()[dIx] = m_initialDistribution[dIx];
                }
            }*/

            // data = Filter.useFilter(data, attrSel);
            //  data.setClassIndex(data.numAttributes() - 1);

            //System.out.print(" the expression for the input data as per algorithm is ");
            //System.out.println(nbClassifier);

            for (OrphanNode orphanNode : orphans) {
                double [] attraction = new double[m_arch.getComponentCount()];

                weka.core.Instances trainingData = getTrainingData(initiallyMapped, orphans, orphanNode, m_arch, getFilter(), stemmer);
                nbClassifier.buildClassifier(trainingData);

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

                // calculate the orphans training data
                // something is needed here...
                //Instances data = getPredictionDataForNode(orphanNode, orphans, m_arch.getComponentNames(), "unknown", getFilter(), stemmer);
                //double [] distribution = nbClassifier.distributionForInstance(data.instance(0));


                orphanNode.setAttractions(attraction);

                ArchDef.Component autoClusteredTo = doAutoMapping(orphanNode, m_arch);
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

    public  int[] getMaxIndices(double[] a_values) {
        int[] ret = new int[] {0, 1};
        for (int cIx = 1; cIx < a_values.length; cIx++) {
            if (a_values[ret[0]] < a_values[cIx]) {
                ret[1] = ret[0];
                ret[0] = cIx;
            } else if (a_values[ret[1]] < a_values[cIx]) {
                ret[1] = cIx;
            }
        }

        return ret;
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


    public Instances getTrainingData(Iterable<ClusteredNode>a_initiallyMapped, Iterable<OrphanNode>a_orphans, OrphanNode a_currentOrphan, ArchDef a_arch, Filter a_filter, weka.core.stemmers.Stemmer a_stemmer) {
        ArrayList<Attribute> attributes = new ArrayList<>();

        // first we have the architectural components
        List<String> components = new ArrayList<>(Arrays.asList(a_arch.getComponentNames()));
        components.add("unmapped");


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

        // add the mapped node stuff
        String relations = "";
        for (ClusteredNode n : a_initiallyMapped) {
            double[] values = new double[data.numAttributes()];
            values[0] = components.indexOf(n.getClusteredComponent().getName());

            // add the cda for the node
            relations = getMappedCDAWords(n, a_initiallyMapped);
            relations += " ";


            // add the identifier texts for the node
            relations += getNodeWords(n.get(), a_stemmer);

            values[1] = data.attribute(1).addStringValue(relations);
            data.add(new DenseInstance(1.0, values));
        }

        // add the orphan node stuff
        relations = "";
        for (OrphanNode o : a_orphans) {
            if (o != a_currentOrphan) {
                double[] values = new double[data.numAttributes()];
                values[0] = components.indexOf("unmapped");

                // add the cda for the node
                //relations = getMappedCDAWords(o, a_orphans);
                relations += " ";


                // add the identifier texts for the node
                relations += getNodeWords(o.get(), a_stemmer);

                values[1] = data.attribute(1).addStringValue(relations);
                data.add(new DenseInstance(1.0, values));
            }
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

