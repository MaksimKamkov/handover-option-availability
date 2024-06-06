package ru.mvideo.handoveroptionavailability.model.external.msp.logistic.responsenextcalendar;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class ResponseBodyNextCalendar {

	@JsonProperty
	private List<AvailabilityOptionNextCalendar> availabilityOptions;
}