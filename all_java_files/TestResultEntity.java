package com.cbp.testgen.database.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "test_results", indexes = {
        @Index(name = "idx_testresults_run_id", columnList = "run_id"),
        @Index(name = "idx_testresults_testcase_id", columnList = "test_case_id"),
        @Index(name = "idx_testresults_status", columnList = "status")
})
public class TestResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_case_id", nullable = false)
    private TestCaseEntity testCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private TestRunEntity testRun;

    @Column(nullable = false, length = 20)
    private String status; // PASS, FAIL, ERROR

    @Column(name = "execution_time_ms")
    private long executionTimeMs;

    @Lob
    @Column(name = "stack_trace", columnDefinition = "CLOB")
    private String stackTrace;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    public TestResultEntity() {}

    public TestResultEntity(TestCaseEntity testCase, TestRunEntity testRun, String status,
                            long executionTimeMs, String stackTrace, String errorMessage) {
        this.testCase = testCase;
        this.testRun = testRun;
        this.status = status;
        this.executionTimeMs = executionTimeMs;
        this.stackTrace = stackTrace;
        this.errorMessage = errorMessage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TestCaseEntity getTestCase() {
        return testCase;
    }

    public void setTestCase(TestCaseEntity testCase) {
        this.testCase = testCase;
    }

    public TestRunEntity getTestRun() {
        return testRun;
    }

    public void setTestRun(TestRunEntity testRun) {
        this.testRun = testRun;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
