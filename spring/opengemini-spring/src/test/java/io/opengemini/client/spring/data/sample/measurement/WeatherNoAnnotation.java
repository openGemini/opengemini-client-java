package io.opengemini.client.spring.data.sample.measurement;

import io.opengemini.client.api.Precision;
import io.opengemini.client.spring.data.annotation.Field;
import io.opengemini.client.spring.data.annotation.Tag;
import io.opengemini.client.spring.data.annotation.Time;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeatherNoAnnotation {

    @Tag(name = "Location")
    private String location;

    @Field(name = "Temperature")
    private Double temperature;

    @Time(precision = Precision.PRECISIONMILLISECOND)
    private Long time;

}
