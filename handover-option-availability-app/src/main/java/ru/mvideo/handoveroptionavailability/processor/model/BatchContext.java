package ru.mvideo.handoveroptionavailability.processor.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.handoveroptionavailability.model.RetailBrand;
import ru.mvideo.lards.restriction.model.KnapsackBatchBriefResponse;

@EqualsAndHashCode(callSuper = true)
public class BatchContext extends Context {

	private Map<String, Map<String, BigDecimal>> materialOptionPrice = Collections.emptyMap();
	private Map<String, Set<String>> materialHandoverOption = Collections.emptyMap();
	private List<KnapsackBatchBriefResponse> batchPickupPoints;

	@Builder
	public BatchContext(Set<String> requestHandoverOptions, Map<String, OptionContext> handoverOptionContext,
	                    String regionId, RetailBrand retailBrand, List<Material> materials) {
		super(requestHandoverOptions, handoverOptionContext, regionId, retailBrand, materials);
	}

	public BatchContext materialOptionPrice(Map<String, Map<String, BigDecimal>> materialOptionPrice) {
		this.materialOptionPrice = materialOptionPrice;
		return this;
	}

	public Map<String, Map<String, BigDecimal>> materialOptionPrice() {
		return materialOptionPrice;
	}

	public BatchContext materialHandoverOption(Map<String, Set<String>> materialHandoverOption) {
		this.materialHandoverOption = materialHandoverOption;
		return this;
	}

	public Map<String, Set<String>> materialHandoverOption() {
		return materialHandoverOption;
	}

	public BatchContext batchPickupPoints(List<KnapsackBatchBriefResponse> batchPickupPoints) {
		this.batchPickupPoints = batchPickupPoints;
		return this;
	}

	public List<KnapsackBatchBriefResponse> batchPickupPoints() {
		return batchPickupPoints;
	}
}
