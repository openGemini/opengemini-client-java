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

package io.opengemini.client.spring.data.sample.measurement;

import io.opengemini.client.api.Precision;
import io.opengemini.client.spring.data.annotation.Database;
import io.opengemini.client.spring.data.annotation.Field;
import io.opengemini.client.spring.data.annotation.Measurement;
import io.opengemini.client.spring.data.annotation.RetentionPolicy;
import io.opengemini.client.spring.data.annotation.Tag;
import io.opengemini.client.spring.data.annotation.Time;
import lombok.Getter;
import lombok.Setter;

@Database(name = "weather_db", create = false)
@RetentionPolicy(name = "weather_rp", create = false)
@Measurement(name = "weather_ms")
@Getter
@Setter
public class WeatherTagNotString {

    @Tag(name = "Location")
    private Long location;

    @Field(name = "Temperature")
    private Double temperature;

    @Time(precision = Precision.PRECISIONMILLISECOND)
    private Long time;

}
