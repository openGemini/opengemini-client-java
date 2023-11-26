package io.opengemini.client.api;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BatchConfig {
    /**
     * BatchInterval batch time interval that triggers batch processing. (unit: ms)
     */
    private int batchInterval;
    /**
     * BatchSize batch size that triggers batch processing.
     */
    private int batchSize;
}
