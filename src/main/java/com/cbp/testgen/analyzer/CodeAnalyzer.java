package com.cbp.testgen.analyzer;

import com.cbp.testgen.analyzer.model.ClassInfo;
import com.cbp.testgen.analyzer.model.FieldInfo;
import com.cbp.testgen.analyzer.model.MethodInfo;
import com.cbp.testgen.analyzer.model.ParameterInfo;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CodeAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(CodeAnalyzer.class);
    private final JavaParser javaParser;

    public CodeAnalyzer() {
        this.javaParser = new JavaParser();
    }

    public ClassInfo analyzeSourceFile(File file) throws IOException {
        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        return analyzeSourceCode(content);
    }

    public ClassInfo analyzeSourceCode(String sourceCode) {
        ParseResult<CompilationUnit> parseResult = javaParser.parse(sourceCode);
        if (!parseResult.isSuccessful() || parseResult.getResult().isEmpty()) {
            throw new IllegalArgumentException("Failed to parse Java source code: " + parseResult.getProblems());
        }

        CompilationUnit cu = parseResult.getResult().get();
        String packageName = cu.getPackageDeclaration()
                .map(pd -> pd.getName().asString())
                .orElse("");

        // Find primary class declaration
        Optional<ClassOrInterfaceDeclaration> classOpt = cu.findFirst(ClassOrInterfaceDeclaration.class);
        if (classOpt.isPresent()) {
            return extractClassInfo(packageName, classOpt.get(), sourceCode);
        }

        // Check for Enum
        Optional<EnumDeclaration> enumOpt = cu.findFirst(EnumDeclaration.class);
        if (enumOpt.isPresent()) {
            return extractEnumInfo(packageName, enumOpt.get(), sourceCode);
        }

        throw new IllegalArgumentException("No class or interface declaration found in source code.");
    }

    private ClassInfo extractClassInfo(String packageName, ClassOrInterfaceDeclaration classDecl, String sourceCode) {
        String className = classDecl.getNameAsString();
        String superClass = classDecl.getExtendedTypes().isNonEmpty()
                ? classDecl.getExtendedTypes().get(0).getNameAsString()
                : null;

        List<String> interfaces = classDecl.getImplementedTypes().stream()
                .map(ClassOrInterfaceType::getNameAsString)
                .collect(Collectors.toList());

        List<String> modifiers = classDecl.getModifiers().stream()
                .map(m -> m.getKeyword().asString())
                .collect(Collectors.toList());

        boolean isAbstract = classDecl.isAbstract();
        boolean isInterface = classDecl.isInterface();
        boolean isEnum = false;

        // Extract Constructors
        List<MethodInfo> constructors = new ArrayList<>();
        for (ConstructorDeclaration ctor : classDecl.getConstructors()) {
            List<ParameterInfo> params = extractParameters(ctor.getParameters());
            List<String> exceptions = ctor.getThrownExceptions().stream()
                    .map(t -> t.asString())
                    .collect(Collectors.toList());
            List<String> ctorModifiers = ctor.getModifiers().stream()
                    .map(m -> m.getKeyword().asString())
                    .collect(Collectors.toList());

            int startLine = ctor.getBegin().map(p -> p.line).orElse(0);
            int endLine = ctor.getEnd().map(p -> p.line).orElse(0);
            String body = ctor.getBody().toString();

            constructors.add(new MethodInfo(
                    className,
                    "void",
                    params,
                    exceptions,
                    ctorModifiers,
                    true,
                    false,
                    false,
                    false,
                    startLine,
                    endLine,
                    body
            ));
        }

        // Extract Methods
        List<MethodInfo> methods = new ArrayList<>();
        for (MethodDeclaration method : classDecl.getMethods()) {
            // Filter: public and protected methods, or package-private if non-private
            boolean isPrivate = method.hasModifier(Modifier.Keyword.PRIVATE);
            if (isPrivate) {
                continue;
            }

            String methodName = method.getNameAsString();
            String returnType = method.getTypeAsString();
            List<ParameterInfo> params = extractParameters(method.getParameters());
            List<String> exceptions = method.getThrownExceptions().stream()
                    .map(t -> t.asString())
                    .collect(Collectors.toList());
            List<String> methodModifiers = method.getModifiers().stream()
                    .map(m -> m.getKeyword().asString())
                    .collect(Collectors.toList());

            boolean isMethodAbstract = method.isAbstract();
            boolean isStatic = method.isStatic();
            boolean isOverridden = method.getAnnotationByName("Override").isPresent();

            int startLine = method.getBegin().map(p -> p.line).orElse(0);
            int endLine = method.getEnd().map(p -> p.line).orElse(0);
            String body = method.getBody().map(Object::toString).orElse("");

            methods.add(new MethodInfo(
                    methodName,
                    returnType,
                    params,
                    exceptions,
                    methodModifiers,
                    false,
                    isMethodAbstract,
                    isStatic,
                    isOverridden,
                    startLine,
                    endLine,
                    body
            ));
        }

        // Extract Fields
        List<FieldInfo> fields = new ArrayList<>();
        for (FieldDeclaration field : classDecl.getFields()) {
            List<String> fieldModifiers = field.getModifiers().stream()
                    .map(m -> m.getKeyword().asString())
                    .collect(Collectors.toList());
            for (VariableDeclarator var : field.getVariables()) {
                fields.add(new FieldInfo(var.getNameAsString(), var.getTypeAsString(), fieldModifiers));
            }
        }

        String sourceHash = computeHash(sourceCode);

        logger.info("Analyzed class: {} ({} methods, {} constructors, {} fields)",
                className, methods.size(), constructors.size(), fields.size());

        return new ClassInfo(
                packageName,
                className,
                superClass,
                interfaces,
                modifiers,
                isAbstract,
                isInterface,
                isEnum,
                constructors,
                methods,
                fields,
                sourceCode,
                sourceHash
        );
    }

    private ClassInfo extractEnumInfo(String packageName, EnumDeclaration enumDecl, String sourceCode) {
        String enumName = enumDecl.getNameAsString();
        List<String> modifiers = enumDecl.getModifiers().stream()
                .map(m -> m.getKeyword().asString())
                .collect(Collectors.toList());

        List<MethodInfo> methods = new ArrayList<>();
        for (MethodDeclaration method : enumDecl.getMethods()) {
            if (method.hasModifier(Modifier.Keyword.PRIVATE)) continue;
            methods.add(new MethodInfo(
                    method.getNameAsString(),
                    method.getTypeAsString(),
                    extractParameters(method.getParameters()),
                    method.getThrownExceptions().stream().map(t -> t.asString()).collect(Collectors.toList()),
                    method.getModifiers().stream().map(m -> m.getKeyword().asString()).collect(Collectors.toList()),
                    false,
                    method.isAbstract(),
                    method.isStatic(),
                    method.getAnnotationByName("Override").isPresent(),
                    method.getBegin().map(p -> p.line).orElse(0),
                    method.getEnd().map(p -> p.line).orElse(0),
                    method.getBody().map(Object::toString).orElse("")
            ));
        }

        return new ClassInfo(
                packageName,
                enumName,
                "Enum",
                Collections.emptyList(),
                modifiers,
                false,
                false,
                true,
                Collections.emptyList(),
                methods,
                Collections.emptyList(),
                sourceCode,
                computeHash(sourceCode)
        );
    }

    private List<ParameterInfo> extractParameters(List<Parameter> parameters) {
        List<ParameterInfo> result = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i++) {
            Parameter p = parameters.get(i);
            result.add(new ParameterInfo(p.getNameAsString(), p.getTypeAsString(), i));
        }
        return result;
    }

    public static String computeHash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
