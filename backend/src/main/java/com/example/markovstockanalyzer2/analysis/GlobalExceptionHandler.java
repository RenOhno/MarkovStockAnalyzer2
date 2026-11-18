package com.example.markovstockanalyzer2.analysis;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Passes through "no data" (404) and "not enough data" (422) from the
     * analysis service; anything else becomes 502 Bad Gateway.
     */
    @ExceptionHandler(AnalysisServiceException.class)
    public ResponseEntity<Map<String, String>> handleAnalysisError(AnalysisServiceException e) {
        int status = e.status();
        if (status != 404 && status != 422) {
            status = 502;
        }
        return ResponseEntity.status(status)
                .body(Map.of("error", e.getMessage() == null ? "analysis error" : e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", String.valueOf(e.getMessage())));
    }
}
