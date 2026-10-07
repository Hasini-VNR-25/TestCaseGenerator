package com.cbp.testgen.web;

import com.cbp.testgen.ai.LlmVerificationService;
import com.cbp.testgen.analyzer.CodeAnalyzer;
import com.cbp.testgen.analyzer.model.ClassInfo;
import com.cbp.testgen.database.entity.ClassMetadataEntity;
import com.cbp.testgen.database.entity.CoverageResultEntity;
import com.cbp.testgen.database.entity.MutationResultEntity;
import com.cbp.testgen.database.repository.ClassMetadataRepository;
import com.cbp.testgen.database.repository.CoverageResultRepository;
import com.cbp.testgen.database.repository.MutationResultRepository;
import com.cbp.testgen.service.PipelineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import com.cbp.testgen.service.CodeRectifierService;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final ClassMetadataRepository classMetadataRepository;
    private final CoverageResultRepository coverageResultRepository;
    private final MutationResultRepository mutationResultRepository;
    private final LlmVerificationService llmVerificationService;
    private final CodeAnalyzer codeAnalyzer;
    private final PipelineService pipelineService;
    private final CodeRectifierService codeRectifierService;
    private final com.cbp.testgen.database.service.DatabaseManager databaseManager;

    public ApiController(ClassMetadataRepository classMetadataRepository,
                         CoverageResultRepository coverageResultRepository,
                         MutationResultRepository mutationResultRepository,
                         LlmVerificationService llmVerificationService,
                         CodeAnalyzer codeAnalyzer,
                         PipelineService pipelineService,
                         CodeRectifierService codeRectifierService,
                         com.cbp.testgen.database.service.DatabaseManager databaseManager) {
        this.classMetadataRepository = classMetadataRepository;
        this.coverageResultRepository = coverageResultRepository;
        this.mutationResultRepository = mutationResultRepository;
        this.llmVerificationService = llmVerificationService;
        this.codeAnalyzer = codeAnalyzer;
        this.pipelineService = pipelineService;
        this.codeRectifierService = codeRectifierService;
        this.databaseManager = databaseManager;
    }

    @GetMapping("/samples/{name}")
    public ResponseEntity<Map<String, String>> getSampleCode(@PathVariable("name") String name) {
        try {
            InputStream is = getClass().getResourceAsStream("/samples/" + name + ".java");
            if (is == null) {
                return ResponseEntity.notFound().build();
            }
            String code = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return ResponseEntity.ok(Map.of("name", name, "code", code));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/trends/{classId}")
    public ResponseEntity<Map<String, Object>> getTrends(@PathVariable("classId") Long classId) {
        List<CoverageResultEntity> coverages = coverageResultRepository.findByClassMetadataIdOrderByTestRunRunTimestampAsc(classId);
        List<MutationResultEntity> mutations = mutationResultRepository.findByClassMetadataIdOrderByTestRunRunTimestampAsc(classId);

        List<String> labels = coverages.stream()
                .map(c -> c.getTestRun().getRunTimestamp().toString().substring(11, 19))
                .toList();

        List<Double> lineCovData = coverages.stream().map(CoverageResultEntity::getLineCoveragePct).toList();
        List<Double> branchCovData = coverages.stream().map(CoverageResultEntity::getBranchCoveragePct).toList();
        List<Double> mutScoreData = mutations.stream().map(MutationResultEntity::getMutationScorePct).toList();

        Map<String, Object> resp = new HashMap<>();
        resp.put("labels", labels);
        resp.put("lineCoverage", lineCovData);
        resp.put("branchCoverage", branchCovData);
        resp.put("mutationScore", mutScoreData);

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/llm/verify/{classId}")
    public ResponseEntity<List<LlmVerificationService.VerifiedSuggestion>> verifyAiSuggestions(@PathVariable("classId") Long classId) {
        ClassMetadataEntity classEntity = classMetadataRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Class not found: " + classId));

        ClassInfo classInfo = codeAnalyzer.analyzeSourceCode(classEntity.getSourceCode());
        List<LlmVerificationService.VerifiedSuggestion> results = llmVerificationService.suggestAndVerifyTests(
                classInfo,
                pipelineService.getSampleDependencies(classInfo)
        );

        return ResponseEntity.ok(results);
    }

    @PostMapping("/rectify/{classId}")
    public ResponseEntity<CodeRectifierService.RectificationResult> rectifyCode(@PathVariable("classId") Long classId) {
        CodeRectifierService.RectificationResult result = codeRectifierService.rectifyAndVerify(classId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/targets/clear")
    public ResponseEntity<Map<String, String>> clearTargets() {
        databaseManager.clearAllTargets();
        return ResponseEntity.ok(Map.of("message", "Target history cleared successfully"));
    }

    @PostMapping("/targets/{id}/delete")
    public ResponseEntity<Map<String, String>> deleteTarget(@PathVariable("id") Long id) {
        databaseManager.deleteTarget(id);
        return ResponseEntity.ok(Map.of("message", "Target deleted successfully"));
    }
}
