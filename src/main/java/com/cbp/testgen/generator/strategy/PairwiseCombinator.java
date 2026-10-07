package com.cbp.testgen.generator.strategy;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Pairwise / All-Pairs Combination Engine.
 * Reduces combinatorial explosion for methods with 3+ parameters by ensuring all 2-way interactions
 * are tested in a minimized set of test tuples.
 */
@Component
public class PairwiseCombinator {

    public List<List<InputValue>> generateCombinations(List<List<InputValue>> parameterValues) {
        if (parameterValues.isEmpty()) {
            return Collections.emptyList();
        }
        if (parameterValues.size() == 1) {
            List<List<InputValue>> result = new ArrayList<>();
            for (InputValue val : parameterValues.get(0)) {
                result.add(List.of(val));
            }
            return result;
        }
        if (parameterValues.size() == 2) {
            // Full cartesian product for 2 parameters
            List<List<InputValue>> result = new ArrayList<>();
            for (InputValue v1 : parameterValues.get(0)) {
                for (InputValue v2 : parameterValues.get(1)) {
                    result.add(List.of(v1, v2));
                }
            }
            return result;
        }

        // For 3+ parameters: Greedy Pairwise Combination (IPOG heuristic)
        return greedyPairwise(parameterValues);
    }

    private List<List<InputValue>> greedyPairwise(List<List<InputValue>> parameterValues) {
        int numParams = parameterValues.size();
        // Collect all required pairs across all parameter index pairs (p1, p2)
        Set<String> uncoveredPairs = new HashSet<>();
        for (int i = 0; i < numParams; i++) {
            for (int j = i + 1; j < numParams; j++) {
                List<InputValue> valuesI = parameterValues.get(i);
                List<InputValue> valuesJ = parameterValues.get(j);
                for (int vi = 0; vi < valuesI.size(); vi++) {
                    for (int vj = 0; vj < valuesJ.size(); vj++) {
                        uncoveredPairs.add(formatPair(i, vi, j, vj));
                    }
                }
            }
        }

        List<List<InputValue>> testSuite = new ArrayList<>();

        while (!uncoveredPairs.isEmpty()) {
            // Find a test candidate that covers the maximum number of uncovered pairs
            int[] bestTupleIndices = new int[numParams];
            int maxCovered = -1;

            // Generate candidates by iterating through uncovered pairs
            String firstUncovered = uncoveredPairs.iterator().next();
            int[] pairData = parsePair(firstUncovered);
            int p1 = pairData[0];
            int v1 = pairData[1];
            int p2 = pairData[2];
            int v2 = pairData[3];

            int[] candidate = new int[numParams];
            for (int p = 0; p < numParams; p++) {
                candidate[p] = 0; // Default to first value
            }
            candidate[p1] = v1;
            candidate[p2] = v2;

            // Greedily choose best value for remaining parameters
            for (int p = 0; p < numParams; p++) {
                if (p == p1 || p == p2) continue;
                int bestVal = 0;
                int maxGain = -1;
                for (int v = 0; v < parameterValues.get(p).size(); v++) {
                    candidate[p] = v;
                    int gain = countCoveredPairs(candidate, uncoveredPairs, numParams);
                    if (gain > maxGain) {
                        maxGain = gain;
                        bestVal = v;
                    }
                }
                candidate[p] = bestVal;
            }

            // Remove covered pairs
            removeCoveredPairs(candidate, uncoveredPairs, numParams);

            // Construct InputValue list
            List<InputValue> tuple = new ArrayList<>();
            for (int p = 0; p < numParams; p++) {
                tuple.add(parameterValues.get(p).get(candidate[p]));
            }
            testSuite.add(tuple);

            if (testSuite.size() > 100) {
                // Safety bound to avoid runaway generation
                break;
            }
        }

        return testSuite;
    }

    private int countCoveredPairs(int[] tuple, Set<String> uncoveredPairs, int numParams) {
        int count = 0;
        for (int i = 0; i < numParams; i++) {
            for (int j = i + 1; j < numParams; j++) {
                if (uncoveredPairs.contains(formatPair(i, tuple[i], j, tuple[j]))) {
                    count++;
                }
            }
        }
        return count;
    }

    private void removeCoveredPairs(int[] tuple, Set<String> uncoveredPairs, int numParams) {
        for (int i = 0; i < numParams; i++) {
            for (int j = i + 1; j < numParams; j++) {
                uncoveredPairs.remove(formatPair(i, tuple[i], j, tuple[j]));
            }
        }
    }

    private String formatPair(int p1, int v1, int p2, int v2) {
        return p1 + ":" + v1 + "|" + p2 + ":" + v2;
    }

    private int[] parsePair(String pairStr) {
        String[] parts = pairStr.split("\\|");
        String[] left = parts[0].split(":");
        String[] right = parts[1].split(":");
        return new int[]{
                Integer.parseInt(left[0]),
                Integer.parseInt(left[1]),
                Integer.parseInt(right[0]),
                Integer.parseInt(right[1])
        };
    }
}
