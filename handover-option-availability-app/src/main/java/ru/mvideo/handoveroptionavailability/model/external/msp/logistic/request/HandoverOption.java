package ru.mvideo.handoveroptionavailability.model.external.msp.logistic.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.mvideo.handoveroptionavailability.model.external.msp.logistic.HandoverType;

@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HandoverOption {

	@JsonProperty
	private List<String> handoverObjects;
	@JsonProperty
	private HandoverType handoverType;
}
