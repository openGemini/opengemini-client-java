package io.opengemini.client.spring.data.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * If unannotated, the method in OpenGeminiTemplate that requires a retention policy name should be used.
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RetentionPolicy {

    String name();

    boolean create() default true;

    String duration() default "";

    String shardGroupDuration() default "";

    String hotDuration() default "";

    String warmDuration() default "";

    String indexDuration() default "";

    int replicaNum() default 1;

    boolean isDefault() default false;

}
