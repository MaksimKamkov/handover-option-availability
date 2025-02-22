package ru.mvideo.handoveroptionavailability.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

	@Bean
	CorsWebFilter corsWebFilter() {
		var config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedOrigin("https://wiki.mvideo.ru");
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");

		var source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return new CorsWebFilter(source);
	}
}
