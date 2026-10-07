package com.cbp.testgen.mutation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MutationResultSummary {
    private final String className;
    private final int totalMutants;
    private final int mutantsKilled;
    private final int mutantsSurvived;
    private final double mutationScorePct;
    private final List<Mutant> mutants;
    private final Map<String, Double> methodScores;

    public MutationResultSummary(String className, int totalMutants, int mutantsKilled,
                                 int mutantsSurvived, double mutationScorePct,
                                 List<Mutant> mutants, Map<String, Double> methodScores) {
        this.className = className;
        this.totalMutants = totalMutants;
        this.mutantsKilled = mutantsKilled;
        this.mutantsSurvived = mutantsSurvived;
        this.mutationScorePct = mutationScorePct;
        this.mutants = mutants != null ? mutants : Collections.emptyList();
        this.methodScores = methodScores != null ? methodScores : Collections.emptyMap();
    }

    public String getClassName() {
        return className;
    }

    public int getTotalMutants() {
        return totalMutants;
    }

    public int getMutantsKilled() {
        return mutantsKilled;
    }

    public int getMutantsSurvived() {
        return mutantsSurvived;
    }

    public double getMutationScorePct() {
        return mutationScorePct;
    }

    public List<Mutant> getMutants() {
        return mutants;
    }

    public Map<String, Double> getMethodScores() {
        return methodScores;
    }
}
