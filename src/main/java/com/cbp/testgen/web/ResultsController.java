package com.cbp.testgen.web;

import com.cbp.testgen.database.entity.*;
import com.cbp.testgen.database.repository.*;
import com.cbp.testgen.mutation.Mutant;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;

import com.cbp.testgen.analyzer.AstDiffAnalyzer;

@Controller
public class ResultsController {

    private static final Logger logger = LoggerFactory.getLogger(ResultsController.class);

    private final ClassMetadataRepository classMetadataRepository;
    private final TestCaseRepository testCaseRepository;
    private final CoverageResultRepository coverageResultRepository;
    private final MutationResultRepository mutationResultRepository;
    private final FlakyTestRepository flakyTestRepository;
    private final TestResultRepository testResultRepository;
    private final ClassVersionRepository classVersionRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public ResultsController(ClassMetadataRepository classMetadataRepository,
                             TestCaseRepository testCaseRepository,
                             CoverageResultRepository coverageResultRepository,
                             MutationResultRepository mutationResultRepository,
                             FlakyTestRepository flakyTestRepository,
                             TestResultRepository testResultRepository,
                             ClassVersionRepository classVersionRepository) {
        this.classMetadataRepository = classMetadataRepository;
        this.testCaseRepository = testCaseRepository;
        this.coverageResultRepository = coverageResultRepository;
        this.mutationResultRepository = mutationResultRepository;
        this.flakyTestRepository = flakyTestRepository;
        this.testResultRepository = testResultRepository;
        this.classVersionRepository = classVersionRepository;
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public ResultsController(ClassMetadataRepository classMetadataRepository,
                             TestCaseRepository testCaseRepository,
                             CoverageResultRepository coverageResultRepository,
                             MutationResultRepository mutationResultRepository,
                             FlakyTestRepository flakyTestRepository,
                             TestResultRepository testResultRepository) {
        this(classMetadataRepository, testCaseRepository, coverageResultRepository,
             mutationResultRepository, flakyTestRepository, testResultRepository, null);
    }

    public static class TestCaseRowDto {
        private final Long id;
        private final String functionName;
        private final String testName;
        private final String testType;
        private final String inputValues;
        private final int assertionCount;
        private final String status; // PASSED, FAILED, ERROR, WEAK
        private final String vulnerability;
        private final String solution;
        private final String generatedCode;
        private final String errorMessage;
        private final boolean hasSubFunctionFailure;
        private final String subFunctionName;
        private final int subFunctionLine;
        private final String callerFunctionName;
        private final String failureExceptionType;

        private final boolean regressionPriority;

        public TestCaseRowDto(Long id, String functionName, String testName, String testType,
                              String inputValues, int assertionCount, String status,
                              String vulnerability, String solution, String generatedCode, String errorMessage,
                              boolean hasSubFunctionFailure, String subFunctionName, int subFunctionLine,
                              String callerFunctionName, String failureExceptionType) {
            this(id, functionName, testName, testType, inputValues, assertionCount, status,
                 vulnerability, solution, generatedCode, errorMessage,
                 hasSubFunctionFailure, subFunctionName, subFunctionLine,
                 callerFunctionName, failureExceptionType, false);
        }

        public TestCaseRowDto(Long id, String functionName, String testName, String testType,
                              String inputValues, int assertionCount, String status,
                              String vulnerability, String solution, String generatedCode, String errorMessage,
                              boolean hasSubFunctionFailure, String subFunctionName, int subFunctionLine,
                              String callerFunctionName, String failureExceptionType,
                              boolean regressionPriority) {
            this.id = id;
            this.functionName = functionName;
            this.testName = testName;
            this.testType = testType;
            this.inputValues = inputValues;
            this.assertionCount = assertionCount;
            this.status = status;
            this.vulnerability = vulnerability;
            this.solution = solution;
            this.generatedCode = generatedCode;
            this.errorMessage = errorMessage;
            this.hasSubFunctionFailure = hasSubFunctionFailure;
            this.subFunctionName = subFunctionName;
            this.subFunctionLine = subFunctionLine;
            this.callerFunctionName = callerFunctionName;
            this.failureExceptionType = failureExceptionType;
            this.regressionPriority = regressionPriority;
        }

        public Long getId() { return id; }
        public String getFunctionName() { return functionName; }
        public String getTestName() { return testName; }
        public String getTestType() { return testType; }
        public String getInputValues() { return inputValues; }
        public int getAssertionCount() { return assertionCount; }
        public String getStatus() { return status; }
        public String getVulnerability() { return vulnerability; }
        public String getSolution() { return solution; }
        public String getGeneratedCode() { return generatedCode; }
        public String getErrorMessage() { return errorMessage; }
        public boolean isHasSubFunctionFailure() { return hasSubFunctionFailure; }
        public String getSubFunctionName() { return subFunctionName; }
        public int getSubFunctionLine() { return subFunctionLine; }
        public String getCallerFunctionName() { return callerFunctionName; }
        public String getFailureExceptionType() { return failureExceptionType; }
        public boolean isRegressionPriority() { return regressionPriority; }
    }

    @GetMapping("/results/{classId}")
    public String showResults(@PathVariable("classId") Long classId, Model model) {
        ClassMetadataEntity classEntity = classMetadataRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Class not found for ID: " + classId));

        List<TestCaseEntity> testCases = testCaseRepository.findByClassMetadataId(classId);
        CoverageResultEntity latestCoverage = coverageResultRepository.findTopByClassMetadataIdOrderByTestRunRunTimestampDesc(classId).orElse(null);
        MutationResultEntity latestMutation = mutationResultRepository.findTopByClassMetadataIdOrderByTestRunRunTimestampDesc(classId).orElse(null);
        List<FlakyTestEntity> flakyTests = flakyTestRepository.findByClassId(classId);

        Long latestRunId = (latestCoverage != null && latestCoverage.getTestRun() != null)
                ? latestCoverage.getTestRun().getId() : null;

        Map<Long, TestResultEntity> resultMap = new HashMap<>();
        if (latestRunId != null) {
            List<TestResultEntity> results = testResultRepository.findByTestRunId(latestRunId);
            for (TestResultEntity r : results) {
                if (r.getTestCase() != null) {
                    resultMap.put(r.getTestCase().getId(), r);
                }
            }
        }

        List<Mutant> mutants = Collections.emptyList();
        if (latestMutation != null && latestMutation.getMutantDetailsJson() != null && !latestMutation.getMutantDetailsJson().isEmpty()) {
            try {
                mutants = objectMapper.readValue(latestMutation.getMutantDetailsJson(), new TypeReference<List<Mutant>>() {});
            } catch (Exception e) {
                logger.error("Could not parse mutant details JSON for class ID {}: {}", classId, e.getMessage(), e);
            }
        }

        List<ClassVersionEntity> versions = classVersionRepository != null
                ? classVersionRepository.findByClassMetadataIdOrderByVersionNumberDesc(classId)
                : Collections.emptyList();

        ClassVersionEntity latestVersion = !versions.isEmpty() ? versions.get(0) : null;
        ClassVersionEntity previousVersion = versions.size() > 1 ? versions.get(1) : null;

        List<AstDiffAnalyzer.MethodDiff> methodDiffs = new ArrayList<>();
        Set<String> modifiedMethods = new HashSet<>();
        if (latestVersion != null && latestVersion.getChangedMethodsJson() != null && !latestVersion.getChangedMethodsJson().isEmpty()) {
            try {
                methodDiffs = objectMapper.readValue(
                        latestVersion.getChangedMethodsJson(),
                        new TypeReference<List<AstDiffAnalyzer.MethodDiff>>() {}
                );
                for (AstDiffAnalyzer.MethodDiff md : methodDiffs) {
                    if (md.getChangeType() == AstDiffAnalyzer.MethodDiff.ChangeType.MODIFIED
                            || md.getChangeType() == AstDiffAnalyzer.MethodDiff.ChangeType.ADDED) {
                        modifiedMethods.add(md.getMethodName());
                    }
                }
            } catch (Exception e) {
                logger.warn("Could not deserialize changed methods JSON: {}", e.getMessage());
            }
        }

        List<TestCaseRowDto> tableRows = new ArrayList<>();
        int passedCount = 0;
        int failedCount = 0;

        for (TestCaseEntity tc : testCases) {
            TestResultEntity tr = resultMap.get(tc.getId());

            // 1. Function / Method Name
            String functionName = tc.getMethodMetadata() != null ? tc.getMethodMetadata().getMethodName() : inferMethodName(tc.getTestName());

            // 2. Input Values Extraction
            String inputValues = extractInputValues(tc);

            // 3. Status
            String status = "PASSED";
            String errorMessage = null;
            String stackTrace = null;
            if (tr != null) {
                stackTrace = tr.getStackTrace();
                if ("FAIL".equalsIgnoreCase(tr.getStatus())) {
                    status = "FAILED";
                    errorMessage = tr.getErrorMessage();
                    failedCount++;
                } else if ("ERROR".equalsIgnoreCase(tr.getStatus())) {
                    status = "ERROR";
                    errorMessage = tr.getErrorMessage();
                    failedCount++;
                } else {
                    status = tc.isWeak() ? "WEAK" : "PASSED";
                    passedCount++;
                }
            } else {
                status = tc.isWeak() ? "WEAK" : "PASSED";
                passedCount++;
            }

            // 4. Vulnerability & Recommended Solution Analysis
            VulnerabilityAssessment va = assessVulnerabilityAndSolution(tc, status, errorMessage, stackTrace, inputValues, functionName);

            boolean isRegressionPriority = modifiedMethods.contains(functionName);

            tableRows.add(new TestCaseRowDto(
                    tc.getId(),
                    functionName,
                    tc.getTestName(),
                    tc.getTestType(),
                    inputValues,
                    tc.getAssertionCount(),
                    status,
                    va.vulnerability,
                    va.solution,
                    tc.getGeneratedCode(),
                    errorMessage,
                    va.hasSubFunction,
                    va.subFunctionName,
                    va.subFunctionLine,
                    va.callerFunction,
                    va.exceptionType,
                    isRegressionPriority
            ));
        }

        long weakCount = testCases.stream().filter(TestCaseEntity::isWeak).count();

        model.addAttribute("classEntity", classEntity);
        model.addAttribute("testCases", testCases);
        model.addAttribute("testCaseRows", tableRows);
        model.addAttribute("coverage", latestCoverage);
        model.addAttribute("mutation", latestMutation);
        model.addAttribute("mutants", mutants);
        model.addAttribute("flakyTests", flakyTests);
        model.addAttribute("weakCount", weakCount);
        model.addAttribute("totalTests", testCases.size());
        model.addAttribute("passedCount", passedCount);
        model.addAttribute("failedCount", failedCount);

        // Versioning and AST Diff Telemetry
        model.addAttribute("versions", versions);
        model.addAttribute("latestVersion", latestVersion);
        model.addAttribute("previousVersion", previousVersion);
        model.addAttribute("methodDiffs", methodDiffs);
        model.addAttribute("modifiedMethods", modifiedMethods);
        model.addAttribute("hasVersions", versions.size() > 1);
        model.addAttribute("previousSourceCode", previousVersion != null ? previousVersion.getSourceCode() : null);

        return "results";
    }

    @GetMapping("/api/versions/{versionId}/code")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getVersionCode(@PathVariable("versionId") Long versionId) {
        if (classVersionRepository == null) {
            return ResponseEntity.notFound().build();
        }
        return classVersionRepository.findById(versionId)
                .map(v -> {
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("id", v.getId());
                    resp.put("versionNumber", v.getVersionNumber());
                    resp.put("className", v.getClassMetadata() != null ? v.getClassMetadata().getClassName() : "Unknown");
                    resp.put("sourceHash", v.getSourceHash());
                    resp.put("timestamp", v.getTimestamp() != null ? v.getTimestamp().toString() : "");
                    resp.put("sourceCode", v.getSourceCode() != null ? v.getSourceCode() : "");
                    resp.put("changedMethodsJson", v.getChangedMethodsJson() != null ? v.getChangedMethodsJson() : "{}");
                    return ResponseEntity.ok(resp);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    static class VulnerabilityAssessment {
        final String vulnerability;
        final String solution;
        final boolean hasSubFunction;
        final String subFunctionName;
        final int subFunctionLine;
        final String callerFunction;
        final String exceptionType;

        VulnerabilityAssessment(String vulnerability, String solution,
                                boolean hasSubFunction, String subFunctionName, int subFunctionLine,
                                String callerFunction, String exceptionType) {
            this.vulnerability = vulnerability;
            this.solution = solution;
            this.hasSubFunction = hasSubFunction;
            this.subFunctionName = subFunctionName;
            this.subFunctionLine = subFunctionLine;
            this.callerFunction = callerFunction;
            this.exceptionType = exceptionType;
        }

        VulnerabilityAssessment(String vulnerability, String solution) {
            this(vulnerability, solution, false, null, 0, null, null);
        }
    }

    private String inferMethodName(String testName) {
        if (testName.startsWith("test")) {
            String sub = testName.substring(4);
            int idx = sub.indexOf('_');
            if (idx > 0) {
                sub = sub.substring(0, idx);
            }
            return Character.toLowerCase(sub.charAt(0)) + sub.substring(1);
        }
        int idx = testName.indexOf('_');
        return idx > 0 ? testName.substring(0, idx) : testName;
    }

    private String extractInputValues(TestCaseEntity tc) {
        String desc = tc.getDescription();
        if (desc != null && desc.contains("inputs: ")) {
            return desc.substring(desc.indexOf("inputs: ") + 8).trim();
        }
        if (desc != null && desc.contains("parameter ")) {
            return desc.substring(desc.indexOf("parameter ")).trim();
        }

        String code = tc.getGeneratedCode();
        if (code != null) {
            Pattern p = Pattern.compile("input_(\\w+)\\s*=\\s*([^;]+);");
            Matcher m = p.matcher(code);
            List<String> args = new ArrayList<>();
            while (m.find()) {
                args.add(m.group(1) + " = " + m.group(2).trim());
            }
            if (!args.isEmpty()) {
                return String.join(", ", args);
            }
        }

        if ("OOP_CONTRACT".equalsIgnoreCase(tc.getTestType()) || "OOP".equalsIgnoreCase(tc.getTestType())) {
            return "Polymorphic Hierarchy / Collaborator Mocks";
        }
        return "Nominal Parameter Matrix";
    }

    private static final Pattern STACK_FRAME_PATTERN = Pattern.compile(
            "^\\s*at\\s+(?:[\\w.]+/)?([\\w.$]+)\\.([\\w$<>]+)\\(([^:]+?)(?::(\\d+))?\\)"
    );

    static class SubFunctionDetail {
        final String name;
        final int line;
        SubFunctionDetail(String name, int line) {
            this.name = name;
            this.line = line;
        }
    }

    SubFunctionDetail extractFailingSubFunction(String stackTrace, String callerFunctionName) {
        if (stackTrace == null || stackTrace.trim().isEmpty()) {
            return null;
        }

        String[] lines = stackTrace.split("\\r?\\n");
        for (String line : lines) {
            Matcher m = STACK_FRAME_PATTERN.matcher(line);
            if (m.find()) {
                String className = m.group(1);
                String methodName = m.group(2);
                int lineNum = (m.group(4) != null) ? Integer.parseInt(m.group(4)) : -1;

                if (isIgnoredClassOrMethod(className, methodName) || isTestMethod(className, methodName)) {
                    continue;
                }

                // If the first application method encountered is different from the caller function
                if (callerFunctionName != null
                        && !methodName.equalsIgnoreCase(callerFunctionName)
                        && !methodName.equals("<init>")
                        && !methodName.equals("<clinit>")) {
                    return new SubFunctionDetail(methodName, lineNum);
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    private boolean isIgnoredClassOrMethod(String className, String methodName) {
        return className.startsWith("org.junit.") ||
                className.startsWith("org.opentest4j.") ||
                className.startsWith("java.") ||
                className.startsWith("javax.") ||
                className.startsWith("jdk.") ||
                className.startsWith("sun.") ||
                className.startsWith("org.springframework.") ||
                className.startsWith("org.mockito.") ||
                className.startsWith("com.cbp.testgen.executor.") ||
                className.startsWith("com.cbp.testgen.service.");
    }

    private boolean isTestMethod(String className, String methodName) {
        return className.endsWith("Test") ||
                className.endsWith("Tests") ||
                className.endsWith("TestCase") ||
                className.contains("Test$") ||
                className.contains("_Test") ||
                methodName.startsWith("test_") ||
                methodName.startsWith("test");
    }

    private static final Pattern ZERO_VALUE_PATTERN = Pattern.compile(
            "(?:^|[=,\\s(])(?:\\(short\\)|\\(byte\\))?\\s*-?0(?:\\.0+)?(?:[dDfFlL])?(?:[\\s,);]|$)"
    );

    private static final Pattern MIN_VALUE_PATTERN = Pattern.compile(
            "\\b(?:Integer|Long|Short|Byte|Double|Float)\\.MIN_VALUE\\b|" +
            "-2147483648\\b|" +
            "-9223372036854775808\\b|" +
            "\\bmin_value\\b"
    );

    private static final Pattern MAX_VALUE_PATTERN = Pattern.compile(
            "\\b(?:Integer|Long|Short|Byte|Double|Float|Character)\\.MAX_VALUE\\b|" +
            "2147483647\\b|" +
            "9223372036854775807\\b|" +
            "\\bmax_value\\b"
    );

    private static final Pattern NEGATIVE_NUMBER_PATTERN = Pattern.compile(
            "(?:^|[=,\\s(])-(?:[1-9]\\d*(?:\\.\\d+)?|0\\.[1-9]\\d*)(?:[dDfFlL])?(?:[\\s,);]|$)"
    );

    private boolean isDivideByZero(String errLower, String inputsLower) {
        if (errLower.contains("/ by zero")
                || errLower.contains("division by zero")
                || errLower.contains("divide by zero")
                || errLower.contains("% by zero")
                || errLower.contains("modulo by zero")
                || inputsLower.contains("/ 0")) {
            return true;
        }
        if (errLower.contains("arithmeticexception")) {
            if (errLower.contains("overflow") || errLower.contains("underflow")) {
                return false;
            }
            if (errLower.contains("divide") || errLower.contains("divisor") || errLower.contains("denom")) {
                return true;
            }
            if (errLower.contains("zero") && !errLower.contains("overflow")) {
                return true;
            }
        }
        return false;
    }

    private boolean isZeroBoundary(String inputs, String inputsLower, TestCaseEntity tc, String errLower) {
        if (inputs != null && ZERO_VALUE_PATTERN.matcher(inputs).find()) {
            return true;
        }
        if (inputsLower.contains("zero boundary") || inputsLower.contains("= zero") || inputsLower.equals("0")) {
            return true;
        }
        if (tc != null) {
            String desc = tc.getDescription();
            if (desc != null && desc.toLowerCase().contains("zero boundary")) {
                return true;
            }
            String testName = tc.getTestName();
            if (testName != null) {
                String tn = testName.toLowerCase();
                if (tn.contains("_0_") || tn.endsWith("_0") || tn.contains("_zero_")) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isMinimumBoundary(String inputs, String inputsLower, TestCaseEntity tc, String errLower) {
        if (inputs != null && MIN_VALUE_PATTERN.matcher(inputs).find()) {
            return true;
        }
        if (inputsLower.contains("min_value")
                || inputsLower.contains("minimum boundary")
                || inputsLower.contains("lower int boundary")
                || inputsLower.contains("lower long boundary")
                || inputsLower.contains("lower short")
                || inputsLower.contains("lower byte")
                || inputsLower.contains("lower boundary")) {
            return true;
        }
        if (tc != null) {
            String desc = tc.getDescription();
            if (desc != null) {
                String descLower = desc.toLowerCase();
                if (descLower.contains("lower int boundary")
                        || descLower.contains("lower long boundary")
                        || descLower.contains("lower short")
                        || descLower.contains("lower byte")
                        || descLower.contains("min_value")
                        || descLower.contains("lower boundary")) {
                    return true;
                }
            }
            String testName = tc.getTestName();
            if (testName != null) {
                String tn = testName.toLowerCase();
                if (tn.contains("min_value") || tn.contains("minvalue")) {
                    return true;
                }
            }
        }
        if (errLower.contains("underflow")
                || (errLower.contains("overflow") && (inputsLower.contains("min") || inputsLower.contains("-2147483648")))) {
            return true;
        }
        return false;
    }

    private boolean isMaximumBoundary(String inputs, String inputsLower, TestCaseEntity tc, String errLower) {
        if (inputs != null && MAX_VALUE_PATTERN.matcher(inputs).find()) {
            return true;
        }
        if (inputsLower.contains("max_value")
                || inputsLower.contains("upper int boundary")
                || inputsLower.contains("upper long boundary")
                || inputsLower.contains("maximum boundary")
                || inputsLower.contains("upper boundary")) {
            return true;
        }
        if (tc != null) {
            String desc = tc.getDescription();
            if (desc != null) {
                String descLower = desc.toLowerCase();
                if (descLower.contains("upper int boundary")
                        || descLower.contains("upper long boundary")
                        || descLower.contains("max_value")
                        || descLower.contains("upper boundary")) {
                    return true;
                }
            }
            String testName = tc.getTestName();
            if (testName != null) {
                String tn = testName.toLowerCase();
                if (tn.contains("max_value") || tn.contains("maxvalue")) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isGeneralBoundary(TestCaseEntity tc, String inputsLower) {
        if (tc != null && "BOUNDARY".equalsIgnoreCase(tc.getTestType())) {
            return true;
        }
        if (tc != null && tc.getDescription() != null && tc.getDescription().toLowerCase().contains("boundary")) {
            return true;
        }
        if (inputsLower.contains("boundary")) {
            return true;
        }
        return false;
    }

    private boolean isIndexOutOfBounds(String errLower) {
        return errLower.contains("indexoutofbounds")
                || errLower.contains("index out of bounds")
                || errLower.contains("arrayindex")
                || errLower.contains("stringindex")
                || (errLower.contains("index") && errLower.contains("bound"));
    }

    private boolean isNegativeInput(String inputs, String inputsLower) {
        if (inputs != null && NEGATIVE_NUMBER_PATTERN.matcher(inputs).find()) {
            return true;
        }
        return inputsLower.contains("negative") || inputsLower.contains("-1");
    }

    VulnerabilityAssessment assessVulnerabilityAndSolution(TestCaseEntity tc, String status, String error, String stackTrace, String inputs, String callerFn) {
        if ("FAILED".equals(status) || "ERROR".equals(status)) {
            String err = (error != null) ? error.toLowerCase() : "";
            if (stackTrace != null) {
                err += " " + stackTrace.toLowerCase();
            }
            String inp = (inputs != null) ? inputs.toLowerCase() : "";
            String caller = (callerFn != null) ? callerFn : "";

            SubFunctionDetail subFn = extractFailingSubFunction(stackTrace, caller);
            boolean hasSubFn = (subFn != null);
            String location = hasSubFn ? (subFn.name + "()" + (subFn.line > 0 ? " (line " + subFn.line + ")" : "")) : "";

            String exceptionType = "Runtime Exception";
            if (stackTrace != null) {
                Matcher em = Pattern.compile("([a-zA-Z_0-9]+(?:Exception|Error))").matcher(stackTrace);
                if (em.find()) {
                    exceptionType = em.group(1);
                }
            } else if (error != null) {
                Matcher em = Pattern.compile("([a-zA-Z_0-9]+(?:Exception|Error))").matcher(error);
                if (em.find()) {
                    exceptionType = em.group(1);
                }
            }
            final String exType = exceptionType;
            java.util.function.BiFunction<String, String, VulnerabilityAssessment> mk = (vuln, sol) ->
                    new VulnerabilityAssessment(vuln, sol, hasSubFn, hasSubFn ? subFn.name : null, hasSubFn ? subFn.line : 0, caller, exType);

            // 1. True Divide-by-Zero Protection
            if (isDivideByZero(err, inp)) {
                if (hasSubFn) {
                    return mk.apply(
                            "Sub-function " + subFn.name + "() failed (Divide by zero)",
                            "The sub-function '" + location + "' under '" + caller + "()' divided by 0. Fix: In '" + subFn.name + "()', check that the divisor is not 0 before dividing."
                    );
                } else {
                    return mk.apply(
                            "Divide by zero error",
                            "The code divided by 0. Fix: In '" + caller + "()', check that the divisor is not 0 before dividing."
                    );
                }
            }

            // 2. Null Pointer Dereference Protection
            if (err.contains("nullpointer") || (inp.contains("null") && (err.contains("null") || exType.contains("Null")))) {
                if (hasSubFn) {
                    return mk.apply(
                            "Missing null check in " + subFn.name + "()",
                            "The sub-function '" + location + "' under '" + caller + "()' crashed with a null value. Fix: In '" + subFn.name + "()', check if the value is null before using it."
                    );
                } else {
                    return mk.apply(
                            "Missing null check",
                            "The code tried to use a null value. Fix: In '" + caller + "()', check if the input is null before using it."
                    );
                }
            }

            // 3. Minimum Boundary Value / Numeric Underflow / Lower Limit
            if (isMinimumBoundary(inputs, inp, tc, err)) {
                boolean isOverflow = err.contains("overflow") || err.contains("underflow");
                if (hasSubFn) {
                    String vuln = isOverflow
                            ? "Sub-function " + subFn.name + "() failed on minimum boundary value (Overflow/Underflow)"
                            : "Sub-function " + subFn.name + "() failed on minimum boundary value";
                    String sol = isOverflow
                            ? "The sub-function '" + location + "' under '" + caller + "()' encountered an arithmetic overflow/underflow when evaluated with a minimum boundary value (e.g., Integer.MIN_VALUE). Fix: In '" + subFn.name + "()', guard against minimum value overflow or handle MIN_VALUE explicitly before arithmetic operations."
                            : "The sub-function '" + location + "' under '" + caller + "()' failed when supplied with an extreme minimum boundary value. Fix: In '" + subFn.name + "()', add explicit lower bound validation and handle minimum boundary values properly.";
                    return mk.apply(vuln, sol);
                } else {
                    String vuln = isOverflow
                            ? "Boundary value error (Minimum value / Overflow)"
                            : "Boundary value error (Minimum value)";
                    String sol = isOverflow
                            ? "The method failed with an arithmetic overflow/underflow when evaluated with an extreme minimum boundary value (such as Integer.MIN_VALUE). Fix: In '" + caller + "()', guard against minimum value overflow or check bounds before negation/arithmetic."
                            : "The method failed when evaluated with an extreme minimum boundary value. Fix: In '" + caller + "()', add explicit lower bound validation and handle minimum boundary values properly.";
                    return mk.apply(vuln, sol);
                }
            }

            // 4. Zero Boundary Value Failure (non-division zero boundary errors)
            if (isZeroBoundary(inputs, inp, tc, err)) {
                if (hasSubFn) {
                    return mk.apply(
                            "Sub-function " + subFn.name + "() failed on zero boundary value",
                            "The sub-function '" + location + "' under '" + caller + "()' failed when supplied with a zero boundary value. Fix: In '" + subFn.name + "()', add an explicit boundary check or condition to handle zero inputs safely."
                    );
                } else {
                    return mk.apply(
                            "Boundary value error (Zero value)",
                            "The code failed when evaluated with a zero boundary value. Fix: In '" + caller + "()', add an explicit boundary check or validation logic to handle zero inputs properly."
                    );
                }
            }

            // 5. Maximum Boundary Value / Upper Limit
            if (isMaximumBoundary(inputs, inp, tc, err)) {
                if (hasSubFn) {
                    return mk.apply(
                            "Sub-function " + subFn.name + "() failed on maximum boundary value",
                            "The sub-function '" + location + "' under '" + caller + "()' failed when supplied with an extreme maximum boundary value. Fix: In '" + subFn.name + "()', add upper bound validation or prevent arithmetic overflow."
                    );
                } else {
                    return mk.apply(
                            "Boundary value error (Maximum value)",
                            "The method failed when evaluated with an extreme maximum boundary value. Fix: In '" + caller + "()', add upper boundary validation and guard against arithmetic overflow."
                    );
                }
            }

            // 6. General Boundary Value Failure
            if (isGeneralBoundary(tc, inp)) {
                if (hasSubFn) {
                    return mk.apply(
                            "Sub-function " + subFn.name + "() failed on boundary value",
                            "The sub-function '" + location + "' under '" + caller + "()' encountered an unhandled boundary condition. Fix: In '" + subFn.name + "()', add boundary validation and edge-case handling."
                    );
                } else {
                    return mk.apply(
                            "Boundary value error",
                            "The method encountered an unhandled boundary condition. Fix: In '" + caller + "()', add boundary validation and edge-case handling for extremal inputs."
                    );
                }
            }

            // 7. Index Out of Bounds
            if (isIndexOutOfBounds(err)) {
                if (hasSubFn) {
                    return mk.apply(
                            "Index out of bounds in " + subFn.name + "()",
                            "The sub-function '" + location + "' under '" + caller + "()' accessed an invalid list or array position. Fix: In '" + subFn.name + "()', check that the index is within range."
                    );
                } else {
                    return mk.apply(
                            "Index out of bounds",
                            "Tried to access an item outside list or array limits. Fix: In '" + caller + "()', check that the index is within range."
                    );
                }
            }

            // 8. Negative Input
            if (isNegativeInput(inputs, inp)) {
                if (hasSubFn) {
                    return mk.apply(
                            "Invalid negative input reached " + subFn.name + "()",
                            "The sub-function '" + location + "' under '" + caller + "()' received a negative number. Fix: In '" + caller + "()' or '" + subFn.name + "()', add a check to validate or reject negative values."
                    );
                } else {
                    return mk.apply(
                            "Invalid negative input",
                            "Received a negative input. Fix: In '" + caller + "()', add a check to validate or reject negative numbers."
                    );
                }
            }

            // 9. Illegal Argument (other invalid inputs)
            if (err.contains("illegalargument")) {
                if (hasSubFn) {
                    return mk.apply(
                            "Invalid argument in " + subFn.name + "()",
                            "The sub-function '" + location + "' under '" + caller + "()' received an illegal argument. Fix: In '" + caller + "()' or '" + subFn.name + "()', validate arguments before invocation."
                    );
                } else {
                    return mk.apply(
                            "Illegal argument error",
                            "Received an invalid argument. Fix: In '" + caller + "()', add input validation to ensure arguments meet method preconditions."
                    );
                }
            }

            // 10. State conflict
            if (err.contains("illegalstate") || caller.contains("transfer") || caller.contains("balance")) {
                if (hasSubFn) {
                    return mk.apply(
                            "State conflict in " + subFn.name + "()",
                            "The sub-function '" + location + "' under '" + caller + "()' failed because an object was not in the right state. Fix: Check the object status before calling '" + subFn.name + "()'."
                    );
                } else {
                    return mk.apply(
                            "Invalid object state",
                            "The object was in an unexpected state. Fix: In '" + caller + "()', verify the object state before proceeding."
                    );
                }
            }

            // 11. Assertion failure / Wrong result
            if (err.contains("assertion") || err.contains("expected")) {
                if (hasSubFn) {
                    return mk.apply(
                            "Sub-function " + subFn.name + "() returned wrong result",
                            "The sub-function '" + location + "' under '" + caller + "()' returned an unexpected calculation. Fix: Check the calculation logic inside '" + subFn.name + "()'."
                    );
                } else {
                    return mk.apply(
                            "Wrong result returned",
                            "The function returned an unexpected answer. Fix: In '" + caller + "()', check the calculation logic."
                    );
                }
            }

            // 12. Fallback unhandled error
            if (hasSubFn) {
                return mk.apply(
                        "Error in sub-function " + subFn.name + "()",
                        "The sub-function '" + location + "' under '" + caller + "()' caused the failure. Fix: In '" + subFn.name + "()', add input checks or error handling."
                );
            } else {
                return mk.apply(
                        "Unhandled error",
                        "An unexpected error occurred. Fix: In '" + caller + "()', add boundary checks or a try-catch block."
                );
            }
        } else if ("WEAK".equals(status)) {
            return new VulnerabilityAssessment(
                    "Weak test (no checks)",
                    "This test runs the method but never checks if the result is correct. Fix: Add an assertEquals(...) to verify the output.",
                    false, null, 0, callerFn, null
            );
        } else {
            return new VulnerabilityAssessment(
                    "Safe (No defect)",
                    "All inputs and function calls were handled safely.",
                    false, null, 0, callerFn, null
            );
        }
    }
}
