package com.flipkart.flux;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectFromRole {
    FluxRole value() default FluxRole.ORCHESTRATION;
    String name() default "";
}
