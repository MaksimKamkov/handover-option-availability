package ru.mvideo.handoveroptionavailability.exception;

public class QtyMaterialsException extends RuntimeException {

	public QtyMaterialsException() {
		super("Validation failure: The sum of all values of the materials qty must not be less than 1 or exceed 100 materials");
	}
}
