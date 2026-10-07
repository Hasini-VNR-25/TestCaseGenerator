package com.cbp.testgen.mutation;

import com.cbp.testgen.analyzer.model.ClassInfo;
import com.cbp.testgen.executor.DynamicCompiler;
import com.cbp.testgen.executor.TestExecutor;
import com.cbp.testgen.generator.JUnit5TestGenerator;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.stmt.ReturnStmt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MutationEngine {

    private static final Logger logger = LoggerFactory.getLogger(MutationEngine.class);
    private final JavaParser javaParser;
    private final DynamicCompiler dynamicCompiler;
    private final TestExecutor testExecutor;

    public MutationEngine(DynamicCompiler dynamicCompiler, TestExecutor testExecutor) {
        this.javaParser = new JavaParser();
        this.dynamicCompiler = dynamicCompiler;
        this.testExecutor = testExecutor;
    }

    public List<Mutant> generateMutants(ClassInfo classInfo) {
        List<Mutant> mutants = new ArrayList<>();
        CompilationUnit originalCu = javaParser.parse(classInfo.getSourceCode()).getResult().orElse(null);
        if (originalCu == null) {
            return mutants;
        }

        AtomicInteger mutantCounter = new AtomicInteger(1);

        for (MethodDeclaration method : originalCu.findAll(MethodDeclaration.class)) {
            String methodName = method.getNameAsString();

            // 1. Mutate Binary Expressions (Relational, Arithmetic, Logical)
            for (BinaryExpr binExpr : method.findAll(BinaryExpr.class)) {
                BinaryExpr.Operator origOp = binExpr.getOperator();
                List<BinaryExpr.Operator> targetOps = getAlternateOperators(origOp);
                int line = binExpr.getBegin().map(p -> p.line).orElse(0);

                for (BinaryExpr.Operator altOp : targetOps) {
                    MutationOperator mop = getMutationOperatorCategory(origOp);
                    CompilationUnit mutantCu = originalCu.clone();
                    MethodDeclaration mClone = mutantCu.findAll(MethodDeclaration.class).stream()
                            .filter(m -> m.getNameAsString().equals(methodName))
                            .findFirst().orElse(null);

                    if (mClone != null) {
                        List<BinaryExpr> exprs = mClone.findAll(BinaryExpr.class);
                        for (BinaryExpr be : exprs) {
                            if (be.getOperator() == origOp && be.getBegin().map(p -> p.line).orElse(0) == line) {
                                String origText = be.toString();
                                be.setOperator(altOp);
                                String mutText = be.toString();
                                String mutantId = "MUT_" + mutantCounter.getAndIncrement();
                                mutants.add(new Mutant(mutantId, methodName, line, mop, origText, mutText, mutantCu.toString()));
                                break;
                            }
                        }
                    }
                }
            }

            // 2. Mutate Boolean Literals (true <-> false)
            for (BooleanLiteralExpr boolExpr : method.findAll(BooleanLiteralExpr.class)) {
                int line = boolExpr.getBegin().map(p -> p.line).orElse(0);
                boolean origVal = boolExpr.getValue();
                CompilationUnit mutantCu = originalCu.clone();
                MethodDeclaration mClone = mutantCu.findAll(MethodDeclaration.class).stream()
                        .filter(m -> m.getNameAsString().equals(methodName))
                        .findFirst().orElse(null);

                if (mClone != null) {
                    List<BooleanLiteralExpr> bools = mClone.findAll(BooleanLiteralExpr.class);
                    for (BooleanLiteralExpr ble : bools) {
                        if (ble.getValue() == origVal && ble.getBegin().map(p -> p.line).orElse(0) == line) {
                            String origText = ble.toString();
                            ble.setValue(!origVal);
                            String mutText = ble.toString();
                            String mutantId = "MUT_" + mutantCounter.getAndIncrement();
                            mutants.add(new Mutant(mutantId, methodName, line, MutationOperator.BOOLEAN_OPERATOR_NEGATION,
                                    origText, mutText, mutantCu.toString()));
                            break;
                        }
                    }
                }
            }

            // 3. Mutate Return Statements
            for (ReturnStmt retStmt : method.findAll(ReturnStmt.class)) {
                if (retStmt.getExpression().isPresent()) {
                    int line = retStmt.getBegin().map(p -> p.line).orElse(0);
                    String origText = retStmt.toString();
                    CompilationUnit mutantCu = originalCu.clone();
                    MethodDeclaration mClone = mutantCu.findAll(MethodDeclaration.class).stream()
                            .filter(m -> m.getNameAsString().equals(methodName))
                            .findFirst().orElse(null);

                    if (mClone != null) {
                        List<ReturnStmt> rets = mClone.findAll(ReturnStmt.class);
                        for (ReturnStmt rs : rets) {
                            if (rs.getBegin().map(p -> p.line).orElse(0) == line) {
                                String returnType = method.getTypeAsString();
                                String mutText;
                                if ("int".equals(returnType) || "long".equals(returnType)) {
                                    rs.setExpression(new IntegerLiteralExpr("0"));
                                } else if ("boolean".equals(returnType)) {
                                    rs.setExpression(new BooleanLiteralExpr(false));
                                } else if ("double".equals(returnType) || "float".equals(returnType)) {
                                    rs.setExpression(new IntegerLiteralExpr("0"));
                                }
                                mutText = rs.toString();
                                String mutantId = "MUT_" + mutantCounter.getAndIncrement();
                                mutants.add(new Mutant(mutantId, methodName, line, MutationOperator.RETURN_VALUE_MUTATION,
                                        origText, mutText, mutantCu.toString()));
                                break;
                            }
                        }
                    }
                }
            }
        }

        logger.info("Generated {} mutants for class {}", mutants.size(), classInfo.getClassName());
        return mutants;
    }

    public MutationResultSummary evaluateTestSuiteOnMutants(ClassInfo classInfo,
                                                           JUnit5TestGenerator.GeneratedTestSuite testSuite,
                                                           Map<String, String> extraSources) {
        List<Mutant> mutants = generateMutants(classInfo);
        if (mutants.size() > 20) {
            mutants = mutants.subList(0, 20);
        }
        int totalMutants = mutants.size();
        if (totalMutants == 0) {
            return new MutationResultSummary(classInfo.getClassName(), 0, 0, 0, 100.0, mutants, Map.of());
        }

        int killedCount = 0;
        int survivedCount = 0;
        Map<String, Integer> methodTotal = new HashMap<>();
        Map<String, Integer> methodKilled = new HashMap<>();

        String testRelPath = (testSuite.getPackageName().isEmpty() ? "" : testSuite.getPackageName().replace('.', '/') + "/")
                + testSuite.getTestClassName() + ".java";
        String classRelPath = (classInfo.getPackageName().isEmpty() ? "" : classInfo.getPackageName().replace('.', '/') + "/")
                + classInfo.getClassName() + ".java";

        for (Mutant mutant : mutants) {
            methodTotal.put(mutant.getMethodName(), methodTotal.getOrDefault(mutant.getMethodName(), 0) + 1);

            Map<String, String> sources = new HashMap<>(extraSources != null ? extraSources : Map.of());
            sources.put(classRelPath, mutant.getMutatedSourceCode());
            sources.put(testRelPath, testSuite.getFullSourceCode());

            try {
                DynamicCompiler.CompilationResult compResult = dynamicCompiler.compileSources(sources);
                if (!compResult.isSuccess()) {
                    mutant.setStatus(Mutant.Status.COMPILE_ERROR);
                    mutant.setExecutionDetails("Mutant caused compilation failure (equivalent/invalid)");
                    continue;
                }

                Class<?> testClass = compResult.getClassLoader().loadClass(
                        (testSuite.getPackageName().isEmpty() ? "" : testSuite.getPackageName() + ".") + testSuite.getTestClassName()
                );

                TestExecutor.TestExecutionSummary execSummary = testExecutor.executeTestClass(testClass, compResult.getClassLoader());

                if (execSummary.getFailedCount() > 0 || execSummary.getErrorCount() > 0) {
                    // Mutant was killed!
                    mutant.setStatus(Mutant.Status.KILLED);
                    killedCount++;
                    methodKilled.put(mutant.getMethodName(), methodKilled.getOrDefault(mutant.getMethodName(), 0) + 1);

                    TestExecutor.SingleTestResult killer = execSummary.getResults().stream()
                            .filter(r -> !r.isPassed())
                            .findFirst()
                            .orElse(null);

                    if (killer != null) {
                        mutant.setKillingTestName(killer.getTestName());
                        mutant.setExecutionDetails("Killed by test: " + killer.getTestName() + " (" + (killer.getErrorMessage() != null ? killer.getErrorMessage() : "assertion failure") + ")");
                    }
                } else {
                    // Mutant survived!
                    mutant.setStatus(Mutant.Status.SURVIVED);
                    survivedCount++;
                    mutant.setExecutionDetails("Mutant survived: test suite passed without catching defect.");
                }
            } catch (Exception e) {
                logger.warn("Error running mutant {}: {}", mutant.getId(), e.getMessage());
                mutant.setStatus(Mutant.Status.COMPILE_ERROR);
            }
        }

        int viableMutants = killedCount + survivedCount;
        double mutationScorePct = viableMutants > 0
                ? Math.round(((double) killedCount / viableMutants * 100.0) * 10.0) / 10.0
                : 100.0;

        Map<String, Double> methodScores = new HashMap<>();
        for (String mName : methodTotal.keySet()) {
            int tot = methodTotal.get(mName);
            int kld = methodKilled.getOrDefault(mName, 0);
            double mScore = tot > 0 ? Math.round(((double) kld / tot * 100.0) * 10.0) / 10.0 : 100.0;
            methodScores.put(mName, mScore);
        }

        logger.info("Mutation Analysis for {}: Total={}, Killed={}, Survived={}, Score={}%",
                classInfo.getClassName(), totalMutants, killedCount, survivedCount, mutationScorePct);

        return new MutationResultSummary(
                classInfo.getClassName(),
                totalMutants,
                killedCount,
                survivedCount,
                mutationScorePct,
                mutants,
                methodScores
        );
    }

    private List<BinaryExpr.Operator> getAlternateOperators(BinaryExpr.Operator op) {
        List<BinaryExpr.Operator> list = new ArrayList<>();
        switch (op) {
            case GREATER -> list.add(BinaryExpr.Operator.GREATER_EQUALS);
            case GREATER_EQUALS -> list.add(BinaryExpr.Operator.GREATER);
            case LESS -> list.add(BinaryExpr.Operator.LESS_EQUALS);
            case LESS_EQUALS -> list.add(BinaryExpr.Operator.LESS);
            case EQUALS -> list.add(BinaryExpr.Operator.NOT_EQUALS);
            case NOT_EQUALS -> list.add(BinaryExpr.Operator.EQUALS);
            case PLUS -> list.add(BinaryExpr.Operator.MINUS);
            case MINUS -> list.add(BinaryExpr.Operator.PLUS);
            case MULTIPLY -> list.add(BinaryExpr.Operator.DIVIDE);
            case DIVIDE -> list.add(BinaryExpr.Operator.MULTIPLY);
            case AND -> list.add(BinaryExpr.Operator.OR);
            case OR -> list.add(BinaryExpr.Operator.AND);
            default -> {}
        }
        return list;
    }

    private MutationOperator getMutationOperatorCategory(BinaryExpr.Operator op) {
        return switch (op) {
            case GREATER, GREATER_EQUALS, LESS, LESS_EQUALS, EQUALS, NOT_EQUALS -> MutationOperator.RELATIONAL_OPERATOR_SWAP;
            case PLUS, MINUS, MULTIPLY, DIVIDE, REMAINDER -> MutationOperator.ARITHMETIC_OPERATOR_SWAP;
            case AND, OR -> MutationOperator.BOOLEAN_OPERATOR_NEGATION;
            default -> MutationOperator.RELATIONAL_OPERATOR_SWAP;
        };
    }
}
