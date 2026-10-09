package com.cbp.testgen;

import com.cbp.testgen.analyzer.AstDiffAnalyzer;
import com.cbp.testgen.analyzer.CodeAnalyzer;
import com.cbp.testgen.analyzer.model.ClassInfo;
import com.cbp.testgen.generator.JUnit5TestGenerator;
import com.cbp.testgen.mutation.Mutant;
import com.cbp.testgen.mutation.MutationOperator;
import com.cbp.testgen.mutation.MutationResultSummary;
import com.cbp.testgen.optimizer.RegressionPrioritizer;
import com.cbp.testgen.optimizer.TestSuiteMinimizer;
import com.cbp.testgen.service.PipelineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Dedicated test validating Key Takeaway #3:
 * "Smart Regression over Brute Force: AST diff and greedy set-cover identify what changed
 * and re-run only the minimal, highest-priority tests first."
 */
@SpringBootTest
public class SmartRegressionTakeawayTest {

    @Autowired
    private CodeAnalyzer codeAnalyzer;

    @Autowired
    private AstDiffAnalyzer astDiffAnalyzer;

    @Autowired
    private RegressionPrioritizer regressionPrioritizer;

    @Autowired
    private TestSuiteMinimizer testSuiteMinimizer;

    @Autowired
    private JUnit5TestGenerator testGenerator;

    @Autowired
    private PipelineService pipelineService;

    // --- Version 1: Original Calculator ---
    private static final String CALCULATOR_V1 = """
        package samples;

        public class Calculator {
            public int add(int a, int b) {
                return a + b;
            }

            public int subtract(int a, int b) {
                return a - b;
            }

            public int multiply(int a, int b) {
                return a * b;
            }
        }
        """;

    // --- Version 2: Modified Calculator (only add() has changed business logic) ---
    private static final String CALCULATOR_V2_MODIFIED_ADD = """
        package samples;

        public class Calculator {
            public int add(int a, int b) {
                // Bugfix / change: added range validation
                if (a > 10000 || b > 10000) {
                    throw new IllegalArgumentException("Inputs too large");
                }
                return a + b;
            }

            public int subtract(int a, int b) {
                return a - b;
            }

            public int multiply(int a, int b) {
                return a * b;
            }
        }
        """;

    @Test
    @DisplayName("Takeaway 3 - Part A: AST Diff Detects Modified Method and Ranks Them as Top Priority")
    void testAstDiffAndRegressionPrioritizer() {
        // Step 1: Parse AST for Version 1 and Version 2
        ClassInfo classV1 = codeAnalyzer.analyzeSourceCode(CALCULATOR_V1);
        ClassInfo classV2 = codeAnalyzer.analyzeSourceCode(CALCULATOR_V2_MODIFIED_ADD);

        assertNotNull(classV1);
        assertNotNull(classV2);

        // Step 2: Compare versions using AST Diff Analyzer
        AstDiffAnalyzer.DiffReport diffReport = astDiffAnalyzer.compareVersions(classV1, classV2);

        assertTrue(diffReport.isHasChanges(), "AST diff must detect changes between v1 and v2");
        assertEquals(1, diffReport.getModifiedMethodNames().size(), "Only one method was modified");
        assertTrue(diffReport.getModifiedMethodNames().contains("add"), "Modified method must be 'add'");
        assertFalse(diffReport.getModifiedMethodNames().contains("subtract"), "'subtract' was untouched");
        assertFalse(diffReport.getModifiedMethodNames().contains("multiply"), "'multiply' was untouched");

        // Step 3: Generate tests for class V2
        JUnit5TestGenerator.GeneratedTestSuite suite = testGenerator.generateTestSuite(classV2);
        assertFalse(suite.getTestCases().isEmpty(), "Generated test cases must not be empty");

        // Step 4: Prioritize regression tests based on touched methods
        Map<String, Integer> failureHistory = Map.of("add_v1_test", 2);
        Map<String, Long> executionTimes = Map.of("add_v1_test", 5L, "subtract_default", 50L);

        RegressionPrioritizer.PrioritizationReport prioReport = regressionPrioritizer.prioritize(
                suite.getTestCases(),
                diffReport.getModifiedMethodNames(),
                failureHistory,
                executionTimes
        );

        assertNotNull(prioReport);
        assertTrue(prioReport.getHighPriorityCount() > 0, "There must be high-priority tests for modified method 'add'");

        // Step 5: Assert that the top-ranked test directly exercises 'add' with priority >= 100.0
        RegressionPrioritizer.PrioritizedTestCase topTest = prioReport.getPrioritizedTests().get(0);
        assertTrue(topTest.isTouchesChangedMethod(), "Top test must touch the changed method 'add'");
        assertEquals("add", topTest.getTestCase().getMethodName(), "Top priority test must target method 'add'");
        assertTrue(topTest.getPriorityScore() >= 100.0, "Priority score for changed method must be >= 100 (base bonus)");

        System.out.println("✅ [VERIFIED] AST Diff detected modified method: " + diffReport.getModifiedMethodNames());
        System.out.println("✅ [VERIFIED] Top prioritized test: " + topTest.getTestCase().getTestName() +
                " (Score: " + topTest.getPriorityScore() + ", Reason: " + topTest.getPriorityReason() + ")");
    }

    @Test
    @DisplayName("Takeaway 3 - Part B: Greedy Set-Cover Minimizer Eliminates Redundant Tests While Preserving Goals")
    void testGreedySetCoverMinimization() {
        // Step 1: Parse and generate a full suite of tests
        ClassInfo classInfo = codeAnalyzer.analyzeSourceCode(CALCULATOR_V1);
        JUnit5TestGenerator.GeneratedTestSuite suite = testGenerator.generateTestSuite(classInfo);
        List<JUnit5TestGenerator.GeneratedTestCaseData> allTests = suite.getTestCases();
        assertTrue(allTests.size() > 3, "Initial test count should be multiple tests");

        // Step 2: Create a simulated mutation summary with 3 mutants killed by distinct tests
        List<Mutant> mutants = new ArrayList<>();
        Mutant m1 = new Mutant("MUT_1", "add", 6, MutationOperator.ARITHMETIC_OPERATOR_SWAP, "+", "-", "");
        m1.setStatus(Mutant.Status.KILLED);
        m1.setKillingTestName(allTests.get(0).getTestName());

        Mutant m2 = new Mutant("MUT_2", "subtract", 10, MutationOperator.ARITHMETIC_OPERATOR_SWAP, "-", "+", "");
        m2.setStatus(Mutant.Status.KILLED);
        m2.setKillingTestName(allTests.get(1).getTestName());

        Mutant m3 = new Mutant("MUT_3", "multiply", 14, MutationOperator.ARITHMETIC_OPERATOR_SWAP, "*", "/", "");
        m3.setStatus(Mutant.Status.KILLED);
        m3.setKillingTestName(allTests.get(2).getTestName());

        mutants.add(m1);
        mutants.add(m2);
        mutants.add(m3);

        MutationResultSummary mutationSummary = new MutationResultSummary(
                "Calculator", mutants.size(), 3, 0, 100.0, mutants, Collections.emptyMap()
        );

        // Step 3: Run Greedy Set-Cover Minimization
        TestSuiteMinimizer.MinimizationReport minReport = testSuiteMinimizer.minimizeTestSuite(allTests, mutationSummary);

        assertNotNull(minReport);
        assertTrue(minReport.getMinimizedTestCount() <= minReport.getInitialTestCount(),
                "Minimized count must be <= initial count");
        assertEquals(allTests.size(), minReport.getInitialTestCount());
        assertEquals(minReport.getSelectedTestCases().size(), minReport.getMinimizedTestCount());
        assertEquals(minReport.getRedundantTestCases().size(),
                minReport.getInitialTestCount() - minReport.getMinimizedTestCount());

        // Step 4: Verify goal preservation (all 3 methods and all 3 mutants must be covered)
        Set<String> coveredGoals = minReport.getTotalGoalsCovered();
        assertTrue(coveredGoals.contains("METHOD:add"), "Goal METHOD:add must be covered");
        assertTrue(coveredGoals.contains("METHOD:subtract"), "Goal METHOD:subtract must be covered");
        assertTrue(coveredGoals.contains("METHOD:multiply"), "Goal METHOD:multiply must be covered");
        assertTrue(coveredGoals.contains("MUTANT:MUT_1"), "Goal MUTANT:MUT_1 must be covered");
        assertTrue(coveredGoals.contains("MUTANT:MUT_2"), "Goal MUTANT:MUT_2 must be covered");
        assertTrue(coveredGoals.contains("MUTANT:MUT_3"), "Goal MUTANT:MUT_3 must be covered");

        System.out.println("✅ [VERIFIED] Greedy Set-Cover reduced suite from " +
                minReport.getInitialTestCount() + " to " + minReport.getMinimizedTestCount() +
                " tests (" + minReport.getReductionPercentage() + "% reduction)");
        System.out.println("✅ [VERIFIED] Redundant tests safely pruned: " + minReport.getRedundantTestCases().size());
        System.out.println("✅ [VERIFIED] Total goals covered preserved: " + coveredGoals.size());
    }

    @Test
    @DisplayName("Takeaway 3 - Part C: Full Pipeline End-to-End Regression Verification")
    void testEndToEndPipelineRegression() throws Exception {
        // Run v1 through pipeline
        PipelineService.PipelineExecutionResult resV1 = pipelineService.runPipelineForSource(
                "RegressionTakeawayProject",
                CALCULATOR_V1,
                msg -> {}
        );
        assertNotNull(resV1);
        assertNotNull(resV1.getMinimizationReport());
        assertTrue(resV1.getMinimizationReport().getMinimizedTestCount() > 0);

        // Run v2 (with modified add method) through pipeline for the same project
        PipelineService.PipelineExecutionResult resV2 = pipelineService.runPipelineForSource(
                "RegressionTakeawayProject",
                CALCULATOR_V2_MODIFIED_ADD,
                msg -> {}
        );
        assertNotNull(resV2);
        assertNotNull(resV2.getPrioritizationReport());

        // Check prioritization in v2 identified modified method
        assertTrue(resV2.getPrioritizationReport().getHighPriorityCount() > 0,
                "Pipeline must report high priority tests for modified methods");
        assertTrue(resV2.getPrioritizationReport().getChangedMethods().contains("add"),
                "Changed methods in pipeline report must contain 'add'");

        System.out.println("✅ [VERIFIED] Pipeline produced full regression report: " +
                resV2.getPrioritizationReport().getSummary());
    }
}
