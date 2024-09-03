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

@Database(name = "testdb", create = false)
@RetentionPolicy(name = "testrp", create = false)
@Measurement(name = "testms")
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
