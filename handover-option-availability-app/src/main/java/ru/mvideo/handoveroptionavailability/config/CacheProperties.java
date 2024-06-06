package ru.mvideo.handoveroptionavailability.config;

import java.time.Duration;
import javax.validation.constraints.NotNull;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

@Value
@Validated
@ConstructorBinding
@ConfigurationProperties(prefix = "handover-option-availability.config.cache")
public class CacheProperties {

	@NotNull
	Duration handoverOptionBriefTtl;
	@NotNull
	Duration handoverOptionZoneObjectsTtl;
}
