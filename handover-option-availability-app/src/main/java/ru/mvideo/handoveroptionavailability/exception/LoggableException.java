package ru.mvideo.handoveroptionavailability.exception;

import org.springframework.http.HttpStatus;

public abstract class LoggableException extends RuntimeException {

	protected LoggableException() {
	}

	protected LoggableException(String message) {
		super(message);
	}

	public abstract HttpStatus getHttpStatus();

	public abstract int getErrorCode();

	public abstract String getReason();

}
