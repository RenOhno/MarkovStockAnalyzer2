package com.example.markovstockanalyzer2.analysis;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Calls the Python analysis service (internal API, token authenticated). */
@Component
public class AnalysisClient {

    private final RestClient restClient;
    private final String internalToken;

    public AnalysisClient(
            @Value("${app.python-api-base-url}") String baseUrl,
            @Value("${app.internal-api-token:}") String internalToken) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.internalToken = internalToken;
    }

    public TransitionMatrixData fetchTransitionMatrix(
            String ticker, LocalDate start, LocalDate end, double theta) {
        TransitionMatrixRequest body =
                new TransitionMatrixRequest(
                        ticker, start.toString(), end.toString(), theta);
        try {
            return restClient
                    .post()
                    .uri("/internal/v1/transition-matrix")
                    .header("X-Internal-Token", internalToken)
                    .body(body)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            (request, response) -> {
                                throw new AnalysisServiceException(
                                        response.getStatusCode().value(),
                                        "analysis service returned "
                                                + response.getStatusCode());
                            })
                    .body(TransitionMatrixData.class);
        } catch (AnalysisServiceException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new AnalysisServiceException(502, "analysis service unreachable: " + e.getMessage());
        }
    }
}
