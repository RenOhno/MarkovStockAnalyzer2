package com.example.markovstockanalyzer2.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BacktestControllerTest {

    private static BacktestResponse sampleResponse() {
        BacktestResponse.ModelMetrics markov =
                new BacktestResponse.ModelMetrics(
                        900,
                        0,
                        0.3722,
                        Map.of("U", 0.4, "F", 0.3, "D", 0.0),
                        Map.of("U", 0.77, "F", 0.25, "D", 0.0),
                        0.2333,
                        0.34,
                        List.of(
                                List.of(276, 72, 0), List.of(176, 59, 1), List.of(247, 68, 0)),
                        0.668,
                        1.1366);
        return new BacktestResponse(
                1L,
                "7203.T",
                LocalDate.of(2023, 1, 4),
                LocalDate.of(2026, 10, 27),
                0.005,
                LocalDate.of(2023, 1, 4),
                LocalDate.of(2026, 10, 26),
                931,
                30,
                900,
                Map.of("markov", markov),
                LocalDateTime.of(2026, 11, 19, 9, 40));
    }

    @Test
    void createDelegatesToService() {
        BacktestService service = mock(BacktestService.class);
        BacktestResponse expected = sampleResponse();
        when(service.create(
                        "7203.T", LocalDate.of(2023, 1, 4), LocalDate.of(2026, 10, 27), null, null))
                .thenReturn(expected);

        BacktestController controller = new BacktestController(service);
        BacktestResponse actual =
                controller.create(
                        new CreateBacktestRequest(
                                "7203.T", LocalDate.of(2023, 1, 4), LocalDate.of(2026, 10, 27),
                                null, null));

        assertEquals(expected, actual);
    }

    @Test
    void listDelegatesToService() {
        BacktestService service = mock(BacktestService.class);
        when(service.list()).thenReturn(List.of(sampleResponse()));

        BacktestController controller = new BacktestController(service);

        assertEquals(1, controller.list().size());
    }

    @Test
    void getDelegatesToService() {
        BacktestService service = mock(BacktestService.class);
        when(service.get(1L)).thenReturn(sampleResponse());

        BacktestController controller = new BacktestController(service);

        assertEquals("7203.T", controller.get(1L).ticker());
    }
}
