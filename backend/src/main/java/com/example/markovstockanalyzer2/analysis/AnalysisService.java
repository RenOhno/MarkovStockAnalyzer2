package com.example.markovstockanalyzer2.analysis;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AnalysisService {

    public static final double DEFAULT_THETA = 0.005;

    private final AnalysisClient client;
    private final AnalysisResultRepository repository;

    public AnalysisService(AnalysisClient client, AnalysisResultRepository repository) {
        this.client = client;
        this.repository = repository;
    }

    /** Runs the analysis on the Python service and saves the result. */
    public AnalysisResultResponse create(
            String ticker, LocalDate start, LocalDate end, Double theta) {
        double effectiveTheta = theta == null ? DEFAULT_THETA : theta;
        TransitionMatrixData data =
                client.fetchTransitionMatrix(ticker, start, end, effectiveTheta);

        AnalysisResult entity = new AnalysisResult();
        entity.setTicker(data.ticker());
        entity.setStartDate(LocalDate.parse(data.start()));
        entity.setEndDate(LocalDate.parse(data.end()));
        entity.setTheta(data.theta());
        entity.setDataStart(LocalDate.parse(data.dataStart()));
        entity.setDataEnd(LocalDate.parse(data.dataEnd()));
        entity.setObservations(data.observations());
        entity.setCurrentState(data.currentState());
        entity.setStateU(data.stateCounts().getOrDefault("U", 0));
        entity.setStateF(data.stateCounts().getOrDefault("F", 0));
        entity.setStateD(data.stateCounts().getOrDefault("D", 0));
        entity.setTransitionCounts(MatrixCodec.encodeCounts(data.transitionCounts()));
        entity.setTransitionProbabilities(
                MatrixCodec.encodeProbabilities(data.transitionProbabilities()));
        entity.setStationaryDistribution(
                data.stationaryDistribution() == null
                        ? null
                        : MatrixCodec.encodeProbabilities(
                                List.of(data.stationaryDistribution())));

        List<List<Double>> horizonRows = new java.util.ArrayList<>();
        for (String key : AnalysisResultResponse.HORIZON_KEYS) {
            List<Double> dist =
                    data.horizonDistributions() == null
                            ? null
                            : data.horizonDistributions().get(key);
            horizonRows.add(
                    dist == null
                            ? java.util.Arrays.<Double>asList(null, null, null)
                            : dist);
        }
        entity.setHorizonDistributions(MatrixCodec.encodeProbabilities(horizonRows));

        List<List<Double>> convDists = new java.util.ArrayList<>();
        List<Double> convDistances = new java.util.ArrayList<>();
        if (data.convergence() != null) {
            for (ConvergenceData point : data.convergence()) {
                convDists.add(
                        point.distribution() == null
                                ? java.util.Arrays.<Double>asList(null, null, null)
                                : point.distribution());
                convDistances.add(point.distance());
            }
        }
        entity.setConvergenceDistributions(MatrixCodec.encodeProbabilities(convDists));
        entity.setConvergenceDistances(MatrixCodec.encodeDoubleList(convDistances));
        entity.setCreatedAt(LocalDateTime.now());

        return AnalysisResultResponse.from(repository.save(entity));
    }

    public List<AnalysisResultResponse> list() {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(AnalysisResultResponse::from)
                .toList();
    }

    public AnalysisResultResponse get(Long id) {
        return repository
                .findById(id)
                .map(AnalysisResultResponse::from)
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "analysis not found: " + id));
    }
}
