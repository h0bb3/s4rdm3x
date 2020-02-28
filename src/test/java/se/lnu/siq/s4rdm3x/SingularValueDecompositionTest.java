package se.lnu.siq.s4rdm3x;


import org.junit.jupiter.api.Test;
import se.lnu.siq.s4rdm3x.model.cmd.mapper.LSIAttractMapper;
import weka.core.matrix.Matrix;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;

public class SingularValueDecompositionTest {

    @Test void failTest() {
        assertTrue(false);
    }

    @Test
    public void svdTest1() {
        // this test is from the example in https://www.youtube.com/watch?v=CwBn0voJDaw
        double[][] inputA = new double[][] {{1, 1, 0, 1, 0, 0},
                                            {1, 0, 1, 1, 0, 0},
                                            {1, 1, 1, 2, 1, 1},
                                            {0, 0, 0, 1, 1, 1}};
        double[][] outU = new double[][] {  {0.37, -0.47, 0.71, -0.38},
                                            {0.37, -0.47, -0.71, -0.38},
                                            {0.78, 0.12, 0.00, 0.61},
                                            {0.34, 0.74, -0.00, -0.58}};
        double[][] outS = new double[][] {  {3.80, 0.00, 0.00, 0.00, 0.00, 0.00},
                                            {0.00, 1.55, 0.00, 0.00, 0.00, 0.00},
                                            {0.00, 0.00, 1.00, 0.00, 0.00, 0.00},
                                            {0.00, 0.00, 0.00, 0.38, 0.00, 0.00}};

        double[][] outV = new double[][] {  {0.40, 0.30, 0.30, 0.69, 0.30, 0.30},
                                            {-0.53, -0.22, -0.22, 0.03, 0.56, 0.56},
                                            {-0.00, 0.71, -0.71, -0.00, -0.00, -0.00},
                                            {-0.40, 0.60, 0.60, -0.34, 0.06, 0.06},
                                            {0.00, 0.00, 0.00, 0.00, 0.00, 0.00},
                                            {0.00, 0.00, 0.00, 0.00, 0.00, 0.00}};

        double[][] outAk = new double[][] { {0.94, 0.59, 0.59, 0.95, 0.01, 0.01},
                                            {0.94, 0.59, 0.59, 0.95, 0.01, 0.01},
                                            {1.09, 0.86, 0.86, 2.08, 0.99, 0.99},
                                            {-0.09, 0.13, 0.13, 0.92, 1.01, 1.01}};


        Matrix input = new Matrix(inputA);

        weka.core.matrix.SingularValueDecomposition sut = input.svd();

        Matrix U = sut.getU();
        Matrix S = sut.getS();
        Matrix V = sut.getV();


        assertTrue(equals(U, new Matrix(outU)));
        assertTrue(equals(S, new Matrix(outS)));
        assertTrue(equals(V.transpose(), new Matrix(outV)));

        //U.times(S).times(V).print(5, 2);

        // check that the A = U * S * V (and again we need to transpose V)
        assertTrue(equals(input, U.times(S).times(V.transpose())));


        final int k = 2;
        Matrix Uk = new Matrix(getTopLeftMatrixCopy(U.getRowDimension(), k, U.getArray()));
        Matrix Sk = new Matrix(getTopLeftMatrixCopy(k, k, S.getArray()));
        Matrix Vk = new Matrix(getTopLeftMatrixCopy(k, V.getColumnDimension(), V.transpose().getArray()));
        Matrix Ak = Uk.times(Sk).times(Vk);

        //Ak.print(5, 2);

        assertTrue(equals(Ak, new Matrix(outAk)));

    }

    @Test
    public void svdTest2() {
        double[][] inputA = new double[][] {{1, 1, 0, 1, 0, 0},
                {1, 0, 1, 1, 0, 0},
                {1, 1, 1, 2, 1, 1},
                {0, 0, 0, 1, 1, 1},
                {0, 0, 0, 1, 1, 1},
                {0, 1, 0, 0, 3, 1},
                {1, 1, 1, 1, 1, 1},
                {1, 1, 1, 4, 1, 1}};


        Matrix input = new Matrix(inputA);

        weka.core.matrix.SingularValueDecomposition sut = input.svd();

        Matrix U = sut.getU();
        Matrix S = sut.getS();
        Matrix V = sut.getV().transpose();

        assertTrue(equals(input, U.times(S).times(V)));


        final int k = 7;
        Matrix Uk = new Matrix(getTopLeftMatrixCopy(U.getRowDimension(), k, U.getArray()));
        Matrix Sk = new Matrix(getTopLeftMatrixCopy(k, k, S.getArray()));
        Matrix Vk = new Matrix(getTopLeftMatrixCopy(k, V.getColumnDimension(), V.transpose().getArray()));
        Matrix Ak = Uk.times(Sk).times(Vk);

        assertTrue(!equals(Ak, input));
    }

    @Test
    public void svdQueryTest1() {
        double[][] inputA = new double[][] {
                {1, 1, 0, 1, 0, 0},
                {1, 0, 1, 1, 0, 0},
                {1, 1, 1, 2, 1, 1},
                {0, 0, 0, 1, 1, 1},
                {0, 0, 0, 1, 1, 1},
                {0, 1, 0, 0, 3, 1},
                {1, 1, 1, 1, 1, 1},
                {1, 1, 1, 4, 1, 1}};

        double [][] inputQ = new double[][]  {
                {1},
                {1},
                {1},
                {0},
                {0},
                {0},
                {1},
                {1}};


        Matrix input = new Matrix(inputA);

        weka.core.matrix.SingularValueDecomposition sut = input.svd();

        Matrix U = sut.getU();
        Matrix S = sut.getS();
        Matrix V = sut.getV().transpose();

        assertTrue(equals(input, U.times(S).times(V)));


        final int k = 2;
        Matrix Uk = new Matrix(getTopLeftMatrixCopy(U.getRowDimension(), k, U.getArray()));
        Matrix Sk = new Matrix(getTopLeftMatrixCopy(k, k, S.getArray()));
        Matrix Vk = new Matrix(getTopLeftMatrixCopy(k, V.getColumnDimension(), V.transpose().getArray()));
        Matrix Ak = (Uk.times(Sk)).times(Vk);
        Matrix Akk = new Matrix(getTopLeftMatrixCopy(k, Ak.getColumnDimension(), Ak.getArray()));

        assertTrue(!equals(Ak, input));

        Matrix q = new Matrix(inputQ);

        Matrix Ski = new Matrix(k, k);
        for (int i = 0; i < k; i++) {
            if (Sk.getArray()[i][i] != 0) {
                Ski.getArray()[i][i] = 1.0 / Sk.getArray()[i][i];
            }
        }
        Matrix qk1 = Ski.times(Uk.transpose()).times(q).transpose();
        Matrix qk2 = (q.transpose().times(Uk).times(Ski));

        assertTrue(equals(qk1, qk2));

        Matrix r1 = qk1.times(Akk);
        Matrix r2 = qk2.times(Akk);

        assertTrue(equals(r1, r2));
    }

    @Test
    public void svdQueryTest2() {
        double[][] inputA = new double[][] {
                {1, 1, 0, 1, 0, 0},
                {1, 0, 1, 1, 0, 0},
                {1, 1, 1, 2, 1, 1},
                {0, 0, 0, 1, 1, 1},
                {0, 0, 0, 1, 1, 1},
                {0, 1, 0, 0, 3, 1},
                {1, 1, 1, 1, 1, 1},
                {1, 1, 1, 4, 1, 1}};

        double [][] inputQ = new double[][]  {
                {1},
                {1},
                {1},
                {0},
                {0},
                {0},
                {1},
                {1}};


        Matrix input = new Matrix(inputA);

        weka.core.matrix.SingularValueDecomposition sut = input.svd();

        Matrix U = sut.getU();
        Matrix S = sut.getS();
        Matrix V = sut.getV().transpose();

        assertTrue(equals(input, U.times(S).times(V)));


        final int k = 3;
        Matrix Uk = new Matrix(getTopLeftMatrixCopy(U.getRowDimension(), k, U.getArray()));
        Matrix Sk = new Matrix(getTopLeftMatrixCopy(k, k, S.getArray()));
        Matrix Vk = new Matrix(getTopLeftMatrixCopy(k, V.getColumnDimension(), V.transpose().getArray()));
        Matrix Ak = (Uk.times(Sk)).times(Vk);
        Matrix Akk = new Matrix(getTopLeftMatrixCopy(Ak.getRowDimension(), Ak.getColumnDimension(), Ak.getArray()));

        assertTrue(!equals(Ak, input));

        Matrix q = new Matrix(inputQ);

        Matrix Ski = new Matrix(Sk.getRowDimension(), Sk.getColumnDimension());
        for (int i = 0; i < k; i++) {
            if (Sk.getArray()[i][i] != 0) {
                Ski.getArray()[i][i] = 1.0 / Sk.getArray()[i][i];
            }
        }

        Matrix qk2 = (q.transpose().times(Uk).times(Ski));
//        Matrix r2 = qk2.times(Akk);

    }

    @Test
    public void testtest() {
        //LSIAttractMapper.permute(10, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }, ' ');
    }

    private double[][] getTopLeftMatrixCopy(int a_rows, int a_cols, double[][] a_m) {
        double[][] ret = new double[a_rows][a_cols];

        if (a_rows > a_m.length) {
            a_rows = a_m.length;
        }

        if (a_cols > a_m[0].length) {
            a_cols = a_m[0].length;
        }

        for (int rIx = 0; rIx < a_rows; rIx++) {
            for (int cIx = 0; cIx < a_cols; cIx++) {
                ret[rIx][cIx] = a_m[rIx][cIx];
            }
        }

        return ret;
    }

    private boolean equals(weka.core.matrix.Matrix a_a, weka.core.matrix.Matrix a_b) {
        final double eps = 0.01;

        if (a_a.getColumnDimension() != a_b.getColumnDimension() ||
            a_a.getRowDimension() != a_b.getRowDimension()) {
            return false;
        }

        for (int rIx = 0; rIx < a_a.getRowDimension(); rIx++) {
            for (int cIx = 0; cIx < a_a.getColumnDimension(); cIx++) {
                if (Math.abs(a_a.get(rIx, cIx) - a_b.get(rIx, cIx)) > eps) {
                    return false;
                }
            }
        }

        return true;
    }
}
