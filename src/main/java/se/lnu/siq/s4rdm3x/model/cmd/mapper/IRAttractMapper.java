package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import javafx.scene.shape.Arc;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.stats;
import weka.core.stemmers.Stemmer;

import java.util.*;

public class IRAttractMapper extends IRMapperBase {

    public int m_autoWrong = 0;
    private double m_cdaWeight = 1.0;

    public static class WordVector {

        HashMap<String, Double> m_words = new HashMap<>();
        private double m_wordCount = 0;



        public double dot(WordVector a_vec) {
            double dot = 0;
            for (String w : m_words.keySet()) {
                if (a_vec.m_words.containsKey(w)) {
                    dot += m_words.get(w) * a_vec.m_words.get(w);
                }
            }

            return dot;
        }

        public double length() {
            double len = 0;
            for (Map.Entry<String, Double> e : m_words.entrySet()) {
                len += e.getValue() * e.getValue();
            }

            return Math.sqrt(len);
        }

        public double cosDistance(WordVector a_vec) {
            return dot(a_vec) / (length() * a_vec.length());
        }

        public void add(String a_word) {
            if (a_word.length() > 0) {
                m_wordCount += 1.0;
                if (m_words.containsKey(a_word)) {
                    m_words.replace(a_word, m_words.get(a_word) + 1.0);
                } else {
                    m_words.put(a_word, 1.0);
                }
            }
        }

        private void applyWeight(double a_weight) {
            for (Map.Entry<String, Double> e : m_words.entrySet()) {
                e.setValue(e.getValue() * a_weight);
            }
        }

        private double getMaxTermFrequency() {
            double max = 0;

            for (Map.Entry<String, Double> e : m_words.entrySet()) {
                if (e.getValue() > max) {
                    max = e.getValue();
                }
            }

            return max;
        }

        public void binaryTFNormalization() {
            for (Map.Entry<String, Double> e : m_words.entrySet()) {
                e.setValue(1.0);
            }
        }

        public void wordCountTFNormalization() {

            for (Map.Entry<String, Double> e : m_words.entrySet()) {
                //e.setValue(a_smoothing + (1.0 - a_smoothing) * e.getValue() / m_wordCount);
                e.setValue(e.getValue() / m_wordCount);
            }
        }

        public void maximumTFNormalization(double a_smoothing) {
            // from: https://nlp.stanford.edu/IR-book/pdf/06vect.pdf
            // section 6.4.2
            double max = getMaxTermFrequency();
            final double a = a_smoothing;

            for (Map.Entry<String, Double> e : m_words.entrySet()) {
                e.setValue(a + (1.0 - a) * e.getValue() / max);
            }
        }

        /*public void makeRelativeToAll() {
            int wordCount = 0;

            for (Map.Entry<String, Double> e : m_words.entrySet()) {
                wordCount += e.getValue();
            }

            for (Map.Entry<String, Double> e : m_words.entrySet()) {
                e.setValue(e.getValue() / (double)wordCount);
            }

        }

        public void normalize() {
            double len = length();

            for (Map.Entry<String, Double> e : m_words.entrySet()) {
                e.setValue(e.getValue() / len);
            }

        }*/

        public Iterable<String> getWords() {
            return m_words.keySet();
        }

        public double getFrequency(String a_word) {
            if (m_words.containsKey(a_word)) {
                return m_words.get(a_word);
            } else {
                return 0;
            }
        }

        public void iDF(int a_docCount, HashMap<String, Double> a_wordDocumentFrequency) {
            for (Map.Entry<String, Double> e : m_words.entrySet()) {
                double f = 1;
                if (a_wordDocumentFrequency.containsKey(e.getKey())) {
                    f = a_wordDocumentFrequency.get(e.getKey());
                }

                f = Math.log(a_docCount / f);

                e.setValue(e.getValue() * f);
            }
        }
    }

    public IRAttractMapper(ArchDef a_arch, boolean a_doManualMapping,  boolean a_doUseCDA, boolean a_doUseNodeText, boolean a_doUseNodeName, boolean a_doUseArchComponentName, int a_minWordLength) {
        super(a_arch, a_doManualMapping,  a_doUseCDA, a_doUseNodeText, a_doUseNodeName, a_doUseArchComponentName, a_minWordLength);
    }

    public double getDocumentFrequency(String a_word, Vector<WordVector> a_documents) {
        double ret = 0;
        for (WordVector wv : a_documents) {
            if (wv.getFrequency(a_word) > 0) {
                ret += 1;
            }
        }

        return ret;
    }

    public void run(CGraph a_g) {

        ArrayList<OrphanNode> orphans = getOrphanNodes(a_g);
        ArrayList<ClusteredNode> initiallyMapped = getInitiallyMappedNodes(a_g);
        HashMap<String, Double> wordDocumentFrequency = new HashMap<>();

        Stemmer stemmer = getStemmer();

        final double smoothing = 0.1;

        Vector<WordVector> trainingData = getTrainingData(initiallyMapped, m_arch, stemmer);
        for (WordVector wv : trainingData) {
            for (String word : wv.getWords()) {
                if (!wordDocumentFrequency.containsKey(word)) {
                    Double f = getDocumentFrequency(word, trainingData);
                    wordDocumentFrequency.put(word, f);
                }
            }
        }
        trainingData.forEach(wv -> wv.maximumTFNormalization(smoothing));
        //trainingData.forEach(wv -> wv.binaryTFNormalization());
        //trainingData.forEach(wv -> wv.wordCountTFNormalization());
        //trainingData.forEach(wv->wv.iDF(m_arch.getComponentCount(), wordDocumentFrequency));



        for (OrphanNode orphanNode : orphans) {
            double[] attraction = new double[m_arch.getComponentCount()];

            for (int i = 0; i < m_arch.getComponentCount(); i++) {
                WordVector words = getWordVector(orphanNode, stemmer);
                addWordsToWordVector(getUnmappedCDAWords(orphanNode, m_arch.getComponent(i), initiallyMapped), words);
                words.applyWeight(m_cdaWeight);

                // add the component names to each document
                m_arch.getComponents().forEach(c -> {
                    Vector<String> names = new Vector<>();
                    addWordsToVector(getArchComponentWords(c, stemmer), names);
                    names.forEach(w -> words.add(w));
                });

                words.maximumTFNormalization(smoothing);
                //words.binaryTFNormalization();
                //words.wordCountTFNormalization();
                //words.iDF(m_arch.getComponentCount(), wordDocumentFrequency);
                attraction[i] = words.cosDistance(trainingData.get(i));
            }

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
    }

    private void addWordsToWordVector(String a_words, WordVector a_target) {
        if (a_words.length() > 0) {
            String[] words = a_words.split(" ");
            for (int i = 0; i < words.length; i++) {
                a_target.add(words[i]);
            }
        }
    }


    private WordVector getWordVector(OrphanNode a_node, weka.core.stemmers.Stemmer a_stemmer) {
        WordVector ret = new WordVector();

        Vector<String> words = getWords(a_node.get(), a_stemmer);
        words.forEach(w -> ret.add(w));

        //ret.makeRelative();

        return ret;
    }

    public Vector<WordVector> getTrainingData(List<ClusteredNode> a_nodes, ArchDef a_arch) {
        return getTrainingData(a_nodes, a_arch, getStemmer());
    }

    private Vector<WordVector> getTrainingData(List<ClusteredNode> a_nodes, ArchDef a_arch, weka.core.stemmers.Stemmer a_stemmer) {
        Vector<WordVector> ret = new Vector<>();

        // add the component names to each document
        a_arch.getComponents().forEach(c -> {
                    WordVector wv = new WordVector();
                    ret.add(wv);
                });

        // Add the cda stuff
        for (ClusteredNode n : a_nodes) {
            int cIx = a_arch.getComponentIx(n.getClusteredComponent());
            Vector<String> words = new Vector<>();
            // add the CDA words
            addWordsToVector(getMappedCDAWords(n, a_nodes), words);

            // add each word to the corresponding document
            words.forEach(w -> ret.get(cIx).add(w));
        }
        ret.forEach(wordVector -> wordVector.applyWeight(m_cdaWeight));


        // add the node words and component nams to the mapped document of the node
        for (ClusteredNode n : a_nodes) {
            int cIx = a_arch.getComponentIx(n.getClusteredComponent());
            Vector<String> words = getWords(n.get(), a_stemmer);

            // component names
            addWordsToVector(getArchComponentWords(a_arch.getComponent(cIx), a_stemmer), words);

            // add each word to the corresponding document
            words.forEach(w -> ret.get(cIx).add(w));
        }



        return ret;
    }

    protected String getRelationType(dmDependency.Type a_relationType) {
        return a_relationType.toString();
    }


    private void addWordsToVector(String a_words, Vector<String>a_target) {
        if (a_words.length() > 0) {
            String[] words = a_words.split(" ");
            for (int i = 0; i < words.length; i++) {
                a_target.add(words[i]);
            }
        }
    }

    private Vector<String> getWords(CNode a_node, weka.core.stemmers.Stemmer a_stemmer) {
        Vector<String> ret = new Vector<>();

        addWordsToVector(getNodeWords(a_node, a_stemmer), ret);
        return ret;
    }

}
