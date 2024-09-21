package io.opengemini.client.api;

/**
 * StarExpression represents the wildcard (*) for selecting all columns
 */
public class StarExpression implements Expression {
    @Override
    public String build() {
        return "*";
    }
}
