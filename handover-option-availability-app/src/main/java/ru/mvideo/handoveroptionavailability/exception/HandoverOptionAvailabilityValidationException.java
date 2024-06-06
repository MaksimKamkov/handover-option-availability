package ru.mvideo.handoveroptionavailability.exception;

import lombok.Getter;

public class HandoverOptionAvailabilityValidationException extends RuntimeException {

	@Getter
	private final int statusCode;

	public HandoverOptionAvailabilityValidationException(String message, int statusCode) {
		super(message);
		this.statusCode = statusCode;
	}
}
