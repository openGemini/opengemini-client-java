package io.opengemini.client.api;

/**
 * AsExpression represents an alias for an expression (e.g., SELECT field AS alias)
 */
public class AsExpression implements Expression {
    private final String alias;

    private final Expression expression;

    public AsExpression(String alias, Expression expression) {
        this.alias = alias;
        this.expression = expression;
    }

    public String alias() {
        return alias;
    }

    public Expression expression() {
        return expression;
    }

    @Override
    public String build() {
        return expression.build() + " AS \"" + alias + "\"";
    }
}
