ALTER TABLE analysis_results
    ADD COLUMN stationary_distribution VARCHAR(128) NULL,
    ADD COLUMN horizon_distributions VARCHAR(1024) NULL,
    ADD COLUMN convergence_distributions VARCHAR(8192) NULL,
    ADD COLUMN convergence_distances VARCHAR(4096) NULL;
