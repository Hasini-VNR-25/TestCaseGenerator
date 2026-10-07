package com.cbp.testgen.database.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "class_versions", indexes = {
        @Index(name = "idx_classversion_class_id", columnList = "class_id"),
        @Index(name = "idx_classversion_hash", columnList = "source_hash")
})
public class ClassVersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassMetadataEntity classMetadata;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(name = "source_hash", nullable = false, length = 64)
    private String sourceHash;

    @Lob
    @Column(name = "changed_methods_json", columnDefinition = "CLOB")
    private String changedMethodsJson;

    @Lob
    @Column(name = "source_code", columnDefinition = "CLOB")
    private String sourceCode;

    @Column(name = "version_timestamp", nullable = false)
    private LocalDateTime timestamp;

    public ClassVersionEntity() {
        this.timestamp = LocalDateTime.now();
    }

    public ClassVersionEntity(ClassMetadataEntity classMetadata, int versionNumber,
                              String sourceHash, String changedMethodsJson) {
        this(classMetadata, versionNumber, sourceHash, changedMethodsJson, null);
    }

    public ClassVersionEntity(ClassMetadataEntity classMetadata, int versionNumber,
                              String sourceHash, String changedMethodsJson, String sourceCode) {
        this.classMetadata = classMetadata;
        this.versionNumber = versionNumber;
        this.sourceHash = sourceHash;
        this.changedMethodsJson = changedMethodsJson;
        this.sourceCode = sourceCode;
        this.timestamp = LocalDateTime.now();
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

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getSourceHash() {
        return sourceHash;
    }

    public void setSourceHash(String sourceHash) {
        this.sourceHash = sourceHash;
    }

    public String getChangedMethodsJson() {
        return changedMethodsJson;
    }

    public void setChangedMethodsJson(String changedMethodsJson) {
        this.changedMethodsJson = changedMethodsJson;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
