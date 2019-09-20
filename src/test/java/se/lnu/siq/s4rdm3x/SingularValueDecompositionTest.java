package se.lnu.siq.s4rdm3x;


import org.junit.jupiter.api.Test;
import weka.core.matrix.Matrix;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;

public class SingularValueDecompositionTest {

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
        Matrix V = sut.getV().transpose(); // for some reason the V needs to be transposed to match the example, this seems to be a quirk of the weka implementation when rows < cols


        assertTrue(equals(U, new Matrix(outU)));
        assertTrue(equals(S, new Matrix(outS)));
        assertTrue(equals(V, new Matrix(outV)));

        //U.times(S).times(V).print(5, 2);

        // check that the A = U * S * V (and again we need to transpose V)
        assertTrue(equals(input, U.times(S).times(V)));


        final int k = 2;
        Matrix Uk = new Matrix(getTopLeftMatrixCopy(U.getRowDimension(), k, U.getArray()));
        Matrix Sk = new Matrix(getTopLeftMatrixCopy(k, k, S.getArray()));
        Matrix Vk = new Matrix(getTopLeftMatrixCopy(k, V.getColumnDimension(), V.getArray()));
        Matrix Ak = Uk.times(Sk).times(Vk);

        //Ak.print(5, 2);

        assertTrue(equals(Ak, new Matrix(outAk)));

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
