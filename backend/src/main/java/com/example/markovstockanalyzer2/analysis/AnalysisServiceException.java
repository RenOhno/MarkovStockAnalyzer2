package com.example.markovstockanalyzer2.analysis;

/** The analysis service returned an error (or could not be reached). */
public class AnalysisServiceException extends RuntimeException {

    private final int status;

    public AnalysisServiceException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() {
        return status;
    }
}
