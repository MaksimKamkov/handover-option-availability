package ru.mvideo.handoveroptionavailability.processor.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import ru.mvideo.handoveroptionavailability.model.CoordinatePoint;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.handoveroptionavailability.model.RetailBrand;
import ru.mvideo.handoveroptionavailability.model.StockObject;
import ru.mvideo.lards.geospatial.model.GeoPoint;

@EqualsAndHashCode(callSuper = true)
public class DeliveryContext extends Context {
	private final String destination;
	private final CoordinatePoint coordinatePoint;
	private GeoPoint geoPoint;
	private Integer deliveryDuration;

	public DeliveryContext(Set<String> requestHandoverOptions, Map<String, OptionContext> handoverOptionContext, String regionId,
	                       RetailBrand retailBrand, List<Material> materials, String destination, CoordinatePoint coordinatePoint,
	                       Set<String> stockHandoverObjects, List<StockObject> stockObjects) {
		super(requestHandoverOptions, handoverOptionContext, regionId, retailBrand, materials, stockHandoverObjects, stockObjects);
		this.destination = destination;
		this.coordinatePoint = coordinatePoint;
	}

	@SuppressWarnings("unused")
	@Builder
	public static DeliveryContext newContext(Set<String> requestHandoverOptions, Map<String, OptionContext> handoverOptionContext,
	                                         String regionId, RetailBrand retailBrand, List<Material> materials,
	                                         String destination, CoordinatePoint coordinatePoint,
	                                         Set<String> stockHandoverObjects, List<StockObject> stockObjects) {
		return new DeliveryContext(requestHandoverOptions, handoverOptionContext, regionId, retailBrand, materials, destination,
				coordinatePoint, stockHandoverObjects, stockObjects);
	}

	public String destination() {
		return destination;
	}

	public CoordinatePoint coordinatePoint() {
		return coordinatePoint;
	}

	public GeoPoint geoPoint() {
		return geoPoint;
	}

	public DeliveryContext geoPoint(GeoPoint geoPoint) {
		this.geoPoint = geoPoint;
		return this;
	}

	public Integer getDeliveryDuration() {
		return deliveryDuration;
	}

	public void deliveryDuration(Integer deliveryDuration) {
		this.deliveryDuration = deliveryDuration;
	}
}
