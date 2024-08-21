package io.opengemini.client.spring.data.annotations;

import io.opengemini.client.api.Precision;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TimeColumn {

    Precision precision() default Precision.PRECISIONMILLISECOND;

}
