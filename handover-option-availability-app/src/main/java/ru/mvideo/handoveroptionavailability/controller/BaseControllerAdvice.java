package ru.mvideo.handoveroptionavailability.controller;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import ru.mvideo.handoveroptionavailability.exception.DuplicateMaterialsException;
import ru.mvideo.handoveroptionavailability.exception.ExceptionResponseModel;
import ru.mvideo.handoveroptionavailability.exception.FailedToGetDeliveryCoordinatesException;
import ru.mvideo.handoveroptionavailability.exception.HandoverOptionAvailabilityException;
import ru.mvideo.handoveroptionavailability.exception.HandoverOptionAvailabilityValidationException;
import ru.mvideo.handoveroptionavailability.exception.LoggableException;
import ru.mvideo.handoveroptionavailability.exception.QtyMaterialsException;
import ru.mvideo.handoveroptionavailability.model.ErrorResponse;

/**
 * Base exception handler.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class BaseControllerAdvice {

	private static final String STANDARD_LOG_TMPL = "Path: {}. Status: {}. Handling {}: ";
	private static final String WEB_INPUT_FAILED_MSG_DEFAULT = "Web input failed";
	private static final String CONSTRAINT_VIOLATION_MSG_DEFAULT = "Constraint violation";
	private static final String CONSTRAINT_VIOLATION_MSG_DELIMITER = ", ";
	private static final String REASON_MSG_DELIMITER = ": ";

	@ExceptionHandler(AccessDeniedException.class)
	public Mono<ResponseEntity<ExceptionResponseModel>> accessDeniedHandler(AccessDeniedException e) {
		return buildResponse(e.getMessage(), 1201, HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(LoggableException.class)
	public Mono<ResponseEntity<ExceptionResponseModel>> loggableExceptionHandler(LoggableException e) {
		return buildResponse(e.getReason(), e.getErrorCode(), e.getHttpStatus());
	}

	@ExceptionHandler(ServerWebInputException.class)
	public Mono<ResponseEntity<ExceptionResponseModel>> handleServerWebInputException(ServerWebInputException ex) {
		String reason = isNotBlank(ex.getReason()) ? ex.getReason() : WEB_INPUT_FAILED_MSG_DEFAULT;

		String message;
		if (ex instanceof WebExchangeBindException) {
			message = toMessage(((WebExchangeBindException) ex).getBindingResult());
		} else {
			message = ex.getMessage();
		}
		return buildResponse((reason + REASON_MSG_DELIMITER + message), 1001, ex.getStatus());
	}

	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Mono<ResponseEntity<ErrorResponse>> handleConstraintViolationException(ServerHttpRequest request, ConstraintViolationException ex) {
		String message = toMessage(ex.getConstraintViolations());
		return handleError(request, HttpStatus.BAD_REQUEST, message, ex);
	}

	@ExceptionHandler(ValidationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Mono<ResponseEntity<ErrorResponse>> handleValidationException(ServerHttpRequest request, ValidationException ex) {
		return handleError(request, HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Mono<ResponseEntity<ErrorResponse>> handleIllegalArgumentException(ServerHttpRequest request, IllegalArgumentException ex) {
		return handleError(request, HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
	}

	@ExceptionHandler(NoSuchElementException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Mono<ResponseEntity<ErrorResponse>> handleNoSuchElementException(ServerHttpRequest request, NoSuchElementException ex) {
		return handleError(request, HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
	}

	@ExceptionHandler(DuplicateMaterialsException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Mono<ResponseEntity<ErrorResponse>> handleDuplicateMaterialsException(ServerHttpRequest request, DuplicateMaterialsException ex) {
		return handleError(request, HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
	}

	@ExceptionHandler(QtyMaterialsException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Mono<ResponseEntity<ErrorResponse>> handleQtyMaterialsException(ServerHttpRequest request, QtyMaterialsException ex) {
		return handleError(request, HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
	}

	@ExceptionHandler({HandoverOptionAvailabilityException.class})
	public Mono<ResponseEntity<ExceptionResponseModel>> handleHandoverOptionAvailabilityException(HandoverOptionAvailabilityException ex) {
		return buildResponse(ex.getMessage(), ex.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler({FailedToGetDeliveryCoordinatesException.class})
	public Mono<ResponseEntity<ExceptionResponseModel>> handleFailedToGetDeliveryCoordinatesException(FailedToGetDeliveryCoordinatesException ex) {
		log.warn("{}; Address: [{}]", ex.getMessage(), ex.getAddress());
		return buildResponse(ex.getMessage(), ex.getStatusCode(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler({HandoverOptionAvailabilityValidationException.class})
	public Mono<ResponseEntity<ExceptionResponseModel>> validationHandler(HandoverOptionAvailabilityValidationException ex) {
		return buildResponse(ex.getMessage(), ex.getStatusCode(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public Mono<ResponseEntity<ErrorResponse>> unhandledException(ServerHttpRequest request, Exception e) {
		return handleError(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
	}

	private Mono<ResponseEntity<ExceptionResponseModel>> buildResponse(final String message, int statusCode, HttpStatus httpStatus) {
		return Mono.just(
				ResponseEntity.status(httpStatus)
						.contentType(MediaType.APPLICATION_JSON)
						.body(ExceptionResponseModel.builder()
								.message(message)
								.statusCode(statusCode)
								.timestamp(LocalDateTime.now())
								.build()
						)
		);
	}

	private @NonNull Mono<ResponseEntity<ErrorResponse>> handleError(@NonNull ServerHttpRequest request, @NonNull HttpStatus status,
	                                                                 String message, @NonNull Exception e) {
		return Mono.fromCallable(
				() -> {
					String path = request.getPath().value();
					if (status.is5xxServerError()) {
						log.error(STANDARD_LOG_TMPL, path, status, e.getClass().getSimpleName(), e);
					} else {
						log.warn(STANDARD_LOG_TMPL, path, status, e.getClass().getSimpleName(), e);
					}

					return toResponseEntity(request.getId(), path, status, message);
				});
	}

	private @NonNull String toMessage(@NonNull Set<ConstraintViolation<?>> violations) {
		String message = violations.stream()
				.map(violation -> violation.getPropertyPath() + StringUtils.SPACE + violation.getMessage())
				.collect(joining(CONSTRAINT_VIOLATION_MSG_DELIMITER));

		if (message.isBlank()) {
			return CONSTRAINT_VIOLATION_MSG_DEFAULT;
		} else {
			return message;
		}
	}

	private @NonNull String toMessage(@NonNull BindingResult bindingResult) {
		if (!bindingResult.hasErrors()) {
			return StringUtils.EMPTY;
		}

		return bindingResult.getAllErrors().stream()
				.map(objectError -> {
					if (objectError instanceof FieldError) {
						var fieldError = (FieldError) objectError;
						return fieldError.getField() + StringUtils.SPACE + objectError.getDefaultMessage();
					} else {
						return objectError.getObjectName() + StringUtils.SPACE + objectError.getDefaultMessage();
					}
				})
				.collect(joining(CONSTRAINT_VIOLATION_MSG_DELIMITER));
	}

	private @NonNull ResponseEntity<ErrorResponse> toResponseEntity(String requestId, String path, @NonNull HttpStatus status, String message) {
		final var dateTime = OffsetDateTime.now();
		return ResponseEntity
				.status(status.value())
				.body(
						new ErrorResponse()
								.requestId(requestId)
								.path(path)
								.status(status.value())
								.error(status.getReasonPhrase())
								.message(message)
								.timestamp(dateTime)
				);
	}
}
