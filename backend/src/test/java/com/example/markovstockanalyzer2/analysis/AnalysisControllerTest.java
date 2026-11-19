package com.example.markovstockanalyzer2.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AnalysisControllerTest {

    private static AnalysisResultResponse sampleResponse() {
        return new AnalysisResultResponse(
                1L,
                "7203.T",
                LocalDate.of(2023, 1, 4),
                LocalDate.of(2026, 10, 27),
                0.005,
                LocalDate.of(2023, 1, 4),
                LocalDate.of(2026, 10, 26),
                931,
                Map.of("U", 358, "F", 251, "D", 322),
                List.of(List.of(142, 88, 128), List.of(95, 77, 78), List.of(121, 86, 115)),
                List.of(
                        List.of(0.3966, 0.2458, 0.3575),
                        List.of(0.3800, 0.3080, 0.3120),
                        List.of(0.3758, 0.2671, 0.3571)),
                "F",
                List.of(0.3850, 0.2699, 0.3451),
                Map.of(
                        "1", List.of(0.3800, 0.3080, 0.3120),
                        "3", List.of(0.3850, 0.2700, 0.3450),
                        "5", List.of(0.3850, 0.2699, 0.3451),
                        "10", List.of(0.3850, 0.2699, 0.3451)),
                List.of(new AnalysisResultResponse.ConvergencePoint(0, List.of(0.0, 1.0, 0.0), 0.23)),
                LocalDateTime.of(2026, 11, 18, 12, 0));
    }

    @Test
    void createDelegatesToService() {
        AnalysisService service = mock(AnalysisService.class);
        AnalysisResultResponse expected = sampleResponse();
        when(service.create("7203.T", LocalDate.of(2023, 1, 4), LocalDate.of(2026, 10, 27), null))
                .thenReturn(expected);

        AnalysisController controller = new AnalysisController(service);
        AnalysisResultResponse actual =
                controller.create(
                        new CreateAnalysisRequest(
                                "7203.T", LocalDate.of(2023, 1, 4), LocalDate.of(2026, 10, 27), null));

        assertEquals(expected, actual);
    }

    @Test
    void listDelegatesToService() {
        AnalysisService service = mock(AnalysisService.class);
        when(service.list()).thenReturn(List.of(sampleResponse()));

        AnalysisController controller = new AnalysisController(service);

        assertEquals(1, controller.list().size());
    }

    @Test
    void getDelegatesToService() {
        AnalysisService service = mock(AnalysisService.class);
        when(service.get(1L)).thenReturn(sampleResponse());

        AnalysisController controller = new AnalysisController(service);

        assertEquals("7203.T", controller.get(1L).ticker());
    }
}
