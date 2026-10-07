-- ==========================================================
-- Intelligent Java Test Case Generator (CBP Project)
-- Database DDL for PostgreSQL 15 (3NF Normalized)
-- ==========================================================

DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;

-- 1. Projects Table
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    upload_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    description VARCHAR(500)
);
CREATE INDEX idx_project_name ON projects (name);

-- 2. Classes Table
CREATE TABLE classes (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    class_name VARCHAR(150) NOT NULL,
    package_name VARCHAR(200),
    source_hash VARCHAR(64) NOT NULL,
    super_class VARCHAR(150),
    interfaces VARCHAR(300),
    source_code TEXT
);
CREATE INDEX idx_classes_project_id ON classes (project_id);
CREATE INDEX idx_classes_class_name ON classes (class_name);
CREATE INDEX idx_classes_source_hash ON classes (source_hash);

-- 3. Methods Table
CREATE TABLE methods (
    id BIGSERIAL PRIMARY KEY,
    class_id BIGINT NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    method_name VARCHAR(150) NOT NULL,
    signature VARCHAR(300) NOT NULL,
    return_type VARCHAR(100) NOT NULL,
    modifiers VARCHAR(100),
    is_abstract BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_methods_class_id ON methods (class_id);
CREATE INDEX idx_methods_name ON methods (method_name);

-- 4. Test Cases Table
CREATE TABLE test_cases (
    id BIGSERIAL PRIMARY KEY,
    class_id BIGINT NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    method_id BIGINT REFERENCES methods(id) ON DELETE SET NULL,
    test_name VARCHAR(200) NOT NULL,
    test_type VARCHAR(50) NOT NULL,
    generated_code TEXT,
    assertion_count INT DEFAULT 0,
    is_weak BOOLEAN DEFAULT FALSE,
    description VARCHAR(500)
);
CREATE INDEX idx_testcases_class_id ON test_cases (class_id);
CREATE INDEX idx_testcases_method_id ON test_cases (method_id);
CREATE INDEX idx_testcases_type ON test_cases (test_type);

-- 5. Test Runs Table
CREATE TABLE test_runs (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    run_timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    triggered_by VARCHAR(100),
    total_tests INT DEFAULT 0,
    passed_tests INT DEFAULT 0,
    failed_tests INT DEFAULT 0,
    execution_duration_ms BIGINT DEFAULT 0
);
CREATE INDEX idx_testruns_project_id ON test_runs (project_id);
CREATE INDEX idx_testruns_timestamp ON test_runs (run_timestamp);

-- 6. Test Results Table
CREATE TABLE test_results (
    id BIGSERIAL PRIMARY KEY,
    test_case_id BIGINT NOT NULL REFERENCES test_cases(id) ON DELETE CASCADE,
    run_id BIGINT NOT NULL REFERENCES test_runs(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL,
    execution_time_ms BIGINT DEFAULT 0,
    stack_trace TEXT,
    error_message VARCHAR(500)
);
CREATE INDEX idx_testresults_run_id ON test_results (run_id);
CREATE INDEX idx_testresults_testcase_id ON test_results (test_case_id);
CREATE INDEX idx_testresults_status ON test_results (status);

-- 7. Coverage Results Table
CREATE TABLE coverage_results (
    id BIGSERIAL PRIMARY KEY,
    class_id BIGINT NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    run_id BIGINT NOT NULL REFERENCES test_runs(id) ON DELETE CASCADE,
    line_coverage_pct DOUBLE PRECISION DEFAULT 0.0,
    branch_coverage_pct DOUBLE PRECISION DEFAULT 0.0,
    method_coverage_pct DOUBLE PRECISION DEFAULT 0.0,
    lines_covered INT DEFAULT 0,
    total_lines INT DEFAULT 0,
    branches_covered INT DEFAULT 0,
    total_branches INT DEFAULT 0
);
CREATE INDEX idx_coverage_class_id ON coverage_results (class_id);
CREATE INDEX idx_coverage_run_id ON coverage_results (run_id);

-- 8. Mutation Results Table
CREATE TABLE mutation_results (
    id BIGSERIAL PRIMARY KEY,
    class_id BIGINT NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    run_id BIGINT NOT NULL REFERENCES test_runs(id) ON DELETE CASCADE,
    total_mutants INT DEFAULT 0,
    mutants_killed INT DEFAULT 0,
    mutation_score_pct DOUBLE PRECISION DEFAULT 0.0,
    mutant_details_json TEXT
);
CREATE INDEX idx_mutation_class_id ON mutation_results (class_id);
CREATE INDEX idx_mutation_run_id ON mutation_results (run_id);
CREATE INDEX idx_mutation_score ON mutation_results (mutation_score_pct);

-- 9. Flaky Tests Table
CREATE TABLE flaky_tests (
    id BIGSERIAL PRIMARY KEY,
    test_case_id BIGINT NOT NULL REFERENCES test_cases(id) ON DELETE CASCADE,
    run_id BIGINT NOT NULL REFERENCES test_runs(id) ON DELETE CASCADE,
    detected_on TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    inconsistency_count INT DEFAULT 0,
    pass_rate_pct DOUBLE PRECISION DEFAULT 0.0
);
CREATE INDEX idx_flaky_testcase_id ON flaky_tests (test_case_id);
CREATE INDEX idx_flaky_run_id ON flaky_tests (run_id);

-- 10. Class Versions Table
CREATE TABLE class_versions (
    id BIGSERIAL PRIMARY KEY,
    class_id BIGINT NOT NULL REFERENCES classes(id) ON DELETE CASCADE,
    version_number INT NOT NULL,
    source_hash VARCHAR(64) NOT NULL,
    changed_methods_json TEXT,
    version_timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_classversion_class_id ON class_versions (class_id);
CREATE INDEX idx_classversion_hash ON class_versions (source_hash);

-- SQL VIEW: latest_class_health
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
