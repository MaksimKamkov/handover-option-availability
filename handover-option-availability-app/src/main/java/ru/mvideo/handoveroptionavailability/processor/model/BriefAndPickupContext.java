package ru.mvideo.handoveroptionavailability.processor.model;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import ru.mvideo.availability_chains.model.RelatedObjectsDetails;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.handoveroptionavailability.model.PartnerBrand;
import ru.mvideo.handoveroptionavailability.model.RetailBrand;
import ru.mvideo.handoveroptionavailability.model.StockObject;
import ru.mvideo.lards.restriction.model.KnapsackBriefResponse;
import ru.mvideo.lards.restriction.model.KnapsackDetailResponse;
import ru.mvideo.msp.quota.model.QuotaAvailabilityResponse;

@EqualsAndHashCode(callSuper = true)
public class BriefAndPickupContext extends Context {
	private final List<PartnerBrand> pickupPointBrands;
	private QuotaAvailabilityResponse quotas;
	private KnapsackBriefResponse briefPickupPoints;
	private List<KnapsackDetailResponse> detailPickupPoints;
	private List<RelatedObjectsDetails> seamlessRelatedObjectsDetails;

	public BriefAndPickupContext(Set<String> requestHandoverOptions, Map<String, OptionContext> handoverOptionContext, String regionId,
	                             RetailBrand retailBrand, List<Material> materials, List<PartnerBrand> pickupPointBrands,
	                             Set<String> stockHandoverObjects, List<StockObject> stockObjects) {
		super(requestHandoverOptions, handoverOptionContext, regionId, retailBrand, materials, stockHandoverObjects, stockObjects);
		this.pickupPointBrands = pickupPointBrands;
	}

	@SuppressWarnings("unused")
	@Builder
	public static BriefAndPickupContext newContext(Set<String> requestHandoverOptions, Map<String, OptionContext> handoverOptionContext,
	                                               String regionId, RetailBrand retailBrand, List<Material> materials,
	                                               List<PartnerBrand> pickupPointBrands,
	                                               Set<String> stockHandoverObjects, List<StockObject> stockObjects) {
		return new BriefAndPickupContext(requestHandoverOptions, handoverOptionContext, regionId, retailBrand, materials, pickupPointBrands,
				stockHandoverObjects, stockObjects);
	}

	public List<PartnerBrand> pickupPointBrands() {
		return pickupPointBrands;
	}

	public QuotaAvailabilityResponse quotas() {
		return quotas;
	}

	public BriefAndPickupContext quotas(QuotaAvailabilityResponse quotas) {
		this.quotas = quotas;
		return this;
	}

	public KnapsackBriefResponse briefPickupPoints() {
		return briefPickupPoints;
	}

	public BriefAndPickupContext briefPickupPoints(KnapsackBriefResponse briefPickupPoints) {
		this.briefPickupPoints = briefPickupPoints;
		return this;
	}

	public List<KnapsackDetailResponse> detailPickupPoints() {
		return detailPickupPoints;
	}

	public BriefAndPickupContext detailPickupPoints(List<KnapsackDetailResponse> detailPickupPoints) {
		this.detailPickupPoints = detailPickupPoints;
		return this;
	}

	public List<RelatedObjectsDetails> seamlessRelatedObjectsDetails() {
		return seamlessRelatedObjectsDetails;
	}

	public BriefAndPickupContext seamlessRelatedObjectsDetails(List<RelatedObjectsDetails> seamlessRelatedObjectsDetails) {
		this.seamlessRelatedObjectsDetails = seamlessRelatedObjectsDetails;
		return this;
	}
}
