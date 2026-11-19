package com.example.markovstockanalyzer2.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Request body for the analysis service's backtest endpoint. */
public record BacktestRequest(
        @JsonProperty("ticker") String ticker,
        @JsonProperty("start") String start,
        @JsonProperty("end") String end,
        @JsonProperty("theta") double theta,
        @JsonProperty("min_train") int minTrain) {
}
