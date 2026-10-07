package com.cbp.testgen.generator.strategy;

public class InputValue {
    public enum Category {
        BOUNDARY,
        EQUIVALENCE_VALID,
        EQUIVALENCE_INVALID,
        EXCEPTION_TRIGGER,
        SPECIAL_OBJECT,
        MOCK
    }

    private final String expression;
    private final Category category;
    private final String description;

    public InputValue(String expression, Category category, String description) {
        this.expression = expression;
        this.category = category;
        this.description = description;
    }

    public String getExpression() {
        return expression;
    }

    public Category getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return expression + " (" + category + ": " + description + ")";
    }
}
