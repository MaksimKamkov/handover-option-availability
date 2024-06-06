package ru.mvideo.handoveroptionavailability.processor.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.handoveroptionavailability.model.RetailBrand;
import ru.mvideo.io.pickup.points.lib.model.response.PickupPoint;

@EqualsAndHashCode(callSuper = true)
public class StockObjectsContext extends Context {

	private List<PickupPoint> pickupPoints = Collections.emptyList();
	private String pickupPointId;
	private Material currentMaterial;

	public StockObjectsContext(Set<String> requestHandoverOptions, Map<String, OptionContext> handoverOptionContext, String regionId,
	                           RetailBrand retailBrand, List<Material> materials, String pickupPointId) {
		super(requestHandoverOptions, handoverOptionContext, regionId, retailBrand, materials);

		this.pickupPointId = pickupPointId;
	}

	@SuppressWarnings("unused")
	@Builder
	public static StockObjectsContext newContext(Set<String> requestHandoverOptions, Map<String, OptionContext> handoverOptionContext,
	                                             String regionId, RetailBrand retailBrand, List<Material> materials, String pickupPointId) {
		return new StockObjectsContext(requestHandoverOptions, handoverOptionContext, regionId, retailBrand, materials, pickupPointId);
	}

	public StockObjectsContext pickupPointId(String pickupPointId) {
		this.pickupPointId = pickupPointId;
		return this;
	}

	public String pickupPointId() {
		return pickupPointId;
	}

	public StockObjectsContext pickupPoints(List<PickupPoint> pickupPoints) {
		this.pickupPoints = pickupPoints;
		return this;
	}

	public List<PickupPoint> pickupPoints() {
		return pickupPoints;
	}

	public StockObjectsContext currentMaterial(Material material) {
		this.currentMaterial = material;
		return this;
	}

	public Material currentMaterial() {
		return currentMaterial;
	}
}
