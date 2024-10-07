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

import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Getter
public enum Precision {
    PRECISIONNANOSECOND("PrecisionNanoSecond", TimeUnit.NANOSECONDS, "ns"),

    PRECISIONMICROSECOND("PrecisionMicrosecond", TimeUnit.MICROSECONDS, "u"),

    PRECISIONMILLISECOND("PrecisionMillisecond", TimeUnit.MILLISECONDS, "ms"),

    PRECISIONSECOND("PrecisionSecond", TimeUnit.SECONDS, "s"),

    PRECISIONMINUTE("PrecisionMinute", TimeUnit.MINUTES, "m"),

    PRECISIONHOUR("PrecisionHour", TimeUnit.HOURS, "h");

    private final String description;

    private final TimeUnit timeUnit;

    private final String epoch;

    Precision(String description, TimeUnit timeUnit, String epoch) {
        this.description = description;
        this.timeUnit = timeUnit;
        this.epoch = epoch;
    }
}
