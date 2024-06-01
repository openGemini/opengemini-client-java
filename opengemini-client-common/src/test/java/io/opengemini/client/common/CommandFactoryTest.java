package io.opengemini.client.common;

import io.opengemini.client.api.RpConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CommandFactoryTest {

    @Test
    void createDatabase() {
        Assertions.assertEquals("CREATE DATABASE \"test_db\"", CommandFactory.createDatabase("test_db"));
    }

    @Test
    void dropDatabase() {
        Assertions.assertEquals("DROP DATABASE \"test_db\"", CommandFactory.dropDatabase("test_db"));
    }

    @Test
    void showDatabases() {
        Assertions.assertEquals("SHOW DATABASES", CommandFactory.showDatabases());
    }

    @Test
    void createRetentionPolicy() {
        Assertions.assertEquals(
                "CREATE RETENTION POLICY \"test_rp\" ON \"test_db\" DURATION 3d REPLICATION 1 SHARD DURATION 1h "
                        + "INDEX DURATION 7h DEFAULT",
                CommandFactory.createRetentionPolicy("test_db", new RpConfig("test_rp", "3d", "1h", "7h"), true));
        Assertions.assertEquals(
                "CREATE RETENTION POLICY \"test_rp\" ON \"test_db\" DURATION 3d REPLICATION 1 SHARD DURATION 1h "
                        + "INDEX DURATION 7h",
                CommandFactory.createRetentionPolicy("test_db", new RpConfig("test_rp", "3d", "1h", "7h"), false));
        Assertions.assertEquals(
                "CREATE RETENTION POLICY \"test_rp\" ON \"test_db\" DURATION 3d REPLICATION 1 INDEX DURATION 7h",
                CommandFactory.createRetentionPolicy("test_db", new RpConfig("test_rp", "3d", "", "7h"), false));
        Assertions.assertEquals(
                "CREATE RETENTION POLICY \"test_rp\" ON \"test_db\" DURATION 3d REPLICATION 1 SHARD DURATION 1h",
                CommandFactory.createRetentionPolicy("test_db", new RpConfig("test_rp", "3d", "1h", ""), false));
        Assertions.assertEquals("CREATE RETENTION POLICY \"test_rp\" ON \"test_db\" DURATION 3d REPLICATION 1",
                CommandFactory.createRetentionPolicy("test_db", new RpConfig("test_rp", "3d", "", ""), false));
    }

    @Test
    void dropRetentionPolicy() {
        Assertions.assertEquals("DROP RETENTION POLICY \"test_rp\" ON \"test_db\"",
                CommandFactory.dropRetentionPolicy("test_db", "test_rp"));
    }

    @Test
    void showRetentionPolicies() {
        Assertions.assertEquals("SHOW RETENTION POLICIES ON \"test_db\"",
                CommandFactory.showRetentionPolicies("test_db"));
    }
}
