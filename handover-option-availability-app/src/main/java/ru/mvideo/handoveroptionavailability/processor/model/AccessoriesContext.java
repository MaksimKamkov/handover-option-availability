package ru.mvideo.handoveroptionavailability.processor.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.handoveroptionavailability.model.RetailBrand;

@EqualsAndHashCode(callSuper = true)
public class AccessoriesContext extends Context {

	private LocalDate deliveryDate;

	@Builder
	public AccessoriesContext(Set<String> requestHandoverOptions, Map<String, OptionContext> handoverOptionContext,
	                          String regionId, RetailBrand retailBrand, List<Material> materials, LocalDate deliveryDate) {
		super(requestHandoverOptions, handoverOptionContext, regionId, retailBrand, materials);
		this.deliveryDate = deliveryDate;
	}

	public LocalDate deliveryDate() {
		return deliveryDate;
	}

	public AccessoriesContext deliveryDate(LocalDate deliveryDate) {
		this.deliveryDate = deliveryDate;
		return this;
	}
}
