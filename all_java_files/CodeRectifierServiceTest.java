package com.cbp.testgen.service;

import com.cbp.testgen.analyzer.CodeAnalyzer;
import com.cbp.testgen.analyzer.model.ClassInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CodeRectifierServiceTest {

    private CodeRectifierService rectifierService;
    private CodeAnalyzer codeAnalyzer;

    @BeforeEach
    void setUp() {
        codeAnalyzer = new CodeAnalyzer();
        rectifierService = new CodeRectifierService(null, null, null, null, null, null, null, codeAnalyzer, null);
    }

    @Test
    void testBillingServiceSubFunctionRectification() {
        String sourceCode = """
                package samples;
                public class BillingService {
                    public int processBilling(int totalInvoiceAmount, int installmentsCount) {
                        int base = calculateInstallment(totalInvoiceAmount, installmentsCount);
                        return base + 15;
                    }
                    public int calculateInstallment(int amount, int count) {
                        return amount / count;
                    }
                }
                """;

        ClassInfo classInfo = codeAnalyzer.analyzeSourceCode(sourceCode);

        String stackTrace = """
                java.lang.ArithmeticException: / by zero
                \tat samples.BillingService.calculateInstallment(BillingService.java:8)
                \tat samples.BillingService.processBilling(BillingService.java:4)
                \tat samples.BillingServiceTest.test_processBilling_0(BillingServiceTest.java:10)
                """;

        // Simulate failed test
        CodeRectifierService.RectificationOutput output = rectifierService.synthesizePatchesAndRectify(
                sourceCode,
                classInfo,
                List.of(new CodeRectifierService.FailedTestDetail(null, "/ by zero", stackTrace, "processBilling"))
        );

        assertNotNull(output);
        assertNotNull(output.rectifiedCode);
        assertFalse(output.patches.isEmpty());

        // Verify defensive guards were synthesized
        assertTrue(output.rectifiedCode.contains("count == 0") || output.rectifiedCode.contains("count <= 0"),
                "Rectified code should contain zero guard for count");
        assertTrue(output.rectifiedCode.contains("installmentsCount <= 0"),
                "Rectified code should contain pre-condition guard for caller");

        // Verify patches list
        boolean hasDivideGuard = output.patches.stream()
                .anyMatch(p -> p.getPatchType().equals("DIVIDE_BY_ZERO_GUARD"));
        assertTrue(hasDivideGuard, "Should have DIVIDE_BY_ZERO_GUARD patch");
    }

    @Test
    void testCalculatorDivideByZeroRectification() {
        String sourceCode = """
                package samples;
                public class Calculator {
                    public double divide(double numerator, double denominator) {
                        if (denominator == 0.0) {
                            throw new ArithmeticException("Division by zero");
                        }
                        return numerator / denominator;
                    }
                }
                """;

        ClassInfo classInfo = codeAnalyzer.analyzeSourceCode(sourceCode);

        String stackTrace = """
                java.lang.ArithmeticException: Division by zero
                \tat samples.Calculator.divide(Calculator.java:5)
                \tat samples.CalculatorTest.test_divide_0(CalculatorTest.java:12)
                """;

        CodeRectifierService.RectificationOutput output = rectifierService.synthesizePatchesAndRectify(
                sourceCode,
                classInfo,
                List.of(new CodeRectifierService.FailedTestDetail(null, "Division by zero", stackTrace, "divide"))
        );

        assertNotNull(output);
        assertTrue(output.rectifiedCode.contains("denominator == 0.0") || output.rectifiedCode.contains("return 0.0"),
                "Should contain defensive return for zero denominator");
    }

    @Test
    void testNullPointerRectification() {
        String sourceCode = """
                package samples;
                public class UserService {
                    public String formatName(String name) {
                        return name.toUpperCase();
                    }
                }
                """;

        ClassInfo classInfo = codeAnalyzer.analyzeSourceCode(sourceCode);

        String stackTrace = """
                java.lang.NullPointerException: Cannot invoke String.toUpperCase()
                \tat samples.UserService.formatName(UserService.java:4)
                """;

        CodeRectifierService.RectificationOutput output = rectifierService.synthesizePatchesAndRectify(
                sourceCode,
                classInfo,
                List.of(new CodeRectifierService.FailedTestDetail(null, "NullPointerException", stackTrace, "formatName"))
        );

        assertNotNull(output);
        assertTrue(output.rectifiedCode.contains("name == null"),
                "Should contain null check guard for name parameter");
    }
}
