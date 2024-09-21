package io.opengemini.client.api;

/**
 * ConstantExpression represents a constant value in the query
 */
public class ConstantExpression implements Expression {
    private final Object value;

    public ConstantExpression(Object value) {
        this.value = value;
    }

    public Object value() {
        return value;
    }

    @Override
    public String build() {
        return value.toString();
    }
}
