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
