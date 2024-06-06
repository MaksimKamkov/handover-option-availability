package ru.mvideo.handoveroptionavailability.config;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@Data
@ConfigurationProperties(prefix = "handover-option-availability.config.quota")
public class QuotaConfig {

	@Min(1)
	@NotNull
	Integer maxDays;
}
