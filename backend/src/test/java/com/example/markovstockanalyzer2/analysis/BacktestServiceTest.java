package com.example.markovstockanalyzer2.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BacktestServiceTest {

    private static BacktestData sampleData() {
        BacktestData.ModelMetricsData markov =
                new BacktestData.ModelMetricsData(
                        3,
                        0,
                        1.0,
                        Map.of("U", 1.0, "F", 1.0, "D", 1.0),
                        Map.of("U", 1.0, "F", 1.0, "D", 1.0),
                        1.0,
                        1.0,
                        List.of(List.of(1, 0, 0), List.of(0, 1, 0), List.of(0, 0, 1)),
                        0.0,
                        0.0);
        BacktestData.ModelMetricsData majority =
                new BacktestData.ModelMetricsData(
                        3,
                        0,
                        1.0 / 3,
                        Map.of("U", 1.0 / 3, "F", 0.0, "D", 0.0),
                        Map.of("U", 1.0, "F", 0.0, "D", 0.0),
                        1.0 / 9,
                        1.0 / 3,
                        List.of(List.of(1, 0, 0), List.of(1, 0, 0), List.of(1, 0, 0)),
                        0.8339,
                        1.3648);
        BacktestData.ModelMetricsData persistence =
                new BacktestData.ModelMetricsData(
                        3,
                        0,
                        0.0,
                        Map.of("U", 0.0, "F", 0.0, "D", 0.0),
                        Map.of("U", 0.0, "F", 0.0, "D", 0.0),
                        0.0,
                        0.0,
                        List.of(List.of(0, 0, 1), List.of(1, 0, 0), List.of(0, 1, 0)),
                        2.0,
                        34.5388);
        return new BacktestData(
                "TEST",
                "2026-01-05",
                "2026-01-13",
                0.005,
                "2026-01-05",
                "2026-01-12",
                7,
                3,
                3,
                Map.of("markov", markov, "majority", majority, "persistence", persistence));
    }

    @Test
    void createFetchesSavesAndMaps() {
        AnalysisClient client = mock(AnalysisClient.class);
        BacktestResultRepository repository = mock(BacktestResultRepository.class);
        when(client.runBacktest(
                        "TEST", LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 13), 0.005, 3))
                .thenReturn(sampleData());
        when(repository.save(any(BacktestResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BacktestService service = new BacktestService(client, repository);
        BacktestResponse response =
                service.create("TEST", LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 13), null, 3);

        assertEquals("TEST", response.ticker());
        assertEquals(7, response.observations());
        assertEquals(3, response.steps());
        BacktestResponse.ModelMetrics markov = response.models().get("markov");
        assertEquals(1.0, markov.accuracy());
        assertEquals(
                List.of(List.of(1, 0, 0), List.of(0, 1, 0), List.of(0, 0, 1)),
                markov.confusionMatrix());
        BacktestResponse.ModelMetrics majority = response.models().get("majority");
        assertEquals(1.0 / 3, majority.accuracy());
        assertEquals(1.0 / 3, majority.precision().get("U"));
        assertEquals(1.0, majority.recall().get("U"));
        assertEquals(0.8339, majority.brierScore());
        verify(repository).save(any(BacktestResult.class));
    }

    @Test
    void createUsesDefaults() {
        AnalysisClient client = mock(AnalysisClient.class);
        BacktestResultRepository repository = mock(BacktestResultRepository.class);
        when(client.runBacktest(any(), any(), any(), eq(0.005), eq(30))).thenReturn(sampleData());
        when(repository.save(any(BacktestResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BacktestService service = new BacktestService(client, repository);
        service.create("TEST", LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 13), null, null);

        verify(client)
                .runBacktest("TEST", LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 13), 0.005, 30);
    }
}
