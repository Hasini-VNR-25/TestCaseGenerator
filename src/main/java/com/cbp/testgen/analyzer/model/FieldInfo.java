package com.cbp.testgen.analyzer.model;

import java.util.Collections;
import java.util.List;

public class FieldInfo {
    private final String name;
    private final String type;
    private final List<String> modifiers;

    public FieldInfo(String name, String type, List<String> modifiers) {
        this.name = name;
        this.type = type;
        this.modifiers = modifiers != null ? modifiers : Collections.emptyList();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public List<String> getModifiers() {
        return modifiers;
    }

    @Override
    public String toString() {
        return type + " " + name;
    }
}
