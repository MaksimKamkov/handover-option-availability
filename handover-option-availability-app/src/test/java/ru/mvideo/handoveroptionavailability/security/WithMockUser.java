package ru.mvideo.handoveroptionavailability.security;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockUserSecurityContextFactory.class)
public @interface WithMockUser {

	String channel() default "b2c";

	String[] authorities() default "TEST";

	String aud() default "aud";
}