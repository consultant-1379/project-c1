CREATE TABLE IF NOT EXISTS jobs (
    id INTEGER PRIMARY KEY DEFAULT 0,
    job_type VARCHAR(100) NOT NULL,
    last_build_number INTEGER NOT NULL,
    time_scale VARCHAR(100) NOT NULL,
    number_of_deliveries INTEGER NOT NULL,
    duration_time FLOAT NOT NULL,
    success_rate FLOAT NOT NULL,
    restore_time FLOAT NOT NULL
    );

ALTER TABLE jobs MODIFY COLUMN id INT auto_increment;