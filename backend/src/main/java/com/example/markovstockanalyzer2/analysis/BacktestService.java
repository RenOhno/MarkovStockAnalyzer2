package com.example.markovstockanalyzer2.analysis;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BacktestService {

    public static final int DEFAULT_MIN_TRAIN = 30;

    private final AnalysisClient client;
    private final BacktestResultRepository repository;

    public BacktestService(AnalysisClient client, BacktestResultRepository repository) {
        this.client = client;
        this.repository = repository;
    }

    /** Runs the backtest on the Python service and saves the result. */
    public BacktestResponse create(
            String ticker, LocalDate start, LocalDate end, Double theta, Integer minTrain) {
        double effectiveTheta = theta == null ? AnalysisService.DEFAULT_THETA : theta;
        int effectiveMinTrain = minTrain == null ? DEFAULT_MIN_TRAIN : minTrain;
        BacktestData data =
                client.runBacktest(ticker, start, end, effectiveTheta, effectiveMinTrain);

        BacktestResult entity = new BacktestResult();
        entity.setTicker(data.ticker());
        entity.setStartDate(LocalDate.parse(data.start()));
        entity.setEndDate(LocalDate.parse(data.end()));
        entity.setTheta(data.theta());
        entity.setDataStart(LocalDate.parse(data.dataStart()));
        entity.setDataEnd(LocalDate.parse(data.dataEnd()));
        entity.setObservations(data.observations());
        entity.setMinTrain(data.minTrain());
        entity.setSteps(data.steps());
        entity.setMarkov(toEmbeddable(data.models().get("markov")));
        entity.setMajority(toEmbeddable(data.models().get("majority")));
        entity.setPersistence(toEmbeddable(data.models().get("persistence")));
        entity.setCreatedAt(LocalDateTime.now());

        return BacktestResponse.from(repository.save(entity));
    }

    private static ModelMetricsEmbeddable toEmbeddable(BacktestData.ModelMetricsData data) {
        ModelMetricsEmbeddable e = new ModelMetricsEmbeddable();
        e.setPredictions(data.predictions());
        e.setSkipped(data.skipped());
        e.setAccuracy(data.accuracy());
        e.setBrierScore(data.brierScore());
        e.setLogLoss(data.logLoss());
        e.setPrecisionMacro(data.precisionMacro());
        e.setRecallMacro(data.recallMacro());
        e.setPrecisionByState(
                MatrixCodec.encodeDoubleList(
                        BacktestResponse.STATE_ORDER.stream()
                                .map(s -> data.precision().getOrDefault(s, 0.0))
                                .toList()));
        e.setRecallByState(
                MatrixCodec.encodeDoubleList(
                        BacktestResponse.STATE_ORDER.stream()
                                .map(s -> data.recall().getOrDefault(s, 0.0))
                                .toList()));
        e.setConfusionMatrix(MatrixCodec.encodeCounts(data.confusionMatrix()));
        return e;
    }

    public List<BacktestResponse> list() {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(BacktestResponse::from)
                .toList();
    }

    public BacktestResponse get(Long id) {
        return repository
                .findById(id)
                .map(BacktestResponse::from)
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "backtest not found: " + id));
    }
}
