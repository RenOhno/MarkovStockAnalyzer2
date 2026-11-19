package com.example.markovstockanalyzer2.analysis;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Evaluation metrics of one backtest model, stored as a column group of
 * the backtest_results table (one group per model, prefixed markov_ /
 * majority_ / persistence_ via {@code @AttributeOverride}).
 * Per-state precision/recall are stored in state order (U, F, D) with
 * {@link MatrixCodec#encodeDoubleList}; the confusion matrix with
 * {@link MatrixCodec#encodeCounts}.
 */
@Embeddable
public class ModelMetricsEmbeddable {

    @Column(name = "accuracy")
    private Double accuracy;

    @Column(name = "brier_score")
    private Double brierScore;

    @Column(name = "log_loss")
    private Double logLoss;

    @Column(name = "predictions", nullable = false)
    private int predictions;

    @Column(name = "skipped", nullable = false)
    private int skipped;

    @Column(name = "precision_macro")
    private Double precisionMacro;

    @Column(name = "recall_macro")
    private Double recallMacro;

    @Column(name = "precision_by_state", length = 256)
    private String precisionByState;

    @Column(name = "recall_by_state", length = 256)
    private String recallByState;

    @Column(name = "confusion_matrix", nullable = false, length = 128)
    private String confusionMatrix;

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Double getBrierScore() {
        return brierScore;
    }

    public void setBrierScore(Double brierScore) {
        this.brierScore = brierScore;
    }

    public Double getLogLoss() {
        return logLoss;
    }

    public void setLogLoss(Double logLoss) {
        this.logLoss = logLoss;
    }

    public int getPredictions() {
        return predictions;
    }

    public void setPredictions(int predictions) {
        this.predictions = predictions;
    }

    public int getSkipped() {
        return skipped;
    }

    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }

    public Double getPrecisionMacro() {
        return precisionMacro;
    }

    public void setPrecisionMacro(Double precisionMacro) {
        this.precisionMacro = precisionMacro;
    }

    public Double getRecallMacro() {
        return recallMacro;
    }

    public void setRecallMacro(Double recallMacro) {
        this.recallMacro = recallMacro;
    }

    public String getPrecisionByState() {
        return precisionByState;
    }

    public void setPrecisionByState(String precisionByState) {
        this.precisionByState = precisionByState;
    }

    public String getRecallByState() {
        return recallByState;
    }

    public void setRecallByState(String recallByState) {
        this.recallByState = recallByState;
    }

    public String getConfusionMatrix() {
        return confusionMatrix;
    }

    public void setConfusionMatrix(String confusionMatrix) {
        this.confusionMatrix = confusionMatrix;
    }
}
