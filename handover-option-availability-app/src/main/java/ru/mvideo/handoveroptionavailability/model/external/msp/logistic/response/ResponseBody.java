package ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class ResponseBody {

	@JsonProperty
	private List<AvailabilityOption> availabilityOptions;
}
