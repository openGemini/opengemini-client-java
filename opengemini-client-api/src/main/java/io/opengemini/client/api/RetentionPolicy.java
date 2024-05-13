package io.opengemini.client.api;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RetentionPolicy {
    String name;

    String duration;

    String shardGroupDuration;

    String hotDuration;

    String warmDuration;

    String indexDuration;

    int replicaNum;

    boolean isDefault;
}
