package com.cbp.testgen.optimizer;

import com.cbp.testgen.generator.JUnit5TestGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class RegressionPrioritizer {

    private static final Logger logger = LoggerFactory.getLogger(RegressionPrioritizer.class);

    public static class PrioritizedTestCase {
        private final JUnit5TestGenerator.GeneratedTestCaseData testCase;
        private final double priorityScore;
        private final boolean touchesChangedMethod;
        private final int historicalFailures;
        private final long avgExecutionTimeMs;
        private final String priorityReason;

        public PrioritizedTestCase(JUnit5TestGenerator.GeneratedTestCaseData testCase,
                                   double priorityScore, boolean touchesChangedMethod,
                                   int historicalFailures, long avgExecutionTimeMs, String priorityReason) {
            this.testCase = testCase;
            this.priorityScore = priorityScore;
            this.touchesChangedMethod = touchesChangedMethod;
            this.historicalFailures = historicalFailures;
            this.avgExecutionTimeMs = avgExecutionTimeMs;
            this.priorityReason = priorityReason;
        }

        public JUnit5TestGenerator.GeneratedTestCaseData getTestCase() {
            return testCase;
        }

        public double getPriorityScore() {
            return priorityScore;
        }

        public boolean isTouchesChangedMethod() {
            return touchesChangedMethod;
        }

        public int getHistoricalFailures() {
            return historicalFailures;
        }

        public long getAvgExecutionTimeMs() {
            return avgExecutionTimeMs;
        }

        public String getPriorityReason() {
            return priorityReason;
        }
    }

    public static class PrioritizationReport {
        private final List<PrioritizedTestCase> prioritizedTests;
        private final Set<String> changedMethods;
        private final int highPriorityCount;
        private final String summary;

        public PrioritizationReport(List<PrioritizedTestCase> prioritizedTests, Set<String> changedMethods,
                                    int highPriorityCount, String summary) {
            this.prioritizedTests = prioritizedTests;
            this.changedMethods = changedMethods;
            this.highPriorityCount = highPriorityCount;
            this.summary = summary;
        }

        public List<PrioritizedTestCase> getPrioritizedTests() {
            return prioritizedTests;
        }

        public Set<String> getChangedMethods() {
            return changedMethods;
        }

        public int getHighPriorityCount() {
            return highPriorityCount;
        }

        public String getSummary() {
            return summary;
        }
    }

    public PrioritizationReport prioritize(List<JUnit5TestGenerator.GeneratedTestCaseData> testCases,
                                           Set<String> changedMethods,
                                           Map<String, Integer> failureCounts,
                                           Map<String, Long> executionTimes) {
        List<PrioritizedTestCase> list = new ArrayList<>();

        for (JUnit5TestGenerator.GeneratedTestCaseData tc : testCases) {
            boolean touchesChanged = changedMethods != null && changedMethods.contains(tc.getMethodName());
            int pastFails = failureCounts != null ? failureCounts.getOrDefault(tc.getTestName(), 0) : 0;
            long execTime = executionTimes != null ? executionTimes.getOrDefault(tc.getTestName(), 10L) : 10L;

            double score = 0.0;
            StringBuilder reason = new StringBuilder();

            if (touchesChanged) {
                score += 100.0;
                reason.append("Directly tests modified method [").append(tc.getMethodName()).append("]. ");
            }

            if (pastFails > 0) {
                score += (pastFails * 15.0);
                reason.append("History: ").append(pastFails).append(" past failures. ");
            }

            // Speed factor: favor faster tests for early feedback
            double speedScore = Math.min(20.0, 1000.0 / (execTime + 1.0));
            score += speedScore;

            if (reason.length() == 0) {
                reason.append("Nominal baseline regression test.");
            }

            list.add(new PrioritizedTestCase(tc, score, touchesChanged, pastFails, execTime, reason.toString().trim()));
        }

        // Sort descending by priority score
        list.sort(Comparator.comparingDouble(PrioritizedTestCase::getPriorityScore).reversed());

        int highPriorityCount = (int) list.stream().filter(PrioritizedTestCase::isTouchesChangedMethod).count();
        String summary = String.format("Prioritized %d tests (%d high-priority for modified methods: %s)",
                list.size(), highPriorityCount, changedMethods != null ? changedMethods : "none");

        logger.info(summary);
        return new PrioritizationReport(list, changedMethods != null ? changedMethods : Collections.emptySet(), highPriorityCount, summary);
    }
}
