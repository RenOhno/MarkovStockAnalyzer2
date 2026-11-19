package com.example.markovstockanalyzer2.analysis;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** A saved walk-forward backtest with the metrics of the three models. */
@Entity
@Table(name = "backtest_results")
public class BacktestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String ticker;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private double theta;

    @Column(name = "data_start", nullable = false)
    private LocalDate dataStart;

    @Column(name = "data_end", nullable = false)
    private LocalDate dataEnd;

    @Column(nullable = false)
    private int observations;

    @Column(name = "min_train", nullable = false)
    private int minTrain;

    @Column(nullable = false)
    private int steps;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "accuracy", column = @Column(name = "markov_accuracy")),
        @AttributeOverride(name = "brierScore", column = @Column(name = "markov_brier_score")),
        @AttributeOverride(name = "logLoss", column = @Column(name = "markov_log_loss")),
        @AttributeOverride(
                name = "predictions",
                column = @Column(name = "markov_predictions", nullable = false)),
        @AttributeOverride(
                name = "skipped", column = @Column(name = "markov_skipped", nullable = false)),
        @AttributeOverride(
                name = "precisionMacro", column = @Column(name = "markov_precision_macro")),
        @AttributeOverride(name = "recallMacro", column = @Column(name = "markov_recall_macro")),
        @AttributeOverride(
                name = "precisionByState",
                column = @Column(name = "markov_precision_by_state", length = 256)),
        @AttributeOverride(
                name = "recallByState",
                column = @Column(name = "markov_recall_by_state", length = 256)),
        @AttributeOverride(
                name = "confusionMatrix",
                column = @Column(name = "markov_confusion_matrix", nullable = false, length = 128))
    })
    private ModelMetricsEmbeddable markov;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "accuracy", column = @Column(name = "majority_accuracy")),
        @AttributeOverride(name = "brierScore", column = @Column(name = "majority_brier_score")),
        @AttributeOverride(name = "logLoss", column = @Column(name = "majority_log_loss")),
        @AttributeOverride(
                name = "predictions",
                column = @Column(name = "majority_predictions", nullable = false)),
        @AttributeOverride(
                name = "skipped", column = @Column(name = "majority_skipped", nullable = false)),
        @AttributeOverride(
                name = "precisionMacro", column = @Column(name = "majority_precision_macro")),
        @AttributeOverride(name = "recallMacro", column = @Column(name = "majority_recall_macro")),
        @AttributeOverride(
                name = "precisionByState",
                column = @Column(name = "majority_precision_by_state", length = 256)),
        @AttributeOverride(
                name = "recallByState",
                column = @Column(name = "majority_recall_by_state", length = 256)),
        @AttributeOverride(
                name = "confusionMatrix",
                column =
                        @Column(name = "majority_confusion_matrix", nullable = false, length = 128))
    })
    private ModelMetricsEmbeddable majority;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "accuracy", column = @Column(name = "persistence_accuracy")),
        @AttributeOverride(
                name = "brierScore", column = @Column(name = "persistence_brier_score")),
        @AttributeOverride(name = "logLoss", column = @Column(name = "persistence_log_loss")),
        @AttributeOverride(
                name = "predictions",
                column = @Column(name = "persistence_predictions", nullable = false)),
        @AttributeOverride(
                name = "skipped",
                column = @Column(name = "persistence_skipped", nullable = false)),
        @AttributeOverride(
                name = "precisionMacro", column = @Column(name = "persistence_precision_macro")),
        @AttributeOverride(
                name = "recallMacro", column = @Column(name = "persistence_recall_macro")),
        @AttributeOverride(
                name = "precisionByState",
                column = @Column(name = "persistence_precision_by_state", length = 256)),
        @AttributeOverride(
                name = "recallByState",
                column = @Column(name = "persistence_recall_by_state", length = 256)),
        @AttributeOverride(
                name = "confusionMatrix",
                column =
                        @Column(
                                name = "persistence_confusion_matrix",
                                nullable = false,
                                length = 128))
    })
    private ModelMetricsEmbeddable persistence;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public double getTheta() {
        return theta;
    }

    public void setTheta(double theta) {
        this.theta = theta;
    }

    public LocalDate getDataStart() {
        return dataStart;
    }

    public void setDataStart(LocalDate dataStart) {
        this.dataStart = dataStart;
    }

    public LocalDate getDataEnd() {
        return dataEnd;
    }

    public void setDataEnd(LocalDate dataEnd) {
        this.dataEnd = dataEnd;
    }

    public int getObservations() {
        return observations;
    }

    public void setObservations(int observations) {
        this.observations = observations;
    }

    public int getMinTrain() {
        return minTrain;
    }

    public void setMinTrain(int minTrain) {
        this.minTrain = minTrain;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public ModelMetricsEmbeddable getMarkov() {
        return markov;
    }

    public void setMarkov(ModelMetricsEmbeddable markov) {
        this.markov = markov;
    }

    public ModelMetricsEmbeddable getMajority() {
        return majority;
    }

    public void setMajority(ModelMetricsEmbeddable majority) {
        this.majority = majority;
    }

    public ModelMetricsEmbeddable getPersistence() {
        return persistence;
    }

    public void setPersistence(ModelMetricsEmbeddable persistence) {
        this.persistence = persistence;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
