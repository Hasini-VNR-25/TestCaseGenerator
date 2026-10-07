package com.cbp.testgen.generator;

import com.cbp.testgen.analyzer.model.ClassInfo;
import com.cbp.testgen.analyzer.model.FieldInfo;
import com.cbp.testgen.analyzer.model.MethodInfo;
import com.cbp.testgen.analyzer.model.ParameterInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * OOP-Aware Test Generator:
 * 1. Mockito collaborator mock injection for interfaces/abstract dependencies.
 * 2. Liskov Substitution Principle (LSP) polymorphic behavior verification.
 * 3. Stateful test sequences (constructor -> mutators -> getters).
 */
@Component
public class OopTestGenerator {

    public static class GeneratedOopTest {
        private final String testName;
        private final String testCode;
        private final String oopPattern; // "LISKOV_SUBSTITUTION", "MOCK_COLLABORATOR", "STATE_SEQUENCE"
        private final String description;

        public GeneratedOopTest(String testName, String testCode, String oopPattern, String description) {
            this.testName = testName;
            this.testCode = testCode;
            this.oopPattern = oopPattern;
            this.description = description;
        }

        public String getTestName() {
            return testName;
        }

        public String getTestCode() {
            return testCode;
        }

        public String getOopPattern() {
            return oopPattern;
        }

        public String getDescription() {
            return description;
        }
    }

    public List<GeneratedOopTest> generateOopTests(ClassInfo classInfo) {
        List<GeneratedOopTest> oopTests = new ArrayList<>();

        // 1. Liskov Substitution Principle (LSP) Check for Superclass Inheritance
        if (classInfo.hasSuperClass()) {
            oopTests.add(generateLspTest(classInfo));
        }

        // 2. Stateful sequence tests (Constructor -> Setter -> Getter consistency)
        generateStatefulTests(classInfo, oopTests);

        return oopTests;
    }

    private GeneratedOopTest generateLspTest(ClassInfo classInfo) {
        String className = classInfo.getClassName();
        String superClass = classInfo.getSuperClass();
        String testName = "lsp_polymorphicSubstitution_satisfiesParentContract";

        StringBuilder sb = new StringBuilder();
        sb.append("    @Test\n");
        sb.append("    @DisplayName(\"LSP Check: ").append(className).append(" can be substituted as ").append(superClass).append("\")\n");
        sb.append("    void ").append(testName).append("() {\n");
        sb.append("        // Arrange: Instantiate child class via parent reference (Liskov Substitution Principle)\n");

        String instantiation = buildInstantiationSnippet(classInfo, "parentRef");
        sb.append(instantiation);

        sb.append("        // Act & Assert: Validate contract adherence\n");
        sb.append("        assertNotNull(parentRef, \"Parent reference instance should be non-null\");\n");
        sb.append("        assertTrue(parentRef instanceof ").append(superClass).append(", \"Must be instance of superclass\");\n");
        sb.append("        assertTrue(parentRef instanceof ").append(className).append(", \"Must be instance of subclass\");\n");

        // If class has methods, call one via parent
        for (MethodInfo m : classInfo.getMethods()) {
            if (m.isOverridden() && m.getParameters().isEmpty() && !m.isVoid()) {
                sb.append("        assertNotNull(parentRef.").append(m.getMethodName()).append("(), \"Overridden method should return valid result\");\n");
                break;
            }
        }

        sb.append("    }\n");

        return new GeneratedOopTest(
                testName,
                sb.toString(),
                "LISKOV_SUBSTITUTION",
                "Verifies Liskov Substitution Principle: " + className + " safely acts as " + superClass
        );
    }

    private void generateStatefulTests(ClassInfo classInfo, List<GeneratedOopTest> oopTests) {
        // Find getters and setters matching property names
        for (MethodInfo setter : classInfo.getMethods()) {
            if (setter.isSetter()) {
                String propName = setter.getMethodName().substring(3);
                MethodInfo matchingGetter = classInfo.getMethods().stream()
                        .filter(m -> m.getMethodName().equalsIgnoreCase("get" + propName) ||
                                m.getMethodName().equalsIgnoreCase("is" + propName))
                        .findFirst()
                        .orElse(null);

                if (matchingGetter != null) {
                    String testName = "stateSequence_" + setter.getMethodName() + "_updates" + propName;
                    StringBuilder sb = new StringBuilder();
                    sb.append("    @Test\n");
                    sb.append("    @DisplayName(\"State Sequence: ").append(setter.getMethodName()).append(" updates state verified by ").append(matchingGetter.getMethodName()).append("\")\n");
                    sb.append("    void ").append(testName).append("() {\n");
                    sb.append("        // Arrange\n");
                    sb.append(buildInstantiationSnippet(classInfo, "instance"));

                    ParameterInfo param = setter.getParameters().get(0);
                    String testValue = getSampleValueForType(param.getType());

                    sb.append("        // Act\n");
                    sb.append("        instance.").append(setter.getMethodName()).append("(").append(testValue).append(");\n");
                    sb.append("        // Assert\n");
                    sb.append("        assertEquals(").append(testValue).append(", instance.").append(matchingGetter.getMethodName()).append("(), \"Getter must reflect value set by setter\");\n");
                    sb.append("    }\n");

                    oopTests.add(new GeneratedOopTest(
                            testName,
                            sb.toString(),
                            "STATE_SEQUENCE",
                            "Verifies state consistency between " + setter.getMethodName() + " and " + matchingGetter.getMethodName()
                    ));
                }
            }
        }
    }

    public String buildInstantiationSnippet(ClassInfo classInfo, String varName) {
        StringBuilder sb = new StringBuilder();
        MethodInfo ctor = classInfo.getPrimaryConstructor();
        String className = classInfo.getClassName();

        if (ctor == null || ctor.getParameters().isEmpty()) {
            sb.append("        ").append(className).append(" ").append(varName).append(" = new ").append(className).append("();\n");
            return sb.toString();
        }

        List<String> argVars = new ArrayList<>();
        for (ParameterInfo p : ctor.getParameters()) {
            String argVar = "arg_" + p.getName();
            if (isInterfaceOrAbstractType(p.getType())) {
                sb.append("        ").append(p.getType()).append(" ").append(argVar)
                        .append(" = org.mockito.Mockito.mock(").append(p.getType()).append(".class);\n");
            } else {
                String val = getSampleValueForType(p.getType());
                sb.append("        ").append(p.getType()).append(" ").append(argVar)
                        .append(" = ").append(val).append(";\n");
            }
            argVars.add(argVar);
        }

        sb.append("        ").append(className).append(" ").append(varName)
                .append(" = new ").append(className).append("(").append(String.join(", ", argVars)).append(");\n");

        return sb.toString();
    }

    private boolean isInterfaceOrAbstractType(String type) {
        return type.endsWith("Service") || type.endsWith("Repository") || type.endsWith("Dao") ||
                type.endsWith("Client") || type.endsWith("Listener") || type.startsWith("I") ||
                type.contains("Notification") || type.contains("ProductRepository");
    }

    public String getSampleValueForType(String type) {
        if ("int".equals(type) || "Integer".equals(type)) return "10";
        if ("double".equals(type) || "Double".equals(type)) return "100.0";
        if ("long".equals(type) || "Long".equals(type)) return "1000L";
        if ("float".equals(type) || "Float".equals(type)) return "10.0f";
        if ("boolean".equals(type) || "Boolean".equals(type)) return "true";
        if ("String".equals(type)) return "\"test_value\"";
        return "null";
    }
}
