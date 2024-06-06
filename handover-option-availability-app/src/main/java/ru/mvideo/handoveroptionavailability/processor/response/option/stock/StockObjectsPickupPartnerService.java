package ru.mvideo.handoveroptionavailability.processor.response.option.stock;

import java.time.LocalDate;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;
import ru.mvideo.handoveroptionavailability.model.MaterialStockObject;
import ru.mvideo.handoveroptionavailability.model.StockObjectsResponseItem;
import ru.mvideo.handoveroptionavailability.processor.model.StockObjectsContext;
import ru.mvideo.handoveroptionavailability.processor.utils.ScheduleUtil;
import ru.mvideo.io.pickup.points.lib.model.response.PickupPoint;
import ru.mvideo.io.pickup.points.lib.model.response.WorkSchedule;

@Service
public class StockObjectsPickupPartnerService extends StockObjectsHandoverOptionService {
	@Override
	protected boolean support(StockObjectsContext context) {
		return context.hasOption(HandoverOption.PICKUP_PARTNER.getValue());
	}

	@Override
	protected StockObjectsResponseItem prepareResponse(StockObjectsContext context) {
		final var material = context.currentMaterial();

		final var pickupPointMap = context.pickupPoints().stream()
				.collect(Collectors.toMap(PickupPoint::getPickupPointId, Function.identity()));

		final var stocks = context.availabilityOptions().stream()
				.filter(option -> material.getMaterial().equals(option.getMaterial()))
				.map(option -> MaterialStockObject.builder()
						.objectId(option.getStockObject())
						.qty(option.getAvailableStock())
						.showcaseQty(option.getShowCaseStock())
						.prepaid(true)
						.availableDate(getAvailabilityDate(option.getAvailableDate(), pickupPointMap.get(context.pickupPointId())))
						.priority(option.getStockObjectPriority())
						.build())
				.collect(Collectors.toList());

		return StockObjectsResponseItem.builder()
				.material(material.getMaterial())
				.stocks(stocks)
				.build();
	}

	private LocalDate getAvailabilityDate(LocalDate date, PickupPoint pickupPoint) {
		final var daysToDeliver = ObjectUtils.firstNonNull(pickupPoint.getDaysToDeliverMin(), pickupPoint.getDaysToDeliverMax());
		final var workSchedule = pickupPoint.getWorkSchedule().stream()
				.map(WorkSchedule::getDay)
				.collect(Collectors.toSet());

		return ScheduleUtil.closestWorkDay(date.plusDays(daysToDeliver), workSchedule);
	}
}
