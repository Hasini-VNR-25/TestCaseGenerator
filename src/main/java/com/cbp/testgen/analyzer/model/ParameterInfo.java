package com.cbp.testgen.analyzer.model;

public class ParameterInfo {
    private final String name;
    private final String type;
    private final int index;

    public ParameterInfo(String name, String type, int index) {
        this.name = name;
        this.type = type;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

    public boolean isInt() {
        return "int".equals(type) || "Integer".equals(type) || "java.lang.Integer".equals(type);
    }

    public boolean isLong() {
        return "long".equals(type) || "Long".equals(type) || "java.lang.Long".equals(type);
    }

    public boolean isDouble() {
        return "double".equals(type) || "Double".equals(type) || "java.lang.Double".equals(type);
    }

    public boolean isFloat() {
        return "float".equals(type) || "Float".equals(type) || "java.lang.Float".equals(type);
    }

    public boolean isShort() {
        return "short".equals(type) || "Short".equals(type) || "java.lang.Short".equals(type);
    }

    public boolean isByte() {
        return "byte".equals(type) || "Byte".equals(type) || "java.lang.Byte".equals(type);
    }

    public boolean isChar() {
        return "char".equals(type) || "Character".equals(type) || "java.lang.Character".equals(type);
    }

    public boolean isBoolean() {
        return "boolean".equals(type) || "Boolean".equals(type) || "java.lang.Boolean".equals(type);
    }

    public boolean isString() {
        return "String".equals(type) || "java.lang.String".equals(type);
    }

    public boolean isNumeric() {
        return isInt() || isLong() || isDouble() || isFloat() || isShort() || isByte();
    }

    public boolean isCollection() {
        return type.startsWith("List") || type.startsWith("Set") || type.startsWith("Collection") ||
                type.startsWith("java.util.List") || type.startsWith("java.util.Set") || type.startsWith("java.util.Collection");
    }

    public boolean isMap() {
        return type.startsWith("Map") || type.startsWith("java.util.Map");
    }

    public boolean isArray() {
        return type.endsWith("[]");
    }

    @Override
    public String toString() {
        return type + " " + name;
    }
}
