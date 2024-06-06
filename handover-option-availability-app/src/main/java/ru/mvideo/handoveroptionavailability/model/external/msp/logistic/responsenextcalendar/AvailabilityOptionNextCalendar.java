package ru.mvideo.handoveroptionavailability.model.external.msp.logistic.responsenextcalendar;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.HandoverType;

@Data
public class AvailabilityOptionNextCalendar {

	@JsonProperty
	private String material;
	@JsonProperty
	private HandoverType handoverType;
	@JsonProperty
	private String handoverObject;
	@JsonProperty
	private String stockObject;
	@JsonProperty
	private int stockObjectPriority;
	@JsonProperty
	private int availableStock;
	@JsonProperty
	private int showCaseStock;
	@JsonProperty
	private int minQuantity;
	@JsonProperty
	private int freeAvailableStock;
	@JsonProperty
	private List<AvailabilityDate> availabilityDates;
	@JsonProperty
	private boolean prepaidOnly;
	@JsonProperty
	private boolean preorderCalendar;
	@JsonProperty
	private String preorderPhase;
	@JsonProperty
	private String storeType;
	@JsonProperty
	private int schemaClass;

}
