package com.cbp.testgen.database.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "test_cases", indexes = {
        @Index(name = "idx_testcases_method_id", columnList = "method_id"),
        @Index(name = "idx_testcases_type", columnList = "test_type")
})
public class TestCaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "method_id", nullable = true)
    private MethodMetadataEntity methodMetadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassMetadataEntity classMetadata;

    @Column(name = "test_name", nullable = false, length = 200)
    private String testName;

    @Column(name = "test_type", nullable = false, length = 50)
    private String testType; // BOUNDARY, EQUIVALENCE, EXCEPTION, OOP, GOLDEN

    @Lob
    @Column(name = "generated_code", columnDefinition = "CLOB")
    private String generatedCode;

    @Column(name = "assertion_count")
    private int assertionCount;

    @Column(name = "is_weak")
    private boolean isWeak;

    @Column(length = 500)
    private String description;

    public TestCaseEntity() {}

    public TestCaseEntity(ClassMetadataEntity classMetadata, MethodMetadataEntity methodMetadata,
                          String testName, String testType, String generatedCode,
                          int assertionCount, boolean isWeak, String description) {
        this.classMetadata = classMetadata;
        this.methodMetadata = methodMetadata;
        this.testName = testName;
        this.testType = testType;
        this.generatedCode = generatedCode;
        this.assertionCount = assertionCount;
        this.isWeak = isWeak;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MethodMetadataEntity getMethodMetadata() {
        return methodMetadata;
    }

    public void setMethodMetadata(MethodMetadataEntity methodMetadata) {
        this.methodMetadata = methodMetadata;
    }

    public ClassMetadataEntity getClassMetadata() {
        return classMetadata;
    }

    public void setClassMetadata(ClassMetadataEntity classMetadata) {
        this.classMetadata = classMetadata;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public String getGeneratedCode() {
        return generatedCode;
    }

    public void setGeneratedCode(String generatedCode) {
        this.generatedCode = generatedCode;
    }

    public int getAssertionCount() {
        return assertionCount;
    }

    public void setAssertionCount(int assertionCount) {
        this.assertionCount = assertionCount;
    }

    public boolean isWeak() {
        return isWeak;
    }

    public void setWeak(boolean weak) {
        isWeak = weak;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
