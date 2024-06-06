package ru.mvideo.handoveroptionavailability.utils;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mvideo.handoveroptionavailability.exception.DuplicateMaterialsException;
import ru.mvideo.handoveroptionavailability.model.Material;

public class RequestValidationUtilsTest {

	@DisplayName("Ошибка в случае, когда в запросе приходят два одинаковых материала")
	@Test
	public void shouldThrowExceptionNotUniqueMaterials() {
		final var materials = List.of(
				new Material("1", 1, 1000.0),
				new Material("1", 1, 1000.0)
		);

		Assertions.assertThrows(DuplicateMaterialsException.class, () -> RequestValidationUtils.requireUniqueMaterials(materials));
	}

	@DisplayName("Ошибка не возникает в случае, когда в запросе приходят уникальные материалы")
	@Test
	public void shouldNotThrowExceptionUniqueMaterials() {
		final var materials = List.of(
				new Material("1", 1, 1000.0),
				new Material("2", 2, 2000.0)
		);

		RequestValidationUtils.requireUniqueMaterials(materials);
	}
}
