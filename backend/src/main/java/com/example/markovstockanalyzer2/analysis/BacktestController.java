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
@RequestMapping("/api/backtests")
public class BacktestController {

    private final BacktestService service;

    public BacktestController(BacktestService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BacktestResponse create(@Valid @RequestBody CreateBacktestRequest request) {
        return service.create(
                request.ticker(),
                request.start(),
                request.end(),
                request.theta(),
                request.minTrain());
    }

    @GetMapping
    public List<BacktestResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public BacktestResponse get(@PathVariable Long id) {
        return service.get(id);
    }
}
