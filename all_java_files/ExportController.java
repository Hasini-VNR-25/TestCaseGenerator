package com.cbp.testgen.web;

import com.cbp.testgen.database.entity.ClassMetadataEntity;
import com.cbp.testgen.database.entity.ClassVersionEntity;
import com.cbp.testgen.database.entity.TestCaseEntity;
import com.cbp.testgen.database.repository.ClassMetadataRepository;
import com.cbp.testgen.database.repository.ClassVersionRepository;
import com.cbp.testgen.database.repository.TestCaseRepository;
import com.cbp.testgen.report.HtmlReportGenerator;
import com.cbp.testgen.service.CodeRectifierService;
import com.cbp.testgen.service.PipelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class ExportController {

    private final ClassMetadataRepository classMetadataRepository;
    private final TestCaseRepository testCaseRepository;
    private final PipelineService pipelineService;
    private final HtmlReportGenerator htmlReportGenerator;
    private final CodeRectifierService codeRectifierService;
    private final ClassVersionRepository classVersionRepository;

    @Autowired
    public ExportController(ClassMetadataRepository classMetadataRepository,
                            TestCaseRepository testCaseRepository,
                            PipelineService pipelineService,
                            HtmlReportGenerator htmlReportGenerator,
                            CodeRectifierService codeRectifierService,
                            ClassVersionRepository classVersionRepository) {
        this.classMetadataRepository = classMetadataRepository;
        this.testCaseRepository = testCaseRepository;
        this.pipelineService = pipelineService;
        this.htmlReportGenerator = htmlReportGenerator;
        this.codeRectifierService = codeRectifierService;
        this.classVersionRepository = classVersionRepository;
    }

    public ExportController(ClassMetadataRepository classMetadataRepository,
                            TestCaseRepository testCaseRepository,
                            PipelineService pipelineService,
                            HtmlReportGenerator htmlReportGenerator,
                            CodeRectifierService codeRectifierService) {
        this(classMetadataRepository, testCaseRepository, pipelineService,
             htmlReportGenerator, codeRectifierService, null);
    }

    @GetMapping("/export/java/{classId}")
    public ResponseEntity<byte[]> exportJavaFile(@PathVariable("classId") Long classId) {
        ClassMetadataEntity classEntity = classMetadataRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Class not found: " + classId));

        List<TestCaseEntity> testCases = testCaseRepository.findByClassMetadataId(classId);

        StringBuilder sb = new StringBuilder();
        if (classEntity.getPackageName() != null && !classEntity.getPackageName().isEmpty()) {
            sb.append("package ").append(classEntity.getPackageName()).append(";\n\n");
        }
        sb.append("import org.junit.jupiter.api.*;\n");
        sb.append("import static org.junit.jupiter.api.Assertions.*;\n");
        sb.append("import org.mockito.Mockito;\n");
        sb.append("import static org.mockito.Mockito.*;\n");
        sb.append("import java.util.*;\n\n");

        sb.append("public class ").append(classEntity.getClassName()).append("GeneratedTest {\n\n");
        for (TestCaseEntity tc : testCases) {
            sb.append(tc.getGeneratedCode()).append("\n");
        }
        sb.append("}\n");

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        String filename = classEntity.getClassName() + "GeneratedTest.java";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }

    @GetMapping("/export/html/{classId}")
    public ResponseEntity<byte[]> exportHtmlReport(@PathVariable("classId") Long classId) {
        ClassMetadataEntity classEntity = classMetadataRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Class not found: " + classId));

        try {
            PipelineService.PipelineExecutionResult result = pipelineService.runPipelineForSource(
                    classEntity.getProject().getName(),
                    classEntity.getSourceCode(),
                    null
            );
            String html = htmlReportGenerator.generateHtmlReport(result);
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            String filename = classEntity.getClassName() + "_QualityReport.html";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.TEXT_HTML)
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(("Error generating report: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }

    @GetMapping("/export/rectified/{classId}")
    public ResponseEntity<byte[]> exportRectifiedSource(@PathVariable("classId") Long classId) {
        CodeRectifierService.RectificationResult result = codeRectifierService.rectifyAndVerify(classId);
        byte[] bytes = result.getRectifiedSourceCode().getBytes(StandardCharsets.UTF_8);
        String filename = "Rectified_" + result.getClassName() + ".java";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }

    @GetMapping("/export/version/{versionId}")
    public ResponseEntity<byte[]> exportVersionSourceCode(@PathVariable("versionId") Long versionId) {
        if (classVersionRepository == null) {
            return ResponseEntity.notFound().build();
        }
        ClassVersionEntity version = classVersionRepository.findById(versionId)
                .orElseThrow(() -> new IllegalArgumentException("Version not found: " + versionId));
        String code = version.getSourceCode() != null ? version.getSourceCode() : "";
        byte[] bytes = code.getBytes(StandardCharsets.UTF_8);
        String className = version.getClassMetadata() != null ? version.getClassMetadata().getClassName() : "SourceCode";
        String filename = className + "_v" + version.getVersionNumber() + ".java";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }
}
