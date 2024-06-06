package ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.HandoverType;

@Data
@Builder(toBuilder = true)
public class AvailabilityOption {

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
	private LocalDate availableDate;
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
	@JsonIgnore
	private LocalDateTime validTo;

	public AvailabilityOption() {
	}

	public AvailabilityOption(String material,
	                          HandoverType handoverType,
	                          String handoverObject,
	                          String stockObject,
	                          int stockObjectPriority,
	                          int availableStock,
	                          int showCaseStock,
	                          int minQuantity,
	                          int freeAvailableStock,
	                          LocalDate availableDate,
	                          boolean prepaidOnly,
	                          boolean preorderCalendar,
	                          String preorderPhase,
	                          String storeType,
	                          int schemaClass,
	                          LocalDateTime validTo) {
		this.material = material;
		this.handoverType = handoverType;
		this.handoverObject = handoverObject;
		this.stockObject = stockObject;
		this.stockObjectPriority = stockObjectPriority;
		this.availableStock = availableStock;
		this.showCaseStock = showCaseStock;
		this.minQuantity = minQuantity;
		this.freeAvailableStock = freeAvailableStock;
		this.availableDate = availableDate;
		this.prepaidOnly = prepaidOnly;
		this.preorderCalendar = preorderCalendar;
		this.preorderPhase = preorderPhase;
		this.storeType = storeType;
		this.schemaClass = schemaClass;
		this.validTo = validTo;
	}
}