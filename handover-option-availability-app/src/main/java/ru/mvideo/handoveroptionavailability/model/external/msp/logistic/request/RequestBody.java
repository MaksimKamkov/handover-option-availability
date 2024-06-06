package ru.mvideo.handoveroptionavailability.model.external.msp.logistic.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.Parameter;

@ToString
@Builder
@Data
public class RequestBody {

	@JsonProperty
	private List<HandoverOption> handoverOptions;
	@JsonProperty
	private List<Parameter> optionParams;
	@JsonProperty
	private List<Position> positions;
}
