package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.stemmers.SnowballStemmer;
import weka.core.stemmers.Stemmer;
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
public class NBFileMapper {

    private boolean m_addRawArchitectureTrainingData = false;
    private Classifier m_nbClassifier;
    private SnowballStemmer m_stemmer;
    private ArchDef m_arch;

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

    public NBFileMapper(double a_mappingThreshold) {
        m_filter.setOutputWordCounts(false);
        m_filter.setTFTransform(false);
        m_filter.setIDFTransform(false);
        setMappingThreshold(a_mappingThreshold);
    }


    public void setMappingThreshold(double a_threshold) {
        m_mappingThreshold = a_threshold;
    }

    public double getClusteringThreshold() {
        return m_mappingThreshold;
    }

    public Filter getFilter() {
        return m_filter;
    }

    public void buildClassifier(ArchDef a_arch) {

        m_arch = a_arch;
        m_stemmer = null;
        if (m_doStemm) {
            m_stemmer = new SnowballStemmer();
            do {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {

                }
            } while (!(m_stemmer).stemmerTipText().contains("english"));  // when using multiple threads this is apparently needed...
        }

        Instances trainingData = getTrainingData(m_arch, getFilter(), m_stemmer);

        m_nbClassifier = new Classifier();
        try {
            m_nbClassifier.buildClassifier(trainingData);
            setUniformClassProbabilities(m_nbClassifier);
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }

    }

    public ArchDef.Component suggest(CNode a_node) {

        // get the mapped nodes
        // compute word frequencies
        // train model
        // execute model and compute attractions (i.e. probabilities)
        // map automatically or manually
        // iterate
        try {

            Instances data = getPredictionDataForNode(a_node, m_arch.getComponentNames(), getFilter(), m_stemmer);

            double [] distribution = m_nbClassifier.distributionForInstance(data.instance(0));
            a_node.setAttractions(distribution);

            ArchDef.Component autoClusteredTo = doAutoMapping(a_node, m_arch, m_mappingThreshold);
            return autoClusteredTo;

        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }

        return null;
    }


    public ArchDef.Component doAutoMapping(CNode a_node, ArchDef a_archDef, double a_clusteringThreshold) {
        double attractions[] = a_node.getAttractions();
        int[] maxes= getMaxIndices(attractions);
        int maxIx = maxes[0];
        int maxIx2 = maxes[1];

        if (attractions[maxIx] > attractions[maxIx2] * a_clusteringThreshold) {
            return a_archDef.getComponent(maxIx);
        }

        return null;

    }

    private ArchDef.Component doAutoMappingAbsThreshold(CNode a_node, ArchDef a_arch, double a_mappingThreshold) {
        double attractions[] = a_node.getAttractions();
        int[] maxes= getMaxIndices(attractions);
        int maxIx = maxes[0];
        int maxIx2 = maxes[1];

        if (attractions[maxIx] > a_mappingThreshold && attractions[maxIx] > attractions[maxIx2]) {
            return a_arch.getComponent(maxIx);
        }

        return null;
    }

    public int[] getMaxIndices(double[] a_values) {
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

    protected void setUniformClassProbabilities(Classifier a_classifier) {
        final double uniformProb = 1.0 / m_arch.getComponentCount();

        for (int dIx = 0; dIx < a_classifier.getProbabilityOfClass().length; dIx++) {
            a_classifier.getProbabilityOfClass()[dIx] = uniformProb;
        }
    }


    protected Instances getPredictionDataForNode(CNode a_node, String[] a_componentNames, Filter a_filter, weka.core.stemmers.Stemmer a_stemmer) {
        ArrayList<Attribute> attributes = new ArrayList<>();

        // first we have the architectural components
        List<String> componentNames = Arrays.asList(a_componentNames);
        attributes.add(new Attribute("concrete_component", componentNames));
        Attribute features = new Attribute("model_features", (ArrayList<String>) null);
        attributes.add(features);

        Instances data = new Instances("PredictionData", attributes, 0);

        String nodeText = toString(getNodeWords(a_node, a_stemmer, 2), " ").toLowerCase();

        // the mapping is used for the concrete component, this is the actual answer but it is only used for confusion matrix stuff in weka and does not affect the prediction
        data.add(createDenseInstance(features, 2, componentNames.indexOf(a_node.getMapping()), nodeText, 1.0));

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

    List<String> getWords(List<String> a_words, Stemmer a_stemmer, int a_tupleSize) {
        List<String> ret = createTuples(a_words, 1);

        if (a_tupleSize > 1 && a_words.size() > 1) {
            ret.addAll(createTuples(a_words, a_tupleSize));
        }

        return ret;
    }

    private String toString(List<String> a_words, String a_delimeter) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < a_words.size(); i++, b.append(a_delimeter)) {
            b.append(a_words.get(i));
        }

        return b.toString();
    }

    public List<String> deCamelCase(String a_string, int a_minLength, weka.core.stemmers.Stemmer a_stemmer) {
        ArrayList<String> ret = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            a_string = a_string.replace("" + i, " ");
        }
        a_string = a_string.replace("_", " ");
        a_string = a_string.replace("-", " ");
        a_string = a_string.replace(".", " ");
        for (String p : a_string.split(" ")) {
            // https://stackoverflow.com/questions/7593969/regex-to-split-camelcase-or-titlecase-advanced
            for (String w : p.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
                w = w.toLowerCase();
                if (w.length() >= a_minLength && !w.contains("$")) {
                    if (a_stemmer != null ) {
                        w = a_stemmer.stem(w);
                    }

                    ret.add(w);
                }
            }
        }

        return ret;
    }

    private List<String> getNodeWords(CNode a_node, Stemmer a_stemmer, int a_tupleSize) {
        List<String> parts = Arrays.asList(a_node.getLogicName().split("\\."));
        List<String> fileNameParts = deCamelCase(parts.get(parts.size() - 1), 0, a_stemmer);
        parts = getWords(parts, a_stemmer, a_tupleSize);

        // the last part is special as it is the filename, we could use deCamelCasing for this

        if (fileNameParts.size() > 1) {

            // if we have more than 2 parts then we can create tuples (i.e. the tow part only tuple is already in the parts list)
            if (fileNameParts.size() > 2) {
                fileNameParts.addAll(createTuples(fileNameParts, 2));
            }

            parts.addAll(fileNameParts);
        }

        return parts;
    }

    private List<String> getArchComponentWords(ArchDef.Component c, Stemmer a_stemmer, int a_tupleSize) {
        return getWords(Arrays.asList(c.getName().toLowerCase().split("\\.")), a_stemmer, a_tupleSize);
    }

    private List<String> createTuples(List<String> a_parts, int a_size) {
        ArrayList<String> ret = new ArrayList<>();

        for (int i = 0; i < a_parts.size() - a_size + 1; i++) {
            StringBuilder b = new StringBuilder();
            for (int j = i; j < i + a_size; j++) {
                b.append(a_parts.get(j));
            }
            ret.add(b.toString());
        }

        return ret;
    }

    protected DenseInstance createDenseInstance(Attribute a_attrib, int a_numAttributes, int a_componentIndex, String a_words, double a_weight) {
        double[] values = new double[a_numAttributes];
        values[0] = a_componentIndex;
        values[1] = a_attrib.addStringValue(a_words);

        return new DenseInstance(a_weight, values);
    }

    protected Instances getTrainingData(ArchDef a_arch, Filter a_filter, weka.core.stemmers.Stemmer a_stemmer) {
        ArrayList<Attribute> attributes = new ArrayList<>();

        // first we have the architectural components
        List<String> components = Arrays.asList(a_arch.getComponentNames());


        attributes.add(new Attribute("concrete_component", components));
        Attribute features = new Attribute("model_features", (ArrayList<String>) null);
        attributes.add(features);

        Instances data = new Instances("TrainingData", attributes, 0);

        // add the component names
        for (ArchDef.Component c : a_arch.getComponents()) {
            String relations = toString(getArchComponentWords(c, a_stemmer, 2), " ").toLowerCase();
            if (relations.length() > 0) {
                data.add(createDenseInstance(features, 2, components.indexOf(c.getName()), relations, 1.0));
            }
        }

        data.setClassIndex(0);

        try {
            a_filter.setInputFormat(data);
            data = Filter.useFilter(data, a_filter);

            return data;
        } catch (Exception e) {

            System.out.println(e.toString());
            e.printStackTrace();

            return null;
        }
    }


}
