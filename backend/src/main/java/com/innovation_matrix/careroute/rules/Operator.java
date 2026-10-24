package com.innovation_matrix.careroute.rules;

public enum Operator {
    GT(">"),
    GTE("≥"),
    LT("<"),
    LTE("≤"),
    EQ("="),
    NEQ("≠"),
    CONTAINS("includes"),
    NOT_CONTAINS("does not include");

    private final String symbol;

    Operator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
