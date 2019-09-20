package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import javafx.scene.shape.Arc;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.stats;
import weka.core.stemmers.Stemmer;

import java.util.*;

public class IRAttractMapper extends IRMapperBase {

    public int m_automaticallyMappedNodes = 0;
    public int m_autoWrong = 0;

    public static class WordVector {

        HashMap<String, Double> m_words = new HashMap<>();


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
            return dot(a_vec)/(length() * a_vec.length());
        }

        public void add(String a_word) {
            if (m_words.containsKey(a_word)) {
                m_words.replace(a_word, m_words.get(a_word) + 1.0);
            } else {
                m_words.put(a_word, 1.0);
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

        public void maximumTFNormalization(double a_smoothing) {
            // from: https://nlp.stanford.edu/IR-book/pdf/06vect.pdf
            // section 6.4.2
            double max = getMaxTermFrequency();
            final double a = a_smoothing;

            for (Map.Entry<String, Double> e : m_words.entrySet()) {
                e.setValue(a + (1.0 - a) * e.getValue() / max);
            }
        }

        public void makeRelativeToAll() {
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

        }

        public Iterable<String> getWords() {
            return m_words.keySet();
        }

        public double getFrequency(String a_word) {
            return m_words.get(a_word);
        }
    }

    public IRAttractMapper(ArchDef a_arch, boolean a_doManualMapping,  boolean a_doUseCDA, boolean a_doUseNodeText, boolean a_doUseNodeName, boolean a_doUseArchComponentName, int a_minWordLength) {
        super(a_arch, a_doManualMapping,  a_doUseCDA, a_doUseNodeText, a_doUseNodeName, a_doUseArchComponentName, a_minWordLength);
    }

    public void run(CGraph a_g) {

        ArrayList<CNode> orphans = getOrphanNodes(a_g);
        ArrayList<CNode> initiallyMapped = getInitiallyMappedNodes(a_g);

        Stemmer stemmer = getStemmer();

        final double smoothing = 0.0;

        Vector<WordVector> trainingData = getTrainingData(initiallyMapped, m_arch, stemmer);
        trainingData.forEach(wv -> wv.maximumTFNormalization(smoothing));

        for (CNode orphanNode : orphans) {
            double[] attraction = new double[m_arch.getComponentCount()];

            for (int i = 0; i < m_arch.getComponentCount(); i++) {
                WordVector words = getWordVector(orphanNode, stemmer);
                addWordsToWordVector(getUnmappedCDAWords(orphanNode, m_arch.getComponent(i), initiallyMapped), words);
                words.maximumTFNormalization(smoothing);
                attraction[i] = words.cosDistance(trainingData.get(i));
            }

            orphanNode.setAttractions(attraction);

            ArchDef.Component autoClusteredTo = HuGMe.doAutoMapping(orphanNode, m_arch);
            if (autoClusteredTo != null) {
                m_automaticallyMappedNodes++;
                if (autoClusteredTo != m_arch.getMappedComponent(orphanNode)) {
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


    private WordVector getWordVector(CNode a_node, weka.core.stemmers.Stemmer a_stemmer) {
        WordVector ret = new WordVector();

        Vector<String> words = getWords(a_node, a_stemmer);
        words.forEach(w -> ret.add(w));

        //ret.makeRelative();

        return ret;
    }

    public Vector<WordVector> getTrainingData(List<CNode> a_nodes, ArchDef a_arch) {
        return getTrainingData(a_nodes, a_arch, getStemmer());
    }

    private Vector<WordVector> getTrainingData(List<CNode> a_nodes, ArchDef a_arch, weka.core.stemmers.Stemmer a_stemmer) {
        Vector<WordVector> ret = new Vector<>();


        // add the component names to each document
        a_arch.getComponents().forEach(c -> {
            WordVector wv = new WordVector();
            Vector<String> names = new Vector<>();
            addWordsToVector(getArchComponentWords(c, a_stemmer), names);
            names.forEach(w -> wv.add(w));
            ret.add(wv);
        });

        // add the node words to the mapped document of the node
        for (CNode n : a_nodes) {
            int cIx = a_arch.getComponentIx(a_arch.getMappedComponent(n));
            Vector<String> words = getWords(n, a_stemmer);

            // add the CDA words
            addWordsToVector(getMappedCDAWords(n, a_nodes), words);

            // add each word to the corresponding document
            words.forEach(w -> ret.get(cIx).add(w));
        }



        return ret;
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
