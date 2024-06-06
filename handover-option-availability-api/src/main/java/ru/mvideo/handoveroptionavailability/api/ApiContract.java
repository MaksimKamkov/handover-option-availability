package ru.mvideo.handoveroptionavailability.api;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ApiContract {
	public static final String BASE_V1 = "/api/v1";
	public static final String HANDOVER_OPTIONS = "/handover-options";
	public static final String DELIVERY = "/handover-options/delivery";
	public static final String DELIVERY_PROVIDERS = "/handover-options/delivery-providers";
	public static final String PICKUP = "/handover-options/pickup";
	public static final String BASE_V2 = "/api/v2";
	public static final String STOCK_OBJECTS = "/handover-options/stock-objects";
	public static final String BATCH = "/handover-options/batch";
	public static final String ACCESSORIES = "/handover-options/accessories";
}
