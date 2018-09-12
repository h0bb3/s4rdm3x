package se.lnu.siq.s4rdm3x;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class statsTest {

    @Test
    void variance() {
        assertEquals(28.333333333333332, stats.variance(new double[]{1, 3, 4, 7, 17, 10}));
        assertEquals(0, stats.variance(new double[]{6, 6, 6, 6, 6, 6}));
        assertEquals(63.88888888888889, stats.variance(new double[]{-1, 3, -4, 7, -17, -10}));
    }

    @Test
    void stdDev() {
    }

    @Test
    void medianTest() {
        assertEquals(0, stats.medianUnsorted(new double[]{0}));
        assertEquals(0.5, stats.medianUnsorted(new double[]{0, 1}));
        assertEquals(0.5, stats.medianUnsorted(new double[]{1, 0}));
        assertEquals(2, stats.medianUnsorted(new double[]{1, 2, 3}));
        assertEquals(2, stats.medianUnsorted(new double[]{3, 2, 1}));
    }

    @Test
    void getGiniIndex1() {
        Double [] v = {0.0};
        ArrayList<Double> values = new ArrayList<>();

        Collections.addAll(values, v);

        assertEquals(0, stats.getUnbiasedGiniCoefficient(values));
    }

    @Test
    void getGiniIndex2() {
        Double [] v = {1.0};
        ArrayList<Double> values = new ArrayList<>();

        Collections.addAll(values, v);

        assertEquals(0, stats.getUnbiasedGiniCoefficient(values));
    }

    @Test
    void getGiniIndex3() {
        Double [] v = {17.0, 17.0};
        ArrayList<Double> values = new ArrayList<>();

        Collections.addAll(values, v);

        assertEquals(0, stats.getUnbiasedGiniCoefficient(values));
    }

    @Test
    void getGiniIndex4() {
        Double [] v = {4.0, 1.0};
        double bias = (double)v.length / (double)(v.length - 1);
        ArrayList<Double> values = new ArrayList<>();

        Collections.addAll(values, v);

        assertEquals(0.3 * bias, stats.getUnbiasedGiniCoefficient(values));
    }

    @Test
    void getGiniIndex5() {
        Double [] v = {67.0, 4.0, 1.0};
        double bias = (double)v.length / (double)(v.length - 1);
        ArrayList<Double> values = new ArrayList<>();

        Collections.addAll(values, v);

        assertEquals(0.61111111111111111 * bias, stats.getUnbiasedGiniCoefficient(values));
    }
}