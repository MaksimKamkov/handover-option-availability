package ru.mvideo.handoveroptionavailability.config;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@Data
@ConfigurationProperties(prefix = "handover-option-availability.config.msp")
public class MspConfig {

	@Min(10)
	@Max(50)
	@NotNull
	Integer maxCountShops;
}
