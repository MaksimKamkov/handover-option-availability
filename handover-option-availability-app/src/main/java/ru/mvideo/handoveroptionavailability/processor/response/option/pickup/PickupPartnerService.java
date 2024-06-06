package ru.mvideo.handoveroptionavailability.processor.response.option.pickup;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import ru.mvideo.handoveroptionavailability.mapper.AvailableAtMapper;
import ru.mvideo.handoveroptionavailability.model.AvailablePickupPoint;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.PartnerBrand;
import ru.mvideo.handoveroptionavailability.model.PickupAvailableAt;
import ru.mvideo.handoveroptionavailability.model.PickupPointType;
import ru.mvideo.handoveroptionavailability.model.PickupResponseItem;
import ru.mvideo.handoveroptionavailability.processor.model.BriefAndPickupContext;
import ru.mvideo.handoveroptionavailability.processor.model.Flags;
import ru.mvideo.lards.restriction.model.KnapsackDetailResponse;
import ru.mvideo.lards.restriction.model.PickupPoint;

@Order(1)
@Service
@RequiredArgsConstructor
public class PickupPartnerService extends PickupHandoverOptionService {

	private final AvailableAtMapper responseMapper;

	@Override
	protected boolean support(BriefAndPickupContext context) {
		return context.hasOption(HandoverOption.PICKUP_PARTNER.getValue());
	}

	@Override
	protected PickupResponseItem prepareResponse(BriefAndPickupContext context) {
		final var optionContext = context.handoverOptionContext().get(HandoverOption.PICKUP_PARTNER.getValue());
		final boolean includeStocks = context.flags().contains(Flags.INCLUDE_STOCKS);

		final var availabilityOptions = optionContext.getAvailabilityOptions();
		//https://jira.mvideo.ru/jira/browse/CS-5994 Для некоторых опций возвращать признак предоплаты всегда true
		availabilityOptions.forEach(opt -> opt.setPrepaidOnly(true));

		var pickupAvailableAts = responseMapper.toPickupAvailableAt(availabilityOptions, includeStocks);

		final var pickupPoints = context.detailPickupPoints();
		setAvailablePickupPoints(pickupAvailableAts, pickupPoints);
		pickupAvailableAts = pickupAvailableAts.stream()
				.filter(pickupAvailableAt -> !pickupAvailableAt.getAvailablePickupPoints().isEmpty())
				.collect(Collectors.toList());
		if (pickupAvailableAts.isEmpty()) {
			return null;
		}

		final var response = new PickupResponseItem();
		response.setHandoverOption(HandoverOption.fromValue(optionContext.getHandoverOption()));
		response.setHandoverOptionMaterial(optionContext.getHandoverOptionMaterial());
		response.setMinPrice(optionContext.getMinPriceRule().getPrice().doubleValue());
		response.setAvailableAt(pickupAvailableAts);
		response.setAvailabilityDate(availabilityDate(pickupAvailableAts));
		response.setPaymentConditions(paymentConditions(context, optionContext));

		return response;
	}

	private LocalDate availabilityDate(List<PickupAvailableAt> availableAt) {
		return availableAt.stream()
				.map(PickupAvailableAt::getAvailablePickupPoints)
				.flatMap(Collection::stream)
				.flatMap(pickupPoint -> Stream.of(pickupPoint.getLeadTimeMin(), pickupPoint.getLeadTimeMax()))
				.filter(Objects::nonNull)
				.min(LocalDate::compareTo)
				.orElse(LocalDate.now());
	}

	private void setAvailablePickupPoints(List<PickupAvailableAt> pickupAvailableAts,
	                                      List<KnapsackDetailResponse> pickupPoints) {

		for (PickupAvailableAt pickupAvailableAt : pickupAvailableAts) {
			final var availablePickupPoints = new ArrayList<AvailablePickupPoint>();
			for (KnapsackDetailResponse response : pickupPoints) {
				if (response.getSapCode().equals(pickupAvailableAt.getHandoverObject())) {
					pickupAvailableAt.setPartnerBrand(PartnerBrand.valueOf(response.getPartnerBrand().name()));

					for (PickupPoint pp : response.getAvailablePickupPoints()) {

						var availablePickupPoint = new AvailablePickupPoint();
						availablePickupPoint.setPickupPointId(pp.getPickupPointId());
						availablePickupPoint.setPickupPointType(PickupPointType.valueOf(pp.getPickupPointType().name()));
						availablePickupPoint.setLatitude(pp.getLatitude());
						availablePickupPoint.setLongitude(pp.getLongitude());
						availablePickupPoint.setLeadTimeMin(pp.getLeadTimeMin());
						availablePickupPoint.setLeadTimeMax(pp.getLeadTimeMax());

						availablePickupPoints.add(availablePickupPoint);
					}
				}
			}
			pickupAvailableAt.setAvailablePickupPoints(availablePickupPoints);
		}
	}
}
