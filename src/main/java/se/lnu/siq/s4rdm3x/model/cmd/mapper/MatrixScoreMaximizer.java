package se.lnu.siq.s4rdm3x.model.cmd.mapper;

import java.util.Arrays;

public class MatrixScoreMaximizer {

    double[][] m_scores;

    int[] m_maxCols;    // this array holds the indices to the maximum column for each row i.e. m_maxCols[0] points to the best score for row 0 column

    public MatrixScoreMaximizer(double[][] a_scores) {

        m_scores = a_scores;
    }

    public int[] compute() {
        // we assume a square matrix for scores
        int n = m_scores.length;
        m_maxCols = null;

        if (n < 11) {
            return bruteForceCompute();
        } else {
            // make some other greedy implementation...
        }

        return null;
    }

    public double getScore(int[] a_cols) {
        double score = 0;
        for (int i = 0; i < a_cols.length; i++) {
            score += m_scores[i][a_cols[i]];
        }

        return score;
    }

    private int[] bruteForceCompute() {

        int n = m_scores.length;
        int[] elements = new int[n];
        for (int i = 0; i < n; i++) {
            elements[i] = i;
        }

        permute(n, elements);

        return m_maxCols;
    }


    private static void swap(int[] input, int a, int b) {
        int tmp = input[a];
        input[a] = input[b];
        input[b] = tmp;
    }

    private void compareToMax(int[] a_cols) {
        if (m_maxCols == null || getScore(a_cols) > getScore(m_maxCols)) {
            m_maxCols = Arrays.copyOf(a_cols, a_cols.length);
        }
    }

    public void permute(int n, int[] elements) {

        int[] indexes = new int[n];
        for(
                int i = 0;
                i<n;i++)

        {
            indexes[i] = 0;
        }

        compareToMax(elements);

        int i = 0;
        while(i<n)

        {
            if (indexes[i] < i) {
                swap(elements, i % 2 == 0 ? 0 : indexes[i], i);
                compareToMax(elements);
                indexes[i]++;
                i = 0;
            } else {
                indexes[i] = 0;
                i++;
            }
        }
    }
}
