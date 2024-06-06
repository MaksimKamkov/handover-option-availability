package ru.mvideo.handoveroptionavailability.config.annotation.condition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@ConditionalOnProperty(
		prefix = "handover-option-availability.config.cache",
		name = "handover-option-objects-cache-enabled",
		havingValue = "true"
)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ConditionalOnObjectsCacheEnabled {
}
