package ru.mvideo.handoveroptionavailability.exception;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import ru.mvideo.handoveroptionavailability.utils.UnixTimeLocalDateTimeSerializer;

@Data
@Builder
public class ExceptionResponseModel {

	private final int statusCode;
	private final String message;

	@JsonSerialize(using = UnixTimeLocalDateTimeSerializer.class)
	private LocalDateTime timestamp;
}
