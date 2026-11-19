package com.example.markovstockanalyzer2.analysis;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BacktestResultRepository extends JpaRepository<BacktestResult, Long> {

    List<BacktestResult> findAllByOrderByCreatedAtDesc();
}
