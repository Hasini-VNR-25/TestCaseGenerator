package com.cbp.testgen.database.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "coverage_results", indexes = {
        @Index(name = "idx_coverage_class_id", columnList = "class_id"),
        @Index(name = "idx_coverage_run_id", columnList = "run_id")
})
public class CoverageResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassMetadataEntity classMetadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private TestRunEntity testRun;

    @Column(name = "line_coverage_pct")
    private double lineCoveragePct;

    @Column(name = "branch_coverage_pct")
    private double branchCoveragePct;

    @Column(name = "method_coverage_pct")
    private double methodCoveragePct;

    @Column(name = "lines_covered")
    private int linesCovered;

    @Column(name = "total_lines")
    private int totalLines;

    @Column(name = "branches_covered")
    private int branchesCovered;

    @Column(name = "total_branches")
    private int totalBranches;

    public CoverageResultEntity() {}

    public CoverageResultEntity(ClassMetadataEntity classMetadata, TestRunEntity testRun,
                                double lineCoveragePct, double branchCoveragePct, double methodCoveragePct,
                                int linesCovered, int totalLines, int branchesCovered, int totalBranches) {
        this.classMetadata = classMetadata;
        this.testRun = testRun;
        this.lineCoveragePct = lineCoveragePct;
        this.branchCoveragePct = branchCoveragePct;
        this.methodCoveragePct = methodCoveragePct;
        this.linesCovered = linesCovered;
        this.totalLines = totalLines;
        this.branchesCovered = branchesCovered;
        this.totalBranches = totalBranches;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ClassMetadataEntity getClassMetadata() {
        return classMetadata;
    }

    public void setClassMetadata(ClassMetadataEntity classMetadata) {
        this.classMetadata = classMetadata;
    }

    public TestRunEntity getTestRun() {
        return testRun;
    }

    public void setTestRun(TestRunEntity testRun) {
        this.testRun = testRun;
    }

    public double getLineCoveragePct() {
        return lineCoveragePct;
    }

    public void setLineCoveragePct(double lineCoveragePct) {
        this.lineCoveragePct = lineCoveragePct;
    }

    public double getBranchCoveragePct() {
        return branchCoveragePct;
    }

    public void setBranchCoveragePct(double branchCoveragePct) {
        this.branchCoveragePct = branchCoveragePct;
    }

    public double getMethodCoveragePct() {
        return methodCoveragePct;
    }

    public void setMethodCoveragePct(double methodCoveragePct) {
        this.methodCoveragePct = methodCoveragePct;
    }

    public int getLinesCovered() {
        return linesCovered;
    }

    public void setLinesCovered(int linesCovered) {
        this.linesCovered = linesCovered;
    }

    public int getTotalLines() {
        return totalLines;
    }

    public void setTotalLines(int totalLines) {
        this.totalLines = totalLines;
    }

    public int getBranchesCovered() {
        return branchesCovered;
    }

    public void setBranchesCovered(int branchesCovered) {
        this.branchesCovered = branchesCovered;
    }

    public int getTotalBranches() {
        return totalBranches;
    }

    public void setTotalBranches(int totalBranches) {
        this.totalBranches = totalBranches;
    }
}
