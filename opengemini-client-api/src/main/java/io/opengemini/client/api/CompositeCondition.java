package io.opengemini.client.api;

import java.util.ArrayList;
import java.util.List;

public class CompositeCondition implements Condition {
    private final LogicalOperator logicalOperator;

    private final Condition[] conditions;

    public CompositeCondition(LogicalOperator logicalOperator, Condition... conditions) {
        this.logicalOperator = logicalOperator;
        this.conditions = conditions;
    }

    @Override
    public String build() {
        List<String> parts = new ArrayList<>();
        for (Condition condition : conditions) {
            parts.add(condition.build());
        }
        return "(" + String.join(" " + logicalOperator.name() + " ", parts) + ")";
    }
}
