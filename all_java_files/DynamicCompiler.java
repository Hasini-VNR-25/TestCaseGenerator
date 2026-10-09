package com.cbp.testgen.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DynamicCompiler {

    private static final Logger logger = LoggerFactory.getLogger(DynamicCompiler.class);

    public static class CompilationResult {
        private final boolean success;
        private final ClassLoader classLoader;
        private final Path outputDir;
        private final List<String> diagnostics;

        public CompilationResult(boolean success, ClassLoader classLoader, Path outputDir, List<String> diagnostics) {
            this.success = success;
            this.classLoader = classLoader;
            this.outputDir = outputDir;
            this.diagnostics = diagnostics;
        }

        public boolean isSuccess() {
            return success;
        }

        public ClassLoader getClassLoader() {
            return classLoader;
        }

        public Path getOutputDir() {
            return outputDir;
        }

        public List<String> getDiagnostics() {
            return diagnostics;
        }
    }

    public CompilationResult compileSources(Map<String, String> sourceFilesByRelPath) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("JDK compiler not found. Ensure Java JDK is used to run the application.");
        }

        Path tempDir = Files.createTempDirectory("testgen_compiled_");
        List<File> sourceFiles = new ArrayList<>();

        for (Map.Entry<String, String> entry : sourceFilesByRelPath.entrySet()) {
            Path filePath = tempDir.resolve(entry.getKey());
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, entry.getValue(), StandardCharsets.UTF_8);
            sourceFiles.add(filePath.toFile());
        }

        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticCollector, null, StandardCharsets.UTF_8)) {
            String classpath = System.getProperty("java.class.path");

            List<String> options = new ArrayList<>(Arrays.asList(
                    "-classpath", classpath,
                    "-d", tempDir.toAbsolutePath().toString(),
                    "-proc:none"
            ));

            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(sourceFiles);
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnosticCollector, options, null, compilationUnits);
            boolean success = Boolean.TRUE.equals(task.call());

            List<String> diagnostics = diagnosticCollector.getDiagnostics().stream()
                    .map(d -> d.getKind() + ": " + d.getMessage(null) + " (Line " + d.getLineNumber() + ")")
                    .collect(Collectors.toList());

            if (!success) {
                logger.warn("Dynamic compilation failed: {}", diagnostics);
                return new CompilationResult(false, null, tempDir, diagnostics);
            }

            URL[] urls = new URL[]{tempDir.toUri().toURL()};
            URLClassLoader classLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
            return new CompilationResult(true, classLoader, tempDir, diagnostics);
        }
    }
}
