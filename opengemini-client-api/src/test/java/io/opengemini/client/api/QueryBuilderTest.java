package io.opengemini.client.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.TimeZone;

public class QueryBuilderTest {

    @Test
    public void testQueryBuilderSelectAllFromTable() {
        Query query = QueryBuilder.create().from(new String[]{"h2o_feet"}).build();

        String expectedQuery = "SELECT * FROM \"h2o_feet\"";
        Assertions.assertEquals(expectedQuery, query.getCommand());
    }

    @Test
    public void testQueryBuilderSelectTopFromTable() {
        FunctionExpression topFunction = new FunctionExpression(FunctionEnum.TOP, new Expression[]{
            new FieldExpression("water_level"),
            new ConstantExpression(5)
        });

        QueryBuilder queryBuilder = QueryBuilder.create();
        Query query = queryBuilder.select(new Expression[]{topFunction}).from(new String[]{"h2o_feet"}).build();

        String expectedQuery = "SELECT TOP(\"water_level\", 5) FROM \"h2o_feet\"";
        Assertions.assertEquals(expectedQuery, query.getCommand());
    }

    @Test
    public void testQueryBuilderSelectLastAndTagFromTable() {
        FunctionExpression lastFunction = new FunctionExpression(FunctionEnum.LAST, new Expression[]{
            new FieldExpression("water_level")
        });

        Query query = QueryBuilder.create().select(new Expression[]{
            lastFunction,
            new FieldExpression("location")
        }).from(new String[]{"h2o_feet"}).build();

        String expectedQuery = "SELECT LAST(\"water_level\"), \"location\" FROM \"h2o_feet\"";
        Assertions.assertEquals(expectedQuery, query.getCommand());
    }

    @Test
    public void testQueryBuilderSelectWithArithmetic() {
        FieldExpression waterLevelField = new FieldExpression("water_level");

        ArithmeticExpression multipliedByFour = new ArithmeticExpression(waterLevelField, new ConstantExpression(4), ArithmeticOperator.MULTIPLY);
        ArithmeticExpression addTwo = new ArithmeticExpression(multipliedByFour, new ConstantExpression(2), ArithmeticOperator.ADD);

        Query query = QueryBuilder.create().select(new Expression[]{addTwo}).from(new String[]{"h2o_feet"}).limit(10).build();

        String expectedQuery = "SELECT ((\"water_level\" * 4) + 2) FROM \"h2o_feet\" LIMIT 10";
        Assertions.assertEquals(expectedQuery, query.getCommand());
    }

    @Test
    public void testQueryBuilderSelectWhereCondition() {
        ComparisonCondition condition = new ComparisonCondition("water_level", ComparisonOperator.GREATER_THAN, 8);

        Query query = QueryBuilder.create().from(new String[]{"h2o_feet"}).where(condition).build();

        String expectedQuery = "SELECT * FROM \"h2o_feet\" WHERE \"water_level\" > 8";
        Assertions.assertEquals(expectedQuery, query.getCommand());
    }

    @Test
    public void testQueryBuilderSelectWithComplexWhereCondition() {
        ComparisonCondition locationCondition = new ComparisonCondition("location", ComparisonOperator.NOT_EQUALS, "santa_monica");
        ComparisonCondition lowerWaterLevelCondition = new ComparisonCondition("water_level", ComparisonOperator.LESS_THAN, -0.57);
        ComparisonCondition higherWaterLevelCondition = new ComparisonCondition("water_level", ComparisonOperator.GREATER_THAN, 9.95);

        CompositeCondition waterLevelCondition = new CompositeCondition(LogicalOperator.OR, lowerWaterLevelCondition, higherWaterLevelCondition);
        CompositeCondition finalCondition = new CompositeCondition(LogicalOperator.AND, locationCondition, waterLevelCondition);

        Query query = QueryBuilder.create().select(new Expression[]{new FieldExpression("water_level")}).from(new String[]{"h2o_feet"}).where(finalCondition).build();

        String expectedQuery = "SELECT \"water_level\" FROM \"h2o_feet\" WHERE (\"location\" <> 'santa_monica' AND (\"water_level\" < -0.57 OR \"water_level\" > 9.95))";
        Assertions.assertEquals(expectedQuery, query.getCommand());
    }

    @Test
    public void testQueryBuilderSelectWithGroupBy() {
        FunctionExpression meanFunction = new FunctionExpression(FunctionEnum.MEAN, new Expression[]{new FieldExpression("water_level")});

        Query query = QueryBuilder.create().select(new Expression[]{meanFunction}).from(new String[]{"h2o_feet"}).groupBy(new Expression[]{new FieldExpression("location")}).build();

        String expectedQuery = "SELECT MEAN(\"water_level\") FROM \"h2o_feet\" GROUP BY \"location\"";
        Assertions.assertEquals(expectedQuery, query.getCommand());
    }

    @Test
    public void testQueryBuilderSelectWithTimeRangeAndGroupByTime() {
        FunctionExpression countFunction = new FunctionExpression(FunctionEnum.COUNT, new Expression[]{new FieldExpression("water_level")});

        ComparisonCondition startTimeCondition = new ComparisonCondition("time", ComparisonOperator.GREATER_THAN_OR_EQUALS, "2019-08-18T00:00:00Z");
        ComparisonCondition endTimeCondition = new ComparisonCondition("time", ComparisonOperator.LESS_THAN_OR_EQUALS, "2019-08-18T00:30:00Z");

        CompositeCondition timeRangeCondition = new CompositeCondition(LogicalOperator.AND, startTimeCondition, endTimeCondition);
        FunctionExpression groupByTime = new FunctionExpression(FunctionEnum.TIME, new Expression[]{new ConstantExpression("12m")});

        Query query = QueryBuilder.create().select(new Expression[]{countFunction}).from(new String[]{"h2o_feet"}).where(timeRangeCondition).groupBy(new Expression[]{groupByTime}).build();

        String expectedQuery = "SELECT COUNT(\"water_level\") FROM \"h2o_feet\" WHERE (\"time\" >= '2019-08-18T00:00:00Z' AND \"time\" <= '2019-08-18T00:30:00Z') GROUP BY TIME(12m)";
        Assertions.assertEquals(expectedQuery, query.getCommand());
    }

    @Test
    public void testQueryBuilderSelectWithLimitAndOffset() {
        FieldExpression waterLevelField = new FieldExpression("water_level");
        FieldExpression locationField = new FieldExpression("location");

        Query query = QueryBuilder.create().select(new Expression[]{waterLevelField, locationField}).from(new String[]{"h2o_feet"}).limit(3).offset(3).build();

        String expectedQuery = "SELECT \"water_level\", \"location\" FROM \"h2o_feet\" LIMIT 3 OFFSET 3";
        Assertions.assertEquals(expectedQuery, query.getCommand());
    }

    @Test
    public void testQueryBuilderSelectWithWhereAndTimezone() {
        QueryBuilder qb = QueryBuilder.create();

        FieldExpression waterLevelField = new FieldExpression("water_level");

        ComparisonCondition locationCondition = new ComparisonCondition("location", ComparisonOperator.EQUALS, "santa_monica");
        ComparisonCondition startTimeCondition = new ComparisonCondition("time", ComparisonOperator.GREATER_THAN_OR_EQUALS, "2019-08-18T00:00:00Z");
        ComparisonCondition endTimeCondition = new ComparisonCondition("time", ComparisonOperator.LESS_THAN_OR_EQUALS, "2019-08-18T00:18:00Z");

        CompositeCondition finalCondition = new CompositeCondition(LogicalOperator.AND, locationCondition, startTimeCondition, endTimeCondition);

        TimeZone timeZone = TimeZone.getTimeZone("America/Chicago");

        Query query = qb.select(new Expression[]{waterLevelField})
                .from(new String[]{"h2o_feet"})
                .where(finalCondition)
                .timezone(timeZone.getID())
                .build();

        String expectedQuery = "SELECT \"water_level\" FROM \"h2o_feet\" WHERE (\"location\" = 'santa_monica' AND \"time\" >= '2019-08-18T00:00:00Z' AND \"time\" <= '2019-08-18T00:18:00Z') TZ('America/Chicago')";
        Assertions.assertEquals(expectedQuery, query.getCommand());
    }

    @Test
    public void testQueryBuilderSelectWithAsExpression() {
        QueryBuilder qb = QueryBuilder.create();

        FieldExpression waterLevelField = new FieldExpression("water_level");

        ComparisonCondition locationCondition = new ComparisonCondition("location", ComparisonOperator.EQUALS, "santa_monica");
        ComparisonCondition startTimeCondition = new ComparisonCondition("time", ComparisonOperator.GREATER_THAN_OR_EQUALS, "2019-08-18T00:00:00Z");
        ComparisonCondition endTimeCondition = new ComparisonCondition("time", ComparisonOperator.LESS_THAN_OR_EQUALS, "2019-08-18T00:18:00Z");

        CompositeCondition finalCondition = new CompositeCondition(LogicalOperator.AND, locationCondition, startTimeCondition, endTimeCondition);

        TimeZone timeZone = TimeZone.getTimeZone("America/Chicago");

        AsExpression asWL = new AsExpression("WL", waterLevelField);

        Query query = qb.select(new Expression[]{asWL})
                .from(new String[]{"h2o_feet"})
                .where(finalCondition)
                .timezone(timeZone.getID())
                .build();

        String expectedQuery = "SELECT \"water_level\" AS \"WL\" FROM \"h2o_feet\" WHERE (\"location\" = 'santa_monica' AND \"time\" >= '2019-08-18T00:00:00Z' AND \"time\" <= '2019-08-18T00:18:00Z') TZ('America/Chicago')";
        Assertions.assertEquals(expectedQuery, query.getCommand());
    }

    @Test
    public void testQueryBuilderSelectWithAggregate() {
        QueryBuilder qb = QueryBuilder.create();

        FieldExpression waterLevelField = new FieldExpression("water_level");
        FunctionExpression countWaterLevelField = new FunctionExpression(FunctionEnum.COUNT, new Expression[]{waterLevelField});

        ComparisonCondition locationCondition = new ComparisonCondition("location", ComparisonOperator.EQUALS, "santa_monica");
        ComparisonCondition startTimeCondition = new ComparisonCondition("time", ComparisonOperator.GREATER_THAN_OR_EQUALS, "2019-08-18T00:00:00Z");
        ComparisonCondition endTimeCondition = new ComparisonCondition("time", ComparisonOperator.LESS_THAN_OR_EQUALS, "2019-08-18T00:18:00Z");

        CompositeCondition finalCondition = new CompositeCondition(LogicalOperator.AND, locationCondition, startTimeCondition, endTimeCondition);

        TimeZone timeZone = TimeZone.getTimeZone("America/Chicago");

        AsExpression asWL = new AsExpression("WL", countWaterLevelField);

        Query query = qb.select(new Expression[]{asWL})
                .from(new String[]{"h2o_feet"})
                .where(finalCondition)
                .timezone(timeZone.getID())
                .build();

        String expectedQuery = "SELECT COUNT(\"water_level\") AS \"WL\" FROM \"h2o_feet\" WHERE (\"location\" = 'santa_monica' AND \"time\" >= '2019-08-18T00:00:00Z' AND \"time\" <= '2019-08-18T00:18:00Z') TZ('America/Chicago')";
        Assertions.assertEquals(expectedQuery, query.getCommand());
    }
}
