package ru.mvideo.handoveroptionavailability.processor.filter.fit;

import java.util.List;
import ru.mvideo.lards.handover.option.model.ProviderZoneAttributesHandoverOptions;

public interface FitService {
	List<ProviderZoneAttributesHandoverOptions> filterProviders(List<ProviderZoneAttributesHandoverOptions> providers, List<ExtendedProduct> products);

	boolean anyMatchProvider(List<ProviderZoneAttributesHandoverOptions> providers, List<ExtendedProduct> products);
}
