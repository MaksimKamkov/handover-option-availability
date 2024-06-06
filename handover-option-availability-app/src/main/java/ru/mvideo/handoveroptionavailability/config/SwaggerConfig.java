package ru.mvideo.handoveroptionavailability.config;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

	private static final String API_FILE_PATH = "contracts/api*.yaml";

	/**
	 * Resource provider for swagger schemas.
	 */
	@Primary
	@Bean
	public SwaggerResourcesProvider swaggerResourcesProvider(
			@Value("${spring.application.name}") String appName,
			ResourceLoader resourceLoader) {
		return () -> Arrays.stream(getApiSchemas(resourceLoader))
				.map(resource -> {
					final SwaggerResource swaggerResource = new SwaggerResource();
					final String filename = resource.getFilename();
					final String version = extractVersion(filename);
					final String apiName = appName + "-" + version;
					swaggerResource.setName(apiName);
					swaggerResource.setSwaggerVersion(version);
					swaggerResource.setLocation("/contracts/" + filename);
					return swaggerResource;
				})
				.sorted(Comparator.comparing(SwaggerResource::getSwaggerVersion).reversed())
				.collect(Collectors.toList());
	}

	private String extractVersion(String fileName) {
		final var version = StringUtils.substringBetween(fileName, "api_", ".yaml");
		return StringUtils.replace(version, "_", ".");
	}

	/*
	 * Load swagger contracts.
	 */
	private Resource[] getApiSchemas(ResourceLoader resourceLoader) {
		try {
			return ResourcePatternUtils
					.getResourcePatternResolver(resourceLoader)
					.getResources("classpath:" + API_FILE_PATH);
		} catch (IOException e) {
			throw new IllegalStateException("Swagger openapi not found.", e);
		}
	}

	/**
	 * Expose swagger api definition.
	 */
	@Bean
	public RouterFunction<ServerResponse> staticResourceRouter() {
		return RouterFunctions.resources("/contracts/**", new ClassPathResource("contracts/"));
	}

	/**
	 * Redirect from "/" to "/swagger-ui/".
	 */
	@Bean
	public RouterFunction<ServerResponse> redirectToSwaggerPageRoute() {
		return RouterFunctions.route(RequestPredicates.GET("/"), req ->
						ServerResponse.temporaryRedirect(URI.create("/swagger-ui/"))
								.build())
				.andRoute(RequestPredicates.GET("/v3/api-docs.yaml"), req ->
						ServerResponse.temporaryRedirect(URI.create("/" + API_FILE_PATH))
								.build());
	}
}
