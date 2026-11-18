CREATE TABLE analysis_results (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ticker VARCHAR(32) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    theta DOUBLE NOT NULL,
    data_start DATE NOT NULL,
    data_end DATE NOT NULL,
    observations INT NOT NULL,
    current_state VARCHAR(1) NOT NULL,
    state_u INT NOT NULL,
    state_f INT NOT NULL,
    state_d INT NOT NULL,
    transition_counts VARCHAR(512) NOT NULL,
    transition_probabilities VARCHAR(512) NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
