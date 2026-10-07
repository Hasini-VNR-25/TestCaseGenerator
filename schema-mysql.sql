-- ==========================================================
-- Intelligent Java Test Case Generator (CBP Project)
-- Database DDL for MySQL 8.0 (3NF Normalized)
-- ==========================================================

DROP DATABASE IF EXISTS testgen_db;
CREATE DATABASE testgen_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE testgen_db;

-- 1. Projects Table
CREATE TABLE projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    upload_date DATETIME NOT NULL,
    description VARCHAR(500),
    INDEX idx_project_name (name)
) ENGINE=InnoDB;

-- 2. Classes Table
CREATE TABLE classes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    class_name VARCHAR(150) NOT NULL,
    package_name VARCHAR(200),
    source_hash VARCHAR(64) NOT NULL,
    super_class VARCHAR(150),
    interfaces VARCHAR(300),
    source_code MEDIUMTEXT,
    INDEX idx_classes_project_id (project_id),
    INDEX idx_classes_class_name (class_name),
    INDEX idx_classes_source_hash (source_hash),
    CONSTRAINT fk_classes_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 3. Methods Table
CREATE TABLE methods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL,
    method_name VARCHAR(150) NOT NULL,
    signature VARCHAR(300) NOT NULL,
    return_type VARCHAR(100) NOT NULL,
    modifiers VARCHAR(100),
    is_abstract BOOLEAN DEFAULT FALSE,
    INDEX idx_methods_class_id (class_id),
    INDEX idx_methods_name (method_name),
    CONSTRAINT fk_methods_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. Test Cases Table
CREATE TABLE test_cases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL,
    method_id BIGINT,
    test_name VARCHAR(200) NOT NULL,
    test_type VARCHAR(50) NOT NULL,
    generated_code MEDIUMTEXT,
    assertion_count INT DEFAULT 0,
    is_weak BOOLEAN DEFAULT FALSE,
    description VARCHAR(500),
    INDEX idx_testcases_class_id (class_id),
    INDEX idx_testcases_method_id (method_id),
    INDEX idx_testcases_type (test_type),
    CONSTRAINT fk_testcases_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    CONSTRAINT fk_testcases_method FOREIGN KEY (method_id) REFERENCES methods(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 5. Test Runs Table
CREATE TABLE test_runs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    run_timestamp DATETIME NOT NULL,
    triggered_by VARCHAR(100),
    total_tests INT DEFAULT 0,
    passed_tests INT DEFAULT 0,
    failed_tests INT DEFAULT 0,
    execution_duration_ms BIGINT DEFAULT 0,
    INDEX idx_testruns_project_id (project_id),
    INDEX idx_testruns_timestamp (run_timestamp),
    CONSTRAINT fk_testruns_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. Test Results Table
CREATE TABLE test_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    test_case_id BIGINT NOT NULL,
    run_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    execution_time_ms BIGINT DEFAULT 0,
    stack_trace MEDIUMTEXT,
    error_message VARCHAR(500),
    INDEX idx_testresults_run_id (run_id),
    INDEX idx_testresults_testcase_id (test_case_id),
    INDEX idx_testresults_status (status),
    CONSTRAINT fk_testresults_testcase FOREIGN KEY (test_case_id) REFERENCES test_cases(id) ON DELETE CASCADE,
    CONSTRAINT fk_testresults_run FOREIGN KEY (run_id) REFERENCES test_runs(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 7. Coverage Results Table
CREATE TABLE coverage_results (
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
    INDEX idx_coverage_class_id (class_id),
    INDEX idx_coverage_run_id (run_id),
    CONSTRAINT fk_coverage_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    CONSTRAINT fk_coverage_run FOREIGN KEY (run_id) REFERENCES test_runs(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 8. Mutation Results Table
CREATE TABLE mutation_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL,
    run_id BIGINT NOT NULL,
    total_mutants INT DEFAULT 0,
    mutants_killed INT DEFAULT 0,
    mutation_score_pct DOUBLE DEFAULT 0.0,
    mutant_details_json MEDIUMTEXT,
    INDEX idx_mutation_class_id (class_id),
    INDEX idx_mutation_run_id (run_id),
    INDEX idx_mutation_score (mutation_score_pct),
    CONSTRAINT fk_mutation_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    CONSTRAINT fk_mutation_run FOREIGN KEY (run_id) REFERENCES test_runs(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 9. Flaky Tests Table
CREATE TABLE flaky_tests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    test_case_id BIGINT NOT NULL,
    run_id BIGINT NOT NULL,
    detected_on DATETIME NOT NULL,
    inconsistency_count INT DEFAULT 0,
    pass_rate_pct DOUBLE DEFAULT 0.0,
    INDEX idx_flaky_testcase_id (test_case_id),
    INDEX idx_flaky_run_id (run_id),
    CONSTRAINT fk_flaky_testcase FOREIGN KEY (test_case_id) REFERENCES test_cases(id) ON DELETE CASCADE,
    CONSTRAINT fk_flaky_run FOREIGN KEY (run_id) REFERENCES test_runs(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 10. Class Versions Table
CREATE TABLE class_versions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_id BIGINT NOT NULL,
    version_number INT NOT NULL,
    source_hash VARCHAR(64) NOT NULL,
    changed_methods_json MEDIUMTEXT,
    version_timestamp DATETIME NOT NULL,
    INDEX idx_classversion_class_id (class_id),
    INDEX idx_classversion_hash (source_hash),
    CONSTRAINT fk_classversions_class FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ==========================================================
-- SQL VIEW: latest_class_health
-- Demonstrates SQL View joining aggregate telemetry
-- ==========================================================
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
