package com.example.markovstockanalyzer2.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/** Response body of the analysis service's transition-matrix endpoint. */
public record TransitionMatrixData(
        @JsonProperty("ticker") String ticker,
        @JsonProperty("start") String start,
        @JsonProperty("end") String end,
        @JsonProperty("theta") double theta,
        @JsonProperty("data_start") String dataStart,
        @JsonProperty("data_end") String dataEnd,
        @JsonProperty("observations") int observations,
        @JsonProperty("state_counts") Map<String, Integer> stateCounts,
        @JsonProperty("transition_counts") List<List<Integer>> transitionCounts,
        @JsonProperty("transition_probabilities") List<List<Double>> transitionProbabilities,
        @JsonProperty("current_state") String currentState) {
}
