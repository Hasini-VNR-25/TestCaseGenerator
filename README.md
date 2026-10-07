# Intelligent Java Test Case Generator (CBP Project)

> **Curricular Based Project (CBP)**  
> **Demonstrating Practical Application of:**  
> - **Software Engineering (SE):** Boundary Value Analysis (BVA), Equivalence Class Partitioning (ECP), Pairwise Combinations, Mutation Testing (PIT-style AST Operators), Greedy Set-Cover Minimization, Regression-Aware Prioritization, Flaky Test Detection.  
> - **Database Management Systems (DBMS):** 3NF Normalized Relational Schema (10 Entities), Foreign Keys, Composite & Timestamp Indexes, Aggregate SQL View (`latest_class_health`).  
> - **Object-Oriented Programming (OOPs):** Polymorphic Strategy Pattern, Factory Pattern, Liskov Substitution Principle (LSP) Verification, Stateful Sequence Generation, Mockito Collaborator Mock Injection.

---

## 1. Project Framing & Problem Solved

**The Core Problem:** Automated test generation tools (Diffblue Cover, EvoSuite, Randoop, UTBotJava) generate large volumes of tests blindly, but:
1. They **do not measure whether those tests are actually effective** at catching subtle defects.
2. They **do not remember test results or history across runs**.
3. They **do not intelligently prioritize regression testing** after code changes.

**Our Solution:**
> *"Existing tools generate tests and forget them; this tool generates tests, measures how effective they really are through mutation analysis, stores results in a relational database, and intelligently prioritizes re-testing across code versions."*

---

## 2. Key Features

### Phase 1: Core Engine
- **AST & Reflection Analysis (`CodeAnalyzer`):** Extracts class hierarchy, constructors, public/protected methods, parameters, exceptions, fields, and computes SHA-256 source hash.
- **Input Generation Strategies (`Strategy Pattern`):**
  - `BoundaryValueStrategy`: Minimum, maximum, zero, unit, and extreme values.
  - `EquivalencePartitionStrategy`: Valid, invalid-low, and invalid-high equivalence classes.
  - `PairwiseCombinator`: Prevents combinatorial explosion for methods with 3+ parameters by generating minimal orthogonal all-pairs combinations.
- **JUnit 5 Generator (`JUnit5TestGenerator`):** Arrange-Act-Assert (AAA) structured tests with descriptive naming (`method_condition_expectedResult()`).
- **Dynamic Compilation & Execution:** In-process dynamic Java compiler (`javax.tools.JavaCompiler`) and JUnit 5 Platform Launcher (`org.junit.platform.launcher.Launcher`).
- **Code Coverage:** JaCoCo integration capturing line coverage %, branch coverage %, and method coverage %.

### Phase 2: Intelligence Layer
- **AST Mutation Testing Engine (`MutationEngine`):**
  - Applies mutation operators: Relational swaps (`>` $\leftrightarrow$ `>=`), Arithmetic swaps (`+` $\leftrightarrow$ `-`, `*` $\leftrightarrow$ `/`), Boolean negation (`!`, `&&` $\leftrightarrow$ `||`), Conditional boundary shifts, Return value mutations.
  - Re-executes test suite against every mutant to compute **Mutation Score = (Mutants Killed / Total Mutants) $\times$ 100%**.
- **Assertion Quality Analyzer:** Asserts golden outputs and state invariants; flags zero-assertion tests as "weak".
- **OOP-Aware Test Generation:**
  - Liskov Substitution Principle (LSP) polymorphic substitution checks.
  - Mockito mock injection for interfaces and abstract collaborator services.
  - Stateful sequences (setter $\to$ getter invariant assertions).
- **Test Suite Minimization (`TestSuiteMinimizer`):** Implements the **Greedy Set-Cover Algorithm** to select the minimal subset of tests preserving full mutant and path coverage.
- **Flaky Test Detector:** Multi-iteration (3–5 runs) test stability inspector.

### Phase 3: Database Layer (DBMS 3NF)
- **10 Normalized Entities in 3NF:**
  - `projects`, `classes`, `methods`, `test_cases`, `test_runs`, `test_results`, `coverage_results`, `mutation_results`, `flaky_tests`, `class_versions`.
- **SQL View:** `latest_class_health` (aggregates latest coverage, mutation score, and flaky test counts).
- **Regression-Aware Prioritizer (`RegressionPrioritizer`):** AST diff analyzer detects altered methods and ranks re-tests by touched changes, failure history, and execution speed.

### Phase 4: Web Dashboard & Reporting
- **Modern Spring Boot Web UI:** Built with Thymeleaf, TailwindCSS, Lucide icons, and Chart.js.
- **Export Options:** Download compilable `.java` test files and standalone interactive `.html` reports.

### Phase 5: LLM Verification Loop
- Proposes test suggestions and **strictly validates them** through compilation and mutant-killing execution before allowing them into the suite.

---

## 3. Technology Stack

- **Language:** Java 21 LTS / Java 17
- **Framework:** Spring Boot 3.2.4 (Web, Thymeleaf, Spring Data JPA)
- **Testing:** JUnit 5 (Jupiter 5.10.2), JUnit Platform Launcher
- **Code Coverage:** JaCoCo Core 0.8.11
- **AST Parsing:** JavaParser 3.25.10
- **Mocking:** Mockito 5.11.0
- **Database:** Embedded H2 (default, zero setup required) + DDL scripts for MySQL 8.0 & PostgreSQL 15
- **Build Tool:** Maven

---

## 4. How to Run the Application

### Option A: Using Maven (Quickest)
```powershell
# In the project root directory:
mvn spring-boot:run
```
Once started, open your browser at:
👉 **`http://localhost:8080`**

### Option B: Running Automated Tests
```powershell
mvn test
```

### Option C: Accessing H2 Database Console
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/testgen_db`
- Username: `sa`
- Password: *(leave blank)*

---

## 5. MySQL / PostgreSQL Schema Setup

For grading and external DBMS demonstration:
- **MySQL 8.0 Script:** [`schema-mysql.sql`](schema-mysql.sql)
  ```bash
  mysql -u root -p < schema-mysql.sql
  ```
- **PostgreSQL 15 Script:** [`schema-postgres.sql`](schema-postgres.sql)
  ```bash
  psql -U postgres -d postgres -f schema-postgres.sql
  ```
- **Entity-Relationship Documentation:** [`ER-Diagram.md`](ER-Diagram.md)

---

## 6. Bundled Sample Classes

The project includes 3 pre-configured sample classes ready for immediate testing:
1. **`BankAccount.java`**: Banking logic with boundary checks, deposit/withdraw rules, balance invariants, and exception throwing.
2. **`Calculator.java`**: Math logic with divide-by-zero checks, factorial boundary loops, prime checking, and arithmetic mutation points.
3. **`InventoryManager.java`**: Extends abstract `BaseManager` (OOP inheritance) and uses Mockito mocks for `NotificationService` and `ProductRepository`.

---

## 7. Design Patterns Implemented

| Design Pattern | Implementation Class | Purpose |
|---|---|---|
| **Strategy Pattern** | `InputGenerationStrategy` (`BoundaryValueStrategy`, `EquivalencePartitionStrategy`, `PairwiseCombinator`) | Interchangeable parameter value generation strategies |
| **Factory Pattern** | `JUnit5TestGenerator`, `AssertionQualityAnalyzer` | Dynamic creation of Arrange-Act-Assert test cases |
| **DAO / Repository** | `DatabaseManager`, Spring Data JPA Repositories | Decoupled data persistence for 10 entities in 3NF |
| **Observer Pattern** | `PipelineService` progress callbacks / SSE | Real-time analysis status streamed to web UI |
| **Template Method** | `DynamicCompiler`, `TestExecutor` | Standardized compilation and test execution harnesses |
