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

import io.opengemini.client.api.QueryResult;
import io.opengemini.client.api.RetentionPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ResultMapper {

    public static List<String> toDatabases(QueryResult result) {
        return result.getResults()
                .get(0)
                .getSeries()
                .get(0)
                .getValues()
                .stream()
                .map(x -> String.valueOf(x.get(0)))
                .collect(Collectors.toList());
    }

    public static List<RetentionPolicy> toRetentionPolicies(QueryResult result) {
        return converseRps(result.getResults().get(0).getSeries().get(0).getValues());
    }

    private static List<RetentionPolicy> converseRps(List<List<Object>> queryRpValues) {
        List<RetentionPolicy> retentionPolicies = new ArrayList<>();
        queryRpValues.forEach(x -> retentionPolicies.add(converseRp(x)));
        return retentionPolicies;
    }

    private static RetentionPolicy converseRp(List<Object> queryRpValue) {
        RetentionPolicy rst = new RetentionPolicy();
        rst.setName((String) queryRpValue.get(0));
        rst.setDuration((String) queryRpValue.get(1));
        rst.setShardGroupDuration((String) queryRpValue.get(2));
        rst.setHotDuration((String) queryRpValue.get(3));
        rst.setWarmDuration((String) queryRpValue.get(4));
        rst.setIndexDuration((String) queryRpValue.get(5));
        rst.setReplicaNum((Integer) queryRpValue.get(6));
        rst.setDefault((Boolean) queryRpValue.get(7));
        return rst;
    }

}
