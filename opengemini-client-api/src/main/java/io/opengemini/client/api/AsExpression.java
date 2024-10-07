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
