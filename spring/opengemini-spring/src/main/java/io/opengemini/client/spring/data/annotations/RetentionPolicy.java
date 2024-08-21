package io.opengemini.client.spring.data.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RetentionPolicy {

    String name();

    boolean create() default true;

    String duration();

    String shardGroupDuration();

    String hotDuration() default "";

    String warmDuration() default "";

    String indexDuration() default "";

    int replicaNum() default 1;

    boolean isDefault() default false;

}
