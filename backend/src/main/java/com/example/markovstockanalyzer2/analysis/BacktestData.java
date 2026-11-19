package com.example.markovstockanalyzer2.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/** Response body of the analysis service's backtest endpoint. */
public record BacktestData(
        @JsonProperty("ticker") String ticker,
        @JsonProperty("start") String start,
        @JsonProperty("end") String end,
        @JsonProperty("theta") double theta,
        @JsonProperty("data_start") String dataStart,
        @JsonProperty("data_end") String dataEnd,
        @JsonProperty("observations") int observations,
        @JsonProperty("min_train") int minTrain,
        @JsonProperty("steps") int steps,
        @JsonProperty("models") Map<String, ModelMetricsData> models) {

    /** Evaluation metrics of one model (markov / majority / persistence). */
    public record ModelMetricsData(
            @JsonProperty("predictions") int predictions,
            @JsonProperty("skipped") int skipped,
            @JsonProperty("accuracy") Double accuracy,
            @JsonProperty("precision") Map<String, Double> precision,
            @JsonProperty("recall") Map<String, Double> recall,
            @JsonProperty("precision_macro") Double precisionMacro,
            @JsonProperty("recall_macro") Double recallMacro,
            @JsonProperty("confusion_matrix") List<List<Integer>> confusionMatrix,
            @JsonProperty("brier_score") Double brierScore,
            @JsonProperty("log_loss") Double logLoss) {
    }
}
