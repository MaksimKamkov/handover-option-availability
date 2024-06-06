package ru.mvideo.handoveroptionavailability.exception;

import lombok.Getter;

public class HandoverOptionAvailabilityException extends RuntimeException {

	@Getter
	private int statusCode;

	public HandoverOptionAvailabilityException() {
	}

	public HandoverOptionAvailabilityException(String message) {
		super(message);
	}

	public HandoverOptionAvailabilityException(String message, int statusCode) {
		super(message);
		this.statusCode = statusCode;
	}
}