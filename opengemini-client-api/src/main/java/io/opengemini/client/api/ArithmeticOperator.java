package io.opengemini.client.api;

public enum ArithmeticOperator {
    ADD("+"),
    SUBTRACT("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    MODULO("%");

    private final String symbol;

    ArithmeticOperator(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }
}
