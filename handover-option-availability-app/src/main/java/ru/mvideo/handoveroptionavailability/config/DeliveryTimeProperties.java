package ru.mvideo.handoveroptionavailability.config;

import java.time.LocalTime;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;

@Validated
@Data
public class DeliveryTimeProperties {

	@NotNull
	@DateTimeFormat(pattern = "HH:mm")
	LocalTime startTime;
	@NotNull
	@DateTimeFormat(pattern = "HH:mm")
	LocalTime endTime;
	@Max(720)
	@Min(0)
	@NotNull
	Integer deliveryTime;
	@Max(720)
	@Min(0)
	@NotNull
	Integer deliveryReturnTime;
}
