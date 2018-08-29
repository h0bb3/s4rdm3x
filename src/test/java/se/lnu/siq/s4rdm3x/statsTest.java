package se.lnu.siq.s4rdm3x;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class statsTest {

    @Test
    void variance() {
    }

    @Test
    void stdDev() {
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