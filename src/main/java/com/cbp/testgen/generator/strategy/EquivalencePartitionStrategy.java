package com.cbp.testgen.generator.strategy;

import com.cbp.testgen.analyzer.model.ParameterInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete Strategy implementing Equivalence Class Partitioning (ECP).
 * Partitions the input domain into valid, invalid-low, and invalid-high equivalence classes.
 */
@Component
public class EquivalencePartitionStrategy implements InputGenerationStrategy {

    @Override
    public String getStrategyName() {
        return "Equivalence Class Partitioning (ECP)";
    }

    @Override
    public boolean supports(ParameterInfo parameter) {
        return true;
    }

    @Override
    public List<InputValue> generateValues(ParameterInfo param) {
        List<InputValue> values = new ArrayList<>();

        if (param.isInt()) {
            values.add(new InputValue("10", InputValue.Category.EQUIVALENCE_VALID, "Valid nominal integer (ECP valid class)"));
            values.add(new InputValue("100", InputValue.Category.EQUIVALENCE_VALID, "Valid large integer (ECP valid class)"));
            values.add(new InputValue("-50", InputValue.Category.EQUIVALENCE_INVALID, "Negative integer (ECP invalid-low class)"));
        } else if (param.isLong()) {
            values.add(new InputValue("1000L", InputValue.Category.EQUIVALENCE_VALID, "Valid nominal long (ECP valid class)"));
            values.add(new InputValue("-500L", InputValue.Category.EQUIVALENCE_INVALID, "Negative long (ECP invalid-low class)"));
        } else if (param.isDouble()) {
            values.add(new InputValue("50.0", InputValue.Category.EQUIVALENCE_VALID, "Valid positive double (ECP valid class)"));
            values.add(new InputValue("100.5", InputValue.Category.EQUIVALENCE_VALID, "Valid fractional double"));
            values.add(new InputValue("-25.5", InputValue.Category.EQUIVALENCE_INVALID, "Negative double (ECP invalid-low class)"));
        } else if (param.isFloat()) {
            values.add(new InputValue("25.5f", InputValue.Category.EQUIVALENCE_VALID, "Valid positive float"));
            values.add(new InputValue("-10.0f", InputValue.Category.EQUIVALENCE_INVALID, "Negative float"));
        } else if (param.isString()) {
            values.add(new InputValue("\"test_valid_string\"", InputValue.Category.EQUIVALENCE_VALID, "Valid alphanumeric string"));
            values.add(new InputValue("\"   \"", InputValue.Category.EQUIVALENCE_INVALID, "Whitespace-only string"));
            values.add(new InputValue("\"Special_!@#$%^&*()\"", InputValue.Category.EQUIVALENCE_VALID, "Special characters string"));
        } else if (param.isBoolean()) {
            values.add(new InputValue("true", InputValue.Category.EQUIVALENCE_VALID, "Boolean valid true"));
            values.add(new InputValue("false", InputValue.Category.EQUIVALENCE_VALID, "Boolean valid false"));
        } else if (param.isCollection()) {
            values.add(new InputValue("java.util.List.of(\"item1\", \"item2\")", InputValue.Category.EQUIVALENCE_VALID, "Multi-element list"));
        }

        return values;
    }
}
