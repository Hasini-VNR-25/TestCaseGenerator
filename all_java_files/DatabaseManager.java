package com.cbp.testgen.database.service;

import com.cbp.testgen.analyzer.model.ClassInfo;
import com.cbp.testgen.analyzer.model.MethodInfo;
import com.cbp.testgen.coverage.CoverageAnalyzer;
import com.cbp.testgen.database.entity.*;
import com.cbp.testgen.database.repository.*;
import com.cbp.testgen.executor.FlakyTestDetector;
import com.cbp.testgen.executor.TestExecutor;
import com.cbp.testgen.generator.JUnit5TestGenerator;
import com.cbp.testgen.mutation.MutationResultSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class DatabaseManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

    private final ProjectRepository projectRepository;
    private final ClassMetadataRepository classMetadataRepository;
    private final MethodMetadataRepository methodMetadataRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestRunRepository testRunRepository;
    private final TestResultRepository testResultRepository;
    private final CoverageResultRepository coverageResultRepository;
    private final MutationResultRepository mutationResultRepository;
    private final FlakyTestRepository flakyTestRepository;
    private final ClassVersionRepository classVersionRepository;
    private final ObjectMapper objectMapper;

    public DatabaseManager(ProjectRepository projectRepository,
                           ClassMetadataRepository classMetadataRepository,
                           MethodMetadataRepository methodMetadataRepository,
                           TestCaseRepository testCaseRepository,
                           TestRunRepository testRunRepository,
                           TestResultRepository testResultRepository,
                           CoverageResultRepository coverageResultRepository,
                           MutationResultRepository mutationResultRepository,
                           FlakyTestRepository flakyTestRepository,
                           ClassVersionRepository classVersionRepository) {
        this.projectRepository = projectRepository;
        this.classMetadataRepository = classMetadataRepository;
        this.methodMetadataRepository = methodMetadataRepository;
        this.testCaseRepository = testCaseRepository;
        this.testRunRepository = testRunRepository;
        this.testResultRepository = testResultRepository;
        this.coverageResultRepository = coverageResultRepository;
        this.mutationResultRepository = mutationResultRepository;
        this.flakyTestRepository = flakyTestRepository;
        this.classVersionRepository = classVersionRepository;
        this.objectMapper = new ObjectMapper()
                .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Transactional
    public ProjectEntity getOrCreateProject(String projectName, String description) {
        return projectRepository.findByName(projectName)
                .orElseGet(() -> projectRepository.save(new ProjectEntity(projectName, description)));
    }

    @Transactional
    public ClassMetadataEntity saveClassMetadata(ProjectEntity project, ClassInfo classInfo) {
        Optional<ClassMetadataEntity> existing = classMetadataRepository.findByProjectIdAndClassName(project.getId(), classInfo.getClassName());
        ClassMetadataEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.setSourceHash(classInfo.getSourceHash());
            entity.setPackageName(classInfo.getPackageName());
            entity.setSuperClass(classInfo.getSuperClass());
            entity.setInterfaces(String.join(",", classInfo.getInterfaces()));
            entity.setSourceCode(classInfo.getSourceCode());
        } else {
            entity = new ClassMetadataEntity(
                    project,
                    classInfo.getClassName(),
                    classInfo.getPackageName(),
                    classInfo.getSourceHash(),
                    classInfo.getSuperClass(),
                    String.join(",", classInfo.getInterfaces()),
                    classInfo.getSourceCode()
            );
        }
        return classMetadataRepository.save(entity);
    }

    @Transactional
    public Map<String, MethodMetadataEntity> saveMethods(ClassMetadataEntity classEntity, ClassInfo classInfo) {
        Map<String, MethodMetadataEntity> methodMap = new HashMap<>();
        // Delete old methods if re-uploading
        List<MethodMetadataEntity> existingMethods = methodMetadataRepository.findByClassMetadataId(classEntity.getId());
        Map<String, MethodMetadataEntity> existingMap = new HashMap<>();
        for (MethodMetadataEntity m : existingMethods) {
            existingMap.put(m.getSignature(), m);
        }

        for (MethodInfo m : classInfo.getMethods()) {
            MethodMetadataEntity me = existingMap.get(m.getSignature());
            if (me == null) {
                me = new MethodMetadataEntity(
                        classEntity,
                        m.getMethodName(),
                        m.getSignature(),
                        m.getReturnType(),
                        String.join(" ", m.getModifiers()),
                        m.isAbstract()
                );
                me = methodMetadataRepository.save(me);
            }
            methodMap.put(m.getMethodName(), me);
        }
        return methodMap;
    }

    @Transactional
    public Map<String, TestCaseEntity> saveTestCases(ClassMetadataEntity classEntity,
                                                    Map<String, MethodMetadataEntity> methodMap,
                                                    List<JUnit5TestGenerator.GeneratedTestCaseData> testCases) {
        Map<String, TestCaseEntity> map = new HashMap<>();
        for (JUnit5TestGenerator.GeneratedTestCaseData tc : testCases) {
            MethodMetadataEntity mEntity = methodMap.get(tc.getMethodName());
            TestCaseEntity tce = new TestCaseEntity(
                    classEntity,
                    mEntity,
                    tc.getTestName(),
                    tc.getTestType(),
                    tc.getCode(),
                    tc.getAssertionCount(),
                    tc.isWeak(),
                    tc.getDescription()
            );
            tce = testCaseRepository.save(tce);
            map.put(tc.getTestName(), tce);
        }
        return map;
    }

    @Transactional
    public TestRunEntity recordTestRun(ProjectEntity project, String triggeredBy,
                                      TestExecutor.TestExecutionSummary execSummary,
                                      Map<String, TestCaseEntity> testCaseEntityMap) {
        TestRunEntity run = new TestRunEntity(
                project,
                triggeredBy,
                execSummary.getTotalCount(),
                execSummary.getPassedCount(),
                execSummary.getFailedCount() + execSummary.getErrorCount(),
                execSummary.getTotalTimeMs()
        );
        run = testRunRepository.save(run);

        for (TestExecutor.SingleTestResult r : execSummary.getResults()) {
            TestCaseEntity tcEntity = testCaseEntityMap.get(r.getTestName());
            if (tcEntity != null) {
                TestResultEntity res = new TestResultEntity(
                        tcEntity,
                        run,
                        r.getStatus(),
                        r.getExecutionTimeMs(),
                        r.getStackTrace(),
                        r.getErrorMessage()
                );
                testResultRepository.save(res);
            }
        }
        return run;
    }

    @Transactional
    public CoverageResultEntity recordCoverage(ClassMetadataEntity classEntity, TestRunEntity runEntity,
                                              CoverageAnalyzer.ClassCoverageSummary cov) {
        CoverageResultEntity cre = new CoverageResultEntity(
                classEntity,
                runEntity,
                cov.getLineCoveragePct(),
                cov.getBranchCoveragePct(),
                cov.getMethodCoveragePct(),
                cov.getCoveredLines(),
                cov.getTotalLines(),
                cov.getCoveredBranches(),
                cov.getTotalBranches()
        );
        return coverageResultRepository.save(cre);
    }

    @Transactional
    public MutationResultEntity recordMutation(ClassMetadataEntity classEntity, TestRunEntity runEntity,
                                              MutationResultSummary mut) {
        String json = "";
        try {
            json = objectMapper.writeValueAsString(mut.getMutants());
        } catch (Exception e) {
            logger.warn("Could not serialize mutants: {}", e.getMessage());
        }

        MutationResultEntity mre = new MutationResultEntity(
                classEntity,
                runEntity,
                mut.getTotalMutants(),
                mut.getMutantsKilled(),
                mut.getMutationScorePct(),
                json
        );
        return mutationResultRepository.save(mre);
    }

    @Transactional
    public void recordFlakyTests(TestRunEntity runEntity, Map<String, TestCaseEntity> testCaseEntityMap,
                                 List<FlakyTestDetector.FlakyTestReport> flakyReports) {
        for (FlakyTestDetector.FlakyTestReport f : flakyReports) {
            if (f.isFlaky()) {
                TestCaseEntity tc = testCaseEntityMap.get(f.getTestName());
                if (tc != null) {
                    FlakyTestEntity fe = new FlakyTestEntity(tc, runEntity, f.getInconsistencyCount(), f.getPassRatePct());
                    flakyTestRepository.save(fe);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public Optional<ClassMetadataEntity> findClassMetadata(Long projectId, String className) {
        return classMetadataRepository.findByProjectIdAndClassName(projectId, className);
    }

    @Transactional
    public ClassVersionEntity recordClassVersion(ClassMetadataEntity classEntity, String changedMethodsJson, String sourceCode) {
        List<ClassVersionEntity> versions = classVersionRepository.findByClassMetadataIdOrderByVersionNumberDesc(classEntity.getId());
        int nextVersion = versions.isEmpty() ? 1 : versions.get(0).getVersionNumber() + 1;
        ClassVersionEntity cve = new ClassVersionEntity(
                classEntity,
                nextVersion,
                classEntity.getSourceHash(),
                changedMethodsJson != null ? changedMethodsJson : "{}",
                sourceCode
        );
        return classVersionRepository.save(cve);
    }

    @Transactional
    public ClassVersionEntity recordClassVersion(ClassMetadataEntity classEntity, String changedMethodsJson) {
        return recordClassVersion(classEntity, changedMethodsJson, classEntity.getSourceCode());
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> getHistoricalFailureCounts(Long classId) {
        List<TestCaseEntity> testCases = testCaseRepository.findByClassMetadataId(classId);
        Map<String, Integer> failureMap = new HashMap<>();
        for (TestCaseEntity tc : testCases) {
            List<TestResultEntity> results = testResultRepository.findByTestCaseId(tc.getId());
            int fails = (int) results.stream().filter(r -> "FAIL".equalsIgnoreCase(r.getStatus()) || "ERROR".equalsIgnoreCase(r.getStatus())).count();
            if (fails > 0) {
                failureMap.put(tc.getTestName(), fails);
            }
        }
        return failureMap;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getHistoricalExecutionTimes(Long classId) {
        List<TestCaseEntity> testCases = testCaseRepository.findByClassMetadataId(classId);
        Map<String, Long> timeMap = new HashMap<>();
        for (TestCaseEntity tc : testCases) {
            List<TestResultEntity> results = testResultRepository.findByTestCaseId(tc.getId());
            if (!results.isEmpty()) {
                long avg = (long) results.stream().mapToLong(TestResultEntity::getExecutionTimeMs).average().orElse(10.0);
                timeMap.put(tc.getTestName(), avg);
            }
        }
        return timeMap;
    }

    @Transactional(readOnly = true)
    public List<ClassMetadataEntity> getAllClasses() {
        return classMetadataRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ClassMetadataEntity> getClassById(Long id) {
        return classMetadataRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<CoverageResultEntity> getCoverageHistory(Long classId) {
        return coverageResultRepository.findByClassMetadataIdOrderByTestRunRunTimestampAsc(classId);
    }

    @Transactional(readOnly = true)
    public List<MutationResultEntity> getMutationHistory(Long classId) {
        return mutationResultRepository.findByClassMetadataIdOrderByTestRunRunTimestampAsc(classId);
    }

    @Transactional(readOnly = true)
    public List<FlakyTestEntity> getFlakyTestsForClass(Long classId) {
        return flakyTestRepository.findByClassId(classId);
    }

    @Transactional(readOnly = true)
    public List<TestCaseEntity> getWeakTestsForClass(Long classId) {
        return testCaseRepository.findByClassMetadataIdAndIsWeakTrue(classId);
    }

    @Transactional
    public void deleteTarget(Long classId) {
        List<TestCaseEntity> testCases = testCaseRepository.findByClassMetadataId(classId);
        for (TestCaseEntity tc : testCases) {
            List<TestResultEntity> results = testResultRepository.findByTestCaseId(tc.getId());
            if (results != null && !results.isEmpty()) {
                testResultRepository.deleteAll(results);
            }
        }
        List<FlakyTestEntity> flaky = flakyTestRepository.findByClassId(classId);
        if (flaky != null && !flaky.isEmpty()) {
            flakyTestRepository.deleteAll(flaky);
        }
        testCaseRepository.deleteAll(testCases);

        List<CoverageResultEntity> cov = coverageResultRepository.findByClassMetadataIdOrderByTestRunRunTimestampAsc(classId);
        if (cov != null && !cov.isEmpty()) {
            coverageResultRepository.deleteAll(cov);
        }

        List<MutationResultEntity> mut = mutationResultRepository.findByClassMetadataIdOrderByTestRunRunTimestampAsc(classId);
        if (mut != null && !mut.isEmpty()) {
            mutationResultRepository.deleteAll(mut);
        }

        List<ClassVersionEntity> vers = classVersionRepository.findByClassMetadataIdOrderByVersionNumberDesc(classId);
        if (vers != null && !vers.isEmpty()) {
            classVersionRepository.deleteAll(vers);
        }

        List<MethodMetadataEntity> methods = methodMetadataRepository.findByClassMetadataId(classId);
        if (methods != null && !methods.isEmpty()) {
            methodMetadataRepository.deleteAll(methods);
        }

        classMetadataRepository.deleteById(classId);
    }

    @Transactional
    public void clearAllTargets() {
        flakyTestRepository.deleteAll();
        testResultRepository.deleteAll();
        testCaseRepository.deleteAll();
        coverageResultRepository.deleteAll();
        mutationResultRepository.deleteAll();
        classVersionRepository.deleteAll();
        methodMetadataRepository.deleteAll();
        testRunRepository.deleteAll();
        classMetadataRepository.deleteAll();
        projectRepository.deleteAll();
    }
}
