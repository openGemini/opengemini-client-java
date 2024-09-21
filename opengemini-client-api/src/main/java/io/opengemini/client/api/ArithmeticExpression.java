package io.opengemini.client.api;

/**
 * ArithmeticExpression represents an arithmetic operation between two expressions
 */
public class ArithmeticExpression implements Expression {
    private final Expression left;

    private final Expression right;

    private final ArithmeticOperator operator;

    public ArithmeticExpression(Expression left, Expression right, ArithmeticOperator operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public Expression left() {
        return left;
    }

    public Expression right() {
        return right;
    }

    public ArithmeticOperator operator() {
        return operator;
    }

    @Override
    public String build() {
        return "(" + left.build() + " " + operator.symbol() + " " + right.build() + ")";
    }
}
