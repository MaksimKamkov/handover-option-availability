package ru.mvideo.handoveroptionavailability.model.external;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.mvideo.handoveroptionavailability.model.Valued;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum WeightMeasureUnit implements Valued<String> {

	KG("КГ"),
	G("Г");

	@Getter
	private final String value;

	/**
	 * Conversion of the weight to grams.
	 *
	 * @param weight source weight value
	 * @return weight in grams
	 */
	public double toGrams(double weight) {
		return switch (this) {
			case KG -> weight * 1000D;
			case G -> weight;
		};
	}

	public double toKilograms(double weight) {
		return switch (this) {
			case KG -> weight;
			case G -> weight / 1000D;
		};
	}
}
