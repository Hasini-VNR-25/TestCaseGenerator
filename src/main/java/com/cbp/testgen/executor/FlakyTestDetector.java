package com.cbp.testgen.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FlakyTestDetector {

    private static final Logger logger = LoggerFactory.getLogger(FlakyTestDetector.class);
    private final TestExecutor testExecutor;

    public FlakyTestDetector(TestExecutor testExecutor) {
        this.testExecutor = testExecutor;
    }

    public static class FlakyTestReport {
        private final String testName;
        private final int totalRuns;
        private final int passCount;
        private final int failCount;
        private final double passRatePct;
        private final int inconsistencyCount;

        public FlakyTestReport(String testName, int totalRuns, int passCount, int failCount,
                               double passRatePct, int inconsistencyCount) {
            this.testName = testName;
            this.totalRuns = totalRuns;
            this.passCount = passCount;
            this.failCount = failCount;
            this.passRatePct = passRatePct;
            this.inconsistencyCount = inconsistencyCount;
        }

        public String getTestName() {
            return testName;
        }

        public int getTotalRuns() {
            return totalRuns;
        }

        public int getPassCount() {
            return passCount;
        }

        public int getFailCount() {
            return failCount;
        }

        public double getPassRatePct() {
            return passRatePct;
        }

        public int getInconsistencyCount() {
            return inconsistencyCount;
        }

        public boolean isFlaky() {
            return passCount > 0 && failCount > 0;
        }
    }

    public List<FlakyTestReport> detectFlakyTests(Class<?> testClass, ClassLoader classLoader, int iterations) {
        if (iterations < 2) {
            iterations = 3;
        }

        Map<String, Integer> passCounts = new HashMap<>();
        Map<String, Integer> failCounts = new HashMap<>();

        for (int i = 0; i < iterations; i++) {
            TestExecutor.TestExecutionSummary summary = testExecutor.executeTestClass(testClass, classLoader);
            for (TestExecutor.SingleTestResult r : summary.getResults()) {
                if (r.isPassed()) {
                    passCounts.put(r.getTestName(), passCounts.getOrDefault(r.getTestName(), 0) + 1);
                } else {
                    failCounts.put(r.getTestName(), failCounts.getOrDefault(r.getTestName(), 0) + 1);
                }
            }
        }

        List<FlakyTestReport> reports = new ArrayList<>();
        for (String testName : passCounts.keySet()) {
            int passes = passCounts.getOrDefault(testName, 0);
            int fails = failCounts.getOrDefault(testName, 0);
            int total = passes + fails;
            double passRate = (double) passes / total * 100.0;
            int inconsistencies = Math.min(passes, fails);

            FlakyTestReport report = new FlakyTestReport(testName, total, passes, fails, passRate, inconsistencies);
            if (report.isFlaky()) {
                logger.warn("Flaky test detected: {} (Pass: {}, Fail: {})", testName, passes, fails);
            }
            reports.add(report);
        }

        return reports;
    }
}
