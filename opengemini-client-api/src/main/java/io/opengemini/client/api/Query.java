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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
@Setter
public class Query {
    /*
     * the query command
     */
    private String command;

    /*
     * the database name of the query command using
     */
    private String database;

    /*
     * the rp name of the query command using
     */
    private String retentionPolicy;

    /*
     * the precision of the time in query result
     */
    private Precision precision;

    private Map<String, Object> attributes = new HashMap<>();

    public Query(String command) {
        this.command = command;
    }

    public Query(String command, String database, String retentionPolicy) {
        this.command = command;
        this.database = database;
        this.retentionPolicy = retentionPolicy;
    }

    public Query(String command, String database, String retentionPolicy, Precision precision) {
        this.command = command;
        this.database = database;
        this.retentionPolicy = retentionPolicy;
        this.precision = precision;
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }
}
