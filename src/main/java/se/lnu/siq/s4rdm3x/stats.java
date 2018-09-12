package se.lnu.siq.s4rdm3x;

import java.util.*;

public class stats {


    public static double sum(final double[] a_values) {
        double sum = 0;
        for(int i = 0;i < a_values.length;i++) {
            sum += a_values[i];
        }
        return sum;
    }

    public static double medianSorted(final double[] a_sortedValues) {
        if (a_sortedValues.length % 2 == 0) {
            return (a_sortedValues[(a_sortedValues.length - 1) / 2] + a_sortedValues[a_sortedValues.length / 2]) * 0.5;
        } else {
            return a_sortedValues[a_sortedValues.length / 2];
        }
    }

    public static double medianUnsorted(final double[] a_unsortedValues) {
        double [] a = Arrays.copyOf(a_unsortedValues, a_unsortedValues.length);
        Arrays.sort(a);

        return medianSorted(a);
    }

    public static double mean(final double[] a_values) {
        return(double)sum(a_values) /(double)a_values.length;
    }

    public static double variance(double[] a_values) {
        return variance(a_values, mean(a_values));
    }

    public static double variance(double [] a_values, double a_mean) {
        double var = 0;
        for (int i = 0; i < a_values.length; i++) {
            var += (a_values[i] - a_mean) * (a_values[i] - a_mean);
        }
        return var / (double)a_values.length;
    }

    public static double stdDev(double [] a_values, double a_mean) {
        //return Math.sqrt(variance(a_values, a_mean)) / (double)a_values.length;
        return Math.sqrt(variance(a_values, a_mean));
    }

    public static double getUnbiasedGiniCoefficient(Iterable<Double> a_numbers) {

        ArrayList<Double> sorted = new ArrayList<>();
        a_numbers.forEach(a_d->sorted.add(a_d));

        if (sorted.size() < 2) {
            return 0;
        }
        Collections.sort(sorted);

        // gini index computation
        // http://planspace.org/2013/06/21/how-to-calculate-gini-coefficient-from-raw-data-in-python/
        // bias explanation
        // https://www.rdocumentation.org/packages/DescTools/versions/0.99.19/topics/Gini
        // calculator
        // http://shlegeris.com/gini

        double height = 0;
        double area = 0;

        for (Double d : sorted) {
            height += d;
            area += height - d * 0.5;
        }

        double fairArea = height * (double)sorted.size() / 2.0;
        if (fairArea > 0) {
            double gini = (fairArea - area) / fairArea;
            double bias = (double) sorted.size() / (double) (sorted.size() - 1);

            return gini * bias;
            //return gini;
        } else {
            return 0;
        }
    }
}
