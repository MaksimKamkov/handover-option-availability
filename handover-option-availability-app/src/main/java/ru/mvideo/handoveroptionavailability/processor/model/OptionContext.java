package ru.mvideo.handoveroptionavailability.processor.model;

import java.util.List;
import lombok.Data;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.ExtendedProduct;
import ru.mvideo.lards.price.rules.model.MinPriceResponse;

@Data
public class OptionContext {
	private String handoverOption;
	private String handoverOptionMaterial;
	private MinPriceResponse minPriceRule;
	private List<HandoverObject> handoverObjects;
	private List<AvailabilityOption> availabilityOptions;
	private List<String> paymentConditions;
	private List<ExtendedProduct> products;
}
