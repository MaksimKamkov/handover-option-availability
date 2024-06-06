package ru.mvideo.handoveroptionavailability.processor.context.batch;

import static ru.mvideo.handoveroptionavailability.model.HandoverOption.ETA_DELIVERY;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.config.MspConfig;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.HandoverType;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.BaseProcessor;
import ru.mvideo.handoveroptionavailability.processor.model.BatchContext;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;
import ru.mvideo.handoveroptionavailability.processor.model.HandoverObject;
import ru.mvideo.handoveroptionavailability.processor.model.OptionContext;
import ru.mvideo.handoveroptionavailability.service.external.msp.logistic.MspLogisticService;

@Component
@RequiredArgsConstructor
public class BatchAvailabilityOptionsProcessor extends BaseProcessor<BatchContext> {

	private final MspLogisticService mspLogisticService;
	private final MspConfig mspConfig;

	@Override
	protected Mono<BatchContext> executeProcessor(BatchContext context) {
		return Mono.defer(() -> {
			final var pickupObjects = extractPickupObjectsByPriority(context);

			final var objectsFirstCall = pickupObjects.stream()
					.limit(mspConfig.getMaxCountShops())
					.toList();

			return mspFirstCall(context, objectsFirstCall)
					.flatMap(response -> {
						if (!containsPickupOption(context)) {
							return Mono.just(context);
						}
						final var materialsWithoutPickup = calculateMaterialsWithoutPickup(
								response.availabilityOptions(),
								context.materials(),
								context.handoverObjects());

						if (materialsWithoutPickup.isEmpty()) {
							return Mono.just(context);
						}

						return mspCall(context, pickupObjects, materialsWithoutPickup, 1);
					});
		}).subscribeOn(Schedulers.boundedElastic());
	}

	@Override
	public boolean shouldRun(BatchContext context) {
		return !context.flags().contains(Flags.INDEPENDENT_SERVICE);
	}

	private Mono<BatchContext> mspFirstCall(BatchContext context, List<String> pickupObjects) {
		return Mono.defer(() -> {
			final var handoverTypes = context.options().stream()
					.map(this::defineHandoverType)
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());

			return mspLogisticService.fetchAvailabilityCalendar(
					context.retailBrand(),
					context.regionId(),
					handoverTypes,
					context.materials(),
					new HashSet<>(pickupObjects),
					context.sapCodes()
			)
					.map(options -> {
						context.availabilityOptions(options);
						return context;
					})
					.switchIfEmpty(Mono.fromCallable(() -> {
						context.availabilityOptions(Collections.emptyList());
						return context;
					}));
		}).subscribeOn(Schedulers.boundedElastic());
	}

	private Mono<BatchContext> mspCall(BatchContext context, List<String> pickupObjects, List<Material> materials, final int count) {
		return Mono.defer(() -> {
			final var pickupObjectsBatch = pickupObjects.stream()
					.skip((long) mspConfig.getMaxCountShops() * count)
					.limit(mspConfig.getMaxCountShops())
					.toList();
			if (pickupObjectsBatch.isEmpty()) {
				return Mono.just(context);
			}

			return mspLogisticService.fetchAvailabilityCalendar(
							context.retailBrand(),
							null,
							Set.of(HandoverType.PICKUP),
							materials,
							new HashSet<>(pickupObjectsBatch),
							Collections.emptySet()
					)
					.flatMap(response -> {
						if (CollectionUtils.isEmpty(context.availabilityOptions())) {
							context.availabilityOptions(response);
						} else {
							context.availabilityOptions().addAll(response);
						}

						final var materialsWithoutPickup
								= calculateMaterialsWithoutPickup(response, materials, context.handoverObjects());
						if (!materialsWithoutPickup.isEmpty()) {
							return mspCall(context, pickupObjects, materialsWithoutPickup, count + 1);
						}
						return Mono.just(context);
					})
					.switchIfEmpty(Mono.defer(() -> mspCall(context, pickupObjects, materials, count + 1)));
		}).subscribeOn(Schedulers.boundedElastic());
	}

	private List<Material> calculateMaterialsWithoutPickup(List<AvailabilityOption> options,
	                                                       List<Material> materials,
	                                                       List<HandoverObject> handoverObjects) {
		final List<Material> result = new ArrayList<>();
		for (Material material : materials) {
			if (!hasOptionsForMaterial(options, material, handoverObjects)) {
				result.add(material);
			}
		}
		return result;
	}

	private boolean containsPickupOption(BatchContext context) {
		return context.hasOption(HandoverOption.ETA_DELIVERY.getValue())
				|| context.hasOption(HandoverOption.EXACTLY_TIME_DELIVERY.getValue())
				|| context.hasOption(HandoverOption.PICKUP_SEAMLESS.getValue())
				|| context.hasOption(HandoverOption.PICKUP.getValue());
	}

	private boolean hasOptionsForMaterial(List<AvailabilityOption> availabilityOptions,
	                                                    Material material,
	                                                    List<HandoverObject> handoverObjects) {

		final var handoverObjectIds = handoverObjects.stream()
				.map(HandoverObject::getObjectId)
				.toList();

		return availabilityOptions.stream()
				.anyMatch(option -> material.getMaterial().contains(option.getMaterial())
						&& handoverObjectIds.contains(option.getHandoverObject())
						&& option.getHandoverObject().equals(option.getStockObject())
						&& option.getAvailableStock() >= material.getQty()
						&& option.getValidTo().toLocalDate().equals(LocalDate.now())
						&& option.getAvailableDate().equals(LocalDate.now())
				);
	}

	private List<String> extractPickupObjectsByPriority(BatchContext context) {
		// в первую очередь пытаемся получить доступные опции для ETA, поэтому помещаем их в начало списка
		List<String> result = new ArrayList<>(context.pickupObjectIds());
		OptionContext etaOptionContext = context.handoverOptionContext().get(ETA_DELIVERY.getValue());
		if (etaOptionContext != null) {
			List<HandoverObject> handoverObjects = etaOptionContext.getHandoverObjects();
			for (int i = 0; i < handoverObjects.size(); i++) {
				String objectId = handoverObjects.get(i).getObjectId();
				result.remove(objectId);
				result.add(i, objectId);
			}
		}

		return result;
	}


	private HandoverType defineHandoverType(String handoverOption) {
		return switch (handoverOption) {
			case "interval-delivery" -> HandoverType.COURIER;
			case "dpd-delivery" -> HandoverType.POSTAL;
			case "electronic-delivery" -> HandoverType.ELECTRONIC;
			case "eta-delivery", "exactly-time-delivery", "pickup", "pickup-partner", "pickup-seamless" -> HandoverType.PICKUP;
			default -> null;
		};
	}
}
