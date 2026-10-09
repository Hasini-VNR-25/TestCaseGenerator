package com.cbp.testgen.analyzer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClassInfo {
    private final String packageName;
    private final String className;
    private final String superClass;
    private final List<String> interfaces;
    private final List<String> modifiers;
    private final boolean isAbstract;
    private final boolean isInterface;
    private final boolean isEnum;
    private final List<MethodInfo> constructors;
    private final List<MethodInfo> methods;
    private final List<FieldInfo> fields;
    private final String sourceCode;
    private final String sourceHash;

    public ClassInfo(String packageName, String className, String superClass,
                     List<String> interfaces, List<String> modifiers,
                     boolean isAbstract, boolean isInterface, boolean isEnum,
                     List<MethodInfo> constructors, List<MethodInfo> methods,
                     List<FieldInfo> fields, String sourceCode, String sourceHash) {
        this.packageName = packageName != null ? packageName : "";
        this.className = className;
        this.superClass = superClass;
        this.interfaces = interfaces != null ? new ArrayList<>(interfaces) : new ArrayList<>();
        this.modifiers = modifiers != null ? new ArrayList<>(modifiers) : new ArrayList<>();
        this.isAbstract = isAbstract;
        this.isInterface = isInterface;
        this.isEnum = isEnum;
        this.constructors = constructors != null ? new ArrayList<>(constructors) : new ArrayList<>();
        this.methods = methods != null ? new ArrayList<>(methods) : new ArrayList<>();
        this.fields = fields != null ? new ArrayList<>(fields) : new ArrayList<>();
        this.sourceCode = sourceCode != null ? sourceCode : "";
        this.sourceHash = sourceHash != null ? sourceHash : "";
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public String getFullClassName() {
        return packageName.isEmpty() ? className : packageName + "." + className;
    }

    public String getSuperClass() {
        return superClass;
    }

    public boolean hasSuperClass() {
        return superClass != null && !superClass.isEmpty() && !"Object".equals(superClass) && !"java.lang.Object".equals(superClass);
    }

    public List<String> getInterfaces() {
        return Collections.unmodifiableList(interfaces);
    }

    public List<String> getModifiers() {
        return Collections.unmodifiableList(modifiers);
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public boolean isEnum() {
        return isEnum;
    }

    public List<MethodInfo> getConstructors() {
        return Collections.unmodifiableList(constructors);
    }

    public List<MethodInfo> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    public List<FieldInfo> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public String getSourceHash() {
        return sourceHash;
    }

    public MethodInfo getPrimaryConstructor() {
        if (constructors.isEmpty()) {
            return null;
        }
        // Prefer constructor with most parameters
        return constructors.stream()
                .max((c1, c2) -> Integer.compare(c1.getParameters().size(), c2.getParameters().size()))
                .orElse(constructors.get(0));
    }
}
