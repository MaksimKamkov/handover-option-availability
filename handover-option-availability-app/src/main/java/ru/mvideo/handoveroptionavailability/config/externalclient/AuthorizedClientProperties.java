package ru.mvideo.handoveroptionavailability.config.externalclient;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.mvideo.lib.client.config.ClientProperties;

@Data
@EqualsAndHashCode(callSuper = true)
public class AuthorizedClientProperties extends ClientProperties {

	private String username;
	private String password;
}