package ru.mvideo.handoveroptionavailability.model.external;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.mvideo.handoveroptionavailability.model.Valued;

/**
 * Dimensions measure units.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum DimensionMeasureUnit implements Valued<String> {

	CM("СМ"),
	DM("ДМ"),
	M("М"),
	MM("ММ");

	@Getter
	private final String value;

	/**
	 * Conversion of the dimension to millimeters.
	 *
	 * @param dim source dimension value
	 * @return dimension in millimeters
	 */
	public double toMillimeters(double dim) {
		return switch (this) {
			case MM -> dim;
			case CM -> dim * 10D;
			case DM -> dim * 100D;
			case M -> dim * 1000D;
		};
	}

	public double toCentimeters(double dim) {
		return switch (this) {
			case MM -> dim / 10D;
			case CM -> dim;
			case DM -> dim * 10D;
			case M -> dim * 100D;
		};
	}
}
