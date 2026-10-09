package com.cbp.testgen.database.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "mutation_results", indexes = {
        @Index(name = "idx_mutation_class_id", columnList = "class_id"),
        @Index(name = "idx_mutation_run_id", columnList = "run_id"),
        @Index(name = "idx_mutation_score", columnList = "mutation_score_pct")
})
public class MutationResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassMetadataEntity classMetadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private TestRunEntity testRun;

    @Column(name = "total_mutants")
    private int totalMutants;

    @Column(name = "mutants_killed")
    private int mutantsKilled;

    @Column(name = "mutation_score_pct")
    private double mutationScorePct;

    @Lob
    @Column(name = "mutant_details_json", columnDefinition = "CLOB")
    private String mutantDetailsJson;

    public MutationResultEntity() {}

    public MutationResultEntity(ClassMetadataEntity classMetadata, TestRunEntity testRun,
                                int totalMutants, int mutantsKilled, double mutationScorePct, String mutantDetailsJson) {
        this.classMetadata = classMetadata;
        this.testRun = testRun;
        this.totalMutants = totalMutants;
        this.mutantsKilled = mutantsKilled;
        this.mutationScorePct = mutationScorePct;
        this.mutantDetailsJson = mutantDetailsJson;
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

    public int getTotalMutants() {
        return totalMutants;
    }

    public void setTotalMutants(int totalMutants) {
        this.totalMutants = totalMutants;
    }

    public int getMutantsKilled() {
        return mutantsKilled;
    }

    public void setMutantsKilled(int mutantsKilled) {
        this.mutantsKilled = mutantsKilled;
    }

    public double getMutationScorePct() {
        return mutationScorePct;
    }

    public void setMutationScorePct(double mutationScorePct) {
        this.mutationScorePct = mutationScorePct;
    }

    public String getMutantDetailsJson() {
        return mutantDetailsJson;
    }

    public void setMutantDetailsJson(String mutantDetailsJson) {
        this.mutantDetailsJson = mutantDetailsJson;
    }
}
