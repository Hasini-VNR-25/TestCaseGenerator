package com.cbp.testgen.database.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "flaky_tests", indexes = {
        @Index(name = "idx_flaky_testcase_id", columnList = "test_case_id"),
        @Index(name = "idx_flaky_run_id", columnList = "run_id")
})
public class FlakyTestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_case_id", nullable = false)
    private TestCaseEntity testCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private TestRunEntity testRun;

    @Column(name = "detected_on", nullable = false)
    private LocalDateTime detectedOn;

    @Column(name = "inconsistency_count")
    private int inconsistencyCount;

    @Column(name = "pass_rate_pct")
    private double passRatePct;

    public FlakyTestEntity() {
        this.detectedOn = LocalDateTime.now();
    }

    public FlakyTestEntity(TestCaseEntity testCase, TestRunEntity testRun, int inconsistencyCount, double passRatePct) {
        this.testCase = testCase;
        this.testRun = testRun;
        this.detectedOn = LocalDateTime.now();
        this.inconsistencyCount = inconsistencyCount;
        this.passRatePct = passRatePct;
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

    public LocalDateTime getDetectedOn() {
        return detectedOn;
    }

    public void setDetectedOn(LocalDateTime detectedOn) {
        this.detectedOn = detectedOn;
    }

    public int getInconsistencyCount() {
        return inconsistencyCount;
    }

    public void setInconsistencyCount(int inconsistencyCount) {
        this.inconsistencyCount = inconsistencyCount;
    }

    public double getPassRatePct() {
        return passRatePct;
    }

    public void setPassRatePct(double passRatePct) {
        this.passRatePct = passRatePct;
    }
}
