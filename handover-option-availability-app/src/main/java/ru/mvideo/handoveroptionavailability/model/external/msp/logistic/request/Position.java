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
public class Position {

	@JsonProperty
	private String material;
	@JsonProperty
	private List<MspStockObject> materialStocks;
	@JsonProperty
	private List<Parameter> params;
	@JsonProperty
	private int qty;
}
