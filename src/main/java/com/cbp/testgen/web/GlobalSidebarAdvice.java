package com.cbp.testgen.web;

import com.cbp.testgen.database.entity.ClassMetadataEntity;
import com.cbp.testgen.database.entity.CoverageResultEntity;
import com.cbp.testgen.database.entity.MutationResultEntity;
import com.cbp.testgen.database.entity.TestRunEntity;
import com.cbp.testgen.database.repository.ClassMetadataRepository;
import com.cbp.testgen.database.repository.CoverageResultRepository;
import com.cbp.testgen.database.repository.MutationResultRepository;
import com.cbp.testgen.database.repository.TestRunRepository;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalSidebarAdvice {

    private final ClassMetadataRepository classMetadataRepository;
    private final CoverageResultRepository coverageResultRepository;
    private final MutationResultRepository mutationResultRepository;
    private final TestRunRepository testRunRepository;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public GlobalSidebarAdvice(ClassMetadataRepository classMetadataRepository,
                               CoverageResultRepository coverageResultRepository,
                               MutationResultRepository mutationResultRepository,
                               TestRunRepository testRunRepository) {
        this.classMetadataRepository = classMetadataRepository;
        this.coverageResultRepository = coverageResultRepository;
        this.mutationResultRepository = mutationResultRepository;
        this.testRunRepository = testRunRepository;
    }

    public static class SidebarTargetDto {
        private final Long id;
        private final String className;
        private final String packageName;
        private final Double mutationScore;
        private final Double lineCoverage;
        private final String lastRunTime;
        private final int totalTests;

        public SidebarTargetDto(Long id, String className, String packageName,
                                Double mutationScore, Double lineCoverage, String lastRunTime, int totalTests) {
            this.id = id;
            this.className = className;
            this.packageName = packageName;
            this.mutationScore = mutationScore;
            this.lineCoverage = lineCoverage;
            this.lastRunTime = lastRunTime;
            this.totalTests = totalTests;
        }

        public Long getId() { return id; }
        public String getClassName() { return className; }
        public String getPackageName() { return packageName; }
        public Double getMutationScore() { return mutationScore; }
        public Double getLineCoverage() { return lineCoverage; }
        public String getLastRunTime() { return lastRunTime; }
        public int getTotalTests() { return totalTests; }
    }

    @ModelAttribute("sidebarTargets")
    public List<SidebarTargetDto> populateSidebarTargets() {
        List<ClassMetadataEntity> classes = classMetadataRepository.findAll();
        List<SidebarTargetDto> targets = new ArrayList<>();

        for (ClassMetadataEntity cls : classes) {
            CoverageResultEntity cov = coverageResultRepository
                    .findTopByClassMetadataIdOrderByTestRunRunTimestampDesc(cls.getId())
                    .orElse(null);
            MutationResultEntity mut = mutationResultRepository
                    .findTopByClassMetadataIdOrderByTestRunRunTimestampDesc(cls.getId())
                    .orElse(null);

            Double mutScore = mut != null ? mut.getMutationScorePct() : null;
            Double lineCov = cov != null ? cov.getLineCoveragePct() : null;
            String timeStr = "Ready";
            int totalTests = 0;

            if (cov != null && cov.getTestRun() != null && cov.getTestRun().getRunTimestamp() != null) {
                timeStr = cov.getTestRun().getRunTimestamp().format(TIME_FMT);
                totalTests = cov.getTestRun().getTotalTests();
            }

            targets.add(new SidebarTargetDto(
                    cls.getId(),
                    cls.getClassName(),
                    cls.getPackageName(),
                    mutScore,
                    lineCov,
                    timeStr,
                    totalTests
            ));
        }

        return targets;
    }
}
