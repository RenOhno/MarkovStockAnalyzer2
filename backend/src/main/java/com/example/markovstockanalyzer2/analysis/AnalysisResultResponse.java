package com.example.markovstockanalyzer2.analysis;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        LocalDateTime createdAt) {

    public static AnalysisResultResponse from(AnalysisResult entity) {
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
                entity.getCreatedAt());
    }
}
