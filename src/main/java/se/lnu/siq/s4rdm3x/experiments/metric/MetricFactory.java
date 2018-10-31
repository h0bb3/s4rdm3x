package se.lnu.siq.s4rdm3x.experiments.metric;

import se.lnu.siq.s4rdm3x.experiments.metric.aggregated.AvgFan;

public class MetricFactory {

    public Metric[] getPrimitiveMetrics() {

        return new Metric[] {   new ByteCodeCyclomaticComplexity(), new ByteCodeInstructions(), new CouplingIn(), new CouplingOut(), new FanIn(), new FanOut(), new LCOMHS(), new LineCount(),
                                new NumberOfMethods(), new NumberOfChildren(), new NumberOfFields(), new NumberOfParents(), new Rank()};
    }
}
