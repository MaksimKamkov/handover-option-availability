package ru.mvideo.handoveroptionavailability.exception;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import ru.mvideo.handoveroptionavailability.model.HandoverOption;

public class UnsupportedOptionException extends LoggableException {

	private final List<HandoverOption> options;

	public UnsupportedOptionException(List<HandoverOption> options) {
		super();

		this.options = options;
	}

	@Override
	public HttpStatus getHttpStatus() {
		return ErrorCodesMapping.BAD_REQUEST.getHttpStatus();
	}

	@Override
	public int getErrorCode() {
		return ErrorCodesMapping.BAD_REQUEST.getErrorCode();
	}

	@Override
	public String getReason() {
		return String.format("Unsupported handover options [%s]", StringUtils.join(options));
	}
}
