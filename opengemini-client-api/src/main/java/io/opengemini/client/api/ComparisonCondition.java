package io.opengemini.client.api;

public class ComparisonCondition implements Condition {
    private final String column;

    private final ComparisonOperator operator;

    private final Object value;

    public ComparisonCondition(String column, ComparisonOperator operator, Object value) {
        this.column = column;
        this.operator = operator;
        this.value = value;
    }

    @Override
    public String build() {
        StringBuilder conditionBuilder = new StringBuilder();
        conditionBuilder.append('"');
        conditionBuilder.append(column);
        conditionBuilder.append('"');
        conditionBuilder.append(" ");
        conditionBuilder.append(operator.symbol());
        conditionBuilder.append(" ");
        if (value instanceof String) {
            conditionBuilder.append("'").append(value).append("'");
        } else {
            conditionBuilder.append(value);
        }

        return conditionBuilder.toString();
    }
}
