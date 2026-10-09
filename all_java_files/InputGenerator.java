package com.cbp.testgen.generator;

import com.cbp.testgen.analyzer.model.MethodInfo;
import com.cbp.testgen.analyzer.model.ParameterInfo;
import com.cbp.testgen.generator.strategy.BoundaryValueStrategy;
import com.cbp.testgen.generator.strategy.EquivalencePartitionStrategy;
import com.cbp.testgen.generator.strategy.InputValue;
import com.cbp.testgen.generator.strategy.PairwiseCombinator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class InputGenerator {

    private final BoundaryValueStrategy boundaryStrategy;
    private final EquivalencePartitionStrategy equivalenceStrategy;
    private final PairwiseCombinator pairwiseCombinator;

    public InputGenerator(BoundaryValueStrategy boundaryStrategy,
                          EquivalencePartitionStrategy equivalenceStrategy,
                          PairwiseCombinator pairwiseCombinator) {
        this.boundaryStrategy = boundaryStrategy;
        this.equivalenceStrategy = equivalenceStrategy;
        this.pairwiseCombinator = pairwiseCombinator;
    }

    public List<List<InputValue>> generateInputVectors(MethodInfo method) {
        List<ParameterInfo> parameters = method.getParameters();
        if (parameters.isEmpty()) {
            return List.of(List.of());
        }

        List<List<InputValue>> paramValueLists = new ArrayList<>();
        for (ParameterInfo param : parameters) {
            Set<InputValue> mergedValues = new LinkedHashSet<>();
            mergedValues.addAll(boundaryStrategy.generateValues(param));
            mergedValues.addAll(equivalenceStrategy.generateValues(param));

            // Deduplicate by expression
            List<InputValue> deduped = new ArrayList<>();
            Set<String> seenExprs = new LinkedHashSet<>();
            for (InputValue iv : mergedValues) {
                if (seenExprs.add(iv.getExpression())) {
                    deduped.add(iv);
                }
            }
            paramValueLists.add(deduped);
        }

        return pairwiseCombinator.generateCombinations(paramValueLists);
    }
}
