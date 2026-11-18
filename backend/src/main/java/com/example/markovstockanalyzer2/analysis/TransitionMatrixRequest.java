package com.example.markovstockanalyzer2.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Request body for the analysis service's transition-matrix endpoint. */
public record TransitionMatrixRequest(
        @JsonProperty("ticker") String ticker,
        @JsonProperty("start") String start,
        @JsonProperty("end") String end,
        @JsonProperty("theta") double theta) {
}
