package com.cbp.testgen.web;

import com.cbp.testgen.database.entity.*;
import com.cbp.testgen.database.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HistoryController {

    private final ClassMetadataRepository classMetadataRepository;
    private final CoverageResultRepository coverageResultRepository;
    private final MutationResultRepository mutationResultRepository;
    private final FlakyTestRepository flakyTestRepository;
    private final TestRunRepository testRunRepository;

    public HistoryController(ClassMetadataRepository classMetadataRepository,
                             CoverageResultRepository coverageResultRepository,
                             MutationResultRepository mutationResultRepository,
                             FlakyTestRepository flakyTestRepository,
                             TestRunRepository testRunRepository) {
        this.classMetadataRepository = classMetadataRepository;
        this.coverageResultRepository = coverageResultRepository;
        this.mutationResultRepository = mutationResultRepository;
        this.flakyTestRepository = flakyTestRepository;
        this.testRunRepository = testRunRepository;
    }

    @GetMapping("/history")
    public String showHistory(@RequestParam(value = "classId", required = false) Long classId, Model model) {
        List<ClassMetadataEntity> classes = classMetadataRepository.findAll();
        ClassMetadataEntity selectedClass = null;

        if (classId != null) {
            selectedClass = classMetadataRepository.findById(classId).orElse(null);
        }
        if (selectedClass == null && !classes.isEmpty()) {
            selectedClass = classes.get(0);
        }

        List<CoverageResultEntity> coverages = selectedClass != null ? coverageResultRepository.findByClassMetadataIdOrderByTestRunRunTimestampAsc(selectedClass.getId()) : List.of();
        List<MutationResultEntity> mutations = selectedClass != null ? mutationResultRepository.findByClassMetadataIdOrderByTestRunRunTimestampAsc(selectedClass.getId()) : List.of();
        List<FlakyTestEntity> flakyTests = flakyTestRepository.findAll();
        List<TestRunEntity> allRuns = testRunRepository.findAll();

        model.addAttribute("classes", classes);
        model.addAttribute("selectedClass", selectedClass);
        model.addAttribute("coverages", coverages);
        model.addAttribute("mutations", mutations);
        model.addAttribute("flakyTests", flakyTests);
        model.addAttribute("allRuns", allRuns);

        return "history";
    }
}
