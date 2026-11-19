package com.example.markovstockanalyzer2.analysis;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** API representation of a saved backtest result. */
public record BacktestResponse(
        Long id,
        String ticker,
        LocalDate start,
        LocalDate end,
        double theta,
        LocalDate dataStart,
        LocalDate dataEnd,
        int observations,
        int minTrain,
        int steps,
        Map<String, ModelMetrics> models,
        LocalDateTime createdAt) {

    /** State order used for the per-state metric lists. */
    public static final List<String> STATE_ORDER = List.of("U", "F", "D");

    public record ModelMetrics(
            int predictions,
            int skipped,
            Double accuracy,
            Map<String, Double> precision,
            Map<String, Double> recall,
            Double precisionMacro,
            Double recallMacro,
            List<List<Integer>> confusionMatrix,
            Double brierScore,
            Double logLoss) {

        static ModelMetrics from(ModelMetricsEmbeddable e) {
            return new ModelMetrics(
                    e.getPredictions(),
                    e.getSkipped(),
                    e.getAccuracy(),
                    toStateMap(MatrixCodec.decodeDoubleList(e.getPrecisionByState())),
                    toStateMap(MatrixCodec.decodeDoubleList(e.getRecallByState())),
                    e.getPrecisionMacro(),
                    e.getRecallMacro(),
                    MatrixCodec.decodeCounts(e.getConfusionMatrix()),
                    e.getBrierScore(),
                    e.getLogLoss());
        }

        private static Map<String, Double> toStateMap(List<Double> values) {
            Map<String, Double> map = new LinkedHashMap<>();
            for (int i = 0; i < STATE_ORDER.size(); i++) {
                map.put(STATE_ORDER.get(i), i < values.size() ? values.get(i) : null);
            }
            return map;
        }
    }

    public static BacktestResponse from(BacktestResult entity) {
        Map<String, ModelMetrics> models = new LinkedHashMap<>();
        models.put("markov", ModelMetrics.from(entity.getMarkov()));
        models.put("majority", ModelMetrics.from(entity.getMajority()));
        models.put("persistence", ModelMetrics.from(entity.getPersistence()));
        return new BacktestResponse(
                entity.getId(),
                entity.getTicker(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getTheta(),
                entity.getDataStart(),
                entity.getDataEnd(),
                entity.getObservations(),
                entity.getMinTrain(),
                entity.getSteps(),
                models,
                entity.getCreatedAt());
    }
}
