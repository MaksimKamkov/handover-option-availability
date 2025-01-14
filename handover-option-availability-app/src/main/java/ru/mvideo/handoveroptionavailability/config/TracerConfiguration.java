package ru.mvideo.handoveroptionavailability.config;

import io.opentracing.Tracer;
import io.opentracing.noop.NoopTracerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(value = "opentracing.jaeger.enabled", havingValue = "false")
@Configuration
public class TracerConfiguration {

	@Bean
	public Tracer jaegerTracer() {
		return NoopTracerFactory.create();
	}
}
