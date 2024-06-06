package ru.mvideo.handoveroptionavailability.exception;

import lombok.Getter;

public class FailedToGetDeliveryCoordinatesException extends RuntimeException {

	@Getter
	private final int statusCode;
	@Getter
	private final String address;

	public FailedToGetDeliveryCoordinatesException(String address) {
		super("Не удалось получить географические координаты адреса доставки");
		this.statusCode = 1102;
		this.address = address;
	}
}
