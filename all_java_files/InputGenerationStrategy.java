package com.cbp.testgen.generator.strategy;

import com.cbp.testgen.analyzer.model.ParameterInfo;
import java.util.List;

/**
 * Strategy interface demonstrating the Strategy Design Pattern for test input generation.
 */
public interface InputGenerationStrategy {
    String getStrategyName();
    List<InputValue> generateValues(ParameterInfo parameter);
    boolean supports(ParameterInfo parameter);
}
