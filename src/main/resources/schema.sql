-- ==========================================================
-- Intelligent Java Test Case Generator (CBP Project)
-- Relational Database Schema (Target 3NF Normalized)
-- Compatible with H2 / MySQL / PostgreSQL
-- ==========================================================

-- 1. Projects Table
CREATE TABLE IF NOT EXISTS projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    upload_date TIMESTAMP NOT NULL,
    description VARCHAR(500)
);

-- 2. Classes Table
CREATE TABLE IF NOT EXISTS classes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    class_name VARCHAR(150) NOT NULL,
    package_name VARCHAR(200),
    source_hash VARCHAR(64) NOT NULL,
    super_class VARCHAR(150),
    interfaces VARCHAR(300),
    source_code CLOB,
    CONSTRAINT fk_classes_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

-- 3. Methods Table
CREATE TABLE IF NOT EXISTS methods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL,
    method_name VARCHAR(150) NOT NULL,
    signature VARCHAR(300) NOT NULL,
    return_type VARCHAR(100) NOT NULL,
    modifiers VARCHAR(100),
    is_abstract BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_methods_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE
);

-- 4. Test Cases Table
CREATE TABLE IF NOT EXISTS test_cases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL,
    method_id BIGINT,
    test_name VARCHAR(200) NOT NULL,
    test_type VARCHAR(50) NOT NULL,
    generated_code CLOB,
    assertion_count INT DEFAULT 0,
    is_weak BOOLEAN DEFAULT FALSE,
    description VARCHAR(500),
    CONSTRAINT fk_testcases_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    CONSTRAINT fk_testcases_method FOREIGN KEY (method_id) REFERENCES methods(id) ON DELETE SET NULL
);

-- 5. Test Runs Table
CREATE TABLE IF NOT EXISTS test_runs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    run_timestamp TIMESTAMP NOT NULL,
    triggered_by VARCHAR(100),
    total_tests INT DEFAULT 0,
    passed_tests INT DEFAULT 0,
    failed_tests INT DEFAULT 0,
    execution_duration_ms BIGINT DEFAULT 0,
    CONSTRAINT fk_testruns_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

-- 6. Test Results Table
CREATE TABLE IF NOT EXISTS test_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    test_case_id BIGINT NOT NULL,
    run_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    execution_time_ms BIGINT DEFAULT 0,
    stack_trace CLOB,
    error_message VARCHAR(500),
    CONSTRAINT fk_testresults_testcase FOREIGN KEY (test_case_id) REFERENCES test_cases(id) ON DELETE CASCADE,
    CONSTRAINT fk_testresults_run FOREIGN KEY (run_id) REFERENCES test_runs(id) ON DELETE CASCADE
);

-- 7. Coverage Results Table
CREATE TABLE IF NOT EXISTS coverage_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL,
    run_id BIGINT NOT NULL,
    line_coverage_pct DOUBLE DEFAULT 0.0,
    branch_coverage_pct DOUBLE DEFAULT 0.0,
    method_coverage_pct DOUBLE DEFAULT 0.0,
    lines_covered INT DEFAULT 0,
    total_lines INT DEFAULT 0,
    branches_covered INT DEFAULT 0,
    total_branches INT DEFAULT 0,
    CONSTRAINT fk_coverage_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    CONSTRAINT fk_coverage_run FOREIGN KEY (run_id) REFERENCES test_runs(id) ON DELETE CASCADE
);

-- 8. Mutation Results Table
CREATE TABLE IF NOT EXISTS mutation_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL,
    run_id BIGINT NOT NULL,
    total_mutants INT DEFAULT 0,
    mutants_killed INT DEFAULT 0,
    mutation_score_pct DOUBLE DEFAULT 0.0,
    mutant_details_json CLOB,
    CONSTRAINT fk_mutation_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    CONSTRAINT fk_mutation_run FOREIGN KEY (run_id) REFERENCES test_runs(id) ON DELETE CASCADE
);

-- 9. Flaky Tests Table
CREATE TABLE IF NOT EXISTS flaky_tests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    test_case_id BIGINT NOT NULL,
    run_id BIGINT NOT NULL,
    detected_on TIMESTAMP NOT NULL,
    inconsistency_count INT DEFAULT 0,
    pass_rate_pct DOUBLE DEFAULT 0.0,
    CONSTRAINT fk_flaky_testcase FOREIGN KEY (test_case_id) REFERENCES test_cases(id) ON DELETE CASCADE,
    CONSTRAINT fk_flaky_run FOREIGN KEY (run_id) REFERENCES test_runs(id) ON DELETE CASCADE
);

-- 10. Class Versions Table
CREATE TABLE IF NOT EXISTS class_versions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL,
    version_number INT NOT NULL,
    source_hash VARCHAR(64) NOT NULL,
    changed_methods_json CLOB,
    version_timestamp TIMESTAMP NOT NULL,
    CONSTRAINT fk_classversions_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_classes_proj ON classes(project_id);
CREATE INDEX IF NOT EXISTS idx_methods_cls ON methods(class_id);
CREATE INDEX IF NOT EXISTS idx_testcases_cls ON test_cases(class_id);
CREATE INDEX IF NOT EXISTS idx_testruns_time ON test_runs(run_timestamp);
CREATE INDEX IF NOT EXISTS idx_testresults_run ON test_results(run_id);
CREATE INDEX IF NOT EXISTS idx_coverage_cls ON coverage_results(class_id);
CREATE INDEX IF NOT EXISTS idx_mutation_cls ON mutation_results(class_id);

-- SQL VIEW: latest_class_health (Demonstrating DBMS Views)
CREATE OR REPLACE VIEW latest_class_health AS
SELECT 
    c.id AS class_id,
    c.class_name,
    c.package_name,
    c.project_id,
    COALESCE(cov.line_coverage_pct, 0.0) AS latest_line_coverage_pct,
    COALESCE(cov.branch_coverage_pct, 0.0) AS latest_branch_coverage_pct,
    COALESCE(mut.mutation_score_pct, 0.0) AS latest_mutation_score_pct,
    COALESCE(mut.total_mutants, 0) AS total_mutants,
    COALESCE(mut.mutants_killed, 0) AS mutants_killed,
    (SELECT COUNT(*) FROM flaky_tests ft 
     JOIN test_cases tc ON ft.test_case_id = tc.id 
     WHERE tc.class_id = c.id) AS total_flaky_count,
    (SELECT COUNT(*) FROM test_cases tc WHERE tc.class_id = c.id AND tc.is_weak = TRUE) AS weak_test_count
FROM classes c
LEFT JOIN (
    SELECT cr1.* FROM coverage_results cr1
    INNER JOIN (
        SELECT class_id, MAX(id) AS max_id FROM coverage_results GROUP BY class_id
    ) cr2 ON cr1.id = cr2.max_id
) cov ON c.id = cov.class_id
LEFT JOIN (
    SELECT mr1.* FROM mutation_results mr1
    INNER JOIN (
        SELECT class_id, MAX(id) AS max_id FROM mutation_results GROUP BY class_id
    ) mr2 ON mr1.id = mr2.max_id
) mut ON c.id = mut.class_id;
