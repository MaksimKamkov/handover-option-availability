package ru.mvideo.handoveroptionavailability.utils;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import ru.mvideo.handoveroptionavailability.model.Valued;

@UtilityClass
public class EnumUtils {

	/**
	 * Get optional enum by value (value type should have valid equals method).
	 *
	 * @param enumClass enum class
	 * @param value     value to search
	 * @param <T>       value type
	 * @param <E>       enum type
	 * @return optional enum found, or empty optional
	 */
	public static <T, E extends Enum<E> & Valued<T>> @NonNull Optional<E> getByValue(
			@NonNull Class<E> enumClass, T value) {
		return Stream.of(enumClass.getEnumConstants())
				.filter(enumConst -> Objects.equals(enumConst.getValue(), value))
				.findFirst();
	}

}
