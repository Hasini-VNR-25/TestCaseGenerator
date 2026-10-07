package com.cbp.testgen.database.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "methods", indexes = {
        @Index(name = "idx_methods_class_id", columnList = "class_id"),
        @Index(name = "idx_methods_name", columnList = "method_name")
})
public class MethodMetadataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassMetadataEntity classMetadata;

    @Column(name = "method_name", nullable = false, length = 150)
    private String methodName;

    @Column(name = "signature", nullable = false, length = 300)
    private String signature;

    @Column(name = "return_type", nullable = false, length = 100)
    private String returnType;

    @Column(length = 100)
    private String modifiers;

    @Column(name = "is_abstract")
    private boolean isAbstract;

    public MethodMetadataEntity() {}

    public MethodMetadataEntity(ClassMetadataEntity classMetadata, String methodName,
                                String signature, String returnType, String modifiers, boolean isAbstract) {
        this.classMetadata = classMetadata;
        this.methodName = methodName;
        this.signature = signature;
        this.returnType = returnType;
        this.modifiers = modifiers;
        this.isAbstract = isAbstract;
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

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getModifiers() {
        return modifiers;
    }

    public void setModifiers(String modifiers) {
        this.modifiers = modifiers;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
    }
}
