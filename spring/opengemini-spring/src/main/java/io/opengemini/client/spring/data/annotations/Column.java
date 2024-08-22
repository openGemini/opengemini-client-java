package io.opengemini.client.spring.data.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {

    /**
     * If unset, the annotated field's name will be used as the column name.
     */
    String name();

    boolean tag() default false;

}
