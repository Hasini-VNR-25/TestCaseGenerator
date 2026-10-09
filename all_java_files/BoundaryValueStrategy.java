package com.cbp.testgen.generator.strategy;

import com.cbp.testgen.analyzer.model.ParameterInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete Strategy implementing Boundary Value Analysis (BVA).
 * Generates extreme, boundary, zero, and edge values for primitives and reference types.
 */
@Component
public class BoundaryValueStrategy implements InputGenerationStrategy {

    @Override
    public String getStrategyName() {
        return "Boundary Value Analysis (BVA)";
    }

    @Override
    public boolean supports(ParameterInfo parameter) {
        return true;
    }

    @Override
    public List<InputValue> generateValues(ParameterInfo param) {
        List<InputValue> values = new ArrayList<>();
        String type = param.getType();

        if (param.isInt()) {
            values.add(new InputValue("0", InputValue.Category.BOUNDARY, "Zero boundary"));
            values.add(new InputValue("1", InputValue.Category.BOUNDARY, "Small positive boundary"));
            values.add(new InputValue("-1", InputValue.Category.BOUNDARY, "Small negative boundary"));
            values.add(new InputValue("Integer.MAX_VALUE", InputValue.Category.BOUNDARY, "Upper int boundary"));
            values.add(new InputValue("Integer.MIN_VALUE", InputValue.Category.BOUNDARY, "Lower int boundary"));
        } else if (param.isLong()) {
            values.add(new InputValue("0L", InputValue.Category.BOUNDARY, "Zero long boundary"));
            values.add(new InputValue("1L", InputValue.Category.BOUNDARY, "Small positive long"));
            values.add(new InputValue("-1L", InputValue.Category.BOUNDARY, "Small negative long"));
            values.add(new InputValue("Long.MAX_VALUE", InputValue.Category.BOUNDARY, "Upper long boundary"));
            values.add(new InputValue("Long.MIN_VALUE", InputValue.Category.BOUNDARY, "Lower long boundary"));
        } else if (param.isDouble()) {
            values.add(new InputValue("0.0", InputValue.Category.BOUNDARY, "Zero double boundary"));
            values.add(new InputValue("1.0", InputValue.Category.BOUNDARY, "Positive unit boundary"));
            values.add(new InputValue("-1.0", InputValue.Category.BOUNDARY, "Negative unit boundary"));
            values.add(new InputValue("Double.MAX_VALUE", InputValue.Category.BOUNDARY, "Upper double boundary"));
            values.add(new InputValue("Double.MIN_VALUE", InputValue.Category.BOUNDARY, "Smallest positive non-zero double"));
        } else if (param.isFloat()) {
            values.add(new InputValue("0.0f", InputValue.Category.BOUNDARY, "Zero float boundary"));
            values.add(new InputValue("1.0f", InputValue.Category.BOUNDARY, "Positive unit float"));
            values.add(new InputValue("-1.0f", InputValue.Category.BOUNDARY, "Negative unit float"));
            values.add(new InputValue("Float.MAX_VALUE", InputValue.Category.BOUNDARY, "Upper float boundary"));
        } else if (param.isShort()) {
            values.add(new InputValue("(short) 0", InputValue.Category.BOUNDARY, "Zero short"));
            values.add(new InputValue("Short.MAX_VALUE", InputValue.Category.BOUNDARY, "Upper short"));
            values.add(new InputValue("Short.MIN_VALUE", InputValue.Category.BOUNDARY, "Lower short"));
        } else if (param.isByte()) {
            values.add(new InputValue("(byte) 0", InputValue.Category.BOUNDARY, "Zero byte"));
            values.add(new InputValue("Byte.MAX_VALUE", InputValue.Category.BOUNDARY, "Upper byte"));
            values.add(new InputValue("Byte.MIN_VALUE", InputValue.Category.BOUNDARY, "Lower byte"));
        } else if (param.isChar()) {
            values.add(new InputValue("'\\0'", InputValue.Category.BOUNDARY, "Null character"));
            values.add(new InputValue("'a'", InputValue.Category.BOUNDARY, "Standard char"));
            values.add(new InputValue("Character.MAX_VALUE", InputValue.Category.BOUNDARY, "Upper char boundary"));
        } else if (param.isBoolean()) {
            values.add(new InputValue("true", InputValue.Category.BOUNDARY, "Boolean true"));
            values.add(new InputValue("false", InputValue.Category.BOUNDARY, "Boolean false"));
        } else if (param.isString()) {
            values.add(new InputValue("\"\"", InputValue.Category.BOUNDARY, "Empty string boundary"));
            values.add(new InputValue("\"a\"", InputValue.Category.BOUNDARY, "Single char string"));
            values.add(new InputValue("\"A\".repeat(256)", InputValue.Category.BOUNDARY, "Large length string (256 chars)"));
            values.add(new InputValue("null", InputValue.Category.BOUNDARY, "Null string reference"));
        } else if (param.isCollection()) {
            values.add(new InputValue("java.util.Collections.emptyList()", InputValue.Category.BOUNDARY, "Empty collection"));
            values.add(new InputValue("null", InputValue.Category.BOUNDARY, "Null collection reference"));
        } else if (param.isMap()) {
            values.add(new InputValue("java.util.Collections.emptyMap()", InputValue.Category.BOUNDARY, "Empty map"));
            values.add(new InputValue("null", InputValue.Category.BOUNDARY, "Null map reference"));
        } else if (param.isArray()) {
            String baseType = type.substring(0, type.length() - 2);
            values.add(new InputValue("new " + baseType + "[0]", InputValue.Category.BOUNDARY, "Empty array"));
            values.add(new InputValue("null", InputValue.Category.BOUNDARY, "Null array reference"));
        } else {
            // General reference object
            values.add(new InputValue("null", InputValue.Category.BOUNDARY, "Null object reference"));
        }

        return values;
    }
}
