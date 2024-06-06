package ru.mvideo.handoveroptionavailability.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.config.EnableWebFlux;
import reactor.core.publisher.Mono;
import ru.mvideo.lastmile.starter.security.components.AuthenticationManager;
import ru.mvideo.lastmile.starter.security.components.SecurityContextRepository;

@EnableWebFlux
@EnableWebFluxSecurity
@RequiredArgsConstructor
//@EnableReactiveMethodSecurity
public class SecurityConfiguration {

	private final AuthenticationManager authenticationManager;
	private final SecurityContextRepository securityContextRepository;

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity serverHttpSecurity) {
		return serverHttpSecurity.exceptionHandling()
				.authenticationEntryPoint(
						(serverWebExchange, exception) ->
								Mono.just(serverWebExchange.getResponse()
										.setStatusCode(HttpStatus.UNAUTHORIZED)).then())
				.accessDeniedHandler(
						(serverWebExchange, exception) ->
								Mono.just(serverWebExchange.getResponse()
										.setStatusCode(HttpStatus.FORBIDDEN)).then())
				.and()
				.csrf().disable()
				.formLogin().disable()
				.httpBasic().disable()
				.authenticationManager(authenticationManager)
				.securityContextRepository(securityContextRepository)
				.authorizeExchange()
				.pathMatchers("/api/v3/**").authenticated()
				.anyExchange().permitAll()
				.and()
				.build();
	}
}
