package se.lnu.siq.s4rdm3x.model.cmd.mapper;

//import no.uib.cipr.matrix.*;
import se.lnu.siq.s4rdm3x.dmodel.dmDependency;
import se.lnu.siq.s4rdm3x.model.CGraph;
import se.lnu.siq.s4rdm3x.model.CNode;
import weka.core.matrix.Matrix;
import weka.core.stemmers.Stemmer;

import java.util.*;
import java.util.Vector;

public class LSIAttractMapper extends IRMapperBase {
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
                double[] freqs = new double[m_docCount];
                freqs[a_docIx] = 1;
                m_wordToIndex.put(a_word, m_words.size());
                m_words.put(a_word, freqs);
            }
        }

        public void iDF() {
            for (Map.Entry<String, double[]> e : m_words.entrySet()) {
                double dfreq = 0;
                for (double d : e.getValue()) {
                    if (d > 0) {
                        dfreq += 1;
                    }
                }

                dfreq = Math.log(m_docCount / dfreq);
                for (int dIx = 0; dIx < m_docCount; dIx++) {
                    if (e.getValue()[dIx] > 0) {
                        double v = e.getValue()[dIx];
                        e.getValue()[dIx] = v * dfreq;
                    }
                }
            }
        }

        public void maximumTFNormalization(double a_smoothing) {
            double[] maxes = getTermFrequencyMaxes();

            for (Map.Entry<String, double[]> e : m_words.entrySet()) {
                for (int dIx = 0; dIx < m_docCount; dIx++) {
                    if (maxes[dIx] > 0 && e.getValue()[dIx] > 0) {
                        e.getValue()[dIx] = a_smoothing + (1.0 - a_smoothing) * e.getValue()[dIx] / maxes[dIx];
                    }
                }
            }
        }

        private double[] getTermFrequencyMaxes() {
            double[] maxes = new double[m_docCount];

            for (Map.Entry<String, double[]> e : m_words.entrySet()) {
                for (int dIx = 0; dIx < m_docCount; dIx++) {
                    if (maxes[dIx] < e.getValue()[dIx]) {
                        maxes[dIx] = e.getValue()[dIx];
                    }
                }
            }

            return maxes;
        }

        double[][] getMatrix() {
            double[][] m = new double[m_words.size()][m_docCount];


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

        public HashMap<String, Double> getWordDocFrequencies() {
            HashMap<String, Double> wordDocumentFrequency = new HashMap<>();

            for (Map.Entry<String, double[]> e : m_words.entrySet()) {
                double dfreq = 0;
                for (double d : e.getValue()) {
                    if (d > 0) {
                        dfreq += 1;
                    }
                }

                wordDocumentFrequency.put(e.getKey(), dfreq);
            }

            return wordDocumentFrequency;
        }

        public void binaryTFNormalization() {

            for (Map.Entry<String, double[]> e : m_words.entrySet()) {
                for (int dIx = 0; dIx < m_docCount; dIx++) {
                    if (e.getValue()[dIx] > 0) {
                        e.getValue()[dIx] = 1;
                    }
                }
            }
        }
    }

    public LSIAttractMapper(ArchDef a_arch, boolean a_doManualMapping, boolean a_doUseCDA, boolean a_doUseNodeText, boolean a_doUseNodeName, boolean a_doUseArchComponentName, int a_minWordLength) {
        super(a_arch, a_doManualMapping, a_doUseCDA, a_doUseNodeText, a_doUseNodeName, a_doUseArchComponentName, a_minWordLength);
    }

    public void run(CGraph a_g) {
        //runhObbE(a_g);
        runBittenCourt(a_g);
    }

    /*public void runhObbE(CGraph a_g) {

        // in this version we add all the nodes as a document and then we transform it to concept space with rank = the number of architectural components
        // in this way we hypothesize that each architectural component will be a "clearly" defined concept.
        // we then use the Vk matrix to check the mapping of each document to each architectural component and find the best match

        ArrayList<OrphanNode> orphans = getOrphanNodes(a_g);
        ArrayList<ClusteredNode> initiallyMapped = getInitiallyMappedNodes(a_g);

        Stemmer stemmer = getStemmer();

        final double smoothing = 0.1;

        WordMatrix trainingData = getTrainingDatByNode(initiallyMapped, m_arch, stemmer);
        trainingData.maximumTFNormalization(smoothing);
        //weka.core.matrix.Matrix tm = new weka.core.matrix.Matrix(trainingData.getMatrix());

        no.uib.cipr.matrix.Matrix tm = new no.uib.cipr.matrix.DenseMatrix(trainingData.getMatrix());

        //weka.core.matrix.SingularValueDecomposition sut = tm.svd();

        SVD svd = null;
        try {
            svd = SVD.factorize(tm);
        } catch (NotConvergedException e) {
            e.printStackTrace();
        }

        no.uib.cipr.matrix.Matrix U = svd.getU();
        double[] S = svd.getS();
        no.uib.cipr.matrix.Matrix V = svd.getVt();
        V = V.transpose(new DenseMatrix(V.numColumns(), V.numRows()));


        final int t = U.numRows();  // number of terms
        final int k = t > m_arch.getComponentCount() ? m_arch.getComponentCount() : t;

        //do the SVD stuff and reduce the rank, finally compose Ak (we will try with this as our matrices are quite small)

        int[] rowsToKeep = new int[U.numRows()];
        int[] columnsToKeep = new int[k];
        for (int i = 0; i < rowsToKeep.length; i++) {
            rowsToKeep[i] = i;
        }
        for (int i = 0; i < columnsToKeep.length; i++) {
            columnsToKeep[i] = i;
        }
        no.uib.cipr.matrix.Matrix Uk = Matrices.getSubMatrix(U, rowsToKeep, columnsToKeep).copy();
        double[] Sk = Arrays.copyOf(S, k);
        rowsToKeep = new int[V.numRows()];
        for (int i = 0; i < rowsToKeep.length; i++) {
            rowsToKeep[i] = i;
        }
        no.uib.cipr.matrix.Matrix Vk = Matrices.getSubMatrix(V, rowsToKeep, columnsToKeep).copy();



        no.uib.cipr.matrix.Matrix Ski = new UpperSymmDenseMatrix(k);
        for (int i = 0; i < Sk.length; i++) {
            Ski.set(i, i, 1.0 / Sk[i]);
        }

        // Vk is now a concept x document matrix where each concept should map to an architectural component
        // the trick is now to find this mapping
        // calculate a score for each category and architectural component.
        double scores[][] = new double[m_arch.getComponentCount()][Vk.numRows()];

        // this is just for debugging (know how many nodes are mapped to each component)
        int nodeComponentCount[] = new int[m_arch.getComponentCount()];
        for (ClusteredNode n : initiallyMapped) {
            nodeComponentCount[m_arch.getComponentIx(m_arch.getMappedComponent(n.get()))]++;
        }

        // it seems as things are flipped in this matrix lib
        Vk = Vk.transpose(new DenseMatrix(Vk.numColumns(), Vk.numRows()));

        // Don't really know about this one...
        // TODO: find what this S matrix acgtually is
        //no.uib.cipr.matrix.Matrix SkiVk = new DenseMatrix(Ski.numRows(), Vk.numColumns())
        //Ski.mult(Vk, SkiVk);



        for (int catIx = 0; catIx < Vk.numRows(); catIx++) {
            for (int dIx = 0; dIx < Vk.numColumns(); dIx++) {
                int componentIx = m_arch.getComponentIx(initiallyMapped.get(dIx).getClusteredComponent());   // this now depends on the order in the list and the training data generation beware...

                scores[componentIx][catIx] += Vk.get(catIx, dIx);
            }
        }

        int conceptToComponentMapping[] = new int[Vk.numRows()];
        ArrayList<Integer> availableComponents = new ArrayList<>();
        for (int i = 0; i < m_arch.getComponentCount(); i++) {
            availableComponents.add(i);
        }

        MatrixScoreMaximizer msm = new MatrixScoreMaximizer(scores);
        int[] bestCombo = msm.compute();    // each index (arch component) holds the best category that maximizes the total score over all elements

        // to concept space transformation matrix
        no.uib.cipr.matrix.Matrix toConceptSpaceMatrix = Uk.mult(Ski, new DenseMatrix(Uk.numRows(), Ski.numColumns()));


        // we can now find the best concept for each orphan and as we have a mapping from architecture to concept
        // will probably be a bit tricky to find the correct attraction as this is not "inline" with the architectural components just yes
        for (OrphanNode orphanNode : orphans) {
            double[] attraction = new double[m_arch.getComponentCount()];


            for (int i = 0; i < m_arch.getComponentCount(); i++) {
                double [][] wordVector = new double[tm.numRows()][1];

                IRAttractMapper.WordVector words = getWordVector(orphanNode.get(), stemmer);
                addWordsToWordVector(getUnmappedCDAWords(orphanNode, m_arch.getComponent(i), initiallyMapped), words);

                words.maximumTFNormalization(smoothing);

                for (String w : words.getWords()) {
                    int ix = trainingData.getWordIndex(w);
                    if (ix >= 0) {
                        wordVector[ix][0] = words.getFrequency(w);
                    }
                }

                no.uib.cipr.matrix.Matrix qm = new no.uib.cipr.matrix.DenseMatrix(wordVector);
                no.uib.cipr.matrix.Matrix qmt = qm.transpose(new DenseMatrix(qm.numColumns(), qm.numRows()));

                // transform the qm into concept space
                no.uib.cipr.matrix.Matrix qmtk = qmt.mult(toConceptSpaceMatrix, new DenseMatrix(1, toConceptSpaceMatrix.numColumns()));


                // find the maxmimum attraction for this component mapping
                int bestCategory = bestCombo[i];

                attraction[i] = qmtk.mult(Vk, new DenseMatrix(qmtk.numRows(), Vk.numColumns())).get(0, bestCategory); // this should now be the  non normalized cos distances
                //if (lengths[i] != 0) {
                //    attraction[i] = attraction[i] / (words.length() * lengths[i]);  // there may be stuff with length 0 as some components may have no initially mapped nodes
                //} else {
                //    attraction[i] = 0;
                //}

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

    }*/

    public void runBittenCourt(CGraph a_g) {

        // Basically this is what I could get from the Bittencourt paper
        // however it does not make sense from a mathematical point of view as typically LSI works on really large matrices i.e. many documents.
        // more specifically the paper mentions:
        // "In this paper, we also investigate the utility of an attraction function based on LSI, called LSIAttract, that is based on a reduced vector space with one hundred dimensions.
        //
        // this indicates a reduction (k) of the vector space to 100 elements, however this is not how LSI works as 100 will certainly be more than the rank of the document word matrix
        // considering that the documents are the architectural entities (i.e. it would be highly unusual to have more than 100 architectural elements)
        //
        // it is also unclear if the cosine distance is then used to determine the final attraction

        ArrayList<OrphanNode> orphans = getOrphanNodes(a_g);
        ArrayList<ClusteredNode> initiallyMapped = getInitiallyMappedNodes(a_g);

        Stemmer stemmer = getStemmer();

        final double smoothing = 0.1;

        WordMatrix trainingData = getTrainingData(initiallyMapped, m_arch, stemmer);
        //trainingData.maximumTFNormalization(smoothing);
        //trainingData.binaryTFNormalization();
        //trainingData.iDF();
        weka.core.matrix.Matrix tm = new weka.core.matrix.Matrix(trainingData.getMatrix());
        //no.uib.cipr.matrix.Matrix tm = new no.uib.cipr.matrix.DenseMatrix(trainingData.getMatrix());

        weka.core.matrix.SingularValueDecomposition svd = tm.svd();
        /*SVD svd = null;
        try {
            svd = SVD.factorize(tm);
        } catch (NotConvergedException e) {
            e.printStackTrace();
        }*/


        Matrix U = svd.getU();
        Matrix S = svd.getS();
        Matrix V = svd.getV();

        /*no.uib.cipr.matrix.Matrix U = svd.getU();
        double[] S = svd.getS();
        no.uib.cipr.matrix.Matrix V = svd.getVt();
        V = V.transpose(new DenseMatrix(V.numColumns(), V.numRows()));*/


        final int t = U.getRowDimension();  // number of terms
        //final int t = U.numRows();  // number of terms
        //final int k = t > 100 ? 100 : t;  // this does not make sense as the number of documents (i.e. modules in the architecture is def < 100)
        final int k = t > m_arch.getComponentCount() ? m_arch.getComponentCount() : t;

        //do the SVD stuff and reduce the rank, finally compose Ak (we will try with this as our matrices are quite small)
        Matrix Uk = new Matrix(getTopLeftMatrixCopy(t, k, U.getArray()));
        Matrix Sk = new Matrix(getTopLeftMatrixCopy(k, k, S.getArray()));
        Matrix Vk = new Matrix(getTopLeftMatrixCopy(k, t, V.transpose().getArray()));
        Matrix Ak = Uk.times(Sk).times(Vk);

        /*int[] rowsToKeep = new int[U.numRows()];
        int[] columnsToKeep = new int[k];
        for (int i = 0; i < rowsToKeep.length; i++) {
            rowsToKeep[i] = i;
        }
        for (int i = 0; i < columnsToKeep.length; i++) {
            columnsToKeep[i] = i;
        }
        no.uib.cipr.matrix.Matrix Uk = Matrices.getSubMatrix(U, rowsToKeep, columnsToKeep).copy();
        double[] Ska = Arrays.copyOf(S, k);
        no.uib.cipr.matrix.Matrix Sk = new UpperSymmDenseMatrix(k);
        for (int i = 0; i < Ska.length; i++) {
            Sk.set(i, i, Ska[i]);
        }
        rowsToKeep = new int[V.numRows()];
        for (int i = 0; i < rowsToKeep.length; i++) {
            rowsToKeep[i] = i;
        }
        no.uib.cipr.matrix.Matrix Vk = Matrices.getSubMatrix(V, rowsToKeep, columnsToKeep).copy();

        no.uib.cipr.matrix.Matrix Ak = Uk.mult(Sk, new DenseMatrix(Uk.numRows(), Sk.numColumns()));
        Ak.mult(Vk, new DenseMatrix(Ak.numRows(), Vk.numColumns()));*/


        double lengths[] = new double[Ak.getColumnDimension()];
        //double lengths[] = new double[Ak.numColumns()];

        for (int i = 0; i < Ak.getColumnDimension(); i++) {
            lengths[i] = getColumnLength(Ak.getArray(), i);
        }
        /*for (int cIx = 0; cIx < Ak.numColumns(); cIx++) {
            for (int rIx = 0; rIx < Ak.numRows(); rIx++) {
                double v = Ak.get(rIx, cIx);
                lengths[cIx] +=  v * v;
            }

            lengths[cIx] = Math.sqrt(lengths[cIx]);
        }*/

        HashMap<String, Double> wordDocFrequencies = trainingData.getWordDocFrequencies();

        for (OrphanNode orphanNode : orphans) {
            double[] attraction = new double[m_arch.getComponentCount()];

            for (int i = 0; i < m_arch.getComponentCount(); i++) {
                double [][] wordVector = new double[tm.getRowDimension()][1];
                //double [][] wordVector = new double[tm.numRows()][1];

                IRAttractMapper.WordVector words = getWordVector(orphanNode.get(), stemmer);
                addWordsToWordVector(getUnmappedCDAWords(orphanNode, m_arch.getComponent(i), initiallyMapped), words);

                //words.maximumTFNormalization(smoothing);
                //words.binaryTFNormalization();
                //words.iDF(m_arch.getComponentCount(), wordDocFrequencies);

                for (String w : words.getWords()) {
                    int ix = trainingData.getWordIndex(w);
                    if (ix >= 0) {
                        wordVector[ix][0] = words.getFrequency(w);
                    }
                }

                weka.core.matrix.Matrix qm = new weka.core.matrix.Matrix(wordVector);
                //no.uib.cipr.matrix.Matrix qm = new no.uib.cipr.matrix.DenseMatrix(wordVector);
                //no.uib.cipr.matrix.Matrix qmt = qm.transpose(new DenseMatrix(qm.numColumns(), qm.numRows()));

                attraction[i] = qm.transpose().times(Ak).get(0, i); // this should now be the  non normalized cos distances
                //attraction[i] = qmt.mult(Ak, new DenseMatrix(qmt.numRows(), Ak.numColumns())).get(0, i); // this should now be the  non normalized cos distances
                if (lengths[i] != 0) {
                    attraction[i] = attraction[i] / (words.length() * lengths[i]);  // there may be stuff with length 0 as some components may have no initially mapped nodes
                } else {
                    attraction[i] = 0;
                }

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

    private double getColumnLength(double[][] a_m, int a_cIx) {
        double sqsum = 0;
        final int rows = a_m.length;
        for (int rIx = 0; rIx < rows; rIx++) {
            sqsum += a_m[rIx][a_cIx] * a_m[rIx][a_cIx];
        }

        return Math.sqrt(sqsum);
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

    public WordMatrix getTrainingData(List<ClusteredNode> a_nodes, ArchDef a_arch) {
        return getTrainingData(a_nodes, a_arch, getStemmer());
    }

    private WordMatrix getTrainingData(List<ClusteredNode> a_nodes, ArchDef a_arch, weka.core.stemmers.Stemmer a_stemmer) {
        WordMatrix ret = new WordMatrix(a_arch.getComponentCount());


        // add the component names to each document
        a_arch.getComponents().forEach(c -> {
            Vector<String> names = new Vector<>();
            addWordsToVector(getArchComponentWords(c, a_stemmer), names);
            names.forEach(w -> ret.add(w, a_arch.getComponentIx(c)));
        });

        // add the node words to the mapped document of the node
        for (ClusteredNode n : a_nodes) {
            int cIx = a_arch.getComponentIx(a_arch.getMappedComponent(n.get()));
            Vector<String> words = getWords(n.get(), a_stemmer);

            // add the CDA words
            addWordsToVector(getMappedCDAWords(n, a_nodes), words);

            // add each word to the corresponding document
            words.forEach(w -> ret.add(w, cIx));
        }

        return ret;
    }


    private WordMatrix getTrainingDatByNode(List<ClusteredNode> a_nodes, ArchDef a_arch, weka.core.stemmers.Stemmer a_stemmer) {
        // one document per node
        WordMatrix ret = new WordMatrix(a_nodes.size());

        // add the node words to the mapped document of the node
        int i = 0;
        for (ClusteredNode n : a_nodes) {

            final int cIx = i;

            // add the component names to the document
            Vector<String> names = new Vector<>();
            addWordsToVector(getArchComponentWords(a_arch.getMappedComponent(n.get()), a_stemmer), names);
            names.forEach(w -> ret.add(w, cIx));


            Vector<String> words = getWords(n.get(), a_stemmer);

            // add the CDA words
            addWordsToVector(getMappedCDAWords(n, a_nodes), words);

            // add each word to the corresponding document
            words.forEach(w -> ret.add(w, cIx));

            i++;
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

        if (a_rows > a_m.length) {
            a_rows = a_m.length;
        }

        if (a_cols > a_m[0].length) {
            a_cols = a_m[0].length;
        }

        double[][] ret = new double[a_rows][a_cols];


        for (int rIx = 0; rIx < a_rows; rIx++) {
            for (int cIx = 0; cIx < a_cols; cIx++) {
                ret[rIx][cIx] = a_m[rIx][cIx];
            }
        }

        return ret;
    }
}
