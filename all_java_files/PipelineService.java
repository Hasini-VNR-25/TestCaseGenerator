package com.cbp.testgen.service;

import com.cbp.testgen.analyzer.AstDiffAnalyzer;
import com.cbp.testgen.analyzer.CodeAnalyzer;
import com.cbp.testgen.analyzer.model.ClassInfo;
import com.cbp.testgen.coverage.CoverageAnalyzer;
import com.cbp.testgen.database.entity.ClassMetadataEntity;
import com.cbp.testgen.database.entity.MethodMetadataEntity;
import com.cbp.testgen.database.entity.ProjectEntity;
import com.cbp.testgen.database.entity.TestCaseEntity;
import com.cbp.testgen.database.entity.TestRunEntity;
import com.cbp.testgen.database.service.DatabaseManager;
import com.cbp.testgen.executor.DynamicCompiler;
import com.cbp.testgen.executor.FlakyTestDetector;
import com.cbp.testgen.executor.TestExecutor;
import com.cbp.testgen.generator.JUnit5TestGenerator;
import com.cbp.testgen.mutation.MutationEngine;
import com.cbp.testgen.mutation.MutationResultSummary;
import com.cbp.testgen.optimizer.RegressionPrioritizer;
import com.cbp.testgen.optimizer.TestSuiteMinimizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

@Service
public class PipelineService {

    private static final Logger logger = LoggerFactory.getLogger(PipelineService.class);

    private final CodeAnalyzer codeAnalyzer;
    private final AstDiffAnalyzer astDiffAnalyzer;
    private final JUnit5TestGenerator testGenerator;
    private final DynamicCompiler dynamicCompiler;
    private final TestExecutor testExecutor;
    private final CoverageAnalyzer coverageAnalyzer;
    private final MutationEngine mutationEngine;
    private final FlakyTestDetector flakyTestDetector;
    private final TestSuiteMinimizer testSuiteMinimizer;
    private final RegressionPrioritizer regressionPrioritizer;
    private final DatabaseManager databaseManager;
    private final ObjectMapper objectMapper;

    public PipelineService(CodeAnalyzer codeAnalyzer,
                           AstDiffAnalyzer astDiffAnalyzer,
                           JUnit5TestGenerator testGenerator,
                           DynamicCompiler dynamicCompiler,
                           TestExecutor testExecutor,
                           CoverageAnalyzer coverageAnalyzer,
                           MutationEngine mutationEngine,
                           FlakyTestDetector flakyTestDetector,
                           TestSuiteMinimizer testSuiteMinimizer,
                           RegressionPrioritizer regressionPrioritizer,
                           DatabaseManager databaseManager) {
        this.codeAnalyzer = codeAnalyzer;
        this.astDiffAnalyzer = astDiffAnalyzer;
        this.testGenerator = testGenerator;
        this.dynamicCompiler = dynamicCompiler;
        this.testExecutor = testExecutor;
        this.coverageAnalyzer = coverageAnalyzer;
        this.mutationEngine = mutationEngine;
        this.flakyTestDetector = flakyTestDetector;
        this.testSuiteMinimizer = testSuiteMinimizer;
        this.regressionPrioritizer = regressionPrioritizer;
        this.databaseManager = databaseManager;
        this.objectMapper = new ObjectMapper();
    }

    public static class PipelineExecutionResult {
        private final ClassInfo classInfo;
        private final JUnit5TestGenerator.GeneratedTestSuite testSuite;
        private final TestExecutor.TestExecutionSummary executionSummary;
        private final CoverageAnalyzer.ClassCoverageSummary coverageSummary;
        private final MutationResultSummary mutationSummary;
        private final List<FlakyTestDetector.FlakyTestReport> flakyReports;
        private final TestSuiteMinimizer.MinimizationReport minimizationReport;
        private final RegressionPrioritizer.PrioritizationReport prioritizationReport;
        private final Long classId;
        private final Long runId;

        public PipelineExecutionResult(ClassInfo classInfo,
                                      JUnit5TestGenerator.GeneratedTestSuite testSuite,
                                      TestExecutor.TestExecutionSummary executionSummary,
                                      CoverageAnalyzer.ClassCoverageSummary coverageSummary,
                                      MutationResultSummary mutationSummary,
                                      List<FlakyTestDetector.FlakyTestReport> flakyReports,
                                      TestSuiteMinimizer.MinimizationReport minimizationReport,
                                      RegressionPrioritizer.PrioritizationReport prioritizationReport,
                                      Long classId, Long runId) {
            this.classInfo = classInfo;
            this.testSuite = testSuite;
            this.executionSummary = executionSummary;
            this.coverageSummary = coverageSummary;
            this.mutationSummary = mutationSummary;
            this.flakyReports = flakyReports;
            this.minimizationReport = minimizationReport;
            this.prioritizationReport = prioritizationReport;
            this.classId = classId;
            this.runId = runId;
        }

        public ClassInfo getClassInfo() {
            return classInfo;
        }

        public JUnit5TestGenerator.GeneratedTestSuite getTestSuite() {
            return testSuite;
        }

        public TestExecutor.TestExecutionSummary getExecutionSummary() {
            return executionSummary;
        }

        public CoverageAnalyzer.ClassCoverageSummary getCoverageSummary() {
            return coverageSummary;
        }

        public MutationResultSummary getMutationSummary() {
            return mutationSummary;
        }

        public List<FlakyTestDetector.FlakyTestReport> getFlakyReports() {
            return flakyReports;
        }

        public TestSuiteMinimizer.MinimizationReport getMinimizationReport() {
            return minimizationReport;
        }

        public RegressionPrioritizer.PrioritizationReport getPrioritizationReport() {
            return prioritizationReport;
        }

        public Long getClassId() {
            return classId;
        }

        public Long getRunId() {
            return runId;
        }
    }

    public PipelineExecutionResult runPipelineForSource(String projectName, String sourceCode,
                                                        Consumer<String> progressListener) throws Exception {
        notifyProgress(progressListener, "Step 1/7: Analyzing Java AST and OOP Structure...");
        ClassInfo classInfo = codeAnalyzer.analyzeSourceCode(sourceCode);

        notifyProgress(progressListener, "Step 2/7: Checking for Version Diffs and Historical Revisions...");
        ProjectEntity project = databaseManager.getOrCreateProject(projectName, "Automated Test Generation Project");

        // Version Diff: check existing version before saving updated metadata
        Optional<ClassMetadataEntity> existingOpt = databaseManager.findClassMetadata(project.getId(), classInfo.getClassName());
        ClassInfo previousClassInfo = null;
        if (existingOpt.isPresent()) {
            String prevSource = existingOpt.get().getSourceCode();
            if (prevSource != null && !prevSource.trim().isEmpty() && !prevSource.equals(sourceCode)) {
                try {
                    previousClassInfo = codeAnalyzer.analyzeSourceCode(prevSource);
                } catch (Exception e) {
                    logger.warn("Could not analyze previous source code for diffing: {}", e.getMessage());
                }
            }
        }
        AstDiffAnalyzer.DiffReport diffReport = astDiffAnalyzer.compareVersions(previousClassInfo, classInfo);

        ClassMetadataEntity classEntity = databaseManager.saveClassMetadata(project, classInfo);
        String changedJson = objectMapper.writeValueAsString(diffReport.getMethodDiffs());
        databaseManager.recordClassVersion(classEntity, changedJson, sourceCode);

        notifyProgress(progressListener, "Step 3/7: Generating Boundary, Equivalence, Exception, and OOP Test Cases...");
        JUnit5TestGenerator.GeneratedTestSuite testSuite = testGenerator.generateTestSuite(classInfo);

        // Save methods and test cases to DB
        Map<String, MethodMetadataEntity> methodMap = databaseManager.saveMethods(classEntity, classInfo);
        Map<String, TestCaseEntity> testCaseEntityMap = databaseManager.saveTestCases(classEntity, methodMap, testSuite.getTestCases());

        notifyProgress(progressListener, "Step 4/7: Compiling Source and Tests Programmatically...");
        Map<String, String> sourcesToCompile = getSampleDependencies(classInfo);
        String classRelPath = (classInfo.getPackageName().isEmpty() ? "" : classInfo.getPackageName().replace('.', '/') + "/")
                + classInfo.getClassName() + ".java";
        String testRelPath = (testSuite.getPackageName().isEmpty() ? "" : testSuite.getPackageName().replace('.', '/') + "/")
                + testSuite.getTestClassName() + ".java";

        sourcesToCompile.put(classRelPath, sourceCode);
        sourcesToCompile.put(testRelPath, testSuite.getFullSourceCode());

        DynamicCompiler.CompilationResult compResult = dynamicCompiler.compileSources(sourcesToCompile);
        if (!compResult.isSuccess()) {
            throw new IllegalStateException("Dynamic compilation failed: " + String.join(", ", compResult.getDiagnostics()));
        }

        String fullTestName = (testSuite.getPackageName().isEmpty() ? "" : testSuite.getPackageName() + ".") + testSuite.getTestClassName();
        Class<?> testClass = compResult.getClassLoader().loadClass(fullTestName);

        notifyProgress(progressListener, "Step 5/7: Executing Test Suite and Measuring JaCoCo Coverage...");
        TestExecutor.TestExecutionSummary execSummary = testExecutor.executeTestClass(testClass, compResult.getClassLoader());
        TestRunEntity runEntity = databaseManager.recordTestRun(project, "AutoPipeline", execSummary, testCaseEntityMap);

        CoverageAnalyzer.ClassCoverageSummary covSummary = coverageAnalyzer.measureCoverage(
                classInfo.getFullClassName(),
                compResult.getOutputDir(),
                () -> testExecutor.executeTestClass(testClass, compResult.getClassLoader())
        );
        databaseManager.recordCoverage(classEntity, runEntity, covSummary);

        notifyProgress(progressListener, "Step 6/7: Running AST Mutation Testing Engine & Mutant Kill Analysis...");
        MutationResultSummary mutationSummary = mutationEngine.evaluateTestSuiteOnMutants(classInfo, testSuite, getSampleDependencies(classInfo));
        databaseManager.recordMutation(classEntity, runEntity, mutationSummary);

        notifyProgress(progressListener, "Step 7/7: Detecting Flaky Tests & Optimizing Suite (Minimization + Regression Prioritization)...");
        List<FlakyTestDetector.FlakyTestReport> flakyReports = flakyTestDetector.detectFlakyTests(testClass, compResult.getClassLoader(), 3);
        databaseManager.recordFlakyTests(runEntity, testCaseEntityMap, flakyReports);

        TestSuiteMinimizer.MinimizationReport minReport = testSuiteMinimizer.minimizeTestSuite(testSuite.getTestCases(), mutationSummary);

        Map<String, Integer> failureCounts = databaseManager.getHistoricalFailureCounts(classEntity.getId());
        Map<String, Long> execTimes = databaseManager.getHistoricalExecutionTimes(classEntity.getId());
        RegressionPrioritizer.PrioritizationReport prioReport = regressionPrioritizer.prioritize(
                testSuite.getTestCases(),
                diffReport.getModifiedMethodNames(),
                failureCounts,
                execTimes
        );

        notifyProgress(progressListener, "Pipeline Complete! Results Ready.");

        return new PipelineExecutionResult(
                classInfo,
                testSuite,
                execSummary,
                covSummary,
                mutationSummary,
                flakyReports,
                minReport,
                prioReport,
                classEntity.getId(),
                runEntity.getId()
        );
    }

    private void notifyProgress(Consumer<String> listener, String msg) {
        if (listener != null) {
            listener.accept(msg);
        }
    }

    public Map<String, String> getSampleDependencies(ClassInfo classInfo) {
        Map<String, String> extra = new HashMap<>();
        String[] sampleNames = {"BankAccount", "Calculator", "NotificationService", "ProductRepository", "BaseManager", "InventoryManager"};
        for (String name : sampleNames) {
            try {
                InputStream is = getClass().getResourceAsStream("/samples/" + name + ".java");
                if (is != null) {
                    String code = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    extra.put("samples/" + name + ".java", code);
                }
            } catch (Exception ignored) {}
        }
        return extra;
    }
}
