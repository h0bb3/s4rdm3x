package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import javafx.scene.shape.Arc;
import se.lnu.siq.s4rdm3x.dmodel.dmClass;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import se.lnu.siq.s4rdm3x.stats;
import weka.core.stemmers.Stemmer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class IRAttractMapper extends IRMapperBase {

    public int m_automaticallyMappedNodes = 0;
    public int m_autoWrong = 0;

    private static class WordVector {

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

        public void makeRelativeToMax() {
            double max = 0;

            for (Map.Entry<String, Double> e : m_words.entrySet()) {
               if (e.getValue() > max) {
                   max = e.getValue();
               }
            }

            for (Map.Entry<String, Double> e : m_words.entrySet()) {
                e.setValue(e.getValue() / max);
            }
        }

        public void makeRelative() {
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
    }

    public IRAttractMapper(ArchDef a_arch, boolean a_doManualMapping) {
        super(a_arch, a_doManualMapping);
    }

    public void run(CGraph a_g) {

        ArrayList<CNode> orphans = getOrphanNodes(a_g);
        ArrayList<CNode> initiallyMapped = getInitiallyMappedNodes(a_g);

        Stemmer stemmer = getStemmer();

        Vector<WordVector> trainingData = getTrainingData(initiallyMapped, m_arch, stemmer);

        for (CNode orphanNode : orphans) {
            double[] attraction = new double[m_arch.getComponentCount()];

            for (int i = 0; i < m_arch.getComponentCount(); i++) {
                attraction[i] = getWordVector(orphanNode, stemmer).cosDistance(trainingData.get(i));
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




    private WordVector getWordVector(CNode a_node, weka.core.stemmers.Stemmer a_stemmer) {
        WordVector ret = new WordVector();

        Vector<String> words = getWords(a_node, a_stemmer);
        words.forEach(w -> ret.add(w));

        ret.makeRelative();

        return ret;
    }

    private Vector<WordVector> getTrainingData(ArrayList<CNode> a_nodes, ArchDef a_arch, weka.core.stemmers.Stemmer a_stemmer) {
        Vector<WordVector> ret = new Vector<>();

        // add the component names to each document
        a_arch.getComponents().forEach(c -> {
            WordVector wv = new WordVector();
            Vector<String> names = new Vector<>();
            addWordsToVector(c.getName().replace(".", " "), names);
            names.forEach(w -> wv.add(w));
            ret.add(wv);

        });

        // add the node words to the mapped document of the node
        for (CNode n : a_nodes) {
            int cIx = a_arch.getComponentIx(a_arch.getMappedComponent(n));
            Vector<String> words = getWords(n, a_stemmer);
            words.forEach(w -> ret.get(cIx).add(w));
        }

        ret.forEach(wv -> wv.makeRelative());


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
        for (dmClass c : a_node.getClasses()) {
            for (String t : c.getTexts()) {
                String deCased = deCamelCase(t, 5, a_stemmer);
                addWordsToVector(deCased, ret);
            }
        }

        String deCased = deCamelCase(a_node.getLogicName().replace(".", " "), 5, a_stemmer);
        addWordsToVector(deCased, ret);
        return ret;
    }




}
