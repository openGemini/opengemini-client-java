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
public class WeatherFixNameNoCreate {

    @Tag(name = "Location")
    private String location;

    @Field(name = "Temperature")
    private Double temperature;

    @Time(precision = Precision.PRECISIONMILLISECOND)
    private Long time;

}
