package com.cbp.testgen.database.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_runs", indexes = {
        @Index(name = "idx_testruns_project_id", columnList = "project_id"),
        @Index(name = "idx_testruns_timestamp", columnList = "run_timestamp")
})
public class TestRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @Column(name = "run_timestamp", nullable = false)
    private LocalDateTime runTimestamp;

    @Column(name = "triggered_by", length = 100)
    private String triggeredBy;

    @Column(name = "total_tests")
    private int totalTests;

    @Column(name = "passed_tests")
    private int passedTests;

    @Column(name = "failed_tests")
    private int failedTests;

    @Column(name = "execution_duration_ms")
    private long executionDurationMs;

    public TestRunEntity() {
        this.runTimestamp = LocalDateTime.now();
    }

    public TestRunEntity(ProjectEntity project, String triggeredBy, int totalTests,
                         int passedTests, int failedTests, long executionDurationMs) {
        this.project = project;
        this.runTimestamp = LocalDateTime.now();
        this.triggeredBy = triggeredBy;
        this.totalTests = totalTests;
        this.passedTests = passedTests;
        this.failedTests = failedTests;
        this.executionDurationMs = executionDurationMs;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }

    public LocalDateTime getRunTimestamp() {
        return runTimestamp;
    }

    public void setRunTimestamp(LocalDateTime runTimestamp) {
        this.runTimestamp = runTimestamp;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public int getTotalTests() {
        return totalTests;
    }

    public void setTotalTests(int totalTests) {
        this.totalTests = totalTests;
    }

    public int getPassedTests() {
        return passedTests;
    }

    public void setPassedTests(int passedTests) {
        this.passedTests = passedTests;
    }

    public int getFailedTests() {
        return failedTests;
    }

    public void setFailedTests(int failedTests) {
        this.failedTests = failedTests;
    }

    public long getExecutionDurationMs() {
        return executionDurationMs;
    }

    public void setExecutionDurationMs(long executionDurationMs) {
        this.executionDurationMs = executionDurationMs;
    }
}
