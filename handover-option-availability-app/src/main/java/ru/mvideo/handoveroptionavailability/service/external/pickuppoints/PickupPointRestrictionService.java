package ru.mvideo.handoveroptionavailability.service.external.pickuppoints;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response.AvailabilityOption;
import ru.mvideo.handoveroptionavailability.processor.filter.fit.ExtendedProduct;
import ru.mvideo.handoveroptionavailability.utils.ProductConverter;
import ru.mvideo.lards.restriction.api.client.KnapsackPublicApi;
import ru.mvideo.lards.restriction.model.AvailableDate;
import ru.mvideo.lards.restriction.model.Brand;
import ru.mvideo.lards.restriction.model.ComplementableMaterial;
import ru.mvideo.lards.restriction.model.KnapsackBatchBriefRequest;
import ru.mvideo.lards.restriction.model.KnapsackBatchBriefResponse;
import ru.mvideo.lards.restriction.model.KnapsackBriefRequest;
import ru.mvideo.lards.restriction.model.KnapsackBriefResponse;
import ru.mvideo.lards.restriction.model.KnapsackDetailRequest;
import ru.mvideo.lards.restriction.model.KnapsackDetailResponse;
import ru.mvideo.lards.restriction.model.Material;
import ru.mvideo.lards.restriction.model.SingleMaterial;

@Slf4j
@Service
@AllArgsConstructor
public class PickupPointRestrictionService {

	private final KnapsackPublicApi client;

	private static final String PICKUP_POINT_RESTRICTION_ERROR_MESSAGE = "Pickup-point-restriction service error: {}";

	public Mono<KnapsackBriefResponse> briefPickupPoints(String rimCode,
	                                                     String brand,
	                                                     List<AvailabilityOption> availabilityOptions,
	                                                     List<ExtendedProduct> products) {

		List<AvailableDate> availableDates = availabilityOptionsToDates(availabilityOptions);

		List<Material> materials = products.stream()
				.filter(product -> CollectionUtils.isEmpty(product.getProduct().getComplementMaterials()))
				.map(ProductConverter::toMaterial)
				.toList();

		final var request = new KnapsackBriefRequest();
		request.setRimCode(rimCode);
		request.setBrand(Brand.valueOf(brand));
		request.setAvailableDates(availableDates);
		request.setMaterials(materials);

		return client.getBrief(request)
				.onErrorResume(
						Throwable.class,
						fallback -> {
							log.error(PICKUP_POINT_RESTRICTION_ERROR_MESSAGE, fallback.getMessage());
							return Mono.empty();
						});
	}

	public Mono<List<KnapsackDetailResponse>> detailPickupPoints(String rimCode,
	                                                             String brand,
	                                                             List<AvailabilityOption> availabilityOptions,
	                                                             List<ExtendedProduct> products) {

		List<AvailableDate> availableDates = availabilityOptionsToDates(availabilityOptions);

		List<Material> materials = products.stream()
				.filter(product -> CollectionUtils.isEmpty(product.getProduct().getComplementMaterials()))
				.map(ProductConverter::toMaterial)
				.toList();

		final var request = new KnapsackDetailRequest();
		request.setRimCode(rimCode);
		request.setBrand(Brand.valueOf(brand));
		request.setAvailableDates(availableDates);
		request.setMaterials(materials);

		return client.getDetails(request)
				.collectList()
				.onErrorResume(
						Throwable.class,
						fallback -> {
							log.error(PICKUP_POINT_RESTRICTION_ERROR_MESSAGE, fallback.getMessage());
							return Mono.just(Collections.emptyList());
						});
	}

	public Mono<List<KnapsackBatchBriefResponse>> batchPickupPoints(String rimCode,
	                                                                String brand,
	                                                                List<AvailabilityOption> availabilityOptions,
	                                                                List<ExtendedProduct> products) {
		final var materials = collectComplementMaterials(products, availabilityOptions);

		final var request = new KnapsackBatchBriefRequest();
		request.setRimCode(rimCode);
		request.setBrand(Brand.valueOf(brand));
		request.positions(materials);

		return client.getBatchBrief(request)
				.collectList()
				.onErrorResume(
						Throwable.class,
						fallback -> {
							log.error(PICKUP_POINT_RESTRICTION_ERROR_MESSAGE, fallback.getMessage());
							return Mono.just(Collections.emptyList());
						});
	}

	private List<AvailableDate> availabilityOptionsToDates(List<AvailabilityOption> availabilityOptions) {
		return availabilityOptionsToDates(null, availabilityOptions);
	}

	private List<AvailableDate> availabilityOptionsToDates(String material, List<AvailabilityOption> availabilityOptions) {
		Map<String, LocalDate> sapCodeDateMap = availabilityOptions.stream()
				.filter(availabilityOption -> material == null || availabilityOption.getMaterial().equals(material))
				.collect(Collectors.toMap(
						AvailabilityOption::getHandoverObject,
						AvailabilityOption::getAvailableDate,
						(oldV, newV) -> {
							if (oldV.isBefore(newV)) {
								return oldV;
							} else {
								return newV;
							}
						}
				));

		return sapCodeDateMap.entrySet().stream()
				.map(entry -> new AvailableDate(entry.getValue(), entry.getKey()))
				.toList();
	}

	private List<ComplementableMaterial> collectComplementMaterials(List<ExtendedProduct> products, List<AvailabilityOption> availabilityOptions) {
		var complementMaterialNumbers = new HashSet<String>();
		var productsMap = new HashMap<String, ExtendedProduct>();
		for (ExtendedProduct product : products) {
			productsMap.put(product.getProduct().getProductId(), product);

			if (CollectionUtils.isNotEmpty(product.getProduct().getComplementMaterials())) {
				product.getProduct().getComplementMaterials()
						.forEach(item -> complementMaterialNumbers.add(item.getMaterialNumber()));
			}
		}
		return products.stream()
				.filter(product -> !complementMaterialNumbers.contains(product.getProduct().getProductId()))
				.map(product -> {
					ComplementableMaterial complementableMaterial = ProductConverter.toComplementableMaterial(product);
					List<AvailableDate> availableDates = availabilityOptionsToDates(
							product.getProduct().getProductId(),
							availabilityOptions
					);
					complementableMaterial.setAvailableDates(availableDates);
					complementableMaterial.setItems(getItems(productsMap, product));
					return complementableMaterial;
				}).toList();
	}

	private List<SingleMaterial> getItems(HashMap<String, ExtendedProduct> productsMap,
	                                      ExtendedProduct product) {
		var complementMaterials = product.getProduct().getComplementMaterials();
		if (complementMaterials == null) {
			return Collections.emptyList();
		}
		return complementMaterials.stream()
				.map(material -> ProductConverter.toSingleMaterial(productsMap.get(material.getMaterialNumber())))
				.toList();
	}
}
