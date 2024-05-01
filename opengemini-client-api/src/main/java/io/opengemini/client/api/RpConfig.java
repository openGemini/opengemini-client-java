package io.opengemini.client.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RpConfig {
    String name;

    String duration;

    String shardGroupDuration;

    String indexDuration;
}
