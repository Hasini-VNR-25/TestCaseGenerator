package com.cbp.testgen.mutation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Mutant {
    public enum Status {
        KILLED,
        SURVIVED,
        COMPILE_ERROR,
        TIMED_OUT
    }

    private String id;
    private String methodName;
    private int lineNumber;
    private MutationOperator operator;
    private String originalSnippet;
    private String mutatedSnippet;
    private String mutatedSourceCode;
    private Status status = Status.SURVIVED;
    private String killingTestName;
    private String executionDetails;

    public Mutant() {
        this.status = Status.SURVIVED;
    }

    public Mutant(String id,
                  String methodName,
                  int lineNumber,
                  MutationOperator operator,
                  String originalSnippet,
                  String mutatedSnippet,
                  String mutatedSourceCode) {
        this(id, methodName, lineNumber, operator, originalSnippet, mutatedSnippet, mutatedSourceCode, Status.SURVIVED, null, null);
    }

    @JsonCreator
    public Mutant(@JsonProperty("id") String id,
                  @JsonProperty("methodName") String methodName,
                  @JsonProperty("lineNumber") int lineNumber,
                  @JsonProperty("operator") MutationOperator operator,
                  @JsonProperty("originalSnippet") String originalSnippet,
                  @JsonProperty("mutatedSnippet") String mutatedSnippet,
                  @JsonProperty("mutatedSourceCode") String mutatedSourceCode,
                  @JsonProperty("status") Status status,
                  @JsonProperty("killingTestName") String killingTestName,
                  @JsonProperty("executionDetails") String executionDetails) {
        this.id = id;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
        this.operator = operator;
        this.originalSnippet = originalSnippet;
        this.mutatedSnippet = mutatedSnippet;
        this.mutatedSourceCode = mutatedSourceCode;
        this.status = status != null ? status : Status.SURVIVED;
        this.killingTestName = killingTestName;
        this.executionDetails = executionDetails;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public MutationOperator getOperator() {
        return operator;
    }

    public void setOperator(MutationOperator operator) {
        this.operator = operator;
    }

    public String getOriginalSnippet() {
        return originalSnippet;
    }

    public void setOriginalSnippet(String originalSnippet) {
        this.originalSnippet = originalSnippet;
    }

    public String getMutatedSnippet() {
        return mutatedSnippet;
    }

    public void setMutatedSnippet(String mutatedSnippet) {
        this.mutatedSnippet = mutatedSnippet;
    }

    public String getMutatedSourceCode() {
        return mutatedSourceCode;
    }

    public void setMutatedSourceCode(String mutatedSourceCode) {
        this.mutatedSourceCode = mutatedSourceCode;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getKillingTestName() {
        return killingTestName;
    }

    public void setKillingTestName(String killingTestName) {
        this.killingTestName = killingTestName;
    }

    public String getExecutionDetails() {
        return executionDetails;
    }

    public void setExecutionDetails(String executionDetails) {
        this.executionDetails = executionDetails;
    }

    @JsonIgnore
    public boolean isKilled() {
        return status == Status.KILLED;
    }

    public void setKilled(boolean killed) {
        if (killed && this.status != Status.KILLED) {
            this.status = Status.KILLED;
        }
    }
}
