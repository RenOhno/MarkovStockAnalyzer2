package com.example.markovstockanalyzer2.analysis;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** API representation of a saved analysis result. */
public record AnalysisResultResponse(
        Long id,
        String ticker,
        LocalDate start,
        LocalDate end,
        double theta,
        LocalDate dataStart,
        LocalDate dataEnd,
        int observations,
        Map<String, Integer> stateCounts,
        List<List<Integer>> transitionCounts,
        List<List<Double>> transitionProbabilities,
        String currentState,
        List<Double> stationaryDistribution,
        Map<String, List<Double>> horizonDistributions,
        List<ConvergencePoint> convergence,
        LocalDateTime createdAt) {

    /** Horizon keys (business days ahead) in storage order. */
    public static final List<String> HORIZON_KEYS = List.of("1", "3", "5", "10");

    public record ConvergencePoint(int h, List<Double> distribution, Double distance) {
    }

    private static List<Double> nullIfAllNull(List<Double> row) {
        return row.stream().allMatch(v -> v == null) ? null : row;
    }

    public static AnalysisResultResponse from(AnalysisResult entity) {
        List<Double> stationary =
                entity.getStationaryDistribution() == null
                        ? null
                        : MatrixCodec.decodeProbabilities(entity.getStationaryDistribution())
                                .get(0);

        Map<String, List<Double>> horizons = new LinkedHashMap<>();
        if (entity.getHorizonDistributions() != null) {
            List<List<Double>> rows =
                    MatrixCodec.decodeProbabilities(entity.getHorizonDistributions());
            for (int i = 0; i < HORIZON_KEYS.size() && i < rows.size(); i++) {
                horizons.put(HORIZON_KEYS.get(i), nullIfAllNull(rows.get(i)));
            }
        }

        List<ConvergencePoint> convergence = new ArrayList<>();
        if (entity.getConvergenceDistributions() != null) {
            List<List<Double>> dists =
                    MatrixCodec.decodeProbabilities(entity.getConvergenceDistributions());
            List<Double> distances =
                    MatrixCodec.decodeDoubleList(entity.getConvergenceDistances());
            for (int h = 0; h < dists.size(); h++) {
                convergence.add(
                        new ConvergencePoint(
                                h,
                                nullIfAllNull(dists.get(h)),
                                h < distances.size() ? distances.get(h) : null));
            }
        }

        return new AnalysisResultResponse(
                entity.getId(),
                entity.getTicker(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getTheta(),
                entity.getDataStart(),
                entity.getDataEnd(),
                entity.getObservations(),
                Map.of(
                        "U", entity.getStateU(),
                        "F", entity.getStateF(),
                        "D", entity.getStateD()),
                MatrixCodec.decodeCounts(entity.getTransitionCounts()),
                MatrixCodec.decodeProbabilities(entity.getTransitionProbabilities()),
                entity.getCurrentState(),
                stationary,
                horizons,
                convergence,
                entity.getCreatedAt());
    }
}
