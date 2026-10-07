package com.cbp.testgen.mutation;

public enum MutationOperator {
    RELATIONAL_OPERATOR_SWAP("Relational Operator Swap", "Swaps comparison operators (e.g. > to >=, == to !=)"),
    ARITHMETIC_OPERATOR_SWAP("Arithmetic Operator Swap", "Swaps arithmetic operators (e.g. + to -, * to /)"),
    BOOLEAN_OPERATOR_NEGATION("Boolean Operator Negation", "Inverts boolean operators and conditions (&& to ||, !x to x)"),
    RETURN_VALUE_MUTATION("Return Value Mutation", "Replaces returned constants and expressions (e.g. true to false, 0 to 1)"),
    CONDITIONAL_BOUNDARY_SHIFT("Conditional Boundary Shift", "Shifts conditional boundary constants (+1 or -1)"),
    METHOD_CALL_REMOVAL("Method Call Removal", "Removes collaborator or void method invocations");

    private final String displayName;
    private final String description;

    MutationOperator(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
