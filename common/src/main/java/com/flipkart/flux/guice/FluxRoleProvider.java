package com.flipkart.flux.guice;


import com.flipkart.flux.FluxRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FluxRoleProvider {
    FluxRole value() default FluxRole.EXECUTION;
}
