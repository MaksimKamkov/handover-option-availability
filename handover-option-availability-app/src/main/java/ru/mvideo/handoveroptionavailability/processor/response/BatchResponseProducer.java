package ru.mvideo.handoveroptionavailability.processor.response;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.model.BatchOption;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.MaterialHandoverOptions;
import ru.mvideo.handoveroptionavailability.model.PartnerBrand;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.HandoverType;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.model.BatchContext;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;
import ru.mvideo.handoveroptionavailability.processor.model.OptionContext;
import ru.mvideo.lards.restriction.model.KnapsackBatchBriefResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchResponseProducer implements ResponseProducer<Flux<MaterialHandoverOptions>, BatchContext> {

	private static final Map<String, HandoverType> HANDOVER_TYPE_MAP = Map.of(
			HandoverOption.ETA_DELIVERY.getValue(), HandoverType.PICKUP,
			HandoverOption.EXACTLY_TIME_DELIVERY.getValue(), HandoverType.PICKUP,
			HandoverOption.DPD_DELIVERY.getValue(), HandoverType.POSTAL,
			HandoverOption.INTERVAL_DELIVERY.getValue(), HandoverType.COURIER,
			HandoverOption.ELECTRONIC_DELIVERY.getValue(), HandoverType.ELECTRONIC,
			HandoverOption.PICKUP.getValue(), HandoverType.PICKUP,
			HandoverOption.PICKUP_PARTNER.getValue(), HandoverType.PICKUP,
			HandoverOption.PICKUP_SEAMLESS.getValue(), HandoverType.PICKUP
	);

	@Override
	public Flux<MaterialHandoverOptions> produce(BatchContext context) {
		return Flux.fromIterable(context.materialHandoverOption().entrySet())
				.parallel()
				.runOn(Schedulers.parallel())
				.map(entry -> processMaterial(context, entry))
				.sequential();
	}

	private MaterialHandoverOptions processMaterial(BatchContext context, Map.Entry<String, Set<String>> materialOptionsEntry) {
		final var material = materialOptionsEntry.getKey();
		final var optionPrice = context.materialOptionPrice().get(material);
		final var batchOptions = new ArrayList<BatchOption>();
		for (String handoverOption : materialOptionsEntry.getValue()) {

			final var minPrice = optionPrice.get(handoverOption);
			if (minPrice == null) {
				continue;
			}

			final var availabilityDate = getAvailabilityDate(context, material, handoverOption);
			if (availabilityDate == null) {
				continue;
			}

			final var partnerBrands = getPartnerBrands(context, handoverOption, material);

			final var batchOption = new BatchOption();
			batchOption.setHandoverOption(handoverOption);
			batchOption.setMinPrice(minPrice.doubleValue());
			batchOption.setAvailabilityDate(availabilityDate);
			batchOption.setEta(handoverOption.equals(HandoverOption.ETA_DELIVERY.getValue()) ? 120 : null);
			batchOption.setPartnerBrand(partnerBrands);

			batchOptions.add(batchOption);
		}

		final var materialHandoverOptions = new MaterialHandoverOptions();
		materialHandoverOptions.setMaterial(material);
		materialHandoverOptions.setHandoverOption(batchOptions);
		return materialHandoverOptions;
	}

	private LocalDate getAvailabilityDate(BatchContext context, String material, String handoverOption) {
		final var options = context.availabilityOptions().stream()
				.filter(option -> material.equals(option.getMaterial()))
				.toList();

		if (HandoverOption.PICKUP_PARTNER.getValue().equals(handoverOption)) {
			final var sapCodes = context.sapCodes();
			final var sapCodesOptions = options.stream()
					.filter(option -> sapCodes.contains(option.getHandoverObject()))
					.toList();
			if (!sapCodesOptions.isEmpty()) {
				final var knapsackBatchBriefResponses = context.batchPickupPoints();
				for (KnapsackBatchBriefResponse knapsackBatchBriefResponse : knapsackBatchBriefResponses) {
					if (knapsackBatchBriefResponse.getMaterial().equals(material)) {
						return knapsackBatchBriefResponse.getMinAvailabilityDate();
					}
				}
			}

			return null;
		} else {
			final var handoverType = HANDOVER_TYPE_MAP.get(handoverOption);

			final var materialAvailabilityOptionsWithType = options.stream()
					.filter(availabilityOption -> handoverType.equals(availabilityOption.getHandoverType()))
					.toList();

			return calculateMinOptionDateForMaterial(handoverOption, materialAvailabilityOptionsWithType, context);
		}
	}

	private LocalDate calculateMinOptionDateForMaterial(String handoverOption,
	                                                    List<AvailabilityOption> availabilityOptions,
	                                                    BatchContext context) {
		final OptionContext optionContext = context.handoverOptionContext().get(handoverOption);
		return switch (handoverOption) {
			case "interval-delivery", "electronic-delivery" -> calculateNotPickupDate(availabilityOptions);
			case "eta-delivery" -> calculateEtaMinDate(availabilityOptions, optionContext);
			case "pickup" -> calculatePickupMinDate(availabilityOptions, optionContext);
			default -> null;
		};
	}

	private LocalDate calculateNotPickupDate(List<AvailabilityOption> availabilityOptions) {
		return availabilityOptions.stream()
				.min(Comparator.comparing(AvailabilityOption::getAvailableDate))
				.map(AvailabilityOption::getAvailableDate)
				.orElse(null);
	}

	private LocalDate calculatePickupMinDate(List<AvailabilityOption> availabilityOptions, OptionContext context) {
		if (context == null) {
			return null;
		}
		final var handoverObjectIds = context.getHandoverObjects().stream()
				.map(HandoverObject::getObjectId)
				.toList();

		return availabilityOptions.stream()
				.filter(option -> handoverObjectIds.contains(option.getHandoverObject()))
				.min(Comparator.comparing(AvailabilityOption::getAvailableDate))
				.map(AvailabilityOption::getAvailableDate)
				.orElse(null);
	}

	private LocalDate calculateEtaMinDate(List<AvailabilityOption> availabilityOptions, OptionContext context) {
		if (context == null) {
			return null;
		}
		final var handoverObjectIds = context.getHandoverObjects().stream()
				.map(HandoverObject::getObjectId)
				.toList();

		final var now = LocalDate.now();
		return availabilityOptions.stream()
				.filter(option -> handoverObjectIds.contains(option.getHandoverObject()))
				.filter(option -> option.getHandoverObject().equals(option.getStockObject()))
				.filter(option -> now.equals(option.getValidTo().toLocalDate()))
				.filter(option -> now.equals(option.getAvailableDate()))
				.findAny()
				.map(AvailabilityOption::getAvailableDate)
				.orElse(null);
	}

	private List<PartnerBrand> getPartnerBrands(BatchContext context, String handoverOption, String material) {
		if (!HandoverOption.PICKUP_PARTNER.getValue().equals(handoverOption)) {
			return Collections.emptyList();
		}

		return context.batchPickupPoints().stream()
				.filter(response -> material.equals(response.getMaterial()))
				.flatMap(response -> response.getPartnerBrand().stream())
				.map(pp -> PartnerBrand.valueOf(pp.name()))
				.distinct()
				.toList();
	}
}
