package com.cbp.testgen;

import com.cbp.testgen.analyzer.CodeAnalyzer;
import com.cbp.testgen.analyzer.model.ClassInfo;
import com.cbp.testgen.analyzer.model.MethodInfo;
import com.cbp.testgen.generator.AssertionQualityAnalyzer;
import com.cbp.testgen.generator.InputGenerator;
import com.cbp.testgen.generator.JUnit5TestGenerator;
import com.cbp.testgen.generator.OopTestGenerator;
import com.cbp.testgen.generator.strategy.BoundaryValueStrategy;
import com.cbp.testgen.generator.strategy.EquivalencePartitionStrategy;
import com.cbp.testgen.generator.strategy.PairwiseCombinator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class CodeAnalyzerTest {

    private CodeAnalyzer codeAnalyzer;
    private JUnit5TestGenerator testGenerator;

    @BeforeEach
    void setUp() {
        codeAnalyzer = new CodeAnalyzer();
        BoundaryValueStrategy boundaryStrategy = new BoundaryValueStrategy();
        EquivalencePartitionStrategy equivalenceStrategy = new EquivalencePartitionStrategy();
        PairwiseCombinator pairwiseCombinator = new PairwiseCombinator();
        InputGenerator inputGenerator = new InputGenerator(boundaryStrategy, equivalenceStrategy, pairwiseCombinator);
        AssertionQualityAnalyzer qualityAnalyzer = new AssertionQualityAnalyzer();
        OopTestGenerator oopTestGenerator = new OopTestGenerator();
        testGenerator = new JUnit5TestGenerator(inputGenerator, oopTestGenerator, qualityAnalyzer);
    }

    @Test
    @DisplayName("Analyze BankAccount sample class")
    void testAnalyzeBankAccount() throws Exception {
        InputStream is = getClass().getResourceAsStream("/samples/BankAccount.java");
        assertNotNull(is, "Sample BankAccount.java resource should exist");
        String code = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        ClassInfo classInfo = codeAnalyzer.analyzeSourceCode(code);
        assertEquals("BankAccount", classInfo.getClassName());
        assertEquals("samples", classInfo.getPackageName());
        assertFalse(classInfo.getMethods().isEmpty());

        MethodInfo withdrawMethod = classInfo.getMethods().stream()
                .filter(m -> "withdraw".equals(m.getMethodName()))
                .findFirst().orElse(null);
        assertNotNull(withdrawMethod);
        assertEquals("double", withdrawMethod.getReturnType());
        assertEquals(1, withdrawMethod.getParameters().size());

        JUnit5TestGenerator.GeneratedTestSuite suite = testGenerator.generateTestSuite(classInfo);
        assertNotNull(suite);
        assertEquals("BankAccountGeneratedTest", suite.getTestClassName());
        assertTrue(suite.getTestCases().size() > 5);
        assertTrue(suite.getFullSourceCode().contains("class BankAccountGeneratedTest"));
    }

    @Test
    @DisplayName("Analyze Calculator sample class")
    void testAnalyzeCalculator() throws Exception {
        InputStream is = getClass().getResourceAsStream("/samples/Calculator.java");
        assertNotNull(is);
        String code = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        ClassInfo classInfo = codeAnalyzer.analyzeSourceCode(code);
        assertEquals("Calculator", classInfo.getClassName());

        JUnit5TestGenerator.GeneratedTestSuite suite = testGenerator.generateTestSuite(classInfo);
        assertNotNull(suite);
        assertTrue(suite.getTestCases().size() >= 10);
    }

    @Test
    @DisplayName("Analyze InventoryManager with OOP Inheritance and Mockito")
    void testAnalyzeInventoryManager() throws Exception {
        InputStream is = getClass().getResourceAsStream("/samples/InventoryManager.java");
        assertNotNull(is);
        String code = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        ClassInfo classInfo = codeAnalyzer.analyzeSourceCode(code);
        assertEquals("InventoryManager", classInfo.getClassName());
        assertEquals("BaseManager", classInfo.getSuperClass());
        assertTrue(classInfo.hasSuperClass());

        JUnit5TestGenerator.GeneratedTestSuite suite = testGenerator.generateTestSuite(classInfo);
        assertNotNull(suite);
        assertTrue(suite.getFullSourceCode().contains("lsp_polymorphicSubstitution_satisfiesParentContract"));
    }
}
