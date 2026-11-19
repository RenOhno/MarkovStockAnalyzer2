package com.example.markovstockanalyzer2.analysis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A saved transition-matrix analysis. The 3x3 matrices are stored in the
 * compact text format produced by {@link MatrixCodec}.
 */
@Entity
@Table(name = "analysis_results")
public class AnalysisResult {

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

    @Column(name = "current_state", nullable = false, length = 1)
    private String currentState;

    @Column(name = "state_u", nullable = false)
    private int stateU;

    @Column(name = "state_f", nullable = false)
    private int stateF;

    @Column(name = "state_d", nullable = false)
    private int stateD;

    @Column(name = "transition_counts", nullable = false, length = 512)
    private String transitionCounts;

    @Column(name = "transition_probabilities", nullable = false, length = 512)
    private String transitionProbabilities;

    @Column(name = "stationary_distribution", length = 128)
    private String stationaryDistribution;

    @Column(name = "horizon_distributions", length = 1024)
    private String horizonDistributions;

    @Column(name = "convergence_distributions", length = 8192)
    private String convergenceDistributions;

    @Column(name = "convergence_distances", length = 4096)
    private String convergenceDistances;

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

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public int getStateU() {
        return stateU;
    }

    public void setStateU(int stateU) {
        this.stateU = stateU;
    }

    public int getStateF() {
        return stateF;
    }

    public void setStateF(int stateF) {
        this.stateF = stateF;
    }

    public int getStateD() {
        return stateD;
    }

    public void setStateD(int stateD) {
        this.stateD = stateD;
    }

    public String getTransitionCounts() {
        return transitionCounts;
    }

    public void setTransitionCounts(String transitionCounts) {
        this.transitionCounts = transitionCounts;
    }

    public String getTransitionProbabilities() {
        return transitionProbabilities;
    }

    public void setTransitionProbabilities(String transitionProbabilities) {
        this.transitionProbabilities = transitionProbabilities;
    }

    public String getStationaryDistribution() {
        return stationaryDistribution;
    }

    public void setStationaryDistribution(String stationaryDistribution) {
        this.stationaryDistribution = stationaryDistribution;
    }

    public String getHorizonDistributions() {
        return horizonDistributions;
    }

    public void setHorizonDistributions(String horizonDistributions) {
        this.horizonDistributions = horizonDistributions;
    }

    public String getConvergenceDistributions() {
        return convergenceDistributions;
    }

    public void setConvergenceDistributions(String convergenceDistributions) {
        this.convergenceDistributions = convergenceDistributions;
    }

    public String getConvergenceDistances() {
        return convergenceDistances;
    }

    public void setConvergenceDistances(String convergenceDistances) {
        this.convergenceDistances = convergenceDistances;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
