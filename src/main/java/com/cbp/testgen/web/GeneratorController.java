package com.cbp.testgen.web;

import com.cbp.testgen.service.PipelineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Controller
public class GeneratorController {

    private static final Logger logger = LoggerFactory.getLogger(GeneratorController.class);
    private final PipelineService pipelineService;

    public GeneratorController(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @GetMapping("/generate")
    public String showGeneratorPage(@RequestParam(value = "sample", required = false) String sample, Model model) {
        String initialCode = "";
        String initialProject = "CBP-TestProject";

        if (sample != null && !sample.trim().isEmpty()) {
            try {
                InputStream is = getClass().getResourceAsStream("/samples/" + sample + ".java");
                if (is != null) {
                    initialCode = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
            } catch (Exception e) {
                logger.warn("Could not load sample: {}", e.getMessage());
            }
        }

        model.addAttribute("selectedSample", sample != null ? sample : "BankAccount");
        model.addAttribute("sourceCode", initialCode);
        model.addAttribute("projectName", initialProject);
        return "generate";
    }

    @PostMapping("/generate")
    public String executeGeneration(@RequestParam("projectName") String projectName,
                                    @RequestParam(value = "sourceCode", required = false) String sourceCode,
                                    @RequestParam(value = "file", required = false) MultipartFile file,
                                    RedirectAttributes redirectAttributes) {
        try {
            String codeToAnalyze = sourceCode;
            if (file != null && !file.isEmpty()) {
                codeToAnalyze = new String(file.getBytes(), StandardCharsets.UTF_8);
            }

            if (codeToAnalyze == null || codeToAnalyze.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Please provide Java source code or upload a .java file.");
                return "redirect:/generate";
            }

            PipelineService.PipelineExecutionResult result = pipelineService.runPipelineForSource(
                    projectName != null && !projectName.trim().isEmpty() ? projectName : "CBP-Project",
                    codeToAnalyze,
                    msg -> logger.info("Progress: {}", msg)
            );

            redirectAttributes.addFlashAttribute("successMessage", "Test suite generated and verified successfully!");
            return "redirect:/results/" + result.getClassId();
        } catch (Exception e) {
            logger.error("Test generation failed", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Execution error: " + e.getMessage());
            return "redirect:/generate";
        }
    }
}
