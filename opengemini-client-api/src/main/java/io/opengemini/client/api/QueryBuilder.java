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
