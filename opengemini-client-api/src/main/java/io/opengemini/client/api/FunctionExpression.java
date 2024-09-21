package io.opengemini.client.api;

/**
 * FunctionExpression represents a function call with arguments (e.g., SUM, COUNT)
 */
public class FunctionExpression implements Expression {
    private final FunctionEnum function;

    private final Expression[] arguments;

    public FunctionExpression(FunctionEnum function, Expression[] arguments) {
        this.function = function;
        this.arguments = arguments;
    }

    public FunctionEnum function() {
        return function;
    }

    public Expression[] arguments() {
        return arguments;
    }

    @Override
    public String build() {
        StringBuilder builder = new StringBuilder();
        builder.append(function.name()).append("(");

        for (int i = 0; i < arguments.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(arguments[i].build());
        }

        builder.append(")");
        return builder.toString();
    }
}
