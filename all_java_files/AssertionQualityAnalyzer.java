package com.cbp.testgen.generator;

import org.springframework.stereotype.Component;

@Component
public class AssertionQualityAnalyzer {

    public static class AssertionQualityResult {
        private final int assertionCount;
        private final boolean isWeak;
        private final String qualityRating; // "STRONG", "MODERATE", "WEAK"
        private final String explanation;

        public AssertionQualityResult(int assertionCount, boolean isWeak, String qualityRating, String explanation) {
            this.assertionCount = assertionCount;
            this.isWeak = isWeak;
            this.qualityRating = qualityRating;
            this.explanation = explanation;
        }

        public int getAssertionCount() {
            return assertionCount;
        }

        public boolean isWeak() {
            return isWeak;
        }

        public String getQualityRating() {
            return qualityRating;
        }

        public String getExplanation() {
            return explanation;
        }
    }

    public AssertionQualityResult evaluateTestQuality(String testCode) {
        if (testCode == null || testCode.trim().isEmpty()) {
            return new AssertionQualityResult(0, true, "WEAK", "Empty test body");
        }

        int assertCount = 0;
        String[] keywords = {"assertNotNull", "assertEquals", "assertTrue", "assertFalse",
                "assertThrows", "assertArrayEquals", "assertSame", "assertNotSame", "assertAll", "verify("};

        for (String kw : keywords) {
            int index = 0;
            while ((index = testCode.indexOf(kw, index)) != -1) {
                assertCount++;
                index += kw.length();
            }
        }

        boolean isWeak = (assertCount == 0);
        String rating;
        String explanation;

        if (assertCount == 0) {
            rating = "WEAK";
            explanation = "Weak test: exercises code execution without verifying output, state invariants, or exceptions.";
        } else if (assertCount == 1) {
            rating = "MODERATE";
            explanation = "Moderate test: contains single assertion verification.";
        } else {
            rating = "STRONG";
            explanation = "Strong test: contains multi-point assertions verifying outputs, state, and invariants.";
        }

        return new AssertionQualityResult(assertCount, isWeak, rating, explanation);
    }
}
