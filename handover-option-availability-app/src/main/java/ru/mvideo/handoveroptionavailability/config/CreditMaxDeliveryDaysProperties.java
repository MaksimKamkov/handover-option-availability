package ru.mvideo.handoveroptionavailability.config;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@Data
@ConfigurationProperties(prefix = "handover-option-availability.config.credit")
public class CreditMaxDeliveryDaysProperties {

	@Max(60)
	@Min(0)
	@NotNull
	Integer maxDeliveryDays;

	@Max(100)
	@Min(0)
	@NotNull
	Integer minStock;
}
