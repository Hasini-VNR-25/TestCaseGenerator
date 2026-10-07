package com.cbp.testgen.analyzer.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MethodInfo {
    private final String methodName;
    private final String returnType;
    private final List<ParameterInfo> parameters;
    private final List<String> thrownExceptions;
    private final List<String> modifiers;
    private final boolean isConstructor;
    private final boolean isAbstract;
    private final boolean isStatic;
    private final boolean isOverridden;
    private final int startLine;
    private final int endLine;
    private final String bodyCode;

    public MethodInfo(String methodName, String returnType, List<ParameterInfo> parameters,
                      List<String> thrownExceptions, List<String> modifiers, boolean isConstructor,
                      boolean isAbstract, boolean isStatic, boolean isOverridden,
                      int startLine, int endLine, String bodyCode) {
        this.methodName = methodName;
        this.returnType = returnType != null ? returnType : "void";
        this.parameters = parameters != null ? new ArrayList<>(parameters) : new ArrayList<>();
        this.thrownExceptions = thrownExceptions != null ? new ArrayList<>(thrownExceptions) : new ArrayList<>();
        this.modifiers = modifiers != null ? new ArrayList<>(modifiers) : new ArrayList<>();
        this.isConstructor = isConstructor;
        this.isAbstract = isAbstract;
        this.isStatic = isStatic;
        this.isOverridden = isOverridden;
        this.startLine = startLine;
        this.endLine = endLine;
        this.bodyCode = bodyCode != null ? bodyCode : "";
    }

    public String getMethodName() {
        return methodName;
    }

    public String getReturnType() {
        return returnType;
    }

    public List<ParameterInfo> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public List<String> getThrownExceptions() {
        return Collections.unmodifiableList(thrownExceptions);
    }

    public List<String> getModifiers() {
        return Collections.unmodifiableList(modifiers);
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isOverridden() {
        return isOverridden;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public String getBodyCode() {
        return bodyCode;
    }

    public boolean isVoid() {
        return "void".equalsIgnoreCase(returnType);
    }

    public boolean isBooleanReturn() {
        return "boolean".equalsIgnoreCase(returnType) || "Boolean".equals(returnType);
    }

    public boolean isNumericReturn() {
        return "int".equals(returnType) || "Integer".equals(returnType) ||
                "double".equals(returnType) || "Double".equals(returnType) ||
                "long".equals(returnType) || "Long".equals(returnType) ||
                "float".equals(returnType) || "Float".equals(returnType);
    }

    public boolean isStringReturn() {
        return "String".equals(returnType) || "java.lang.String".equals(returnType);
    }

    public boolean isOptionalReturn() {
        return returnType.startsWith("Optional") || returnType.startsWith("java.util.Optional");
    }

    public boolean isGetter() {
        return (methodName.startsWith("get") || methodName.startsWith("is")) && parameters.isEmpty() && !isVoid();
    }

    public boolean isSetter() {
        return methodName.startsWith("set") && parameters.size() == 1;
    }

    public String getSignature() {
        String paramsStr = parameters.stream()
                .map(ParameterInfo::getType)
                .collect(Collectors.joining(", "));
        return methodName + "(" + paramsStr + ")";
    }

    public String getFullSignature() {
        return returnType + " " + getSignature();
    }

    @Override
    public String toString() {
        return getFullSignature();
    }
}
