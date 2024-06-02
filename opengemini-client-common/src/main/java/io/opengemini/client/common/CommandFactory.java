package io.opengemini.client.common;

import io.opengemini.client.api.RpConfig;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class CommandFactory {

    public static String createDatabase(String database) {
        return format("CREATE DATABASE \"%s\"", database);
    }

    public static String dropDatabase(String database) {
        return format("DROP DATABASE \"%s\"", database);
    }

    public static String showDatabases() {
        return "SHOW DATABASES";
    }

    public static String createRetentionPolicy(String database, RpConfig rpConfig, boolean isDefault) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE RETENTION POLICY \"").append(rpConfig.getName()).append("\"");
        sb.append(" ON \"").append(database).append("\"");
        sb.append(" DURATION ").append(rpConfig.getDuration());
        sb.append(" REPLICATION 1");

        if (StringUtils.isNotBlank(rpConfig.getShardGroupDuration())) {
            sb.append(" SHARD DURATION ").append(rpConfig.getShardGroupDuration());
        }

        if (StringUtils.isNotBlank(rpConfig.getIndexDuration())) {
            sb.append(" INDEX DURATION ").append(rpConfig.getIndexDuration());
        }

        if (isDefault) {
            sb.append(" DEFAULT");
        }
        return sb.toString();
    }

    public static String dropRetentionPolicy(String database, String retentionPolicy) {
        return format("DROP RETENTION POLICY \"%s\" ON \"%s\"", retentionPolicy, database);
    }

    public static String showRetentionPolicies(String database) {
        return format("SHOW RETENTION POLICIES ON \"%s\"", database);
    }

    private static String format(String format, Object... args) {
        return String.format(Locale.ROOT, format, args);
    }

}
