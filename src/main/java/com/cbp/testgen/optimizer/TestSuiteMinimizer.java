package com.cbp.testgen.optimizer;

import com.cbp.testgen.generator.JUnit5TestGenerator;
import com.cbp.testgen.mutation.Mutant;
import com.cbp.testgen.mutation.MutationResultSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Test Suite Minimization using the Greedy Set-Cover Algorithm.
 * Selects the minimal subset of test cases that achieves 100% of the original test goals
 * (methods covered, boundary partitions, and mutants killed).
 */
@Component
public class TestSuiteMinimizer {

    private static final Logger logger = LoggerFactory.getLogger(TestSuiteMinimizer.class);

    public static class MinimizationReport {
        private final int initialTestCount;
        private final int minimizedTestCount;
        private final double reductionPercentage;
        private final List<JUnit5TestGenerator.GeneratedTestCaseData> selectedTestCases;
        private final List<JUnit5TestGenerator.GeneratedTestCaseData> redundantTestCases;
        private final Set<String> totalGoalsCovered;

        public MinimizationReport(int initialTestCount, int minimizedTestCount, double reductionPercentage,
                                  List<JUnit5TestGenerator.GeneratedTestCaseData> selectedTestCases,
                                  List<JUnit5TestGenerator.GeneratedTestCaseData> redundantTestCases,
                                  Set<String> totalGoalsCovered) {
            this.initialTestCount = initialTestCount;
            this.minimizedTestCount = minimizedTestCount;
            this.reductionPercentage = reductionPercentage;
            this.selectedTestCases = selectedTestCases;
            this.redundantTestCases = redundantTestCases;
            this.totalGoalsCovered = totalGoalsCovered;
        }

        public int getInitialTestCount() {
            return initialTestCount;
        }

        public int getMinimizedTestCount() {
            return minimizedTestCount;
        }

        public double getReductionPercentage() {
            return reductionPercentage;
        }

        public List<JUnit5TestGenerator.GeneratedTestCaseData> getSelectedTestCases() {
            return selectedTestCases;
        }

        public List<JUnit5TestGenerator.GeneratedTestCaseData> getRedundantTestCases() {
            return redundantTestCases;
        }

        public Set<String> getTotalGoalsCovered() {
            return totalGoalsCovered;
        }
    }

    public MinimizationReport minimizeTestSuite(List<JUnit5TestGenerator.GeneratedTestCaseData> allTests,
                                               MutationResultSummary mutationSummary) {
        if (allTests == null || allTests.isEmpty()) {
            return new MinimizationReport(0, 0, 0.0, Collections.emptyList(), Collections.emptyList(), Collections.emptySet());
        }

        // 1. Build coverage goals for each test case
        Map<String, Set<String>> testToGoalsMap = new HashMap<>();
        Set<String> universe = new HashSet<>();

        for (JUnit5TestGenerator.GeneratedTestCaseData tc : allTests) {
            Set<String> goals = new HashSet<>();
            // Goal 1: Method exercised
            goals.add("METHOD:" + tc.getMethodName());
            // Goal 2: Test Type/Category exercised
            goals.add("TYPE:" + tc.getTestType() + "@" + tc.getMethodName());

            // Goal 3: Mutants killed by this test
            if (mutationSummary != null) {
                for (Mutant m : mutationSummary.getMutants()) {
                    if (m.isKilled() && tc.getTestName().equals(m.getKillingTestName())) {
                        goals.add("MUTANT:" + m.getId());
                    }
                }
            }

            testToGoalsMap.put(tc.getTestName(), goals);
            universe.addAll(goals);
        }

        // 2. Greedy Set-Cover Algorithm
        Set<String> uncoveredGoals = new HashSet<>(universe);
        List<JUnit5TestGenerator.GeneratedTestCaseData> selectedTests = new ArrayList<>();
        Set<String> selectedNames = new HashSet<>();

        Map<String, JUnit5TestGenerator.GeneratedTestCaseData> testLookup = new HashMap<>();
        for (JUnit5TestGenerator.GeneratedTestCaseData tc : allTests) {
            testLookup.put(tc.getTestName(), tc);
        }

        while (!uncoveredGoals.isEmpty()) {
            JUnit5TestGenerator.GeneratedTestCaseData bestCandidate = null;
            int maxGain = -1;
            Set<String> bestCoveredSubset = Collections.emptySet();

            for (JUnit5TestGenerator.GeneratedTestCaseData tc : allTests) {
                if (selectedNames.contains(tc.getTestName())) continue;

                Set<String> candidateGoals = new HashSet<>(testToGoalsMap.getOrDefault(tc.getTestName(), Collections.emptySet()));
                candidateGoals.retainAll(uncoveredGoals);

                if (candidateGoals.size() > maxGain) {
                    maxGain = candidateGoals.size();
                    bestCandidate = tc;
                    bestCoveredSubset = candidateGoals;
                }
            }

            if (bestCandidate == null || maxGain <= 0) {
                // All remaining uncovered goals cannot be covered by any remaining test
                break;
            }

            selectedTests.add(bestCandidate);
            selectedNames.add(bestCandidate.getTestName());
            uncoveredGoals.removeAll(bestCoveredSubset);
        }

        List<JUnit5TestGenerator.GeneratedTestCaseData> redundantTests = new ArrayList<>();
        for (JUnit5TestGenerator.GeneratedTestCaseData tc : allTests) {
            if (!selectedNames.contains(tc.getTestName())) {
                redundantTests.add(tc);
            }
        }

        int initialCount = allTests.size();
        int minCount = selectedTests.size();
        double reductionPct = initialCount > 0
                ? Math.round(((double) (initialCount - minCount) / initialCount * 100.0) * 10.0) / 10.0
                : 0.0;

        logger.info("Test Suite Minimization (Greedy Set-Cover): {} -> {} tests ({}% reduction)",
                initialCount, minCount, reductionPct);

        return new MinimizationReport(initialCount, minCount, reductionPct, selectedTests, redundantTests, universe);
    }
}
