package ru.mvideo.handoveroptionavailability.security;

import static java.util.Map.of;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.assertj.core.util.Lists;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import ru.mvideo.lastmile.auth.common.model.context.DefaultKAuthServerUser;
import ru.mvideo.lastmile.auth.common.model.context.KAuthServerAuthenticationToken;

public class WithMockUserSecurityContextFactory implements WithSecurityContextFactory<WithMockUser> {

	@Override
	public SecurityContext createSecurityContext(WithMockUser withMockUser) {
		final SecurityContext context = SecurityContextHolder.createEmptyContext();

		final Set<GrantedAuthority> authorities = Arrays.stream(withMockUser.authorities())
				.map(SimpleGrantedAuthority::new).collect(Collectors.toSet());

		final var defaultKAuthServerUser = new DefaultKAuthServerUser(authorities,
				of("channel", withMockUser.channel(), "aud", Lists.newArrayList(withMockUser.aud()))
		);
		final var authentication = new KAuthServerAuthenticationToken(defaultKAuthServerUser, null);
		context.setAuthentication(authentication);
		return context;
	}
}