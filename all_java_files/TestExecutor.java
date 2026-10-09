package com.cbp.testgen.executor;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TestExecutor {

    private static final Logger logger = LoggerFactory.getLogger(TestExecutor.class);

    public static class SingleTestResult {
        private final String testName;
        private final String displayName;
        private final String status; // "PASS", "FAIL", "ERROR"
        private final long executionTimeMs;
        private final String stackTrace;
        private final String errorMessage;

        public SingleTestResult(String testName, String displayName, String status,
                                long executionTimeMs, String stackTrace, String errorMessage) {
            this.testName = testName;
            this.displayName = displayName;
            this.status = status;
            this.executionTimeMs = executionTimeMs;
            this.stackTrace = stackTrace;
            this.errorMessage = errorMessage;
        }

        public String getTestName() {
            return testName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getStatus() {
            return status;
        }

        public boolean isPassed() {
            return "PASS".equalsIgnoreCase(status);
        }

        public long getExecutionTimeMs() {
            return executionTimeMs;
        }

        public String getStackTrace() {
            return stackTrace;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public static class TestExecutionSummary {
        private final int totalCount;
        private final int passedCount;
        private final int failedCount;
        private final int errorCount;
        private final long totalTimeMs;
        private final List<SingleTestResult> results;

        public TestExecutionSummary(int totalCount, int passedCount, int failedCount,
                                    int errorCount, long totalTimeMs, List<SingleTestResult> results) {
            this.totalCount = totalCount;
            this.passedCount = passedCount;
            this.failedCount = failedCount;
            this.errorCount = errorCount;
            this.totalTimeMs = totalTimeMs;
            this.results = results != null ? results : Collections.emptyList();
        }

        public int getTotalCount() {
            return totalCount;
        }

        public int getPassedCount() {
            return passedCount;
        }

        public int getFailedCount() {
            return failedCount;
        }

        public int getErrorCount() {
            return errorCount;
        }

        public long getTotalTimeMs() {
            return totalTimeMs;
        }

        public List<SingleTestResult> getResults() {
            return results;
        }
    }

    public TestExecutionSummary executeTestClass(Class<?> testClass, ClassLoader classLoader) {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);

            LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                    .selectors(DiscoverySelectors.selectClass(testClass))
                    .build();

            Launcher launcher = LauncherFactory.create();
            TestPlan testPlan = launcher.discover(request);

            List<SingleTestResult> results = new ArrayList<>();
            Map<String, Long> startTimes = new ConcurrentHashMap<>();
            long suiteStartTime = System.currentTimeMillis();

            launcher.registerTestExecutionListeners(new TestExecutionListener() {
                @Override
                public void executionStarted(TestIdentifier testIdentifier) {
                    if (testIdentifier.isTest()) {
                        startTimes.put(testIdentifier.getUniqueId(), System.currentTimeMillis());
                    }
                }

                @Override
                public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
                    if (testIdentifier.isTest()) {
                        long start = startTimes.getOrDefault(testIdentifier.getUniqueId(), System.currentTimeMillis());
                        long duration = System.currentTimeMillis() - start;

                        String status = "PASS";
                        String stackTrace = null;
                        String errorMessage = null;

                        if (testExecutionResult.getStatus() == TestExecutionResult.Status.FAILED) {
                            status = "FAIL";
                            if (testExecutionResult.getThrowable().isPresent()) {
                                Throwable t = testExecutionResult.getThrowable().get();
                                errorMessage = t.getMessage();
                                StringWriter sw = new StringWriter();
                                t.printStackTrace(new PrintWriter(sw));
                                stackTrace = sw.toString();
                            }
                        } else if (testExecutionResult.getStatus() == TestExecutionResult.Status.ABORTED) {
                            status = "ERROR";
                        }

                        String testMethodName = testIdentifier.getLegacyReportingName();
                        if (testMethodName.endsWith("()")) {
                            testMethodName = testMethodName.substring(0, testMethodName.length() - 2);
                        }

                        results.add(new SingleTestResult(
                                testMethodName,
                                testIdentifier.getDisplayName(),
                                status,
                                duration,
                                stackTrace,
                                errorMessage
                        ));
                    }
                }
            });

            launcher.execute(testPlan);
            long totalSuiteTime = System.currentTimeMillis() - suiteStartTime;

            int total = results.size();
            int passed = (int) results.stream().filter(r -> "PASS".equals(r.getStatus())).count();
            int failed = (int) results.stream().filter(r -> "FAIL".equals(r.getStatus())).count();
            int errors = (int) results.stream().filter(r -> "ERROR".equals(r.getStatus())).count();

            logger.info("Executed {} tests: {} passed, {} failed, {} errors in {}ms",
                    total, passed, failed, errors, totalSuiteTime);

            return new TestExecutionSummary(total, passed, failed, errors, totalSuiteTime, results);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }
}
