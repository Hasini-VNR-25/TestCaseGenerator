# Complete Java Files Study Guide & Architecture Index

This folder (`all_java_files`) contains a flat, single-folder copy of **all Java source files** in the project, allowing you to study them sequentially line-by-line without navigating through nested package directory hierarchies.

---

## 🧭 High-Level Architecture Overview

```
                      ┌──────────────────────────────────────────┐
                      │        TestGeneratorApplication          │
                      └────────────────────┬─────────────────────┘
                                           │
                      ┌────────────────────▼─────────────────────┐
                      │    Web & REST Controllers (Spring Boot)  │
                      │ Generator, Dashboard, Results, History   │
                      └────────────────────┬─────────────────────┘
                                           │
                      ┌────────────────────▼─────────────────────┐
                      │             PipelineService              │
                      │       (Master Pipeline Orchestrator)     │
                      └────────┬───────────┬───────────┬─────────┘
                               │           │           │
           ┌───────────────────┘           │           └──────────────────┐
           ▼                               ▼                              ▼
┌───────────────────────┐       ┌───────────────────────┐      ┌───────────────────────┐
│     CodeAnalyzer      │       │     InputGenerator    │      │  JUnit5TestGenerator  │
│  (JavaParser / AST)   │       │  (BVA, ECP, Pairwise) │      │  (AAA Pattern Tests)  │
└──────────┬────────────┘       └──────────┬────────────┘      └──────────┬────────────┘
           │                               │                              │
           └───────────────────────────────┼──────────────────────────────┘
                                           ▼
                                ┌───────────────────────┐
                                │    DynamicCompiler    │
                                │ (javax.tools Compiler)│
                                └──────────┬────────────┘
                                           │
                                ┌──────────▼────────────┐
                                │     TestExecutor      │
                                │(JUnit Platform/JaCoCo)│
                                └──────────┬────────────┘
                                           │
           ┌───────────────────────────────┼──────────────────────────────┐
           ▼                               ▼                              ▼
┌───────────────────────┐       ┌───────────────────────┐      ┌───────────────────────┐
│    MutationEngine     │       │   TestSuiteMinimizer  │      │ RegressionPrioritizer │
│ (PIT-style AST Mutants)│      │  (Greedy Set-Cover)   │      │(Diff-driven Prioritize│
└──────────┬────────────┘       └──────────┬────────────┘      └──────────┬────────────┘
           │                               │                              │
           └───────────────────────────────┼──────────────────────────────┘
                                           ▼
                                ┌───────────────────────┐
                                │    DatabaseManager    │
                                │ (10 3NF JPA Entities) │
                                └───────────────────────┘
```

---

## 📖 Recommended Step-by-Step Reading Roadmap

To understand the codebase line by line, follow this curated order from foundational data structures to core algorithms, pipeline execution, and web controllers:

### Phase 1: Data Models & AST Representations (Understand the Core Entities)
Start here to see how code structures are represented in memory:
1. `ClassInfo.java` — Holds parsed metadata of a class (package, name, modifiers, constructors, methods, fields, superclass, interfaces).
2. `MethodInfo.java` — Holds parsed method details (name, return type, parameters, modifiers, throws, cyclomatic complexity).
3. `FieldInfo.java` — Represents class fields and their types.
4. `ParameterInfo.java` — Represents method parameters.
5. `InputValue.java` — Wrapper for generated input values with description and equivalence category.
6. `Mutant.java` — Represents an AST-mutated variant of the class (mutant ID, mutation operator, line number, mutated code).
7. `MutationResultSummary.java` — Aggregates mutation testing outcomes (mutants killed, survived, score %).

---

### Phase 2: Sample Classes Under Test (The Target Programs)
Understand the code that the generator actually analyzes and tests:
8. `Calculator.java` — Simple arithmetic operations (addition, subtraction, multiplication, division, modulo, absolute value, factorial) with edge cases (division by zero, negative factorial).
9. `BankAccount.java` — Encapsulated stateful entity (balance, deposit, withdraw, account locks, transfer).
10. `BaseManager.java` — Abstract base class illustrating OOP inheritance and template methods.
11. `InventoryManager.java` — Subclass of `BaseManager` using dependency injection (`NotificationService`, `ProductRepository`).
12. `BillingService.java` — Multi-branch financial logic with installment calculations.
13. `ProductRepository.java` — Interface collaborator for mock testing.
14. `NotificationService.java` — Interface collaborator for mock testing.

*(Note: Files prefixed with `ResourceSample_` in this folder are identical copies from `src/main/resources/samples/` containing additional Javadoc explanations used by the UI).*

---

### Phase 3: Code Parsing & AST Analysis
Learn how source code text is converted into abstract syntax trees and inspected:
15. `CodeAnalyzer.java` — Uses **JavaParser** (`CompilationUnit`, `ClassOrInterfaceDeclaration`, `MethodDeclaration`) to parse source code, compute cyclomatic complexity, calculate SHA-256 source hash, and construct `ClassInfo`.
16. `AstDiffAnalyzer.java` — Compares two versions of a Java class at AST level to detect which methods were added, removed, or modified.

---

### Phase 4: Test Input Generation & Software Engineering Strategies
Learn how inputs are intelligently chosen using testing theory:
17. `InputGenerationStrategy.java` — Strategy design pattern interface (`generateInputs(ParameterInfo)`).
18. `BoundaryValueStrategy.java` — **BVA (Boundary Value Analysis)**: Generates boundaries (e.g. `0`, `1`, `-1`, `Integer.MAX_VALUE`, `Integer.MIN_VALUE`, empty strings, null).
19. `EquivalencePartitionStrategy.java` — **ECP (Equivalence Class Partitioning)**: Partitions input domains into valid and invalid classes.
20. `PairwiseCombinator.java` — **Combinatorial All-Pairs Testing**: Generates minimal 2-way orthogonal combinations to prevent exponential test explosion for methods with 3+ parameters.
21. `InputGenerator.java` — Orchestrates strategies, combining values for multi-parameter methods.
22. `OopTestGenerator.java` — Generates advanced OOP test patterns:
    - Mockito mock injection for interfaces/collaborators.
    - Liskov Substitution Principle (LSP) checks.
    - Stateful sequence tests (setter-then-getter invariant checks).
23. `AssertionQualityAnalyzer.java` — Evaluates generated test assertions; flags weak or missing assertions.
24. `JUnit5TestGenerator.java` — Generates complete, compilable JUnit 5 Jupiter test classes following the **Arrange-Act-Assert (AAA)** pattern with `@Test`, `@DisplayName`, Assertions, and Mockito annotations.

---

### Phase 5: Dynamic In-Memory Compilation & Execution Engine
Learn how the code and its tests are compiled and executed at runtime:
25. `DynamicCompiler.java` — Compiles Java source strings entirely in memory using standard `javax.tools.JavaCompiler` and a custom `SimpleJavaFileObject` and classloader.
26. `TestExecutor.java` — Executes compiled test classes using JUnit 5 Platform `Launcher`, captures stdout/stderr, measures execution time, and records JaCoCo code coverage (line and branch coverage).
27. `FlakyTestDetector.java` — Executes a test suite multiple times in succession to identify non-deterministic or flaky tests.

---

### Phase 6: Mutation Testing & Test Suite Optimization
Learn how test suite effectiveness is measured and optimized:
28. `MutationOperator.java` — Defines mutation operators:
    - Relational swaps (`>` $\leftrightarrow$ `>=`, `==` $\leftrightarrow$ `!=`)
    - Arithmetic swaps (`+` $\leftrightarrow$ `-`, `*` $\leftrightarrow$ `/`)
    - Boolean negation (`!`, `&&` $\leftrightarrow$ `||`)
    - Conditional boundary mutations
29. `MutationEngine.java` — Uses JavaParser AST visitors to generate mutants, dynamically compiles each mutant, and executes tests against it to calculate **Mutation Score = (Mutants Killed / Total Mutants) * 100%**.
30. `TestSuiteMinimizer.java` — Implements the **Greedy Set-Cover Algorithm**: finds the smallest subset of tests that maintains 100% of the original mutant and branch coverage, eliminating redundant tests.
31. `RegressionPrioritizer.java` — Ranks and orders test cases based on:
    - Touch of modified methods (via `AstDiffAnalyzer`).
    - Historic failure rate.
    - Execution duration (fast failing tests run first).

---

### Phase 7: Database & Persistence Layer (3NF Relational Schema)
Learn how test results, coverage, mutation scores, and class versions are stored:
32. **Entities (`database/entity/`):**
    - `ProjectEntity.java` — Represents projects.
    - `ClassMetadataEntity.java` — Represents analyzed class metadata and source code.
    - `MethodMetadataEntity.java` — Represents methods within a class.
    - `TestCaseEntity.java` — Represents generated test methods.
    - `TestRunEntity.java` — Represents an execution session/run.
    - `TestResultEntity.java` — Represents individual test execution outcomes.
    - `CoverageResultEntity.java` — Line, branch, and instruction coverage metrics.
    - `MutationResultEntity.java` — Mutation testing outcomes per mutant.
    - `FlakyTestEntity.java` — Records detected flaky tests.
    - `ClassVersionEntity.java` — Tracks source revisions and hashes across commits.
33. **Repositories (`database/repository/`):**
    - `ProjectRepository.java`
    - `ClassMetadataRepository.java`
    - `MethodMetadataRepository.java`
    - `TestCaseRepository.java`
    - `TestRunRepository.java`
    - `TestResultRepository.java`
    - `CoverageResultRepository.java`
    - `MutationResultRepository.java`
    - `FlakyTestRepository.java`
    - `ClassVersionRepository.java`
34. `DatabaseManager.java` — High-level transactional service wrapping database repositories, managing runs, and executing SQL queries for the SQL console.

---

### Phase 8: Core Services & AI Integration
Learn how the pipeline is orchestrated and how automated fixes and LLM validation work:
35. `CodeRectifierService.java` — Parses Java exception stack traces (e.g. `ArithmeticException`, `NullPointerException`, `AssertionFailedError`) and recommends code rectifications and patches.
36. `PipelineService.java` — The central orchestrator: chains AST Analysis $\to$ Input Generation $\to$ Test Generation $\to$ Dynamic Compilation $\to$ Test Execution $\to$ Coverage Analysis $\to$ Mutation Testing $\to$ Minimization $\to$ Database Persistence.
37. `LlmVerificationService.java` — AI loop that proposes test cases and verifies them via dynamic compilation and mutant killing before accepting.
38. `HtmlReportGenerator.java` — Generates a self-contained, interactive HTML report with test counts, coverage bars, mutation breakdown, and minimized suite details.

---

### Phase 9: Spring Boot Web Layer (UI & REST Endpoints)
Learn how the application exposes its capabilities:
39. `TestGeneratorApplication.java` — Spring Boot `@SpringBootApplication` entry point.
40. `GeneratorController.java` — Handles the main test generation wizard UI, sample loading, and pipeline triggers.
41. `DashboardController.java` — Displays overall system health, total runs, coverage metrics, and charts.
42. `ResultsController.java` — Renders test run results, test case tables, mutant outcomes, and rectified suggestions.
43. `HistoryController.java` — Displays past test runs and regression trends.
44. `SqlConsoleController.java` & `SqlConsoleApiController.java` — Provides an interactive SQL console to query the 3NF database directly.
45. `ExportController.java` — Handles file downloads (`.java` test file and `.html` report).
46. `ApiController.java` — REST API endpoints for external integrations.
47. `GlobalSidebarAdvice.java` — Controller advice populating sidebar navigation and project metadata.

---

### Phase 10: Automated Test Suite (Testing the Tool Itself)
Understand how the test generator's own components are verified:
48. `CodeAnalyzerTest.java` — Unit tests for JavaParser AST parsing.
49. `PipelineIntegrationTest.java` — End-to-end integration tests running the full pipeline on sample classes.
50. `CodeRectifierServiceTest.java` — Tests exception diagnosis and patch recommendation.
51. `SmartRegressionTakeawayTest.java` — Tests regression prioritization across class versions.
52. `WebEndpointsTest.java` — Tests HTTP endpoints and Spring MVC controllers.
53. `SqlConsoleApiControllerTest.java` — Tests SQL console query execution and sanitization.
54. `ResultsControllerVulnerabilityTest.java` — Tests security and edge cases in results rendering.

---

### Phase 11: Classroom Training Programs (`classroom_batch_3225/`)
The subfolder `classroom_batch_3225/` contains the 23 core Java training exercises from the parent folder:
- **Language Fundamentals:** `Hello.java`, `UserInput.java`, `PrimeRange.java`, `PrimeInRange.java`, `CharTests.java`, `Switch.java`, `Quadratic.java`, `OneDimArray.java`.
- **OOP Concepts:** `BoxDemo.java.java`, `This2Form.java`, `CurConverter.java`, `AbstractClasses.java`, `DMD.java.java` (Dynamic Method Dispatch), `MethodOverRiding.java`, `MethodOverRiding1.java`, `Driver.java`, `Driver3.java`, `Version2.java`.
- **Advanced Features:** `MultiThreading.java`, `RunnableMT.java`, `BusReservation.java` (synchronization), `CCLambda.java` (Lambda expressions), `Test.java`.

---

## 📋 Full File Inventory in `all_java_files`

| File Name | Original Layer / Package | Key Responsibility |
|---|---|---|
| `TestGeneratorApplication.java` | `com.cbp.testgen` | Spring Boot Main Entry Point |
| `CodeAnalyzer.java` | `com.cbp.testgen.analyzer` | AST parser & complexity analyzer using JavaParser |
| `AstDiffAnalyzer.java` | `com.cbp.testgen.analyzer` | Computes AST differences between class versions |
| `ClassInfo.java` | `com.cbp.testgen.analyzer.model` | Extracted class metadata model |
| `MethodInfo.java` | `com.cbp.testgen.analyzer.model` | Extracted method metadata model |
| `FieldInfo.java` | `com.cbp.testgen.analyzer.model` | Extracted field metadata model |
| `ParameterInfo.java` | `com.cbp.testgen.analyzer.model` | Extracted parameter metadata model |
| `CoverageAnalyzer.java` | `com.cbp.testgen.coverage` | JaCoCo code coverage analyzer |
| `DynamicCompiler.java` | `com.cbp.testgen.executor` | In-memory Java compiler via `javax.tools` |
| `TestExecutor.java` | `com.cbp.testgen.executor` | JUnit 5 Launcher execution engine |
| `FlakyTestDetector.java` | `com.cbp.testgen.executor` | Multi-run flaky test detector |
| `InputGenerator.java` | `com.cbp.testgen.generator` | Coordinates input generation strategies |
| `JUnit5TestGenerator.java` | `com.cbp.testgen.generator` | Generates compilable AAA JUnit 5 test classes |
| `OopTestGenerator.java` | `com.cbp.testgen.generator` | Generates Mockito, LSP, and state sequence tests |
| `AssertionQualityAnalyzer.java` | `com.cbp.testgen.generator` | Evaluates assertion strength |
| `BoundaryValueStrategy.java` | `com.cbp.testgen.generator.strategy` | Boundary Value Analysis (BVA) strategy |
| `EquivalencePartitionStrategy.java` | `com.cbp.testgen.generator.strategy` | Equivalence Class Partitioning (ECP) strategy |
| `PairwiseCombinator.java` | `com.cbp.testgen.generator.strategy` | Orthogonal 2-way pairwise combinations |
| `InputGenerationStrategy.java` | `com.cbp.testgen.generator.strategy` | Strategy interface for input generators |
| `InputValue.java` | `com.cbp.testgen.generator.strategy` | Value and type representation |
| `MutationEngine.java` | `com.cbp.testgen.mutation` | AST-based mutation testing engine |
| `MutationOperator.java` | `com.cbp.testgen.mutation` | Mutation operator definitions |
| `Mutant.java` | `com.cbp.testgen.mutation` | Mutated variant representation |
| `MutationResultSummary.java` | `com.cbp.testgen.mutation` | Mutation score and summary model |
| `TestSuiteMinimizer.java` | `com.cbp.testgen.optimizer` | Greedy Set-Cover test suite minimization |
| `RegressionPrioritizer.java` | `com.cbp.testgen.optimizer` | AST-diff-based regression test prioritization |
| `HtmlReportGenerator.java` | `com.cbp.testgen.report` | Self-contained HTML report builder |
| `PipelineService.java` | `com.cbp.testgen.service` | Master pipeline service |
| `CodeRectifierService.java` | `com.cbp.testgen.service` | Stack trace failure diagnosis & rectifications |
| `LlmVerificationService.java` | `com.cbp.testgen.ai` | AI test suggestion and verification loop |
| `DatabaseManager.java` | `com.cbp.testgen.database.service` | Database operations facade |
| `ProjectEntity.java` | `com.cbp.testgen.database.entity` | 3NF Entity: projects |
| `ClassMetadataEntity.java` | `com.cbp.testgen.database.entity` | 3NF Entity: classes |
| `MethodMetadataEntity.java` | `com.cbp.testgen.database.entity` | 3NF Entity: methods |
| `TestCaseEntity.java` | `com.cbp.testgen.database.entity` | 3NF Entity: test_cases |
| `TestRunEntity.java` | `com.cbp.testgen.database.entity` | 3NF Entity: test_runs |
| `TestResultEntity.java` | `com.cbp.testgen.database.entity` | 3NF Entity: test_results |
| `CoverageResultEntity.java` | `com.cbp.testgen.database.entity` | 3NF Entity: coverage_results |
| `MutationResultEntity.java` | `com.cbp.testgen.database.entity` | 3NF Entity: mutation_results |
| `FlakyTestEntity.java` | `com.cbp.testgen.database.entity` | 3NF Entity: flaky_tests |
| `ClassVersionEntity.java` | `com.cbp.testgen.database.entity` | 3NF Entity: class_versions |
| `ProjectRepository.java` | `com.cbp.testgen.database.repository` | Spring Data JPA Repository |
| `ClassMetadataRepository.java` | `com.cbp.testgen.database.repository` | Spring Data JPA Repository |
| `MethodMetadataRepository.java` | `com.cbp.testgen.database.repository` | Spring Data JPA Repository |
| `TestCaseRepository.java` | `com.cbp.testgen.database.repository` | Spring Data JPA Repository |
| `TestRunRepository.java` | `com.cbp.testgen.database.repository` | Spring Data JPA Repository |
| `TestResultRepository.java` | `com.cbp.testgen.database.repository` | Spring Data JPA Repository |
| `CoverageResultRepository.java` | `com.cbp.testgen.database.repository` | Spring Data JPA Repository |
| `MutationResultRepository.java` | `com.cbp.testgen.database.repository` | Spring Data JPA Repository |
| `FlakyTestRepository.java` | `com.cbp.testgen.database.repository` | Spring Data JPA Repository |
| `ClassVersionRepository.java` | `com.cbp.testgen.database.repository` | Spring Data JPA Repository |
| `GeneratorController.java` | `com.cbp.testgen.web` | Web controller for test generation |
| `DashboardController.java` | `com.cbp.testgen.web` | Web controller for main dashboard |
| `ResultsController.java` | `com.cbp.testgen.web` | Web controller for results view |
| `HistoryController.java` | `com.cbp.testgen.web` | Web controller for test run history |
| `SqlConsoleController.java` | `com.cbp.testgen.web` | Web controller for interactive SQL console |
| `SqlConsoleApiController.java` | `com.cbp.testgen.web` | REST API for SQL query execution |
| `ExportController.java` | `com.cbp.testgen.web` | Controller for test code / report export |
| `ApiController.java` | `com.cbp.testgen.web` | General REST API endpoints |
| `GlobalSidebarAdvice.java` | `com.cbp.testgen.web` | `@ControllerAdvice` providing navigation data |
| `Calculator.java` | `samples` | Sample target class: Arithmetic operations |
| `BankAccount.java` | `samples` | Sample target class: Encapsulated banking logic |
| `BaseManager.java` | `samples` | Sample target class: Abstract base class |
| `InventoryManager.java` | `samples` | Sample target class: Injected collaborators |
| `BillingService.java` | `samples` | Sample target class: Multi-branch business logic |
| `ProductRepository.java` | `samples` | Sample collaborator interface |
| `NotificationService.java` | `samples` | Sample collaborator interface |
| `ResourceSample_*.java` (7 files) | `src/main/resources/samples` | UI resource copies with detailed Javadoc |
| `CodeAnalyzerTest.java` | `src/test/java` | Unit tests for AST parsing |
| `PipelineIntegrationTest.java` | `src/test/java` | Integration test for the end-to-end pipeline |
| `CodeRectifierServiceTest.java` | `src/test/java` | Tests for automated failure rectification |
| `SmartRegressionTakeawayTest.java` | `src/test/java` | Tests for regression prioritization |
| `ResultsControllerVulnerabilityTest.java` | `src/test/java` | Tests for results controller edge cases |
| `SqlConsoleApiControllerTest.java` | `src/test/java` | Tests for SQL console queries |
| `WebEndpointsTest.java` | `src/test/java` | Tests for web controllers |
