package ru.mvideo.handoveroptionavailability.exception;

public class DuplicateMaterialsException extends RuntimeException {

	public DuplicateMaterialsException() {
		super("Validation failure: duplicate materials");
	}
}
