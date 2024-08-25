package io.opengemini.client.spring.data.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If unannotated, the method in OpenGeminiTemplate that requires a database name should be used.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Database {

    String name();

    boolean create() default true;

}
