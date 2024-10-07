/*
 * Copyright 2024 openGemini Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
