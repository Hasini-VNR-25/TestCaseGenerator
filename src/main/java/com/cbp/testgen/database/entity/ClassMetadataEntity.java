package com.cbp.testgen.database.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "classes", indexes = {
        @Index(name = "idx_classes_project_id", columnList = "project_id"),
        @Index(name = "idx_classes_class_name", columnList = "class_name"),
        @Index(name = "idx_classes_source_hash", columnList = "source_hash")
})
public class ClassMetadataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @Column(name = "class_name", nullable = false, length = 150)
    private String className;

    @Column(name = "package_name", length = 200)
    private String packageName;

    @Column(name = "source_hash", nullable = false, length = 64)
    private String sourceHash;

    @Column(name = "super_class", length = 150)
    private String superClass;

    @Column(name = "interfaces", length = 300)
    private String interfaces;

    @Lob
    @Column(name = "source_code", columnDefinition = "CLOB")
    private String sourceCode;

    public ClassMetadataEntity() {}

    public ClassMetadataEntity(ProjectEntity project, String className, String packageName,
                               String sourceHash, String superClass, String interfaces, String sourceCode) {
        this.project = project;
        this.className = className;
        this.packageName = packageName;
        this.sourceHash = sourceHash;
        this.superClass = superClass;
        this.interfaces = interfaces;
        this.sourceCode = sourceCode;
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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getSourceHash() {
        return sourceHash;
    }

    public void setSourceHash(String sourceHash) {
        this.sourceHash = sourceHash;
    }

    public String getSuperClass() {
        return superClass;
    }

    public void setSuperClass(String superClass) {
        this.superClass = superClass;
    }

    public String getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(String interfaces) {
        this.interfaces = interfaces;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }
}
