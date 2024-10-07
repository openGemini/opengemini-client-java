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
