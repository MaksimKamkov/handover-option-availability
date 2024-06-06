package ru.mvideo.handoveroptionavailability.client.config.properties;

import java.time.Duration;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties("ru.mvideo.handover-option-availability.client")
public class HandoverOptionAvailabilityClientProperties {

	@NotBlank
	private String host;

	private Duration readTimeout = Duration.ofSeconds(30);

	private Duration connectionTimeout = Duration.ofSeconds(2);

	private Duration maxIdleTime = Duration.ofSeconds(3);
}
