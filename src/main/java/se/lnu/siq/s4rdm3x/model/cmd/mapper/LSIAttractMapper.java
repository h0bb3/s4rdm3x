package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import weka.core.matrix.Matrix;
import weka.core.stemmers.Stemmer;

import java.util.*;

public class LSIAttractMapper extends IRMapperBase {
    public int m_automaticallyMappedNodes = 0;
    public int m_autoWrong = 0;

    public static class WordMatrix {

        HashMap<String, Integer> m_wordToIndex = new HashMap<>();

        int m_docCount;
        HashMap<String, double[]> m_words = new HashMap<>();    // these are the words one value for each document

        WordMatrix(int a_docCount) {
            m_docCount = a_docCount;
        }


        public void add(String a_word, int a_docIx) {
            if (m_words.containsKey(a_word)) {
                m_words.get(a_word)[a_docIx] += 1.0;
            } else {
                double [] freqs = new double[m_docCount];
                freqs[a_docIx] = 1;
                m_wordToIndex.put(a_word, m_words.size());
                m_words.put(a_word, new double[m_docCount]);
            }
        }

        double[][] getMatrix() {
            double [][] m = new double[m_words.size()][m_docCount];


            for (String w : m_words.keySet()) {

                int row = m_wordToIndex.get(w);
                double[] f = m_words.get(w);
                for (int c = 0; c < m_docCount; c++) {
                    m[row][c] = f[c];
                }
            }

            return m;
        }

        public int getWordIndex(String a_w) {
            if (m_wordToIndex.containsKey(a_w)) {
                return m_wordToIndex.get(a_w);
            }

            return -1;
        }
    }

    public LSIAttractMapper(ArchDef a_arch, boolean a_doManualMapping,  boolean a_doUseCDA, boolean a_doUseNodeText, boolean a_doUseNodeName, boolean a_doUseArchComponentName, int a_minWordLength) {
        super(a_arch, a_doManualMapping,  a_doUseCDA, a_doUseNodeText, a_doUseNodeName, a_doUseArchComponentName, a_minWordLength);
    }

    public void run(CGraph a_g) {

        ArrayList<CNode> orphans = getOrphanNodes(a_g);
        ArrayList<CNode> initiallyMapped = getInitiallyMappedNodes(a_g);

        Stemmer stemmer = getStemmer();

        final double smoothing = 0.0;

        WordMatrix trainingData = getTrainingData(initiallyMapped, m_arch, stemmer);
        weka.core.matrix.Matrix tm = new weka.core.matrix.Matrix(trainingData.getMatrix());

        weka.core.matrix.SingularValueDecomposition sut = tm.svd();

        Matrix U = sut.getU();
        Matrix S = sut.getS();
        Matrix V = sut.getV();

        int k = 100;
        if (tm.rank() < k) {
            k = tm.rank() / 2;
            if (k < 2) {
                k = 2;
            }
        }

        Matrix Uk = new Matrix(getTopLeftMatrixCopy(U.getRowDimension(), k, U.getArray()));
        Matrix Sk = new Matrix(getTopLeftMatrixCopy(k, k, S.getArray()));
        Matrix Vk = new Matrix(getTopLeftMatrixCopy(k, V.getColumnDimension(), V.getArray()));
        Matrix Ak = Uk.times(Sk).times(Vk);

        //do the SVD stuff and reduce the rank, finally compose Ak (we will try with this as our matrices are quite small)




        for (CNode orphanNode : orphans) {
            double[] attraction = new double[m_arch.getComponentCount()];

            for (int i = 0; i < m_arch.getComponentCount(); i++) {
                double [][] wordVector = new double[tm.getRowDimension()][1];

                IRAttractMapper.WordVector words = getWordVector(orphanNode, stemmer);
                addWordsToWordVector(getUnmappedCDAWords(orphanNode, m_arch.getComponent(i), initiallyMapped), words);

                for (String w : words.getWords()) {
                    int ix = trainingData.getWordIndex(w);
                    if (ix >= 0) {
                        wordVector[ix][0] = words.getFrequency(w);
                    }
                }

                weka.core.matrix.Matrix qm = new weka.core.matrix.Matrix(wordVector);

                attraction[i] = qm.transpose().times(Ak).get(0, i);
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

    private void addWordsToWordVector(String a_words, IRAttractMapper.WordVector a_target) {
        if (a_words.length() > 0) {
            String[] words = a_words.split(" ");
            for (int i = 0; i < words.length; i++) {
                a_target.add(words[i]);
            }
        }
    }


    private IRAttractMapper.WordVector getWordVector(CNode a_node, weka.core.stemmers.Stemmer a_stemmer) {
        IRAttractMapper.WordVector ret = new IRAttractMapper.WordVector();

        Vector<String> words = getWords(a_node, a_stemmer);
        words.forEach(w -> ret.add(w));

        //ret.makeRelative();

        return ret;
    }

    public WordMatrix getTrainingData(List<CNode> a_nodes, ArchDef a_arch) {
        return getTrainingData(a_nodes, a_arch, getStemmer());
    }

    private WordMatrix getTrainingData(List<CNode> a_nodes, ArchDef a_arch, weka.core.stemmers.Stemmer a_stemmer) {
        WordMatrix ret = new WordMatrix(a_arch.getComponentCount());


        // add the component names to each document
        a_arch.getComponents().forEach(c -> {
            Vector<String> names = new Vector<>();
            addWordsToVector(getArchComponentWords(c, a_stemmer), names);
            names.forEach(w -> ret.add(w, a_arch.getComponentIx(c)));
        });

        // add the node words to the mapped document of the node
        for (CNode n : a_nodes) {
            int cIx = a_arch.getComponentIx(a_arch.getMappedComponent(n));
            Vector<String> words = getWords(n, a_stemmer);

            // add the CDA words
            addWordsToVector(getMappedCDAWords(n, a_nodes), words);

            // add each word to the corresponding document
            words.forEach(w -> ret.add(w, cIx));
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

    private double[][] getTopLeftMatrixCopy(int a_rows, int a_cols, double[][] a_m) {
        double[][] ret = new double[a_rows][a_cols];

        for (int rIx = 0; rIx < a_rows; rIx++) {
            for (int cIx = 0; cIx < a_cols; cIx++) {
                ret[rIx][cIx] = a_m[rIx][cIx];
            }
        }

        return ret;
    }
}
