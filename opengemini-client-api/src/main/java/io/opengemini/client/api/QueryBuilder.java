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

public class QueryBuilder {
    private Expression[] selectExprs;

    private String[] from;

    private Condition where;

    private Expression[] groupByExpressions;

    private SortOrder orderBy;

    private long limit;

    private long offset;

    private String timezone;

    private QueryBuilder() {}

    public static QueryBuilder create() {
        return new QueryBuilder();
    }

    public QueryBuilder select(Expression[] selectExprs) {
        this.selectExprs = selectExprs;
        return this;
    }

    public QueryBuilder from(String[] from) {
        this.from = from;
        return this;
    }

    public QueryBuilder where(Condition where) {
        this.where = where;
        return this;
    }

    public QueryBuilder groupBy(Expression[] groupByExpressions) {
        this.groupByExpressions = groupByExpressions;
        return this;
    }

    public QueryBuilder orderBy(SortOrder order) {
        this.orderBy = order;
        return this;
    }

    public QueryBuilder limit(long limit) {
        this.limit = limit;
        return this;
    }

    public QueryBuilder offset(long offset) {
        this.offset = offset;
        return this;
    }

    public QueryBuilder timezone(String timezone) {
        this.timezone = timezone;
        return this;
    }

    public Query build() {
        StringBuilder commandBuilder = new StringBuilder();

        if (selectExprs != null && selectExprs.length > 0) {
            commandBuilder.append("SELECT ");
            for (int i = 0; i < selectExprs.length; i++) {
                if (i > 0) {
                    commandBuilder.append(", ");
                }
                commandBuilder.append(selectExprs[i].build());
            }
        } else {
            commandBuilder.append("SELECT *");
        }

        if (from != null && from.length > 0) {
            commandBuilder.append(" FROM ");
            String[] quotedTables = new String[from.length];
            for (int i = 0; i < from.length; i++) {
                quotedTables[i] = "\"" + from[i] + "\"";
            }
            commandBuilder.append(String.join(", ", quotedTables));
        }

        if (where != null) {
            commandBuilder.append(" WHERE ");
            commandBuilder.append(where.build());
        }

        if (groupByExpressions != null && groupByExpressions.length > 0) {
            commandBuilder.append(" GROUP BY ");
            for (int i = 0; i < groupByExpressions.length; i++) {
                if (i > 0) {
                    commandBuilder.append(", ");
                }
                commandBuilder.append(groupByExpressions[i].build());
            }
        }

        if (orderBy != null) {
            commandBuilder.append(" ORDER BY time ");
            commandBuilder.append(orderBy.name());
        }

        if (limit > 0) {
            commandBuilder.append(" LIMIT ").append(limit);
        }

        if (offset > 0) {
            commandBuilder.append(" OFFSET ").append(offset);
        }

        if (timezone != null) {
            commandBuilder.append(" TZ('").append(timezone).append("')");
        }

        return new Query(commandBuilder.toString());
    }
}
