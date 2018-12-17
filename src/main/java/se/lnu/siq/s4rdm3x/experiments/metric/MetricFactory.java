package se.lnu.siq.s4rdm3x.experiments.metric;

import se.lnu.siq.s4rdm3x.experiments.metric.aggregated.AvgFan;

public class MetricFactory {

    public Metric[] getPrimitiveMetrics() {

        return new Metric[] {   new ByteCodeCyclomaticComplexity(), new ByteCodeInstructions(), new CouplingIn(), new CouplingOut(), new FanIn(), new FanOut(), new LCOMHS(), new LineCount(),
                                new NumberOfMethods(), new NumberOfChildren(), new NumberOfChildLevels(), new NumberOfChildrenLevel0(), new NumberOfFields(), new NumberOfParents(), new Rank(), new NumberOfClasses()};
    }

    public Metric getMetric(String a_metricName) {
        Metric[] metrics = getPrimitiveMetrics();

        for (int i = 0; i < metrics.length; i++) {
            if (a_metricName.contentEquals(metrics[i].getName())) {
                return metrics[i];
            }
        }

        return null;
    }
}
