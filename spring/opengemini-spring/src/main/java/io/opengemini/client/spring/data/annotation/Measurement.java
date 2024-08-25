package io.opengemini.client.spring.data.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Measurement {

    /**
     * If unset, the method in OpenGeminiTemplate that requires a measurement name should be used.
     */
    String name() default "";

}
