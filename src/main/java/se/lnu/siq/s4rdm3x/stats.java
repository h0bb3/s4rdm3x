package se.lnu.siq.s4rdm3x;

public class stats {


    public static double sum(double[] a_values) {
        double sum = 0;
        for(int i = 0;i < a_values.length;i++) {
            sum += a_values[i];
        }
        return sum;
    }

    public static double mean(double[] a_values) {
        return(double)sum(a_values) /(double)a_values.length;
    }

    public static double variance(double [] a_values, double a_mean) {
        double var = 0;
        for (int i = 0; i < a_values.length; i++) {
            var += (a_values[i] - a_mean) * (a_values[i] - a_mean);
        }
        return var;
    }

    public static double stdDev(double [] a_values, double a_mean) {
        return Math.sqrt(variance(a_values, a_mean) / (double)a_values.length);
    }
}
