package ru.mvideo.handoveroptionavailability.processor.utils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.experimental.UtilityClass;
import ru.mvideo.lards.handover.option.model.ProviderAttribute;
import ru.mvideo.lards.handover.option.model.ProviderZoneAttributesHandoverOptions;

@UtilityClass
public class ProviderUtil {

	public Optional<Double> findMaxRadius(List<ProviderZoneAttributesHandoverOptions> providers) {
		return providers.stream()
				.map(ProviderZoneAttributesHandoverOptions::getProviderAttributes)
				.filter(attributes -> attributes.stream().anyMatch(attribute -> "source_radius".equals(attribute.getName())))
				.map(attributes -> {
					double radius = 0;
					for (ProviderAttribute attribute : attributes) {
						if ("source_radius".equals(attribute.getName())) {
							radius = Double.parseDouble(attribute.getValue());
						}
					}
					return radius;
				})
				.max(Double::compareTo);
	}

	public boolean containsHandoverOption(List<ProviderZoneAttributesHandoverOptions> providers, String option) {
		for (ProviderZoneAttributesHandoverOptions provider : providers) {
			for (ru.mvideo.lards.handover.option.model.HandoverOption handoverOption : provider.getHandoverOptions()) {
				if (option.equals(handoverOption.getName())) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean containsHandoverOption(ProviderZoneAttributesHandoverOptions provider, Set<String> options) {
		for (ru.mvideo.lards.handover.option.model.HandoverOption handoverOption : provider.getHandoverOptions()) {
			if (options.contains(handoverOption.getName())) {
				return true;
			}
		}
		return false;
	}
}
