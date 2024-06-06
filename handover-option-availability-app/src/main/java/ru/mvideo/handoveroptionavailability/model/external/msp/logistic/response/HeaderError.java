package ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response;

import java.util.List;
import lombok.Data;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.Parameter;

@Data
public class HeaderError {

	private String errorCode;
	private String errorLevel;
	private String errorMessage;
	private String errorLink;
	private List<Parameter> errorParams;
}
