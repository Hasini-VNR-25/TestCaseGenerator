package com.cbp.testgen.web;

import com.cbp.testgen.database.entity.ClassMetadataEntity;
import com.cbp.testgen.database.entity.CoverageResultEntity;
import com.cbp.testgen.database.entity.MutationResultEntity;
import com.cbp.testgen.database.entity.TestRunEntity;
import com.cbp.testgen.database.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final ProjectRepository projectRepository;
    private final ClassMetadataRepository classMetadataRepository;
    private final TestRunRepository testRunRepository;
    private final CoverageResultRepository coverageResultRepository;
    private final MutationResultRepository mutationResultRepository;
    private final FlakyTestRepository flakyTestRepository;

    public DashboardController(ProjectRepository projectRepository,
                               ClassMetadataRepository classMetadataRepository,
                               TestRunRepository testRunRepository,
                               CoverageResultRepository coverageResultRepository,
                               MutationResultRepository mutationResultRepository,
                               FlakyTestRepository flakyTestRepository) {
        this.projectRepository = projectRepository;
        this.classMetadataRepository = classMetadataRepository;
        this.testRunRepository = testRunRepository;
        this.coverageResultRepository = coverageResultRepository;
        this.mutationResultRepository = mutationResultRepository;
        this.flakyTestRepository = flakyTestRepository;
    }

    @GetMapping({"/", "/dashboard"})
    public String showDashboard(Model model) {
        List<ClassMetadataEntity> classes = classMetadataRepository.findAll();
        List<TestRunEntity> runs = testRunRepository.findAll();
        List<CoverageResultEntity> coverages = coverageResultRepository.findAll();
        List<MutationResultEntity> mutations = mutationResultRepository.findAll();

        double avgCoverage = coverages.isEmpty() ? 0.0 :
                Math.round(coverages.stream().mapToDouble(CoverageResultEntity::getLineCoveragePct).average().orElse(0.0) * 10.0) / 10.0;
        double avgMutation = mutations.isEmpty() ? 0.0 :
                Math.round(mutations.stream().mapToDouble(MutationResultEntity::getMutationScorePct).average().orElse(0.0) * 10.0) / 10.0;

        int totalTests = runs.stream().mapToInt(TestRunEntity::getTotalTests).sum();
        long totalFlaky = flakyTestRepository.count();

        model.addAttribute("totalClasses", classes.size());
        model.addAttribute("totalRuns", runs.size());
        model.addAttribute("totalTests", totalTests);
        model.addAttribute("avgCoverage", avgCoverage);
        model.addAttribute("avgMutation", avgMutation);
        model.addAttribute("totalFlaky", totalFlaky);
        model.addAttribute("classes", classes);
        model.addAttribute("recentRuns", runs.stream().sorted((r1, r2) -> r2.getRunTimestamp().compareTo(r1.getRunTimestamp())).limit(5).toList());

        return "index";
    }
}
