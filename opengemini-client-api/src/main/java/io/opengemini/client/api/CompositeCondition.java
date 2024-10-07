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
