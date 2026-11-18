package com.example.markovstockanalyzer2.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AnalysisServiceTest {

    private static TransitionMatrixData sampleData() {
        return new TransitionMatrixData(
                "TEST",
                "2026-01-05",
                "2026-01-10",
                0.005,
                "2026-01-05",
                "2026-01-09",
                4,
                Map.of("U", 2, "F", 1, "D", 1),
                List.of(List.of(0, 1, 0), List.of(0, 0, 1), List.of(1, 0, 0)),
                List.of(List.of(0.0, 1.0, 0.0), List.of(0.0, 0.0, 1.0), List.of(1.0, 0.0, 0.0)),
                "U");
    }

    @Test
    void createFetchesSavesAndMaps() {
        AnalysisClient client = mock(AnalysisClient.class);
        AnalysisResultRepository repository = mock(AnalysisResultRepository.class);
        when(client.fetchTransitionMatrix("TEST", LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 10), 0.005))
                .thenReturn(sampleData());
        when(repository.save(any(AnalysisResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AnalysisService service = new AnalysisService(client, repository);
        AnalysisResultResponse response =
                service.create("TEST", LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 10), null);

        assertEquals("TEST", response.ticker());
        assertEquals(4, response.observations());
        assertEquals(Map.of("U", 2, "F", 1, "D", 1), response.stateCounts());
        assertEquals(
                List.of(List.of(0, 1, 0), List.of(0, 0, 1), List.of(1, 0, 0)),
                response.transitionCounts());
        assertEquals("U", response.currentState());
        verify(repository).save(any(AnalysisResult.class));
    }

    @Test
    void createUsesGivenTheta() {
        AnalysisClient client = mock(AnalysisClient.class);
        AnalysisResultRepository repository = mock(AnalysisResultRepository.class);
        when(client.fetchTransitionMatrix(any(), any(), any(), org.mockito.ArgumentMatchers.eq(0.01)))
                .thenReturn(sampleData());
        when(repository.save(any(AnalysisResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AnalysisService service = new AnalysisService(client, repository);
        service.create("TEST", LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 10), 0.01);

        verify(client)
                .fetchTransitionMatrix("TEST", LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 10), 0.01);
    }
}
