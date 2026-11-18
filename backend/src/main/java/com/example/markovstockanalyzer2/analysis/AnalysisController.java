package com.example.markovstockanalyzer2.analysis;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analyses")
public class AnalysisController {

    private final AnalysisService service;

    public AnalysisController(AnalysisService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AnalysisResultResponse create(@Valid @RequestBody CreateAnalysisRequest request) {
        return service.create(request.ticker(), request.start(), request.end(), request.theta());
    }

    @GetMapping
    public List<AnalysisResultResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public AnalysisResultResponse get(@PathVariable Long id) {
        return service.get(id);
    }
}
