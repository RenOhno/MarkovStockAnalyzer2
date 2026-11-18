package com.example.markovstockanalyzer2.analysis;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

/** Request body for POST /api/analyses. {@code end} is exclusive. */
public record CreateAnalysisRequest(
        @NotBlank String ticker,
        @NotNull LocalDate start,
        @NotNull LocalDate end,
        @Positive Double theta) {
}
