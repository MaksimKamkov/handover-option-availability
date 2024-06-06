package ru.mvideo.handoveroptionavailability.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCodesMapping {
	BAD_REQUEST(HttpStatus.BAD_REQUEST, 1001, "Некорректный запрос");

	private final HttpStatus httpStatus;
	private final int errorCode;
	private final String errorMessage;

	ErrorCodesMapping(HttpStatus httpStatus, int errorCode, String errorMessage) {
		this.httpStatus = httpStatus;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
}
