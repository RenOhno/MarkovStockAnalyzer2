package com.example.markovstockanalyzer2.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** One point of the convergence series returned by the analysis service. */
public record ConvergenceData(
        @JsonProperty("h") int h,
        @JsonProperty("distribution") List<Double> distribution,
        @JsonProperty("distance") Double distance) {
}
