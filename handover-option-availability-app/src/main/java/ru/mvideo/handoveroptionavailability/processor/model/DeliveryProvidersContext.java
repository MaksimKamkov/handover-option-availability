package ru.mvideo.handoveroptionavailability.processor.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import ru.mvideo.handoveroptionavailability.model.LocationDescriptionV2;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.handoveroptionavailability.model.RetailBrand;
import ru.mvideo.lards.geospatial.model.GeoPoint;

@EqualsAndHashCode(callSuper = true)
public class DeliveryProvidersContext extends Context {

	private String handoverOptionMaterial;

	private LocationDescriptionV2 recipient;
	private LocationDescriptionV2 source;

	private GeoPoint coordinatesRecipient;
	private GeoPoint coordinatesSource;

	@Builder
	public DeliveryProvidersContext(Set<String> requestHandoverOptions, Map<String, OptionContext> handoverOptionContext,
	                                String regionId, RetailBrand retailBrand, List<Material> materials,
	                                LocationDescriptionV2 recipient, LocationDescriptionV2 source,
	                                GeoPoint coordinatesRecipient, GeoPoint coordinatesSource,
	                                String handoverOptionMaterial) {
		super(requestHandoverOptions, handoverOptionContext, regionId, retailBrand, materials);
		this.handoverOptionMaterial = handoverOptionMaterial;
		this.recipient = recipient;
		this.source = source;
		this.coordinatesRecipient = coordinatesRecipient;
		this.coordinatesSource = coordinatesSource;
	}

	public String getHandoverOptionMaterial() {
		return handoverOptionMaterial;
	}

	public void setHandoverOptionMaterial(String handoverOptionMaterial) {
		this.handoverOptionMaterial = handoverOptionMaterial;
	}

	public LocationDescriptionV2 getRecipient() {
		return recipient;
	}

	public void setRecipient(LocationDescriptionV2 recipient) {
		this.recipient = recipient;
	}

	public LocationDescriptionV2 getSource() {
		return source;
	}

	public void setSource(LocationDescriptionV2 source) {
		this.source = source;
	}

	public GeoPoint getCoordinatesRecipient() {
		return coordinatesRecipient;
	}

	public void setCoordinatesRecipient(GeoPoint coordinatesRecipient) {
		this.coordinatesRecipient = coordinatesRecipient;
	}

	public GeoPoint getCoordinatesSource() {
		return coordinatesSource;
	}

	public void setCoordinatesSource(GeoPoint coordinatesSource) {
		this.coordinatesSource = coordinatesSource;
	}
}
