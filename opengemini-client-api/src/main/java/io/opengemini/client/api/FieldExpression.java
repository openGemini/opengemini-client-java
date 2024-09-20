package io.opengemini.client.api;

/**
 * FieldExpression represents a column or field in the query
 */
public class FieldExpression implements Expression {
    private final String field;

    public FieldExpression(String field) {
        this.field = field;
    }

    public String field() {
        return field;
    }

    @Override
    public String build() {
        return "\"" + field + "\"";
    }
}
