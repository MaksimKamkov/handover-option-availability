package ru.mvideo.handoveroptionavailability.service.external.msp.logistic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.mvideo.handoveroptionavailability.client.MspClient;
import ru.mvideo.handoveroptionavailability.mapper.AvailabilityCalendarMapper;
import ru.mvideo.handoveroptionavailability.model.Material;
import ru.mvideo.handoveroptionavailability.model.RetailBrand;
import ru.mvideo.handoveroptionavailability.model.StockObject;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.HandoverType;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.Parameter;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.request.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.request.MspRequest;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.request.MspStockObject;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.request.Position;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.request.RequestBody;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.responsenextcalendar.MspResponseNextCalendar;

@Slf4j
@Service
@RequiredArgsConstructor
public class MspLogisticServiceImpl implements MspLogisticService {

	private static final List<Parameter> MVIDEO_OPTION_PARAMS = List.of(
			new Parameter("displayMode", "all"),
			new Parameter("displayShowcase", "true"),
			new Parameter("displayNextCalendar", "true"),
			new Parameter("displayPreorderInfo", "true"),
			new Parameter("minDisplayQty", "0"),
			new Parameter("excludeZeroStock", "true"),
			new Parameter("excludeCourierShowCase", "true"),
			new Parameter("excludePickupMovementShowCase", "true")
	);

	private static final List<Parameter> MVIDEO_STOCK_OBJECTS_OPTION_PARAMS = List.of(
			new Parameter("displayNextCalendar", "true"),
			new Parameter("displayPreorderInfo", "true")
	);

	private static final List<Parameter> ELDORADO_OPTION_PARAMS = List.of(
			new Parameter("displayMode", "all"),
			new Parameter("displayShowcase", "true"),
			new Parameter("displayNextCalendar", "true"),
			new Parameter("displayPreorderInfo", "true"),
			new Parameter("minDisplayQty", "0"),
			new Parameter("excludeZeroStock", "true"),
			new Parameter("excludeCourierShowCase", "true"),
			new Parameter("excludePickupMovementShowCase", "true"),
			new Parameter("isEldoBrand", "true")
	);

	private static final List<Parameter> ELDORADO_STOCK_OBJECTS_OPTION_PARAMS = List.of(
			new Parameter("displayNextCalendar", "true"),
			new Parameter("displayPreorderInfo", "true"),
			new Parameter("isEldoBrand", "true")
	);

	private static final List<Parameter> THRESHOLD_PARAM = List.of(new Parameter("treshold", "true"));

	private final MspClient client;
	private final AvailabilityCalendarMapper mapper;

	@Override
	public Mono<List<AvailabilityOption>> fetchAvailabilityCalendar(RetailBrand brand, String regionId,
	                                                                Set<HandoverType> types, List<Material> materials,
	                                                                Set<String> objectsIds, Set<String> sapCodes) {
		if (isPickupWithoutObjects(types, objectsIds, sapCodes)) {
			log.warn("For PICKUP sap codes and handover objects is empty");
			return Mono.empty();
		}

		final var request = prepareRequest(brand, regionId, materials, types, objectsIds, sapCodes);

		return sendRequest(request);
	}

	@Override
	public Mono<List<AvailabilityOption>> fetchAvailabilityCalendarForStockObjects(RetailBrand brand, String regionId,
	                                                                               Set<HandoverType> types, Map<String, StockObject> stockObjects,
	                                                                               Set<String> objectsIds, Set<String> sapCodes) {
		if (isPickupWithoutObjects(types, objectsIds, sapCodes)) {
			log.warn("For PICKUP sap codes and handover objects is empty");
			return Mono.empty();
		}

		var request = prepareRequestForStockObjects(brand, regionId, stockObjects, types, objectsIds);

		return sendRequest(request);
	}

	private Mono<List<AvailabilityOption>> sendRequest(MspRequest request) {
		return client.fetchMaterialAvailabilityCalendar(request)
				.publishOn(Schedulers.parallel())
				.flatMap(this::processResponse)
				.onErrorResume(Exception.class, fallback -> {
					if (!(fallback instanceof WebClientResponseException.UnprocessableEntity)) {
						log.error("Msp service error: {}. Request {}", ExceptionUtils.getRootCauseMessage(fallback), request);
					}
					return Mono.empty();
				});
	}

	private boolean isPickupWithoutObjects(Set<HandoverType> types, Set<String> objectsIds, Set<String> sapCodes) {
		return types.size() == 1 && types.contains(HandoverType.PICKUP) && sapCodes.isEmpty() && objectsIds.isEmpty();
	}

	private MspRequest prepareRequest(RetailBrand brand, String regionId,
	                                  List<Material> materials, Set<HandoverType> types,
	                                  Set<String> objectsIds, Set<String> sapCodes) {

		final var positions = getPositions(materials);
		final var handoverOptions = getHandoverOptions(
				regionId,
				types,
				() -> Stream.of(objectsIds, sapCodes)
						.flatMap(Collection::stream)
						.collect(Collectors.toList())
		);
		final var optionParams = getOptionParams(brand);

		return MspRequest.builder()
				.requestBody(RequestBody.builder()
						.positions(positions)
						.handoverOptions(handoverOptions)
						.optionParams(optionParams)
						.build())
				.build();
	}

	private MspRequest prepareRequestForStockObjects(RetailBrand brand, String regionId,
	                                                 Map<String, StockObject> stockObjects, Set<HandoverType> types,
	                                                 Set<String> objectsIds) {
		final var positions = getPositionsForStockObjects(stockObjects);
		final var handoverOptions = getHandoverOptions(regionId, types, () -> new ArrayList<>(objectsIds));
		final var optionParams = getOptionParamsForStockObjects(brand);

		return MspRequest.builder()
				.requestBody(RequestBody.builder()
						.positions(positions)
						.handoverOptions(handoverOptions)
						.optionParams(optionParams)
						.build())
				.build();
	}

	@NotNull
	private List<Position> getPositions(List<Material> materials) {
		return materials.stream()
				.map(material -> Position.builder()
						.material(material.getMaterial())
						.qty(material.getQty())
						.params(THRESHOLD_PARAM)
						.build())
				.collect(Collectors.toList());
	}

	@NotNull
	private List<Position> getPositionsForStockObjects(Map<String, StockObject> stockObjects) {
		var stockObjectsMap = stockObjects.entrySet().stream()
				.flatMap(e -> e.getValue().getMaterials().stream()
						.map(m -> Map.entry(m, e.getKey())))
				.collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

		return stockObjectsMap.entrySet().stream()
				.map(stockObjectEntry -> Position.builder()
						.material(stockObjectEntry.getKey().getMaterial())
						.materialStocks(stockObjectEntry.getValue().stream().map(so ->
								MspStockObject.builder()
										.stockObject(so)
										.qty(stockObjectEntry.getKey().getQty())
										.build())
								.collect(Collectors.toList()))
						.build())
				.collect(Collectors.toList());
	}

	@NotNull
	private List<HandoverOption> getHandoverOptions(String regionId, Set<HandoverType> types, Supplier<List<String>> handoverObjects) {
		final var options = new ArrayList<HandoverOption>();
		for (HandoverType type : types) {
			if (HandoverType.PICKUP.equals(type)) {
				var pickupOption = new HandoverOption(
						handoverObjects.get(),
						type
				);
				options.add(pickupOption);
			} else {
				options.add(new HandoverOption(List.of(regionId), type));
			}
		}
		return options;
	}

	private List<Parameter> getOptionParams(RetailBrand brand) {
		return RetailBrand.ELDORADO.equals(brand) ? ELDORADO_OPTION_PARAMS : MVIDEO_OPTION_PARAMS;
	}

	private List<Parameter> getOptionParamsForStockObjects(RetailBrand brand) {
		return RetailBrand.ELDORADO.equals(brand) ? ELDORADO_STOCK_OBJECTS_OPTION_PARAMS : MVIDEO_STOCK_OBJECTS_OPTION_PARAMS;
	}

	private Mono<List<AvailabilityOption>> processResponse(MspResponseNextCalendar response) {
		if (response.getResponseBody() == null || response.getResponseBody().getAvailabilityOptions() == null) {
			final var errors = response.getResponseHeader().getHeaderErrors();
			if (CollectionUtils.isNotEmpty(errors)) {
				errors.forEach(headerError -> log.warn("Msp error: {}", headerError.getErrorMessage()));
			} else {
				log.warn("Msp error: available options not found");
			}

			return Mono.empty();
		}

		var availabilityOptions = response.getResponseBody()
				.getAvailabilityOptions().stream()
				.flatMap(calendar -> mapper.map(calendar).stream())
				.collect(Collectors.toList());

		return Mono.justOrEmpty(availabilityOptions);
	}

}
