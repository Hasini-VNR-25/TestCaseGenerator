package com.cbp.testgen;

import com.cbp.testgen.database.entity.ClassMetadataEntity;
import com.cbp.testgen.database.repository.ClassMetadataRepository;
import com.cbp.testgen.service.PipelineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PipelineIntegrationTest {

    @Autowired
    private PipelineService pipelineService;

    @Autowired
    private ClassMetadataRepository classMetadataRepository;

    @Autowired
    private com.cbp.testgen.database.repository.MutationResultRepository mutationResultRepository;

    @Test
    @DisplayName("End-to-End Pipeline Execution on BankAccount")
    void testBankAccountPipeline() throws Exception {
        InputStream is = getClass().getResourceAsStream("/samples/BankAccount.java");
        assertNotNull(is);
        String code = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        PipelineService.PipelineExecutionResult result = pipelineService.runPipelineForSource(
                "BankAccount-CBP",
                code,
                msg -> System.out.println("[PROGRESS] " + msg)
        );

        assertNotNull(result);
        assertEquals("BankAccount", result.getClassInfo().getClassName());
        assertTrue(result.getTestSuite().getTestCases().size() > 5);
        assertTrue(result.getExecutionSummary().getTotalCount() > 5);
        assertTrue(result.getCoverageSummary().getLineCoveragePct() > 50.0);
        assertTrue(result.getMutationSummary().getTotalMutants() > 0);
        assertTrue(result.getMutationSummary().getMutationScorePct() > 0.0);
        assertNotNull(result.getMinimizationReport());
        assertTrue(result.getMinimizationReport().getMinimizedTestCount() <= result.getMinimizationReport().getInitialTestCount());

        // Verify Database Persistence
        List<ClassMetadataEntity> entities = classMetadataRepository.findAll();
        assertFalse(entities.isEmpty());

        // Verify Mutation details JSON is valid and deserializable
        com.cbp.testgen.database.entity.MutationResultEntity mutEntity = mutationResultRepository
                .findTopByClassMetadataIdOrderByTestRunRunTimestampDesc(result.getClassId())
                .orElse(null);
        assertNotNull(mutEntity, "MutationResultEntity should be recorded in database");
        assertNotNull(mutEntity.getMutantDetailsJson(), "Mutant details JSON should not be null");
        assertFalse(mutEntity.getMutantDetailsJson().isEmpty(), "Mutant details JSON should not be empty");

        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper()
                .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<com.cbp.testgen.mutation.Mutant> deserializedMutants = mapper.readValue(
                mutEntity.getMutantDetailsJson(),
                new com.fasterxml.jackson.core.type.TypeReference<List<com.cbp.testgen.mutation.Mutant>>() {}
        );
        assertFalse(deserializedMutants.isEmpty(), "Deserialized mutants list must not be empty");
        assertTrue(deserializedMutants.stream().anyMatch(m -> m.getStatus() == com.cbp.testgen.mutation.Mutant.Status.KILLED), "At least one mutant should be marked KILLED");
    }

    @Test
    @DisplayName("End-to-End Pipeline Execution on Calculator")
    void testCalculatorPipeline() throws Exception {
        InputStream is = getClass().getResourceAsStream("/samples/Calculator.java");
        assertNotNull(is);
        String code = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        PipelineService.PipelineExecutionResult result = pipelineService.runPipelineForSource(
                "Calculator-CBP",
                code,
                msg -> System.out.println("[PROGRESS] " + msg)
        );

        assertNotNull(result);
        assertEquals("Calculator", result.getClassInfo().getClassName());
        assertTrue(result.getMutationSummary().getTotalMutants() >= 5);
        assertTrue(result.getMutationSummary().getMutantsKilled() > 0);
        assertTrue(result.getMutationSummary().getMutationScorePct() >= 50.0);
    }

    @Test
    @DisplayName("End-to-End Pipeline Execution on InventoryManager (OOP + Mockito)")
    void testInventoryManagerPipeline() throws Exception {
        InputStream is = getClass().getResourceAsStream("/samples/InventoryManager.java");
        assertNotNull(is);
        String code = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        PipelineService.PipelineExecutionResult result = pipelineService.runPipelineForSource(
                "Inventory-CBP",
                code,
                msg -> System.out.println("[PROGRESS] " + msg)
        );

        assertNotNull(result);
        assertEquals("InventoryManager", result.getClassInfo().getClassName());
        assertTrue(result.getTestSuite().getFullSourceCode().contains("lsp_polymorphicSubstitution_satisfiesParentContract"));
        assertTrue(result.getExecutionSummary().getPassedCount() > 0);
    }
}
