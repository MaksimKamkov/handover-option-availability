package ru.mvideo.handoveroptionavailability.utils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import ru.mvideo.handoveroptionavailability.model.RetailBrand;
import ru.mvideo.io.pickup.points.lib.model.response.PickupPoint;
import ru.mvideo.io.pickup.points.lib.model.response.PickupPointCompressed;

@UtilityClass
public class SapCodeUtils {

	public Set<String> collectPickupPointsSapCodes(List<PickupPoint> pickupPoints, RetailBrand retailBrand) {
		return pickupPoints.stream()
				.map(pp -> getSapCode(pp, retailBrand))
				.collect(Collectors.toSet());
	}

	public Set<String> collectCompressedPickupPointsSapCodes(List<PickupPointCompressed> pickupPoints, RetailBrand retailBrand) {
		return pickupPoints.stream()
				.map(pp -> getSapCode(pp, retailBrand))
				.collect(Collectors.toSet());
	}

	private String getSapCode(PickupPoint pickupPoint, RetailBrand brand) {
		if (brand.equals(RetailBrand.MVIDEO)) {
			return pickupPoint.getSapCode();
		} else {
			return pickupPoint.getEldoSapCode();
		}
	}

	private String getSapCode(PickupPointCompressed pickupPoint, RetailBrand brand) {
		if (brand.equals(RetailBrand.MVIDEO)) {
			return pickupPoint.getSapCode();
		} else {
			return pickupPoint.getEldoSapCode();
		}
	}
}
