package com.cbp.testgen.analyzer;

import com.cbp.testgen.analyzer.model.ClassInfo;
import com.cbp.testgen.analyzer.model.MethodInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AstDiffAnalyzer {

    public static class MethodDiff {
        public enum ChangeType {
            ADDED,
            MODIFIED,
            DELETED,
            UNCHANGED
        }

        private final String methodName;
        private final String signature;
        private final ChangeType changeType;
        private final String details;

        public MethodDiff(String methodName, String signature, ChangeType changeType, String details) {
            this.methodName = methodName;
            this.signature = signature;
            this.changeType = changeType;
            this.details = details;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getSignature() {
            return signature;
        }

        public ChangeType getChangeType() {
            return changeType;
        }

        public String getDetails() {
            return details;
        }
    }

    public static class DiffReport {
        private final boolean hasChanges;
        private final List<MethodDiff> methodDiffs;
        private final Set<String> modifiedMethodNames;

        public DiffReport(boolean hasChanges, List<MethodDiff> methodDiffs, Set<String> modifiedMethodNames) {
            this.hasChanges = hasChanges;
            this.methodDiffs = methodDiffs;
            this.modifiedMethodNames = modifiedMethodNames;
        }

        public boolean isHasChanges() {
            return hasChanges;
        }

        public List<MethodDiff> getMethodDiffs() {
            return methodDiffs;
        }

        public Set<String> getModifiedMethodNames() {
            return modifiedMethodNames;
        }
    }

    public DiffReport compareVersions(ClassInfo oldClass, ClassInfo newClass) {
        if (oldClass == null) {
            List<MethodDiff> diffs = newClass.getMethods().stream()
                    .map(m -> new MethodDiff(m.getMethodName(), m.getSignature(), MethodDiff.ChangeType.ADDED, "New method"))
                    .collect(Collectors.toList());
            Set<String> allMethodNames = newClass.getMethods().stream().map(MethodInfo::getMethodName).collect(Collectors.toSet());
            return new DiffReport(true, diffs, allMethodNames);
        }

        Map<String, MethodInfo> oldMethods = oldClass.getMethods().stream()
                .collect(Collectors.toMap(MethodInfo::getSignature, m -> m, (a, b) -> a));
        Map<String, MethodInfo> newMethods = newClass.getMethods().stream()
                .collect(Collectors.toMap(MethodInfo::getSignature, m -> m, (a, b) -> a));

        List<MethodDiff> diffs = new ArrayList<>();
        Set<String> modifiedNames = new HashSet<>();

        // Check for added or modified methods
        for (MethodInfo newM : newClass.getMethods()) {
            MethodInfo oldM = oldMethods.get(newM.getSignature());
            if (oldM == null) {
                diffs.add(new MethodDiff(newM.getMethodName(), newM.getSignature(), MethodDiff.ChangeType.ADDED, "Method added in new version"));
                modifiedNames.add(newM.getMethodName());
            } else if (!normalizeBody(oldM.getBodyCode()).equals(normalizeBody(newM.getBodyCode()))) {
                diffs.add(new MethodDiff(newM.getMethodName(), newM.getSignature(), MethodDiff.ChangeType.MODIFIED, "Method body logic altered"));
                modifiedNames.add(newM.getMethodName());
            } else {
                diffs.add(new MethodDiff(newM.getMethodName(), newM.getSignature(), MethodDiff.ChangeType.UNCHANGED, "No changes"));
            }
        }

        // Check for deleted methods
        for (MethodInfo oldM : oldClass.getMethods()) {
            if (!newMethods.containsKey(oldM.getSignature())) {
                diffs.add(new MethodDiff(oldM.getMethodName(), oldM.getSignature(), MethodDiff.ChangeType.DELETED, "Method deleted"));
                modifiedNames.add(oldM.getMethodName());
            }
        }

        boolean hasChanges = !modifiedNames.isEmpty() || !oldClass.getSourceHash().equals(newClass.getSourceHash());
        return new DiffReport(hasChanges, diffs, modifiedNames);
    }

    private String normalizeBody(String code) {
        if (code == null) return "";
        return code.replaceAll("\\s+", " ").trim();
    }
}
