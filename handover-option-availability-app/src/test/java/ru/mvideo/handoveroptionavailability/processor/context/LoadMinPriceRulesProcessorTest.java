package ru.mvideo.handoveroptionavailability.processor.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mvideo.handoveroptionavailability.processor.model.DeliveryContext;
import ru.mvideo.handoveroptionavailability.service.external.pricerules.PriceRulesService;

@ExtendWith(MockitoExtension.class)
public class LoadMinPriceRulesProcessorTest {
	//todo проверка получения не пустого списка
	//todo проверка получения пустого списка
	//todo проверка ни один из провайдеров не поддерживает запрашиваемые способы доставки
	//todo проверка priceRules не содержат обязательные атрибуты
	//todo проверка supplierPriority
	//todo проверка неподдерживаемых опций доставки
	//todo проверка метода shouldRun

	@InjectMocks
	private LoadMinPriceRulesProcessor<DeliveryContext> processor;
	@Mock
	private PriceRulesService priceRulesService;

	@BeforeEach
	public void beforeEach() {
		Mockito.clearInvocations(priceRulesService);
	}
}
