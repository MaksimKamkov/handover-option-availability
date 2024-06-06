package ru.mvideo.handoveroptionavailability.processor.model;

import java.time.LocalTime;
import java.time.ZoneId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.mvideo.lards.geospatial.model.GeoPoint;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HandoverObject {
	private String objectId;
	private Double distance;
	private LocalTime deliveryStartTime;
	private LocalTime deliveryEndTime;
	private LocalTime workStartTime;
	private LocalTime workEndTime;
	private ZoneId timeZone;
	private Boolean isAutoCourierAvailable;
	private GeoPoint coordinate;
}
