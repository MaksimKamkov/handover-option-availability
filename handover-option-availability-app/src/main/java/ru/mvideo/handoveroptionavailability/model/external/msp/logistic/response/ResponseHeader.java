package ru.mvideo.handoveroptionavailability.model.external.msp.logistic.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.Parameter;

@Data
public class ResponseHeader {

	@JsonProperty
	private String clientId;
	@JsonProperty
	private String interfaceVersion;
	@JsonProperty
	private String responseDate;
	@JsonProperty
	private int responseCode;
	@JsonProperty
	private String responseMessage;
	@JsonProperty
	private String responseDescription;
	@JsonProperty
	private List<HeaderError> headerErrors;
	@JsonProperty
	private List<Parameter> headerParams;
}
